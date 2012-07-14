package hongbosb.springflow;

public class TestUtils {
    public static void sleep() {
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
