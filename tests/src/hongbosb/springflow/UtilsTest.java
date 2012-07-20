package hongbosb.springflow;

import android.test.AndroidTestCase;

public class UtilsTest extends AndroidTestCase {
    public void testParseJson() throws Exception {
        String content = new JsonParser().loadContent();
        assertNotNull(content);

        String[] pics = new JsonParser().getImageUrls();
        assertTrue(pics.length > 0);
    }
}
