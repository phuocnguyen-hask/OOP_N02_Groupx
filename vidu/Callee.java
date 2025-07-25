public class Callee implements Incrementable {
    private int count = 0;

    @Override
    public void increment() {
        count++;
        System.out.println("Callee incremented: " + count);
    }
}

