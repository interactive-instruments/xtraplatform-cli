package de.ii.xtraplatform.cli;

import de.ii.xtraplatform.values.domain.Identifier;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Objects;
import shadow.com.networknt.schema.Error;
import shadow.com.networknt.schema.path.NodePath;
import shadow.com.networknt.schema.path.PathType;

public class Migration extends Messages {
  public Migration(EntitiesHandler.Type type, Identifier identifier, Path path) {
    super(type, identifier, path);
  }

  public Migration(EntitiesHandler.Type type, Identifier identifier, Path path, String error) {
    super(type, identifier, path, error);
  }

  @Override
  protected String getSummary() {
    return String.format(
        "Migrations are available for %s configuration: %s",
        getType().name().toLowerCase(), getPath());
  }

  @Override
  public void log(Result result, boolean verbose) {
    if (hasWarnings()) {
      super.log(result, verbose);
    }
  }

  @Override
  protected boolean isWarning(Error vm) {
    return isMigration(vm);
  }

  private static boolean isMigration(Error vm) {
    return Objects.equals(vm.getKeyword(), MIGRATION);
  }

  private static final String MIGRATION = "migration";

  static Error migration(String path, String message) {
    return new Error.Builder()
        .keyword(MIGRATION)
        .instanceLocation(new NodePath(PathType.JSON_PATH).append(path))
        .arguments(message)
        .format(new MessageFormat("{0}: {1}"))
        .build();
  }
}
