public class App{

    public static void main(String[] args) {
       System.out.println("Hello, OOP N02 Groupx!");

       Leaf leaf = new Leaf();
       leaf.increment().increment().increment();
       leaf.print();
    }
}