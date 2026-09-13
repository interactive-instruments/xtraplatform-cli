package de.ii.xtraplatform.cli.cmd;

import de.ii.ldproxy.cfg.Layout;
import de.ii.ldproxy.cfg.LdproxyCfg;
import de.ii.xtraplatform.cli.*;
import java.util.Map;
import java.util.Optional;

public class Upgrade extends Store {

  private final boolean pre;
  private final boolean force;
  private final boolean backup;

  public Upgrade(Optional<String> subcommand, Map<String, Object> parameters, boolean pre) {
    super(subcommand, parameters);

    this.pre = pre;
    this.force = flag(parameters, "force");
    this.backup = flag(parameters, "backup");
  }

  public Result run(Subcommand cmd, LdproxyCfg ldproxyCfg, Layout layout) {
    if (pre) {
      switch (cmd) {
        case cfg:
          return CfgHandler.preUpgrade(ldproxyCfg, ignoreRedundant, force, verbose, debug);
        case entities:
          return EntitiesHandler.preUpgrade(
              ldproxyCfg, EntitiesHandler.Type.All, path, ignoreRedundant, force, verbose, debug);
        case values:
          return ValuesHandler.preUpgrade(ldproxyCfg, path, force, verbose, debug);
        case layout:
          return LayoutHandler.preUpgrade(layout, verbose);
        default:
          throw new IllegalStateException("Unexpected subcommand: " + subcommand);
      }
    }

    switch (cmd) {
      case cfg:
        return CfgHandler.upgrade(ldproxyCfg, backup, ignoreRedundant, force, verbose, debug);
      case entities:
        return EntitiesHandler.upgrade(
            ldproxyCfg,
            EntitiesHandler.Type.All,
            path,
            backup,
            ignoreRedundant,
            force,
            verbose,
            debug);
      case values:
        return ValuesHandler.upgrade(ldproxyCfg, path, backup, force, verbose, debug);
      case layout:
        return LayoutHandler.upgrade(layout, verbose);
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
