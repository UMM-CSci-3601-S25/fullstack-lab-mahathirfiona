//import { Todo} from 'src/app/todos/todo';

export class TodoListPage {
  private readonly baseUrl = '/todos';
  private readonly pageTitle = '[data-test=todoListTitle]';
  private readonly todoCardSelector = '.todo-cards-container app-todo-card';
  private readonly todoListItemsSelector = '.todo-nav-list .todo-list-item';
  private readonly profileButtonSelector = '[data-test=viewProfileButton]';
  private readonly todo = '[data-test=todo]';
  private readonly todoOwnerFilter = '[data-test=todoOwnerFilter]';
  private readonly todoOwner = '[data-test=todoOwner]';
  private readonly todoCategory = '[data-test=todoCategory]';
  private readonly todoCategoryFilter = '[data-test=todoCategoryFilter]';
  private readonly todoBody = '[data-test=todoBody]';
  private readonly todoBodyFilter = '[data-test=todoBodyFilter]';
  private readonly todoStatus = '[data-test=todoStatus]';
  private readonly todoStatusFilter = '[data-test=todoStatusFilter]';
  private readonly radioButtonSelector = '[data-test=viewTypeRadio] mat-radio-button';
  private readonly todoRoleDropdownSelector = '[data-test=todoRoleSelect]';
  private readonly dropdownOptionSelector = 'mat-option';
  private readonly addTodoButtonSelector = '[data-test=addTodoButton]';

  navigateTo() {
    return cy.visit(this.baseUrl);
  }


  getTodoTitle() {
    return cy.get(this.pageTitle);
  }


   getTodoCards() {
    return cy.get(this.todoCardSelector);
  }

  getVisibleTodos() {
    return cy.get(this.todo);
  }

  getTodoOwners() {
    return cy.get(this.todoOwner);
  }

  filterByOwner(owner: string) {
    return cy.get(this.todoOwnerFilter).type(owner.toString());
  }

  getTodoCategories() {
    return cy.get(this.todoCategory);
  }

  filterByCategory(category: string) {
    cy.get(this.todoCategoryFilter).click().then(() => {
      return cy.get(`[value="${category}"]`).click();
    })
  }

  getTodoBodies() {
    return cy.get(this.todoBody);
  }

  filterByBody(body: string) {
    return cy.get(this.todoBodyFilter).type(body.toString());
  }

  getTodoStatuses() {
    return cy.get(this.todoStatus);
  }

  filterByStatus(status: string) {
    cy.get(this.todoStatusFilter).click().then(() => {
      return cy.get(`[value="${status}"]`).click();
    })
  }



  getTodoListItems() {
    return cy.get(this.todoListItemsSelector);
  }


  clickViewProfile(card: Cypress.Chainable<JQuery<HTMLElement>>) {
    return card.find<HTMLButtonElement>(this.profileButtonSelector).click();
  }


  changeView(viewType: 'card' | 'list') {
    return cy.get(`${this.radioButtonSelector}[value="${viewType}"]`).click();
  }




  addTodoButton() {
    return cy.get(this.addTodoButtonSelector);
  }
}
