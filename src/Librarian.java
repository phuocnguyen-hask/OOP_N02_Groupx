public interface Librarian{
    public BookStorage getStorage();
    public void addBook(Book book);
    public void removeBook(int bookId);
    public void showAllBooks();
    public void manageUser();
    
}
