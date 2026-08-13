package de.ii.xtraplatform.cli;

import de.ii.ldproxy.cfg.DeprecatedKeyword;
import de.ii.ldproxy.cfg.LdproxyCfg;
import de.ii.xtraplatform.cli.cmd.FileType.FileInfo;
import de.ii.xtraplatform.entities.app.MapSubtractor;
import de.ii.xtraplatform.values.domain.Identifier;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import shadow.com.networknt.schema.Error;
import shadow.com.networknt.schema.keyword.KeywordType;
import shadow.com.networknt.schema.path.NodePath;
import shadow.com.networknt.schema.path.PathType;

public class Validation extends Messages {

  private final FileInfo fileInfo;

  public Validation(
      EntitiesHandler.Type type, Identifier identifier, Path path, FileInfo fileInfo) {
    super(type, identifier, path);
    this.fileInfo = fileInfo;
  }

  public Validation(
      EntitiesHandler.Type type,
      Identifier identifier,
      Path path,
      FileInfo fileInfo,
      String error) {
    super(type, identifier, path, error);
    this.fileInfo = fileInfo;
  }

  @Override
  public String getSummary() {
    return String.format("%s configuration %s: %s", getType().name(), getQualifiers(), getPath());
  }

  private String getQualifiers() {
    if (hasErrors()) {
      return "has errors";
    } else if (hasWarnings()) {
      List<String> kinds = new ArrayList<>();
      if (getMessages().stream().anyMatch(Validation::isUnknown)) {
        kinds.add("unknown");
      }
      if (getMessages().stream().anyMatch(DeprecatedKeyword::isDeprecated)) {
        kinds.add("deprecated");
      }
      if (getMessages().stream().anyMatch(Validation::isRedundant)) {
        kinds.add("redundant");
      }

      return String.format("has %s settings", String.join(" and ", kinds));
    }
    return "is fine";
  }

  public void validate(LdproxyCfg ldproxyCfg, FileInfo fileInfo) {
    if (!fileInfo.isValid()) {
      return;
    }

    try {
      String fileContent =
          loadFileContent(ldproxyCfg.getDataDirectory().resolve(getPath()), getType(), fileInfo);

      for (Error msg : ldproxyCfg.validateEntity(fileContent, fileInfo.entityType)) {
        if ( // msg.getMessage().contains("string found, boolean expected") ||
        // msg.getMessage().contains("integer found, string expected") ||
        msg.getMessage().contains(".tileProviderId: is deprecated")
            || (getType() == EntitiesHandler.Type.Defaults
                && (
                /*msg.getMessage().contains("$.serviceType: is missing but it is required")
                || msg.getMessage().contains("$.providerType: is missing but it is required")
                ||*/ msg.getMessage()
                        .contains("$.providerSubType: is missing but it is required")
                    || msg.getMessage()
                        .contains("$.featureProviderType: is missing but it is required")))) {
          // ignore
          continue;
        }

        if (fileInfo.subProperty.isPresent()) {
          String newMessage =
              msg.getMessage().startsWith("$." + fileInfo.subProperty.get() + ".")
                  ? msg.getMessage().replace(fileInfo.subProperty.get() + ".", "")
                  : msg.getMessage().startsWith("$." + fileInfo.subProperty.get() + "[")
                      ? msg.getMessage()
                          .replaceFirst(fileInfo.subProperty.get() + "\\[[0-9]+\\]\\.", "")
                      : msg.getMessage();

          msg = copyWith(msg, newMessage);
        }

        addMessage(msg);
      }
    } catch (IOException e) {
      setError(e.getMessage());
    }
  }

  private static Error copyWith(Error msg, String newMessage) {
    return new Error.Builder()
        .keyword(msg.getKeyword())
        .instanceLocation(msg.getInstanceLocation())
        .format(new MessageFormat(""))
        .message(newMessage)
        .schemaLocation(msg.getSchemaLocation())
        .build();
  }

  static String loadFileContent(Path path, EntitiesHandler.Type type, FileInfo fileInfo)
      throws IOException {
    String fileContent = Files.readString(path);
    String entityType = fileInfo.entityType;

    if (type == EntitiesHandler.Type.Defaults && fileInfo.entitySubType.isPresent()) {

      if (fileContent.startsWith("---")) {
        fileContent = fileContent.substring(fileContent.indexOf("\n") + 1);
      }

      if (fileInfo.discriminatorKey.isPresent() && fileInfo.discriminatorValue.isPresent()) {
        String discriminatorKey = fileInfo.discriminatorKey.get();
        String discriminatorValue = fileInfo.discriminatorValue.get();
        fileContent =
            "- "
                + discriminatorKey
                + ": "
                + discriminatorValue
                + "\n  "
                + fileContent.replace("\n", "\n  ");
      }

      if (fileInfo.subProperty.isPresent()) {
        String subProperty = fileInfo.subProperty.get();
        fileContent =
            Arrays.stream(fileContent.split("\n"))
                .map(line -> "  " + line)
                .collect(Collectors.joining("\n"));
        fileContent = subProperty + ":\n" + fileContent;
      }

      fileContent =
          fileContent
              + "\n"
              + entityType.substring(0, entityType.length() - 1)
              + "Type: "
              + fileInfo.entitySubType.get().toUpperCase();
    }

    if (type == EntitiesHandler.Type.Overrides && fileInfo.entitySubType.isPresent()) {
      fileContent =
          fileContent
              + "\n"
              + entityType.substring(0, entityType.length() - 1)
              + "Type: "
              + fileInfo.entitySubType.get().toUpperCase()
              + "\n";
    }
    return fileContent;
  }

  public void validateRedundant(Map<String, Object> original, Map<String, Object> upgraded) {
    // TODO
    Map<String, Object> redundant =
        MapSubtractor.subtract(
            original,
            upgraded,
            List.of(),
            Map.of("api", "buildingBlock", "extensions", "type"),
            true);
    List<String> redundantPath = paths(redundant, "");
    List<String> upgradedPaths = paths(upgraded, "");

    List<String> coveredPaths =
        getMessages().stream()
            .flatMap(
                vm -> {
                  String prefix = vm.getInstanceLocation().toString() + ".";
                  String path = prefix.substring(2) + vm.getProperty();
                  // allow single values instead of list like yaml parser
                  return Stream.of(path, path.replaceAll("\\[0\\]", ""));
                })
            .collect(Collectors.toList());

    for (String path : redundantPath) {
      // allow single values instead of list like yaml parser
      String pathSingles = path.replaceAll("\\[0\\]", "");
      if (!coveredPaths.contains(path)
          && !coveredPaths.contains(pathSingles)
          && !upgradedPaths.contains(path)
          && !upgradedPaths.contains(pathSingles)) {

        if (coveredPaths.stream()
            .anyMatch(cp -> path.startsWith(cp + ".") || pathSingles.startsWith(cp + "."))) {
          continue;
        }

        addMessage(redundant(path));
      }
    }
  }

  private static List<String> paths(Map<String, Object> map, String prefix) {
    List<String> paths = new ArrayList<>();

    for (String key : map.keySet()) {
      String newPrefix = prefix.isEmpty() ? key : prefix + "." + key;
      if (map.get(key) instanceof Map) {
        paths.addAll(paths((Map<String, Object>) map.get(key), newPrefix));
      } else if (map.get(key) instanceof List) {
        for (int i = 0; i < ((List<Object>) map.get(key)).size(); i++) {
          Object item = ((List<Object>) map.get(key)).get(i);
          if (item instanceof Map) {
            paths.addAll(paths((Map<String, Object>) item, newPrefix + "[" + i + "]"));
          } else if (Objects.nonNull(item)) {
            paths.add(newPrefix);
          }
        }
      } else if (!newPrefix.endsWith("buildingBlock") && Objects.nonNull(map.get(key))) {
        paths.add(newPrefix);
      }
    }

    return paths;
  }

  @Override
  protected boolean isWarning(Error vm) {
    return DeprecatedKeyword.isDeprecated(vm) || isUnknown(vm) || isRedundant(vm);
  }

  @Override
  protected String getMessage(Error vm) {
    if (isUnknown(vm)) {
      return String.format(
          "%s.%s is unknown for type %s",
          vm.getInstanceLocation(),
          vm.getProperty(),
          vm.getSchemaLocation()
              .toString()
              .replace("#/$defs/", "")
              .replace("/additionalProperties", ""));
    }

    return vm.getMessage();
  }

  private static boolean isUnknown(Error vm) {
    return Objects.equals(vm.getKeyword(), KeywordType.ADDITIONAL_PROPERTIES.getValue());
  }

  private static boolean isRedundant(Error vm) {
    return Objects.equals(vm.getKeyword(), REDUNDANT);
  }

  private static final String REDUNDANT = "redundant";

  static Error redundant(String path) {
    NodePath nodePath = new NodePath(PathType.LEGACY);
    String[] parts = path.split("\\.");
    for (int i = 0; i < parts.length - 1; i++) {
      nodePath = nodePath.append(parts[i]);
    }

    return new Error.Builder()
        .keyword(REDUNDANT)
        .instanceLocation(nodePath)
        .property(parts[parts.length - 1])
        .arguments(parts[parts.length - 1])
        .format(new MessageFormat("{0}.{1}: is redundant and can be removed"))
        .build();
  }
}
