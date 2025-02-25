export interface Todo {
  _id: string;
  status: boolean;
  owner: string;
  body: string;
  category: TodoCategory;
}
  
  export type TodoCategory = 'groceries' | 'software design' | 'video games';
  

