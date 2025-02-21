package umm3601.todo;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;

//import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
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
   public static final String STATUS_KEY = "status";
  static final String BODY_KEY = "body";
  public static final String CATEGORY_KEY = "category";
  public static final String LIMIT_KEY = "limit";
  static final String ORDER_SORT_KEY = "order";

  private static final String CATEGORY_REGEX = "^(video games|homework|groceries|software design)$";

  private final JacksonMongoCollection<Todo> todoCollection;

  
  public TodoController(MongoDatabase database) {
    todoCollection = JacksonMongoCollection.builder().build(
        database,
        "todos",
        Todo.class,
        UuidRepresentation.STANDARD);
  }

  
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

  public void getTodos(Context ctx) {
    Bson combinedFilter = constructFilter(ctx);
    Bson sortingOrder = constructSortingOrder(ctx);

    ArrayList<Todo> matchingTodos = todoCollection
      .find(combinedFilter)
      .sort(sortingOrder)
      .limit(limit(ctx))
      .into(new ArrayList<>());

    ctx.json(matchingTodos);

    // Explicitly set the context status to OK
    ctx.status(HttpStatus.OK);
  }

  private Bson constructFilter(Context ctx) {
    List<Bson> filters = new ArrayList<>(); // start with an empty list of filters

    if (ctx.queryParamMap().containsKey(BODY_KEY)) {
      String targetContent = ctx.queryParam(BODY_KEY);
      Pattern pattern = Pattern.compile(targetContent, Pattern.CASE_INSENSITIVE);
      filters.add(regex("body", pattern));
    }

    if (ctx.queryParamMap().containsKey(STATUS_KEY)) {
      String statusParam = ctx.queryParam(STATUS_KEY);
      boolean targetStatus;
      if (statusParam.equalsIgnoreCase("complete")) {
        targetStatus = true;
      } else if (statusParam.equalsIgnoreCase("incomplete")) {
        targetStatus = false;
      } else {
        throw new BadRequestResponse("Invalid status request");
      }
      filters.add(eq(STATUS_KEY, targetStatus));
    }




    if (ctx.queryParamMap().containsKey(OWNER_KEY)) {
    String targetOwner = ctx.queryParam(OWNER_KEY);
      filters.add(regex("owner", Pattern.compile(targetOwner, Pattern.CASE_INSENSITIVE)));
    }


    if (ctx.queryParamMap().containsKey(CATEGORY_KEY)) {
      String role = ctx.queryParamAsClass(CATEGORY_KEY, String.class)
        .check(it -> it.matches(CATEGORY_REGEX), "todo must have a legal todo category")
        .get();
      filters.add(eq(CATEGORY_KEY, role));
    }

    Bson combinedFilter = filters.isEmpty() ? new Document() : and(filters);

    return combinedFilter;
  }

  private Bson constructSortingOrder(Context ctx) {

    String sortBy = Objects.requireNonNullElse(ctx.queryParam("sortby"), "owner");
    String sortOrder = Objects.requireNonNullElse(ctx.queryParam("sortorder"), "asc");
    Bson sortingOrder = sortOrder.equals("desc") ?  Sorts.descending(sortBy) : Sorts.ascending(sortBy);
    return sortingOrder;
  }

  private int limit(Context ctx) {
    if (ctx.queryParamMap().containsKey(LIMIT_KEY)) {
      int targetLimit = ctx.queryParamAsClass(LIMIT_KEY, Integer.class)
      .check(it -> it > 0, "Limit should be greater than 0. You gave" + ctx.queryParam(LIMIT_KEY))
      .get();
      return targetLimit;
    } else {
      return (int) todoCollection.countDocuments();
    }
  }


  public void addRoutes(Javalin server) {
    server.get(API_TODO_BY_ID, this::getTodo);

    server.get(API_TODOS, this::getTodos);
  }
}
