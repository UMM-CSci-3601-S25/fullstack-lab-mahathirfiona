export interface Todo {
  _id: string;
  status: boolean;
  owner: string;
  body: string;
  category: TodoCategory;
}
  
export type TodoCategory = 'homework' | 'video games' | 'groceries' | 'software design';

  

 