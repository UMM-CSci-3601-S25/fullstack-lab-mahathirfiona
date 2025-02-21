import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Todo } from './todo';



@Injectable({
  providedIn: 'root'
})
export class TodoService {

  readonly todoUrl: string = `${environment.apiUrl}todos`;

  private readonly categoryKey = 'category';
  private readonly ownerKey = 'owner';
  private readonly bodyKey = 'body';
  private readonly statusKey = 'status';
  private readonly limitKey = 'limit';
  private readonly sortKey = 'orderBy';


  constructor(private httpClient: HttpClient) {
  }


  getTodos(filters?: { owner?: string; body?: string; status?: boolean; category?: string; sort?: string; limit?: string }): Observable<Todo[]> {

    let httpParams: HttpParams = new HttpParams();
    if (filters) {
      if (filters.owner) {
        httpParams = httpParams.set(this.ownerKey, filters.owner);
      }
      if (filters.body) {
        httpParams = httpParams.set(this.bodyKey, filters.body);
      }
      if (filters.status) {
        httpParams = httpParams.set(this.statusKey, filters.status);
      }
      if (filters.sort) {
        httpParams = httpParams.set(this.sortKey, filters.sort);
      }
      if (filters.limit) {
        httpParams = httpParams.set(this.limitKey, filters.limit);
      }
      if (filters.category) {
        httpParams = httpParams.set(this.categoryKey, filters.category);
      }

    }

    return this.httpClient.get<Todo[]>(this.todoUrl, {
      params: httpParams,
    });
  }



  getTodoById(id: string): Observable<Todo> {

    return this.httpClient.get<Todo>(`${this.todoUrl}/${id}`);
  }


  filterTodos(todos: Todo[], filters: { owner?: string; status?: boolean; body?: string; category?: string }): Todo[] { // skipcq: JS-0105
    let filteredTodos = todos;

    // Filter by owner
    if (filters.owner) {
      filters.owner = filters.owner.toLowerCase();
      filteredTodos = filteredTodos.filter(todo => todo.owner.toLowerCase().indexOf(filters.owner) !== -1);
    }

     // Filter by category
    if (filters.category) {
      filters.category = filters.category.toLowerCase();
      filteredTodos = filteredTodos.filter(todo => todo.category.toLowerCase().indexOf(filters.category) !== -1);
    }

    // Filter by body
    if (filters.body) {
      filters.body = filters.body.toLowerCase();
      filteredTodos = filteredTodos.filter(todo => todo.body.toLowerCase().indexOf(filters.body) !== -1);
    }

     return filteredTodos;
   }


  addTodo(newTodo: Partial<Todo>): Observable<string> {

    return this.httpClient.post<{id: string}>(this.todoUrl, newTodo).pipe(map(response => response.id));
  }
}
