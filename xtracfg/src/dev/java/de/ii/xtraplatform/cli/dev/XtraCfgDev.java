package de.ii.xtraplatform.cli.dev;

import de.ii.xtraplatform.cli.CommandHandler;
import de.ii.xtraplatform.cli.EntitiesHandler;

public class XtraCfgDev {

  // TODO: json
  public static void main(String[] args) {
    if (args.length == 0) {
      throw new IllegalArgumentException("No command given");
    }

    EntitiesHandler.DEV = true;
    String parameters = "";

    try {
      CommandHandler commandHandler = new CommandHandler();

      for (int i = 1; i < args.length; i++) {
        if (args[i].startsWith("-")) {
          parameters += i == 1 ? "?" : "&";
          parameters += args[i].replaceAll("-", "").replace("src", "source");
          if (args.length <= i + 1 || args[i + 1].startsWith("-")) {
            parameters += "=true";
          }
        } else {
          parameters += "=" + args[i];
        }
      }

      String connect = "/connect" + parameters;

      String connectResult = commandHandler.handleCommand(connect);

      System.out.println(connectResult);

      String command = "/" + args[0] + parameters;

      System.out.println("COMMAND: " + command);

      String result = commandHandler.handleCommand(command);

      System.out.println(result);

    } catch (Throwable e) {
      System.out.println("ERROR: " + parameters + " | " + e.getMessage());
      e.printStackTrace();
    }
  }
}
