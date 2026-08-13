package de.ii.xtraplatform.cli;

import static de.ii.xtraplatform.cli.CfgHandler.AS_MAP;

import de.ii.ldproxy.cfg.DeprecatedKeyword;
import de.ii.ldproxy.cfg.LdproxyCfg;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import shadow.com.networknt.schema.Error;
import shadow.com.networknt.schema.keyword.KeywordType;
import shadow.com.networknt.schema.path.NodePath;
import shadow.com.networknt.schema.path.PathType;

public class CfgValidation extends Messages {

  public CfgValidation(Path path) {
    super(null, null, path);
  }

  public CfgValidation(Path path, String error) {
    super(null, null, path, error);
  }

  @Override
  public String getSummary() {
    return String.format("Global configuration %s: %s", getQualifiers(), getPath());
  }

  private String getQualifiers() {
    if (hasErrors()) {
      return "has errors";
    } else if (hasWarnings()) {
      List<String> kinds = new ArrayList<>();
      if (getMessages().stream().anyMatch(CfgValidation::isUnknown)) {
        kinds.add("unknown");
      }
      if (getMessages().stream().anyMatch(DeprecatedKeyword::isDeprecated)) {
        kinds.add("deprecated");
      }
      if (getMessages().stream().anyMatch(CfgValidation::isRedundant)) {
        kinds.add("redundant");
      }

      return String.format("has %s settings", String.join(" and ", kinds));
    }
    return "is fine";
  }

  public void validate(LdproxyCfg ldproxyCfg, Path yml) {
    try {
      Map<String, Object> original = ldproxyCfg.getObjectMapper().readValue(yml.toFile(), AS_MAP);

      if (original.containsKey("store") && (original.get("store") instanceof Map)) {
        Map<String, Object> store = (Map<String, Object>) original.get("store");

        if (store.containsKey("additionalLocations")
            && store.get("additionalLocations") instanceof List
            && !((List<?>) store.get("additionalLocations")).isEmpty()) {
          addMessage(deprecated("store.additionalLocations"));
        }
      }

      if (original.containsKey("proj") && (original.get("proj") instanceof Map)) {
        Map<String, Object> proj = (Map<String, Object>) original.get("proj");

        if (proj.containsKey("location")
            && proj.get("location") instanceof String
            && !((String) proj.get("location")).isBlank()) {
          addMessage(deprecated("proj.location"));
        }
      }
    } catch (IOException e) {
      setError(e.getMessage());
    }
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
    return new Error.Builder()
        .keyword(REDUNDANT)
        .instanceLocation(new NodePath(PathType.JSON_PATH).append(path))
        .format(new MessageFormat("$.{0}: is redundant and can be removed"))
        .build();
  }

  static Error deprecated(String path) {
    return new Error.Builder()
        .keyword(DeprecatedKeyword.KEYWORD)
        .instanceLocation(new NodePath(PathType.JSON_PATH).append(path))
        .format(new MessageFormat("$.{0}: is deprecated and should be upgraded"))
        .build();
  }
}
