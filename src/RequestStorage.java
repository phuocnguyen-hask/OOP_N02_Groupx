import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class RequestStorage implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<BorrowRequest> requests;
    private int lastId; 

    public RequestStorage() {
        loadFromFile();
        if (requests == null) requests = new ArrayList<>();
        for (BorrowRequest r : requests) {
            try {
                lastId = Math.max(lastId, r.getRequestId());
            } catch (Throwable ignore) {
                try {
                    lastId = Math.max(lastId, (int) BorrowRequest.class.getMethod("getId").invoke(r));
                } catch (Throwable ignored) {}
            }
        }
    }

    /* phuong thuc public */

    public synchronized int nextId() {
        return ++lastId;
    }


    public synchronized void addRequest(BorrowRequest r) {
        if (r == null) return;
        requests.add(r);
        saveToFile();
    }


    public synchronized List<BorrowRequest> getAll() {
        return new ArrayList<>(requests);
    }

    public synchronized List<BorrowRequest> getPending() {
        ArrayList<BorrowRequest> out = new ArrayList<>();
        for (BorrowRequest r : requests) {
            if (r.getStatus() == BorrowRequest.Status.PENDING) out.add(r);
        }
        return out;
    }

    public synchronized List<BorrowRequest> getByReader(int readerId) {
        ArrayList<BorrowRequest> out = new ArrayList<>();
        for (BorrowRequest r : requests) {
            if (r.getReaderId() == readerId) out.add(r);
        }
        return out;
    }

    public synchronized List<BorrowRequest> getPendingByReader(int readerId) {
        ArrayList<BorrowRequest> out = new ArrayList<>();
        for (BorrowRequest r : requests) {
            if (r.getReaderId() == readerId && r.getStatus() == BorrowRequest.Status.PENDING) {
                out.add(r);
            }
        }
        return out;
    }

    public synchronized boolean approveRequest(int requestId, BorrowStorage borrowStorage) {
        BorrowRequest r = findById(requestId);
        if (r == null || r.getStatus() != BorrowRequest.Status.PENDING) return false;

        if (borrowStorage == null) return false;
        BookStorage bs = borrowStorage.getBookStorage();
        if (bs == null) return false;

        // check xem con sach k
        Book available = bs.findBookById(r.getBookId());
        if (available == null) return false;

        // mượn 1 bản
        LocalDate now = LocalDate.now();
        LocalDate due = now.plusDays(14);
        borrowStorage.borrowBook(r.getReaderId(), r.getBookId(), now, due);

        // đánh dấu approved
        r.setStatus(BorrowRequest.Status.APPROVED);
        r.setDecisionDate(now);
        saveToFile();
        return true;
    }

    //tu choi request
    public synchronized boolean rejectRequest(int requestId) {
        BorrowRequest r = findById(requestId);
        if (r == null || r.getStatus() != BorrowRequest.Status.PENDING) return false;
        r.setStatus(BorrowRequest.Status.REJECTED);
        r.setDecisionDate(LocalDate.now());
        saveToFile();
        return true;
    }

    public synchronized boolean cancelIfPending(int requestId, int readerId) {
        Iterator<BorrowRequest> it = requests.iterator();
        while (it.hasNext()) {
            BorrowRequest r = it.next();
            if (getReqId(r) == requestId && r.getReaderId() == readerId
                    && r.getStatus() == BorrowRequest.Status.PENDING) {
                it.remove(); // huỷ = xoá
                saveToFile();
                return true;
            }
        }
        return false;
    }

    //hien thi lai
    public synchronized void reloadFromFilePublic() {
        loadFromFile();
    }

    private int getReqId(BorrowRequest r) {
        try {
            return r.getRequestId();
        } catch (Throwable ignore) {
            try {
                return (int) BorrowRequest.class.getMethod("getId").invoke(r);
            } catch (Throwable e) {
                // fallback: không tìm được -> -1
                return -1;
            }
        }
    }

    private BorrowRequest findById(int id) {
        for (BorrowRequest r : requests) {
            if (getReqId(r) == id) return r;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = new File("database/requests.obj");
        if (file.exists() && file.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                requests = (ArrayList<BorrowRequest>) ois.readObject();
                System.out.println("Requests loaded from: " + file.getAbsolutePath());
            } catch (Exception e) {
                requests = new ArrayList<>();
                System.err.println("Failed to load requests. Start empty.");
                e.printStackTrace();
            }
        } else {
            requests = new ArrayList<>();
            System.out.println("No existing requests. Starting fresh.");
        }
    }

    private void saveToFile() {
        File file = new File("database/requests.obj");
        file.getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(requests);
            System.out.println("Requests saved to: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error saving requests.");
            e.printStackTrace();
        }
    }
}
