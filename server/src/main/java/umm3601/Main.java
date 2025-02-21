package umm3601;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import umm3601.user.UserController;
import umm3601.todo.TodoController;

public class Main {

  public static void main(String[] args) {
    String mongoAddr = Main.getEnvOrDefault("MONGO_ADDR", "localhost");
    String databaseName = Main.getEnvOrDefault("MONGO_DB", "dev");

    MongoClient mongoClient = Server.configureDatabase(mongoAddr);
    MongoDatabase database = mongoClient.getDatabase(databaseName);

    final Controller[] controllers = Main.getControllers(database);
    Server server = new Server(mongoClient, controllers);
    server.startServer();
  }

  static String getEnvOrDefault(String envName, String defaultValue) {
    return System.getenv().getOrDefault(envName, defaultValue);
  }


  static Controller[] getControllers(MongoDatabase database) {
    Controller[] controllers = new Controller[] {
      new UserController(database), new TodoController(database)
    };
    return controllers;
  }

}
