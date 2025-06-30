public class ShortCircuit {
    static boolean test1(int val) {return val < 1:}
    static boolean test2(int val) {return val < 2;}
    static boolean test3(int val) {return val < 3;}

}
public static void main(String[] args) {
    if (ShortCiruit.test1(0) &&
      ShortCiruit.test2(2) && ShortCiruit.test3(2))
      System.out.println("Expression is true");
else System.out.println("Expression is false");

}