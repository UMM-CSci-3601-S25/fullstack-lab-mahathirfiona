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
    //page.getTodoCards().should('have.length', 10);
    page.getVisibleTodos().should('have.length', 300);

  });

  it('Should type something in the owner filter and check that it returned correct elements', () => {
    // Filter for todo 'Fry'


    page.filterByOwner('Fry')

    page.getVisibleTodos().should('have.lengthOf',61)

     // Go through each of the visible todos that are being shown and get the owner

     page.getTodoOwners()
   //We should see these todos who owner contains Fry
   .should('contain.text', 'Fry')
      // We shouldn't see these todos
      .should('not.contain.text', 'Blanche')
      .should('not.contain.text', 'Barry')
  });

  it('Should type something in the category filter and check that it returned correct elements', () => {
    // Filter for todo 'Fry'


    page.filterByCategory('homework')

    page.getVisibleTodos().should('have.lengthOf', 79)

     // Go through each of the visible todos that are being shown and get the category

     page.getTodoCategories()

   .should('contain.text', 'homework')
      // We shouldn't see these todos
      .should('not.contain.text', 'groceries')
      .should('not.contain.text', 'software design')
      .should('not.contain.text', 'video games')
  });



  it('Should type something in the body filter and check that it returned correct elements', () => {
    // Filter for todo 'Fry'


    page.filterByBody('quis')

    page.getVisibleTodos().should('have.lengthOf', 89)

     // Go through each of the visible todos that are being shown and get the body

     page.getTodoBodies()
   //We should see these todos who owner contains Fry
   .should('contain.text', 'quis')


  });

});


