import java.io.Serializable;
import java.time.LocalDate;

public class BorrowRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { PENDING, APPROVED, REJECTED }

    private int requestId;
    private int readerId;
    private int bookId;
    private String title;     // snapshot để librarian dễ xem
    private String author;    // snapshot
    private LocalDate requestDate;
    private Status status = Status.PENDING;
    private LocalDate decisionDate;   // khi approve/reject

    public BorrowRequest(int requestId, int readerId, int bookId, String title, String author, LocalDate requestDate) {
        this.requestId = requestId;
        this.readerId = readerId;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.requestDate = requestDate;
    }

    public int getRequestId() { return requestId; }
    public int getReaderId() { return readerId; }
    public int getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public LocalDate getRequestDate() { return requestDate; }
    public Status getStatus() { return status; }
    public LocalDate getDecisionDate() { return decisionDate; }

    public void setStatus(Status status) { this.status = status; }
    public void setDecisionDate(LocalDate decisionDate) { this.decisionDate = decisionDate; }
}
