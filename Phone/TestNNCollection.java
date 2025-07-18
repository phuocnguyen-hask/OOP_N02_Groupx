public class TestNNCollection {
    public static void main(String[] args){
        NNCollection phoneList = new NNCollection();
        phoneList.insert(new NameNumber("Just a cat", "0312321456"));
        System.out.println("TelNum of 'Just a Cat' " + phoneList.findNumber("Just a cat"));
    }
}
