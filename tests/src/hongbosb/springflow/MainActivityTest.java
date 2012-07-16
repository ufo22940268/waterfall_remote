package hongbosb.springflow;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.ImageView;
import android.os.Message;

import java.util.Arrays;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class hongbosb.springflow.MainActivityTest \
 * hongbosb.springflow.tests/android.test.InstrumentationTestRunner
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super("hongbosb.springflow", MainActivity.class);
    }

    public void testDivide() throws Exception {
        LazyScrollView view = new LazyScrollView(getActivity(), null);
        assertEquals(4, view.getFallCnt());
        assertTrue(view.getFallWidth() <= LazyScrollView.EXPECTED_WIDTH && view.getFallWidth() > 0);
    }

    public void testLoadImage() throws Exception {
        ItemLoader loader = new ItemLoader(getActivity());
        testOneImage(loader,  "images/1.jpg");
        testOneImage(loader,  "images/1.jpg");
    }

    private void testOneImage(ItemLoader loader, String fileName) {
        //TODO still need to find a better way to test handler.
        ImageView view = new ImageView(getActivity());

        loader.loadImage(view, fileName);

        Message msg = new Message();
        msg.what = ItemLoader.MESSAGE_REQUEST_LOAD;
        loader.handleMessage(msg);

        TestUtils.sleep();

        msg = new Message();
        msg.what = ItemLoader.MESSAGE_REFRESH_VIEW;
        loader.handleMessage(msg);

        assertNotNull(view.getDrawable());
    }

    public void testListFiles() throws Exception {
        String[] files = LoadUtils.listAssets(getActivity());
        System.out.println("++++++++++++++++++++" + Arrays.toString(files) + "++++++++++++++++++++");
        assertTrue(files.length > 30);
    }
}
