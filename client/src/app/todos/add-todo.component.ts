import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatOptionModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Todo, TodoCategory } from './todo';
import { TodoService } from './todo.service';

@Component({
    selector: 'app-add-todo',
    templateUrl: './add-todo.component.html',
    styleUrls: ['./add-todo.component.scss'],
    imports: [FormsModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatOptionModule, MatButtonModule]
})
export class AddTodoComponent {

  addTodoForm = new FormGroup({
    owner: new FormControl('', Validators.compose([
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(50),
    ])),

    body: new FormControl(''),

    status: new FormControl('Incomplete', Validators.compose([
      Validators.required,
      Validators.pattern('^(Complete|Incomplete)$'),
    ])),

    category: new FormControl<TodoCategory>('video games', Validators.compose([
      Validators.required,
      Validators.pattern('^(homework|groceries|software design|video games)$'),
    ])),
  });


  // We can only display one error at a time,
  // the order the messages are defined in is the order they will display in.
  readonly addTodoValidationMessages = {
    owner: [
      {type: 'required', message: 'Owner is required'},
      {type: 'minlength', message: 'Owner must be at least 2 characters long'},
      {type: 'maxlength', message: 'Owner cannot be more than 50 characters long'},
    ],

    category: [
      { type: 'required', message: 'Category is required' },
      { type: 'pattern', message: 'Category must be Homework, Groceries, Software Design, and Video Games' },
    ],

      body: [
        { type: 'required', message: 'body is required' },
        { type: 'minlength', message: 'body must be at least 4 characters long'},
  
    ]
  };

  constructor(
    private todoService: TodoService,
    private snackBar: MatSnackBar,
    private router: Router) {
  }

  formControlHasError(controlOwner: string): boolean {
    return this.addTodoForm.get(controlOwner).invalid &&
      (this.addTodoForm.get(controlOwner).dirty || this.addTodoForm.get(controlOwner).touched);
  }

  getErrorMessage(owner: keyof typeof this.addTodoValidationMessages): string {
    for(const {type, message} of this.addTodoValidationMessages[owner]) {
      if (this.addTodoForm.get(owner).hasError(type)) {
        return message;
      }
    }
    return 'Unknown error';
  }

  submitForm() {
    const newTodo: Partial<Todo> = { owner: this.addTodoForm.value.owner, status: this.addTodoForm.value.status === 'Complete', category: this.addTodoForm.value.category, body: this.addTodoForm.value.body};
    this.todoService.addTodo(newTodo).subscribe({
      next: (newId) => {
        this.snackBar.open(
          `Added todo ${this.addTodoForm.value.body}`,
          null,
          { duration: 2000 }
        );
        this.router.navigate(['/todos/', newId]);
      },
      error: err => {
        if (err.status === 400) {
          this.snackBar.open(
            `Tried to add an illegal new todo – Error Code: ${err.status}\nMessage: ${err.message}`,
            'OK',
            { duration: 5000 }
          );
        } else if (err.status === 500) {
          this.snackBar.open(
            `The server failed to process your request to add a new todo. Is the server up? – Error Code: ${err.status}\nMessage: ${err.message}`,
            'OK',
            { duration: 5000 }
          );
        } else {
          this.snackBar.open(
            `An unexpected error occurred – Error Code: ${err.status}\nMessage: ${err.message}`,
            'OK',
            { duration: 5000 }
          );
        }
      },
    });
  }

}
