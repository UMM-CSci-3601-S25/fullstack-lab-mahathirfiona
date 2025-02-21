package umm3601;

import java.util.Arrays;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.bson.UuidRepresentation;

import io.javalin.Javalin;
import io.javalin.http.InternalServerErrorResponse;


public class Server {

  private static final int SERVER_PORT = 4567;

  private final MongoClient mongoClient;

  private Controller[] controllers;

  
  public Server(MongoClient mongoClient, Controller[] controllers) {
    this.mongoClient = mongoClient;
    this.controllers = Arrays.copyOf(controllers, controllers.length);
  }

 
  static MongoClient configureDatabase(String mongoAddr) {
    MongoClient mongoClient = MongoClients.create(MongoClientSettings
      .builder()
      .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(mongoAddr))))
      .uuidRepresentation(UuidRepresentation.STANDARD)
      .build());

    return mongoClient;
  }


  void startServer() {
    Javalin javalin = configureJavalin();
    setupRoutes(javalin);
    javalin.start(SERVER_PORT);
  }


  private Javalin configureJavalin() {

    Javalin server = Javalin.create(config ->
      config.bundledPlugins.enableRouteOverview("/api")
    );

    configureShutdowns(server);


    server.exception(Exception.class, (e, ctx) -> {
      throw new InternalServerErrorResponse(e.toString());
    });

    return server;
  }


  private void configureShutdowns(Javalin server) {

    Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
   
    server.events(event -> {
      event.serverStartFailed(mongoClient::close);
      event.serverStopped(mongoClient::close);
    });
  }

  
  private void setupRoutes(Javalin server) {
    for (Controller controller : controllers) {
      controller.addRoutes(server);
    }
  }
}
