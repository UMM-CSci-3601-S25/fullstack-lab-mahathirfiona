import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Todo, TodoRole } from './todo';
import { Company } from '../company-list/company';
import { todo } from 'node:test';

/**
 * Service that provides the interface for getting information
 * about `Users` from the server.
 */
@Injectable({
  providedIn: 'root'
})
export class TodoService {
  // The URL for the users part of the server API.
  readonly todoUrl: string = `${environment.apiUrl}todos`;

  private readonly statusKey = 'status';
  private readonly ownerKey = 'owner';
  private readonly bodyKey = 'body';
  private readonly categoryKey = 'category';

  // The private `HttpClient` is *injected* into the service
  // by the Angular framework. This allows the system to create
  // only one `HttpClient` and share that across all services
  // that need it, and it allows us to inject a mock version
  // of `HttpClient` in the unit tests so they don't have to
  // make "real" HTTP calls to a server that might not exist or
  // might not be currently running.
  constructor(private httpClient: HttpClient) {
  }

  /**
            </mat-list-item>
   * Get all the users from the server, filtered by the information
   * in the `filters` map.
   *
   * It would be more consistent with `UserListComponent` if this
   * only supported filtering on age and role, and left company to
   * just be in `filterUsers()` below. We've included it here, though,
   * to provide some additional examples.
   *
   * @param filters a map that allows us to specify a target role, age,
   *  or company to filter by, or any combination of those
   * @returns an `Observable` of an array of `Users`. Wrapping the array
   *  in an `Observable` means that other bits of of code can `subscribe` to
   *  the result (the `Observable`) and get the results that come back
   *  from the server after a possibly substantial delay (because we're
   *  contacting a remote server over the Internet).
   */
  getTodos(filters?: { status?: TodoRole; owner?: string; body?: string; category?: string; }): Observable<Todo[]> {
    // `HttpParams` is essentially just a map used to hold key-value
    // pairs that are then encoded as "?key1=value1&key2=value2&…" in
    // the URL when we make the call to `.get()` below.
    let httpParams: HttpParams = new HttpParams();
    if (filters) {
      if (filters.status) {
        httpParams = httpParams.set(this.statusKey, filters.status);
      }
      if (filters.owner) {
        httpParams = httpParams.set(this.ownerKey, filters.owner.toString());
      }
      if (filters.body) {
        httpParams = httpParams.set(this.bodyKey, filters.body);
      }
      if (filters.category) {
        httpParams = httpParams.set(this.categoryKey, filters.category)
      }
    
      
    }
    // Send the HTTP GET request with the given URL and parameters.
    // That will return the desired `Observable<User[]>`.
    return this.httpClient.get<Todo[]>(this.todoUrl, {
      params: httpParams,
    });
  }

  /**
   * A service method that filters an array of `User` using
   * the specified filters.
   *
            </mat-list-item>
   * Note that the filters here support partial matches. Since the
   * matching is done locally we can afford to repeatedly look for
   * partial matches instead of waiting until we have a full string
   * to match against.
   *
   * @param todos the array of `Users` that we're filtering
   * @param filters the map of key-value pairs used for the filtering
   * @returns an array of `Users` matching the given filters
   */
  filterTodos(todos: Todo[], filters: { owner?: string, status?: boolean; company?: string; body?: string; category?: string; }): Todo[] { // skipcq: JS-0105

    let filteredTodos = todos;
    //Filter by Owner
    if (filters.owner) {
      filters.owner = filters.owner.toLowerCase();
      filteredTodos = filteredTodos.filter(todo => todo.owner.toLowerCase().indexOf(filters.owner) !== -1);

    // Filter by Status
    if (filters.status) {
      filteredTodos = filteredTodos.filter(todo => todo.status === filters.status);
    }

    // Filter by body
    if (filters.body) {
      filters.body = filters.body.toLowerCase();
      filteredTodos = filteredTodos.filter(todo => todo.body.toLowerCase().indexOf(filters.body) !== -1);

    //Filter by Category
    if (filters.category) {
      filters.body = filters.category.toLowerCase();
      filteredTodos = filteredTodos.filter(todo => todo.category.toLowerCase().indexOf(filters.category) !== -1);
    }
    }
  }

    return filteredTodos;
  }

  getCompanies(): Observable<Company[]> {
    return this.httpClient.get<Company[]>(`${environment.apiUrl}todosByBody`);
  }

  addTodos(newTodo: Partial<Todo>): Observable<string> {
    // Send post request to add a new user with the user data as the body.
    // `res.id` should be the MongoDB ID of the newly added `User`.
    return this.httpClient.post<{id: string}>(this.todoUrl, newTodo).pipe(map(response => response.id));
  }
}
