package de.ii.xtraplatform.cli.cmd;

import de.ii.ldproxy.cfg.Layout;
import de.ii.ldproxy.cfg.LdproxyCfg;
import de.ii.xtraplatform.cli.*;
import java.util.Map;
import java.util.Optional;

public class Check extends Store {

  public Check(Optional<String> subcommand, Map<String, Object> parameters) {
    super(subcommand, parameters);
  }

  public Result run(Subcommand cmd, LdproxyCfg ldproxyCfg, Layout layout) {
    switch (cmd) {
      case cfg:
        return CfgHandler.check(ldproxyCfg, ignoreRedundant, verbose, debug);
      case entities:
        return EntitiesHandler.check(
            ldproxyCfg, EntitiesHandler.Type.All, path, ignoreRedundant, verbose, debug);
      case values:
        return ValuesHandler.check(ldproxyCfg, path, verbose, debug);
      case layout:
        return LayoutHandler.check(layout, verbose);
      default:
        throw new IllegalStateException("Unexpected subcommand: " + subcommand);
    }
  }

  @Override
  public Result run(Context context) {
    Result result = Result.empty();

    for (Subcommand cmd : Subcommand.values()) {
      if (shouldRun(cmd)) {
        result = result.merge(run(cmd, context.ldproxyCfg, context.layout));
      }
    }

    if (result.isEmpty()) {
      result.success("Everything is fine");
    }

    return result;
  }
}
