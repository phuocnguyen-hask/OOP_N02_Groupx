# 📚 Quản Lý Thư Viện

## Giới thiệu
**Quản Lý Thư Viện** là một ứng dụng được phát triển bằng Java, sử dụng thư viện **Swing** để xây dựng giao diện đồ họa. Ứng dụng giúp thư viện quản lý thông tin sách, độc giả và hoạt động mượn/trả sách một cách hiệu quả và dễ dàng.

## 🧑‍💻 Thành viên thực hiện
- **Nguyễn Công Phước**
- **Lê Minh Đức**

## 🎯 Tính năng chính
- Quản lý sách:
  - Thêm, sửa, xóa sách
  - Tìm kiếm theo tên sách, tác giả, thể loại,...
- Quản lý độc giả:
  - Đăng ký thành viên mới
  - Cập nhật và tìm kiếm thông tin độc giả
- Quản lý mượn và trả sách:
  - Tạo phiếu mượn/trả
  - Theo dõi hạn trả sách
## Cac doi tuong chinh
| Class             | Attributes                          | Methods                  |
| ----------------- | ----------------------------------- | ------------------------ |
| **Book**          | - id: String                        | + getters/setters        |
|                   | - title: String                     |                          |
|                   | - author: String                    |                          |
|                   | - category: String                  |                          |
|                   | - quantity: int                     |                          |
| **Librarian**     | - id: String                        | + getters/setters        |
|                   | - name: String                      |                          |
|                   | - email: String                     |                          |
|                   | - phone: String                     |                          |
| **Reader**        | - id: String                        | + getters/setters        |
|                   | - name: String                      |                          |
|                   | - email: String                     |                          |
|                   | - phone: String                     |                          |
|                   | - address: String                   |                          |
| **BookStorage**   | - books: List<Book>                 | + addBook()              |
|                   |                                     | + removeBook()           |
|                   |                                     | + findBookById()         |
|                   |                                     | + getAllBooks()          |
| **BorrowStorage** | - borrowedBooks: List<BorrowRecord> | + addBorrowRecord()      |
|                   |                                     | + removeBorrowRecord()   |
|                   |                                     | + findBorrowRecordById() |
|                   |                                     | + getAllBorrowRecords()  |




## 🛠 Công nghệ sử dụng
- **Ngôn ngữ**: Java
- **Giao diện**: Java Swing
- **Lưu trữ dữ liệu**: File nhi phan

## 🚀 Cách chạy chương trình
