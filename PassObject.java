// Định nghĩa lớp Number với biến i
class Number {
    int i;
}

public class PassObject {
    static void f(Number m) {
        m.i = 15;  
    }

    public static void main(String[] args) {
        Number n = new Number();  // Sửa: bỏ khoảng trắng thừa, thêm định nghĩa lớp Number
        n.i = 14;
        PassObject.f(n);         
        System.out.println(n.i); 
    }
}
