package src;

import java.util.List;
import java.util.stream.Collectors;

public class BookFeature implements Searchable {
    private BookStorage1 bookStorage;

    public BookFeature(BookStorage1 storage) {
        this.bookStorage = storage;
    }

    @Override
    public void search(String keyword) {
        List<Book> ketQua = bookStorage.getBooks().stream()
            .filter(book -> book.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                            book.getAuthor().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());

        if (ketQua.isEmpty()) {
            System.out.println(" Không tìm thấy sách nào.");
        } else {
            ketQua.forEach(System.out::println);
        }
    }
}
