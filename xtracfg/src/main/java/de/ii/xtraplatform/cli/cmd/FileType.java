package de.ii.xtraplatform.cli.cmd;

import de.ii.ldproxy.cfg.LdproxyCfg;
import de.ii.xtraplatform.cli.Result;
import de.ii.xtraplatform.entities.domain.EntityFactory;
import java.nio.file.Path;
import java.util.*;
import shadow.com.google.common.io.Files;

public class FileType extends Common<LdproxyCfg> {

  private static final List<String> CONTENT_TYPES =
      List.of("defaults", "entities", "instances", "overrides");
  private static final List<String> ENTITY_TYPES = List.of("providers", "services");

  public final String fullPathString;
  public final Path fullPath;

  public final Path path;
  public final String fileName;
  public final String fileExtension;

  public FileType(Map<String, Object> parameters) {
    super(parameters);
    this.fullPathString = string(parameters, "path");
    this.fullPath = Path.of(fullPathString);

    String file = Optional.ofNullable(fullPath.getFileName()).map(Path::toString).orElse("");

    this.path = fullPath.getParent();
    this.fileName = Files.getNameWithoutExtension(file);
    this.fileExtension = Files.getFileExtension(file);
  }

  @Override
  public Result run(LdproxyCfg ldproxyCfg) {

    if (Objects.isNull(path)
        || path.getNameCount() < 2
        || !Objects.equals(fileExtension, "yml")
        || (!path.startsWith("entities") && !path.startsWith("store"))) {
      return Result.empty();
    }

    String type = path.getName(1).toString();

    System.out.println(
        "FILE_TYPE "
            + fullPath
            + " - "
            + path
            + " - "
            + fileName
            + " - "
            + fileExtension
            + " - "
            + type);

    // TODO: multi-file overrides
    if (path.getNameCount() >= 3
        && CONTENT_TYPES.contains(type)
        && ENTITY_TYPES.contains(path.getFileName().toString())) {
      return found(path.getFileName().toString());
    }

    // TODO: multi-file defaults
    if (Objects.equals(type, "defaults")) {
      if (ENTITY_TYPES.contains(fileName)) {
        return found(fileName);
      }

      if (path.getNameCount() >= 4 && ENTITY_TYPES.contains(path.getName(2).toString())) {
        try {
          EntityFactory entityFactory =
              ldproxyCfg
                  .getEntityFactories()
                  .get(path.getName(2).toString(), path.getName(3).toString());

          Optional<Set<Map.Entry<String, Object>>> keyPathAlias =
              entityFactory
                  .getKeyPathAlias(fileName)
                  .map(keyPathAlias1 -> keyPathAlias1.wrapMap(Map.of()).entrySet());

          if (keyPathAlias.isPresent() && !keyPathAlias.get().isEmpty()) {
            Map.Entry<String, Object> next = keyPathAlias.get().iterator().next();
            String property = next.getKey();
            String discriminatorKey = null;
            String discriminatorValue = null;
            if (next.getValue() instanceof List
                && !((List<?>) next.getValue()).isEmpty()
                && ((List<?>) next.getValue()).get(0) instanceof Map) {
              Map.Entry<String, Object> disc =
                  ((Map<String, Object>) ((List<?>) next.getValue()).get(0))
                      .entrySet()
                      .iterator()
                      .next();
              discriminatorKey = disc.getKey();
              if (disc.getValue() instanceof String) {
                discriminatorValue = (String) disc.getValue();
              }
            }

            return found(path.getName(2).toString(), property, discriminatorKey, discriminatorValue);
          } else if (keyPathAlias.isEmpty()) {
            return found(path.getName(2).toString(), fileName, null, null);
          }
        } catch (Throwable e) {
          System.out.println("ERR " + e.getMessage());
        }
      }
    }

    return Result.empty();
  }

  private Result found(String entityType) {
    System.out.println("FOUND entities/" + entityType + " - " + fullPathString);
    return Result.ok("found", Map.of("path", fullPathString, "fileType", "entities/" + entityType));
  }

  private Result found(
      String entityType, String subProperty, String discriminatorKey, String discriminatorValue) {
    System.out.println(
        "FOUND entities/"
            + entityType
            + " - "
            + fullPathString
            + " - "
            + subProperty
            + " - "
            + discriminatorKey
            + " - "
            + discriminatorValue);

    if (Objects.nonNull(discriminatorKey) && Objects.nonNull(discriminatorValue)) {
      return Result.ok(
          "found",
          Map.of(
              "path",
              fullPathString,
              "fileType",
              "entities/" + entityType,
              "subProperty",
              subProperty,
              "discriminatorKey",
              discriminatorKey,
              "discriminatorValue",
              discriminatorValue));
    }
    return Result.ok(
        "found",
        Map.of(
            "path",
            fullPathString,
            "fileType",
            "entities/" + entityType,
            "subProperty",
            subProperty));
  }
}
