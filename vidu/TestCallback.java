public class TestCallback {
    public static void main(String[] args) {
        Callee callee = new Callee();
        Caller caller = new Caller(callee);
        caller.go();
    }
}

