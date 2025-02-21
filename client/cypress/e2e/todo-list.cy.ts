import { TodoListPage } from '../support/todo-list.po';

const page = new TodoListPage();

describe('Todo list', () => {

  before(() => {
    cy.task('seed:database');
  });

  beforeEach(() => {
    page.navigateTo();
  });

  it('Should have the correct title', () => {
    page.getTodoTitle().should('have.text', 'Todos');
  });

  it('Should show 300 todos in list view', () => {
    page.getVisibleTodos().should('have.length', 300);

  });

  it('Should type something in the owner filter and check that it returned correct elements', () => {


    page.filterByOwner('Fry')

    page.getVisibleTodos().should('have.lengthOf',61)


     page.getTodoOwners()
   .should('contain.text', 'Fry')
      .should('not.contain.text', 'Blanche')
      .should('not.contain.text', 'Barry')
  });

  it('Should type something in the category filter and check that it returned correct elements', () => {


    page.filterByCategory('homework')

    page.getVisibleTodos().should('have.lengthOf', 79)


     page.getTodoCategories()

   .should('contain.text', 'homework')
   .should('not.contain.text', 'groceries')
   .should('not.contain.text', 'software design')
   .should('not.contain.text', 'video games')
  });



  it('Should type something in the body filter and check that it returned correct elements', () => {


    page.filterByBody('quis')

    page.getVisibleTodos().should('have.lengthOf', 89)


     page.getTodoBodies()
   .should('contain.text', 'quis')


  });


  });


