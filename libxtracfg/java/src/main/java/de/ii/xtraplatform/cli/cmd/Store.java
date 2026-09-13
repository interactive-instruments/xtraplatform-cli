package de.ii.xtraplatform.cli.cmd;

import java.util.Map;
import java.util.Optional;

public abstract class Store extends Common<Context> {

  public enum Subcommand {
    cfg,
    entities,
    values,
    layout
  }

  public final Optional<Subcommand> subcommand;
  public final boolean ignoreRedundant;
  public final Optional<String> path;

  protected Store(Optional<String> subcommand, Map<String, Object> parameters) {
    super(parameters);

    this.subcommand = subcommand(subcommand, Subcommand::valueOf);
    this.ignoreRedundant = flag(parameters, "ignoreRedundant");
    this.path = optionalString(parameters, "path");
  }

  protected Store(Store store) {
    super(store);

    this.subcommand = store.subcommand;
    this.ignoreRedundant = store.ignoreRedundant;
    this.path = store.path;
  }

  public final boolean shouldRun(Subcommand subcommand) {
    return this.subcommand.isEmpty() || this.subcommand.get() == subcommand;
  }
}
