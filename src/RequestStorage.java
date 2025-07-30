import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class RequestStorage implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<BorrowRequest> requests;
    private int lastId; // auto-increment id

    public RequestStorage() {
        loadFromFile();
        if (requests == null) requests = new ArrayList<>();
        // gợi ý lastId từ dữ liệu cũ
        for (BorrowRequest r : requests) {
            try {
                lastId = Math.max(lastId, r.getRequestId());
            } catch (Throwable ignore) {
                // nếu class dùng getId(): fallback
                try {
                    lastId = Math.max(lastId, (int) BorrowRequest.class.getMethod("getId").invoke(r));
                } catch (Throwable ignored) {}
            }
        }
    }

    /* ===================== Public API ===================== */

    /** Tạo id mới tăng dần */
    public synchronized int nextId() {
        return ++lastId;
    }

    /** Thêm request và lưu file */
    public synchronized void addRequest(BorrowRequest r) {
        if (r == null) return;
        requests.add(r);
        saveToFile();
    }

    /** Lấy TẤT CẢ request (copy phòng thủ) */
    public synchronized List<BorrowRequest> getAll() {
        return new ArrayList<>(requests);
    }

    /** Lấy tất cả request còn pending */
    public synchronized List<BorrowRequest> getPending() {
        ArrayList<BorrowRequest> out = new ArrayList<>();
        for (BorrowRequest r : requests) {
            if (r.getStatus() == BorrowRequest.Status.PENDING) out.add(r);
        }
        return out;
    }

    /** Lấy request của 1 reader (mọi trạng thái) */
    public synchronized List<BorrowRequest> getByReader(int readerId) {
        ArrayList<BorrowRequest> out = new ArrayList<>();
        for (BorrowRequest r : requests) {
            if (r.getReaderId() == readerId) out.add(r);
        }
        return out;
    }

    /** Lấy request PENDING của 1 reader (phục vụ màn "My Requests") */
    public synchronized List<BorrowRequest> getPendingByReader(int readerId) {
        ArrayList<BorrowRequest> out = new ArrayList<>();
        for (BorrowRequest r : requests) {
            if (r.getReaderId() == readerId && r.getStatus() == BorrowRequest.Status.PENDING) {
                out.add(r);
            }
        }
        return out;
    }

    /**
     * Librarian phê duyệt request:
     * - Kiểm tra còn sách trong BookStorage
     * - Gọi borrowStorage.borrowBook(...) để mượn thực
     * - Cập nhật trạng thái APPROVED + decisionDate
     */
    public synchronized boolean approveRequest(int requestId, BorrowStorage borrowStorage) {
        BorrowRequest r = findById(requestId);
        if (r == null || r.getStatus() != BorrowRequest.Status.PENDING) return false;

        if (borrowStorage == null) return false;
        BookStorage bs = borrowStorage.getBookStorage();
        if (bs == null) return false;

        // còn sách không?
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

    /** Librarian từ chối request (đổi sang REJECTED) */
    public synchronized boolean rejectRequest(int requestId) {
        BorrowRequest r = findById(requestId);
        if (r == null || r.getStatus() != BorrowRequest.Status.PENDING) return false;
        r.setStatus(BorrowRequest.Status.REJECTED);
        r.setDecisionDate(LocalDate.now());
        saveToFile();
        return true;
    }

    /**
     * Reader tự huỷ request của chính mình nếu còn PENDING.
     * Ở đây mặc định là XÓA request khỏi danh sách.
     * (Nếu bạn có trạng thái CANCELLED trong enum, có thể đổi sang setStatus(CANCELLED))
     */
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

    /** Cho phép UI nạp lại từ file trước khi hiển thị danh sách */
    public synchronized void reloadFromFilePublic() {
        loadFromFile();
    }

    /* ===================== Helpers ===================== */

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

    /* ===================== I/O ===================== */

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
