package Test;

import src.BookFeature;
import src.BookStorage1;
import src.Book1;

import java.util.ArrayList;
import java.util.Scanner;

public class TestFeature {
    public static void main(String[] args) {
        ArrayList<Book1> books = new ArrayList<>();
        books.add(new Book1("B001", "Lập trình Java", "Nguyễn Văn A"));
        books.add(new Book1("B002", "Cấu trúc dữ liệu", "Trần Thị B"));
        books.add(new Book1("B003", "Hệ điều hành", "Lê Văn C"));

        BookStorage1 storage = new BookStorage1(books);
        BookFeature feature = new BookFeature(storage);

        Scanner sc = new Scanner(System.in);
        System.out.print(" Nhập từ khóa để tìm sách: ");
        String keyword = sc.nextLine();

        feature.search(keyword);
    }
}
