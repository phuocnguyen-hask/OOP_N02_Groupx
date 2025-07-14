class Borrow{
    private String transactionID;
    private String readerID;
    private String bookID;
    private String borrowDate;
    private String returnDate;
    public Borrow(String transactionID, String readerID, String bookID, String borrowDate, String returnDate){
        this.transactionID = transactionID;
        this.readerID = readerID;
        this.bookID = bookID;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }
    public void createTransaction(){
        System.out.println("Transaction is created");
    }
    public void closeTransaction(){
        System.out.println("Transaction is closed");
    }
    public String getTransactionID(){return transactionID;}
    public String getReaderID(){return readerID;}
    public String getBookID(){return bookID;}
    public String getBorrowDate(){return borrowDate;}
    public String getReturnDate(){return returnDate;}
}

