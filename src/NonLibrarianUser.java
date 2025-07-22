public class NonLibrarianUser implements Librarian{
    public void addBook(Book book){
        System.out.println("Can't add book");
    }
    public void removeBook(int BookId){
        //can't remove
        System.out.println("Can't remove book");
    }
    public void manageUser(){
        //don't have permission
        System.out.println("Don't have permisson");
    }
    
}
