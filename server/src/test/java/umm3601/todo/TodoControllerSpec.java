package umm3601.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;

/**
 * Tests the logic of the TodoController
 *
 * @throws IOException
 */
// The tests here include a ton of "magic numbers" (numeric constants).
// It wasn't clear to me that giving all of them names would actually
// help things. The fact that it wasn't obvious what to call some
// of them says a lot. Maybe what this ultimately means is that
// these tests can/should be restructured so the constants (there are
// also a lot of "magic strings" that Checkstyle doesn't actually
// flag as a problem) make more sense.
@SuppressWarnings({ "MagicNumber" })
class TodoControllerSpec {

  // An instance of the controller we're testing that is prepared in
  // `setupEach()`, and then exercised in the various tests below.
  private TodoController todoController;

  // A Mongo object ID that is initialized in `setupEach()` and used
  // in a few of the tests. It isn't used all that often, though,
  // which suggests that maybe we should extract the tests that
  // care about it into their own spec file?
  private ObjectId samsId;

  // The client and database that will be used
  // for all the tests in this spec file.
  private static MongoClient mongoClient;
  private static MongoDatabase db;

  @Mock
  private Context ctx;

  @Captor
  private ArgumentCaptor<ArrayList<Todo>> todoArrayListCaptor;

  @Captor
  private ArgumentCaptor<Todo> todoCaptor;

  @Captor
  private ArgumentCaptor<Map<String, String>> mapCaptor;

  /**
   * Sets up (the connection to the) DB once; that connection and DB will
   * then be (re)used for all the tests, and closed in the `teardown()`
   * method. It's somewhat expensive to establish a connection to the
   * database, and there are usually limits to how many connections
   * a database will support at once. Limiting ourselves to a single
   * connection that will be shared across all the tests in this spec
   * file helps both speed things up and reduce the load on the DB
   * engine.
   */
  @BeforeAll
  static void setupAll() {
    String mongoAddr = System.getenv().getOrDefault("MONGO_ADDR", "localhost");

    mongoClient = MongoClients.create(
        MongoClientSettings.builder()
            .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(mongoAddr))))
            .build());
    db = mongoClient.getDatabase("test");
  }

  @AfterAll
  static void teardown() {
    db.drop();
    mongoClient.close();
  }

  @BeforeEach
  void setupEach() throws IOException {
    // Reset our mock context and argument captor (declared with Mockito
    // annotations @Mock and @Captor)
    MockitoAnnotations.openMocks(this);

    // Setup database
    MongoCollection<Document> todoDocuments = db.getCollection("todos");
    todoDocuments.drop();
    List<Document> testTodos = new ArrayList<>();
    testTodos.add(
        new Document()
            .append("owner", "Barry")
            .append("status", false)
            .append("body",
            "Deserunt velit reprehenderit deserunt sunt excepteur sit eu eiusmod in voluptate aute minim mollit.")
            .append("category", "homework"));
    testTodos.add(
      new Document()
          .append("owner", "Fry")
          .append("status", false)
          .append("body",
          "Sunt esse dolore sunt Lorem velit reprehenderit incididunt minim Lorem sint Lorem sit voluptate proident.")
          .append("category", "homework"));
    testTodos.add(
      new Document()
          .append("owner", "Dawn")
          .append("status", true)
          .append("body",
          "Est ullamco consequat consectetur velit dolor qui pariatur proident dolor commodo ex.")
          .append("category", "groceries"));

    samsId = new ObjectId();
    Document sam = new Document()
        .append("_id", samsId)
        .append("owner", "Sam")
        .append("status", false)
        .append("body",
        "In velit adipisicing ea in in consequat. Deserunt id deserunt minim quis reprehenderit et dolore.")
        .append("category", "video games");

    todoDocuments.insertMany(testTodos);
    todoDocuments.insertOne(sam);

    todoController = new TodoController(db);
  }

  @Test
  void canGetAllTodos() throws IOException {
    // When something asks the (mocked) context for the queryParamMap,
    // it will return an empty map (since there are no query params in
    // this case where we want all todos).
    when(ctx.queryParamMap()).thenReturn(Collections.emptyMap());

    // Now, go ahead and ask the todoController to getTodos
    // (which will, indeed, ask the context for its queryParamMap)
    todoController.getTodos(ctx);

    // We are going to capture an argument to a function, and the type of
    // that argument will be of type ArrayList<Todo> (we said so earlier
    // using a Mockito annotation like this):
    // @Captor
    // private ArgumentCaptor<ArrayList<Todo>> todoArrayListCaptor;
    // We only want to declare that captor once and let the annotation
    // help us accomplish reassignment of the value for the captor
    // We reset the values of our annotated declarations using the command
    // `MockitoAnnotations.openMocks(this);` in our @BeforeEach

    // Specifically, we want to pay attention to the ArrayList<Todo> that
    // is passed as input when ctx.json is called --- what is the argument
    // that was passed? We capture it and can refer to it later.
    verify(ctx).json(todoArrayListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    // Check that the database collection holds the same number of documents
    // as the size of the captured List<Todo>
    assertEquals(
        db.getCollection("todos").countDocuments(),
        todoArrayListCaptor.getValue().size());
  }

  @Test
  void canGetTodosWithCategory() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put(TodoController.CATEGORY_KEY, Arrays.asList(new String[] {"groceries"}));
    queryParams.put("sortorder", Arrays.asList(new String[] {"desc"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    when(ctx.queryParam(TodoController.CATEGORY_KEY)).thenReturn("groceries");
    when(ctx.queryParam("sortorder")).thenReturn("desc");

    todoController.getTodos(ctx);

    verify(ctx).json(todoArrayListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    for (Todo todo : todoArrayListCaptor.getValue()) {
      assertEquals("groceries", todo.category);
    }
  }

  @Test
  void canGetTodosWithOwner() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put(TodoController.OWNER_KEY, Arrays.asList(new String[] {"Barry"}));
    queryParams.put("sortorder", Arrays.asList(new String[] {"desc"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    when(ctx.queryParam(TodoController.OWNER_KEY)).thenReturn("Barry");
    when(ctx.queryParam("sortorder")).thenReturn("desc");

    todoController.getTodos(ctx);

    verify(ctx).json(todoArrayListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    for (Todo todo : todoArrayListCaptor.getValue()) {
      assertEquals("Barry", todo.owner);
    }
  }

  @Test
  void canGetTodosWithStatusTrue() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put(TodoController.STATUS_KEY, Arrays.asList(new String[] {"complete"}));
    queryParams.put("sortorder", Arrays.asList(new String[] {"desc"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    when(ctx.queryParam(TodoController.STATUS_KEY)).thenReturn("complete");
    when(ctx.queryParam("sortorder")).thenReturn("desc");

    todoController.getTodos(ctx);

    verify(ctx).json(todoArrayListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    for (Todo todo : todoArrayListCaptor.getValue()) {
      assertEquals(true, todo.status);
    }
  }

  @Test
  void canGetTodosWithStatusFalse() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put(TodoController.STATUS_KEY, Arrays.asList(new String[] {"incomplete"}));
    queryParams.put("sortorder", Arrays.asList(new String[] {"desc"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    when(ctx.queryParam(TodoController.STATUS_KEY)).thenReturn("incomplete");
    when(ctx.queryParam("sortorder")).thenReturn("desc");

    todoController.getTodos(ctx);

    verify(ctx).json(todoArrayListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    for (Todo todo : todoArrayListCaptor.getValue()) {
      assertEquals(false, todo.status);
    }
  }

  @Test
  void canGetTodosWithStatusNeither() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put(TodoController.STATUS_KEY, Arrays.asList(new String[] {"abc"}));
    queryParams.put("sortorder", Arrays.asList(new String[] {"desc"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    when(ctx.queryParam(TodoController.STATUS_KEY)).thenReturn("abc");
    when(ctx.queryParam("sortorder")).thenReturn("desc");

    todoController.getTodos(ctx);

    verify(ctx).json(todoArrayListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    for (Todo todo : todoArrayListCaptor.getValue()) {
      assertEquals(false, todo.status);
    }
  }

  @Test
  void getUserWithExistentId() throws IOException {
    String id = samsId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);

    todoController.getTodo(ctx);

    verify(ctx).json(todoCaptor.capture());
    verify(ctx).status(HttpStatus.OK);
    assertEquals("Sam", todoCaptor.getValue().owner);
    assertEquals(samsId.toHexString(), todoCaptor.getValue()._id);
  }

  @Test
  void getUserWithBadId() throws IOException {
    when(ctx.pathParam("id")).thenReturn("bad");

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      todoController.getTodo(ctx);
    });

    assertEquals("The requested todo id wasn't a legal Mongo Object ID.", exception.getMessage());
  }

  @Test
  void getUserWithNonexistentId() throws IOException {
    String id = "588935f5c668650dc77df581";
    when(ctx.pathParam("id")).thenReturn(id);

    Throwable exception = assertThrows(NotFoundResponse.class, () -> {
      todoController.getTodo(ctx);
    });

    assertEquals("The requested todo was not found", exception.getMessage());
  }

  @Test
  void addsRoutes() {
    Javalin mockServer = mock(Javalin.class);
    todoController.addRoutes(mockServer);
    verify(mockServer, Mockito.atLeast(2)).get(any(), any());
  }

  @Test
  void canGetTodosWithBody() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put(TodoController.BODY_KEY, Arrays.asList(new String[]
      {"Est ullamco consequat consectetur velit dolor qui pariatur proident dolor commodo ex."}));
    queryParams.put("sortorder", Arrays.asList(new String[] {"desc"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    when(ctx.queryParam(TodoController.BODY_KEY)).thenReturn(
      "Est ullamco consequat consectetur velit dolor qui pariatur proident dolor commodo ex.");
    when(ctx.queryParam("sortorder")).thenReturn("desc");

    todoController.getTodos(ctx);

    verify(ctx).json(todoArrayListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    for (Todo todo : todoArrayListCaptor.getValue()) {
      assertEquals("Est ullamco consequat consectetur velit dolor qui pariatur proident dolor commodo ex.", todo.body);
    }
  }

  @Test
  void canLimitNumberOfTodos() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put(TodoController.LIMIT_KEY, Arrays.asList(new String[]{"1"}));
    queryParams.put("sortorder", Arrays.asList(new String[] {"desc"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    when(ctx.queryParam(TodoController.LIMIT_KEY)).thenReturn("1");
    when(ctx.queryParam("sortorder")).thenReturn("desc");

    todoController.getTodos(ctx);

    verify(ctx).json(todoArrayListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    assertEquals(todoArrayListCaptor.getAllValues().size(), 1);
  }

  @Test
  void canGetWithStatusAndOwner() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put(TodoController.STATUS_KEY, Arrays.asList(new String[] {"incomplete"}));
    queryParams.put(TodoController.OWNER_KEY, Arrays.asList(new String[] {"Barry"}));
    queryParams.put("sortorder", Arrays.asList(new String[] {"asc"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    when(ctx.queryParam(TodoController.OWNER_KEY)).thenReturn("Barry");
    when(ctx.queryParam(TodoController.STATUS_KEY)).thenReturn("incomplete");

    todoController.getTodos(ctx);

    verify(ctx).json(todoArrayListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    for (Todo todo : todoArrayListCaptor.getValue()) {
      assertEquals("Barry", todo.owner);
      assertEquals(false, todo.status);
    }
  }
}
