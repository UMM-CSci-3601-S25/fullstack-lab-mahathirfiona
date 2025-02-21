import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { AppComponent } from 'src/app/app.component';
import { Todo, TodoCategory } from '../app/todos/todo';
import { TodoService } from '../app/todos/todo.service';


@Injectable({
  providedIn: AppComponent
})
export class MockTodoService extends TodoService {
  static testTodos: Todo[] = [
    {
      _id: '58af3a600343927e48e87218',
      owner: 'Workman',
      status: true,
      body: 'commodo',
      category: 'software design',
  
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


  getTodos(_filters: { owner?: string; status?:boolean; body?: string; category?: string }): Observable<Todo[]> {
  
    return of(MockTodoService.testTodos);
  }

  
}
  

