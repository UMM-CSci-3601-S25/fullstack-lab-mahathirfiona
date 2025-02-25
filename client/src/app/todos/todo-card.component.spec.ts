import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TodoCardComponent } from './todo-card.component';
import { Todo } from './todo';


describe('TodoCardComponent', () => {
  let component: TodoCardComponent;
  let fixture: ComponentFixture<TodoCardComponent>;
  let expectedTodo: Todo;


  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        MatCardModule,
        TodoCardComponent
      ]
    })
      .compileComponents();
  }));

  beforeEach(async() => {

    fixture = TestBed.createComponent(TodoCardComponent);
    component = fixture.componentInstance;
    expectedTodo = {
     _id: '58af3a600343927e48e8720f',
     owner: 'Blanche',
     status: false,
     body: 'In sunt ex non tempor cillum commodo amet incididunt anim qui commodo quis. Cillum non labore ex sint esse.',
     category: 'software design'
    };
    fixture.componentRef.setInput('todo', expectedTodo);
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be associated with the correct todo', () => {
    expect(component.todo()).toEqual(expectedTodo);
  });

  it('should be the todo owner Blanche', () => {
    expect(component.todo().owner).toEqual('Blanche');
  });

});
