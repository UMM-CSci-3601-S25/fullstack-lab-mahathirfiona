import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { AddUserComponent } from './users/add-user.component';
import { UserListComponent } from './users/user-list.component';
import { UserProfileComponent } from './users/user-profile.component';
import { CompanyListComponent } from './company-list/company-list.component';
import { AddTodoComponent } from './todos/add-todo.component';
import { TodoListComponent } from './todos/todo-list.component';
import { TodoProfileComponent } from './todos/todo-profile.component';

const routes: Routes = [
  {path: '', component: HomeComponent, title: 'Home'},
  {path: 'users', component: UserListComponent, title: 'Users'},
  {path: 'users/new', component: AddUserComponent, title: 'Add User'},
  {path: 'users/:id', component: UserProfileComponent, title: 'User Profile'},
  {path: 'companies', component: CompanyListComponent, title: 'Companies'},
  {path: 'todos', component: TodoListComponent, title: 'Todos'},
  {path: 'todos/new', component: AddTodoComponent, title: 'Add Todo'},
  {path: 'todo/:id', component: TodoProfileComponent, title: 'Todo Profile'},


];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }