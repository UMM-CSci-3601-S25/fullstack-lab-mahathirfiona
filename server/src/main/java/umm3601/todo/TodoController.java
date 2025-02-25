package umm3601.todo;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;

import java.util.ArrayList;
import java.util.List;
//import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
//import com.mongodb.client.result.DeleteResult;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Controller;

public class TodoController implements Controller {

  private static final String API_TODOS = "/api/todos";
  private static final String API_TODO_BY_ID = "/api/todos/{id}";
  static final String OWNER_KEY = "owner";
  static final String STATUS_KEY = "status";
  static final String BODY_KEY = "contains";
  static final String CATEGORY_KEY = "category";
  static final String LIMIT_KEY = "limit";
  static final String ORDER_KEY = "orderBy";


  //private static final String ROLE_REGEX = "^(admin|editor|viewer)$";
  public static final String EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

  private final JacksonMongoCollection<Todo> todoCollection;

  /**
   * Construct a controller for todos.
   *
   * @param database the database containing todo data
   */
  public TodoController(MongoDatabase database) {
    todoCollection = JacksonMongoCollection.builder().build(
        database,
        "todos",
        Todo.class,
        UuidRepresentation.STANDARD);
  }

  /**
   * Set the JSON body of the response to be the single todo
   * specified by the `id` parameter in the request
   *
   * @param ctx a Javalin HTTP context
   */
  public void getTodo(Context ctx) {
    String id = ctx.pathParam("id");
    Todo todo;

    try {
      todo = todoCollection.find(eq("_id", new ObjectId(id))).first();
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("The requested todo id wasn't a legal Mongo Object ID.");
    }
    if (todo == null) {
      throw new NotFoundResponse("The requested todo was not found");
    } else {
      ctx.json(todo);
      ctx.status(HttpStatus.OK);
    }
  }

  /**
   * Set the JSON body of the response to be a list of all the todos returned from the database
   * that match any requested filters and ordering
   *
   * @param ctx a Javalin HTTP context
   */

  public void getTodos(Context ctx) {
    Bson combinedFilter = constructFilter(ctx);
    Bson sortingOrder = constructSortingOrder(ctx);

    // All three of the find, sort, and into steps happen "in parallel" inside the
    // database system. So MongoDB is going to find the todos with the specified
    // properties, return those sorted in the specified manner, and put the
    // results into an initially empty ArrayList.
    ArrayList<Todo> matchingTodos = todoCollection
      .find(combinedFilter)
      .sort(sortingOrder)
      .into(new ArrayList<>());

    if (ctx.queryParamMap().containsKey(LIMIT_KEY)) {
      int limit = Integer.parseInt(ctx.queryParam(LIMIT_KEY));
      while (matchingTodos.size() > limit) {
        matchingTodos.remove(matchingTodos.size() - 1);
      }
    }
    // Set the JSON body of the response to be the list of todos returned by the database.
    // According to the Javalin documentation (https://javalin.io/documentation#context),
    // this calls result(jsonString), and also sets content type to json
    ctx.json(matchingTodos);

    // Explicitly set the context status to OKBODY_KEY
    ctx.status(HttpStatus.OK);
  }


  private Bson constructFilter(Context ctx) {
    List<Bson> filters = new ArrayList<>(); // start with an empty list of filters

    if (ctx.queryParamMap().containsKey(OWNER_KEY)) {
        Pattern pattern = Pattern.compile(Pattern.quote(ctx.queryParam(OWNER_KEY)), Pattern.CASE_INSENSITIVE);
        filters.add(regex(OWNER_KEY, pattern));
    }
    if (ctx.queryParamMap().containsKey(STATUS_KEY)) {
        boolean value1 = ctx.queryParam(STATUS_KEY).equals("complete");
        if (value1) {
          filters.add(eq(STATUS_KEY, value1));
        }
        boolean value2 = ctx.queryParam(STATUS_KEY).equals("incomplete");
        if (value2) {
          filters.add(eq(STATUS_KEY, !value2));
        }
        if (!value1 && !value2) {
          filters.add(eq(STATUS_KEY, null));
        }
    }
    if (ctx.queryParamMap().containsKey(CATEGORY_KEY)) {
        Pattern pattern = Pattern.compile(Pattern.quote(ctx.queryParam(CATEGORY_KEY)), Pattern.CASE_INSENSITIVE);
        filters.add(regex(CATEGORY_KEY, pattern));
    }
    if (ctx.queryParamMap().containsKey(BODY_KEY)) {
      Pattern pattern = Pattern.compile(Pattern.quote(ctx.queryParam(BODY_KEY)), Pattern.CASE_INSENSITIVE);
      filters.add(regex("body", pattern));
    }


    // Combine the list of filters into a single filtering document.
    Bson combinedFilter = filters.isEmpty() ? new Document() : and(filters);

    return combinedFilter;
  }

  private Bson constructSortingOrder(Context ctx) {
    String sortBy = Objects.requireNonNullElse(ctx.queryParam(ORDER_KEY), "owner");
    String sortOrder = Objects.requireNonNullElse(ctx.queryParam("sortorder"), "asc");
    Bson sortingOrder = sortOrder.equals("desc") ?  Sorts.descending(sortBy) : Sorts.ascending(sortBy);
    return sortingOrder;
  }
  /**
   * Setup routes for the `todo` collection endpoints.
   *
   * These endpoints are:
   *   - `GET /api/todos/:id`
   *       - Get the specified todo
   *   - `GET /api/todos?age=NUMBER&company=STRING&name=STRING`
   *      - List todos, filtered using query parameters
   *      - `age`, `company`, and `name` are optional query parameters
   *   - `GET /api/todosByCompany`
   *     - Get todo names and IDs, possibly filtered, grouped by company
   *   - `DELETE /api/todos/:id`
   *      - Delete the specified todo
   *   - `POST /api/todos`
   *      - Create a new todo
   *      - The todo info is in the JSON body of the HTTP request
   *
   * GROUPS SHOULD CREATE THEIR OWN CONTROLLERS THAT IMPLEMENT THE
   * `Controller` INTERFACE FOR WHATEVER DATA THEY'RE WORKING WITH.
   * You'll then implement the `addRoutes` method for that controller,
   * which will set up the routes for that data. The `Server#setupRoutes`
   * method will then call `addRoutes` for each controller, which will
   * add the routes for that controller's data.
   *
   * @param server The Javalin server instance
   * @param todoController The controller that handles the todo endpoints
   */
  public void addRoutes(Javalin server) {
    // Get the specified todo
    server.get(API_TODO_BY_ID, this::getTodo);

    // List todos, filtered using query parameters
    server.get(API_TODOS, this::getTodos);
  }
}
