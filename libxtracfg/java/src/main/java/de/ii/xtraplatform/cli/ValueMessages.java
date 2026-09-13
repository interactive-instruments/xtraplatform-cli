package de.ii.xtraplatform.cli;

import java.nio.file.Path;
import java.util.Objects;
import shadow.com.networknt.schema.Error;

public class ValueMessages extends Messages {

  public ValueMessages(Path path) {
    super(null, null, path);
  }

  public ValueMessages(Path path, String error) {
    super(null, null, path, error);
  }

  @Override
  protected String getSummary() {
    return String.format("Migrations are available for value configuration: %s", getPath());
  }

  @Override
  public void log(Result result, boolean verbose) {
    if (getError().isPresent() || hasWarnings()) {
      super.log(result, verbose);
    }
  }

  @Override
  protected boolean isWarning(Error vm) {
    return Objects.equals(vm.getKeyword(), Migration.MIGRATION);
  }
}
