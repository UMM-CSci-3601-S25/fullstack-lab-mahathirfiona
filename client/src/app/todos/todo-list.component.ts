import { Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatOptionModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { catchError, combineLatest, of, switchMap, tap } from 'rxjs';
import { TodoCardComponent } from './todo-card.component';
import { TodoService } from './todo.service';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { Todo } from './todo';


@Component({
  selector: 'app-todo-list-component',
  templateUrl: 'todo-list.component.html',
  styleUrls: ['./todo-list.component.scss'],
  providers: [],
  imports: [
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatSelectModule,
    MatOptionModule,
    MatRadioModule,
    TodoCardComponent,
    MatListModule,
    RouterLink,
    MatButtonModule,
    MatTooltipModule,
    MatIconModule,
  ],
})
export class TodoListComponent {
  todoOwner = signal<string | undefined>(undefined);
  todoStatus = signal<boolean | undefined>(undefined);
  todoBody = signal<string | undefined>(undefined);
  todoCategory = signal<string | undefined>(undefined);
  todoSort = signal<string | undefined>(undefined);
  todoLimit = signal<string | undefined>(undefined);

  viewType = signal<'card' | 'list'>('card');

  errMsg = signal<string | undefined>(undefined);

  /**

   *
   * @param todoService the `TodoService` used to get Todos from the server
   * @param snackBar the `MatSnackBar` used to display feedback
   */
  constructor(private todoService: TodoService, private snackBar: MatSnackBar) {
  }


  private todoOwner$ = toObservable(this.todoOwner);
  private todoBody$ = toObservable(this.todoBody);
  private todoStatus$ = toObservable(this.todoStatus);
  private todoCategory$ = toObservable(this.todoCategory);
  private todoSort$ = toObservable(this.todoSort);


  serverFilteredTodos =
 
    toSignal(
      combineLatest([this.todoOwner$, this.todoBody$, this.todoStatus$, this.todoCategory$, this.todoSort$]).pipe(
        switchMap(([owner, body, status, category, sort]) =>
          this.todoService.getTodos({
            owner,
            body,
            status,
            category,
            sort,
          })
        ),
        catchError((err) => {
          if (!(err.error instanceof ErrorEvent)) {

            this.errMsg.set(
              `Problem contacting the server – Error Code: ${err.status}\nMessage: ${err.message}`
            );
          }
          this.snackBar.open(this.errMsg(), 'OK', { duration: 6000 });

          return of<Todo[]>([]);
        }),
        tap(() => {

        })
      )
    );

  filteredTodos = computed(() => {
    const serverFilteredTodos = this.serverFilteredTodos();
    return this.todoService.filterTodos(serverFilteredTodos, {
      owner : this.todoOwner(),
      body: this.todoBody(),
    });
  });
}
