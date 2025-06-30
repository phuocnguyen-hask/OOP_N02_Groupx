public class Leaf {
    int i = 0;
    Leaf increment() {
        i++;
        return this;
        
    }
    void print() {
        System.out.println("i = " + i);
    }
}