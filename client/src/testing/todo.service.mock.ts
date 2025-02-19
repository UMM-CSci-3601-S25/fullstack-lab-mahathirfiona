import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { AppComponent } from 'src/app/app.component';
import { Todo, TodoCategory } from '../app/todos/todo';
import { TodoService } from '../app/todos/todo.service';

/**
 * A "mock" version of the `UserService` that can be used to test components
 * without having to create an actual service. It needs to be `Injectable` since
 * that's how services are typically provided to components.
 */
@Injectable({
  providedIn: AppComponent
})
export class MockTodoService extends TodoService {
  static testTodos: Todo[] = [
    {
      _id: '58af3a600343927e48e8720f',
      owner: 'Blanche',
      status: true,
      body: 'amet',
      category: 'video games',
  
    },
    {
      _id: '58af3a600343927e48e87211',
      owner: 'Fry',
      status: true,
      body: 'officia',
      category: 'groceries',


    },
    {
        _id: '58af3a600343927e48e87214',
        owner: 'Barry ',
        status: true,
        body: 'veniam ',
        category: 'video games',
      
   
    }
  ];

  constructor() {
    super(null);
  }

  // skipcq: JS-0105
  // It's OK that the `_filters` argument isn't used here, so we'll disable
  // this warning for just his function.
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  getTodos(_filters: { owner?: string; status?:boolean; body?: string; category?: string }): Observable<Todo[]> {
    // Our goal here isn't to test (and thus rewrite) the service, so we'll
    // keep it simple and just return the test users regardless of what
    // filters are passed in.
    //
    // The `of()` function converts a regular object or value into an
    // `Observable` of that object or value.
    return of(MockTodoService.testTodos);
  }

  // skipcq: JS-0105
  getTodoById(id: string): Observable<Todo> {
    // If the specified ID is for one of the first two test users,
    // return that user, otherwise return `null` so
    // we can test illegal user requests.
    // If you need more, just add those in too.
    if (id === MockTodoService.testTodos[0]._id) {
      return of(MockTodoService.testTodos[0]);
    } else if (id === MockTodoService.testTodos[1]._id) {
      return of(MockTodoService.testTodos[1]);
    } else {
      return of(null);
    }
  }
}
