package de.ii.xtraplatform.cli;

import de.ii.ldproxy.cfg.LdproxyCfg;
import de.ii.ldproxy.cfg.ValueMigration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import shadow.com.fasterxml.jackson.databind.JsonNode;
import shadow.com.fasterxml.jackson.databind.ObjectMapper;

/** Checks and upgrades value files (e.g. stored queries) using the value migrations of ldproxy-cfg. */
public class ValuesHandler {

  private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

  private static class ValueUpgrade {
    final Path path;
    final Path relativePath;
    final boolean isJson;
    final JsonNode upgraded;
    final ValueMessages messages;

    ValueUpgrade(
        Path path, Path relativePath, boolean isJson, JsonNode upgraded, ValueMessages messages) {
      this.path = path;
      this.relativePath = relativePath;
      this.isJson = isJson;
      this.upgraded = upgraded;
      this.messages = messages;
    }

    boolean hasError() {
      return messages.getError().isPresent();
    }

    boolean hasUpgrade() {
      return Objects.nonNull(upgraded);
    }
  }

  public static Result check(
      LdproxyCfg ldproxyCfg, Optional<String> path, boolean verbose, boolean debug) {
    if (Objects.isNull(ldproxyCfg)) {
      return Result.failure("Not connected to store");
    }

    Result result = new Result();
    result.details("path", path.orElse(""));

    for (ValueUpgrade upgrade : getUpgrades(ldproxyCfg, path, false, debug)) {
      upgrade.messages.log(result, verbose);
    }

    return result;
  }

  public static Result preUpgrade(
      LdproxyCfg ldproxyCfg, Optional<String> path, boolean force, boolean verbose, boolean debug) {
    if (Objects.isNull(ldproxyCfg)) {
      return Result.failure("Not connected to store");
    }

    Result result = new Result();

    int i = 0;
    for (ValueUpgrade upgrade : getUpgrades(ldproxyCfg, path, force, debug)) {
      if (upgrade.hasError()) {
        upgrade.messages.logErrors(result, verbose);
      } else if (upgrade.hasUpgrade()) {
        if (i++ == 0) {
          result.info("The following value configurations will be upgraded:");
        }
        result.info("  - " + upgrade.relativePath);
      }
    }

    if (result.has(Result.Status.INFO)) {
      result.confirmation("Are you sure?");
    }

    return result;
  }

  public static Result upgrade(
      LdproxyCfg ldproxyCfg,
      Optional<String> path,
      boolean doBackup,
      boolean force,
      boolean verbose,
      boolean debug) {
    if (Objects.isNull(ldproxyCfg)) {
      return Result.failure("Not connected to store");
    }

    Result result = new Result();

    for (ValueUpgrade upgrade : getUpgrades(ldproxyCfg, path, force, debug)) {
      if (upgrade.hasError()) {
        result.error(
            String.format(
                "Could not read %s: %s", upgrade.relativePath, upgrade.messages.getError().get()));
        continue;
      }
      if (!upgrade.hasUpgrade()) {
        continue;
      }

      if (doBackup) {
        Path backup =
            upgrade.path.getParent().resolve(upgrade.path.getFileName().toString() + ".backup");
        try {
          Files.copy(
              upgrade.path,
              backup,
              StandardCopyOption.REPLACE_EXISTING,
              StandardCopyOption.COPY_ATTRIBUTES);

          if (verbose) {
            result.success(
                String.format(
                    "Value configuration backup created: %s",
                    ldproxyCfg.getDataDirectory().relativize(backup)));
          }
        } catch (IOException e) {
          result.error(String.format("Could not create backup %s: %s", backup, e.getMessage()));
          continue;
        }
      }

      try {
        if (upgrade.isJson) {
          JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValue(upgrade.path.toFile(), upgrade.upgraded);
        } else {
          ldproxyCfg.getObjectMapper().writeValue(upgrade.path.toFile(), upgrade.upgraded);
        }

        result.success(String.format("Value configuration upgraded: %s", upgrade.relativePath));
      } catch (IOException e) {
        result.error(
            String.format("Could not upgrade %s: %s", upgrade.relativePath, e.getMessage()));
      }
    }

    return result;
  }

  private static List<ValueUpgrade> getUpgrades(
      LdproxyCfg ldproxyCfg, Optional<String> path, boolean force, boolean debug) {
    Map<String, List<ValueMigration>> migrationsByType =
        ldproxyCfg.migrations().values().stream()
            .collect(
                Collectors.groupingBy(
                    ValueMigration::getValueType, LinkedHashMap::new, Collectors.toList()));

    List<ValueUpgrade> upgrades = new ArrayList<>();

    for (Map.Entry<String, List<ValueMigration>> entry : migrationsByType.entrySet()) {
      Path typePath = ldproxyCfg.getValuesPath().resolve(entry.getKey());

      if (!Files.isDirectory(typePath)) {
        continue;
      }

      try (Stream<Path> files = Files.walk(typePath)) {
        files
            .filter(Files::isRegularFile)
            .filter(ValuesHandler::isValueFile)
            .filter(
                file ->
                    path.isEmpty()
                        || Objects.equals(
                            Path.of(path.get()).toString(),
                            ldproxyCfg.getDataDirectory().relativize(file).toString()))
            .sorted()
            .forEach(
                file ->
                    upgrades.add(getUpgrade(ldproxyCfg, file, entry.getValue(), force, debug)));
      } catch (IOException e) {
        upgrades.add(
            new ValueUpgrade(
                typePath,
                ldproxyCfg.getDataDirectory().relativize(typePath),
                false,
                null,
                new ValueMessages(
                    ldproxyCfg.getDataDirectory().relativize(typePath), e.getMessage())));
      }
    }

    return upgrades;
  }

  private static ValueUpgrade getUpgrade(
      LdproxyCfg ldproxyCfg,
      Path file,
      List<ValueMigration> migrations,
      boolean force,
      boolean debug) {
    Path relativePath = ldproxyCfg.getDataDirectory().relativize(file);
    boolean isJson = isJson(file);
    ValueMessages messages = new ValueMessages(relativePath);

    try {
      JsonNode original =
          isJson
              ? JSON_MAPPER.readTree(file.toFile())
              : ldproxyCfg.getObjectMapper().readTree(file.toFile());
      JsonNode upgraded = original;
      boolean isUpgraded = false;

      for (ValueMigration migration : migrations) {
        if (migration.isApplicable(upgraded)) {
          messages.addMessage(
              Migration.migration(migration.getSubject(), migration.getDescription()));
          upgraded = migration.migrate(upgraded);
          isUpgraded = true;
        }
      }

      if (debug) {
        System.out.println("VALUE " + relativePath + " upgraded: " + isUpgraded);
      }

      return new ValueUpgrade(
          file, relativePath, isJson, isUpgraded || force ? upgraded : null, messages);
    } catch (Throwable e) {
      if (debug) {
        System.err.println("Could not read " + file);
        e.printStackTrace(System.err);
      }
      return new ValueUpgrade(
          file, relativePath, isJson, null, new ValueMessages(relativePath, e.getMessage()));
    }
  }

  private static boolean isValueFile(Path file) {
    String name = file.getFileName().toString().toLowerCase(Locale.ROOT);

    return name.endsWith(".json") || name.endsWith(".yml") || name.endsWith(".yaml");
  }

  private static boolean isJson(Path file) {
    return file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json");
  }
}
