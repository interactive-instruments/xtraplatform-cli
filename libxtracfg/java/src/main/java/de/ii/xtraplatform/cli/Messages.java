package de.ii.xtraplatform.cli;

import de.ii.xtraplatform.values.domain.Identifier;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import shadow.com.networknt.schema.Error;

public abstract class Messages {

  private final EntitiesHandler.Type type;
  private final Path path;
  private final Identifier identifier;
  private final Set<Error> validationMessages;
  private String error;

  public Messages(EntitiesHandler.Type type, Identifier identifier, Path path) {
    this(type, identifier, path, null);
  }

  public Messages(EntitiesHandler.Type type, Identifier identifier, Path path, String error) {
    this.type = type;
    this.identifier = identifier;
    this.path = path;
    this.error = error;
    this.validationMessages = new LinkedHashSet<>();
  }

  public EntitiesHandler.Type getType() {
    return type;
  }

  public Path getPath() {
    return path;
  }

  public Identifier getIdentifier() {
    return identifier;
  }

  public Optional<String> getError() {
    return Optional.ofNullable(error);
  }

  protected final void setError(String error) {
    this.error = error;
  }

  public List<String> getErrors() {
    return validationMessages.stream()
        .filter(this::isError)
        .map(this::getMessage)
        .collect(Collectors.toList());
  }

  public List<String> getWarnings() {
    return validationMessages.stream()
        .filter(this::isWarning)
        .map(this::getMessage)
        .collect(Collectors.toList());
  }

  public boolean hasErrors() {
    return !getErrors().isEmpty();
  }

  public boolean hasWarnings() {
    return !getWarnings().isEmpty();
  }

  protected Set<Error> getMessages() {
    return validationMessages;
  }

  public void addMessage(Error validationMessage) {
    this.validationMessages.add(validationMessage);
  }

  public void addMessages(Set<Error> validationMessages) {
    this.validationMessages.addAll(validationMessages);
  }

  public void log(Result result, boolean verbose) {
    if (getError().isPresent()) {
      result.error(String.format("Could not read %s: %s", getPath(), getError().get()));
      return;
    }

    if (hasErrors()) {
      result.error(getSummary());
      getErrors().forEach(message -> result.info("  - " + message));
    } else if (hasWarnings()) {
      result.warning(getSummary());

      if (verbose) {
        getWarnings().forEach(message -> result.info("  - " + message));
      }
    } else if (verbose) {
      result.success(getSummary());
    }
  }

  public void logErrors(Result result, boolean verbose) {
    if (getError().isPresent()) {
      result.error(String.format("Could not read %s: %s", getPath(), getError().get()));
      result.info("");
      return;
    }

    if (hasErrors()) {
      result.error(getSummary());
      if (verbose) {
        getErrors().forEach(message -> result.info("  - " + message));
      }
      result.info("");
    }
  }

  protected abstract String getSummary();

  protected abstract boolean isWarning(Error vm);

  protected boolean isError(Error vm) {
    return !isWarning(vm);
  }

  protected String getMessage(Error vm) {
    return vm.getMessage();
  }
}
