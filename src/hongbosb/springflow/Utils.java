package hongbosb.springflow;

import java.io.InputStream;
import java.io.OutputStream;

class Utils {
    public static void copyStream(InputStream in, OutputStream out) {
        try {
            int bufferSize = 1024;
            byte[] bytes = new byte[bufferSize];
            int len = in.read(bytes, 0, bufferSize);
            while (len != -1) {
                out.write(bytes, 0, len);
                len = in.read(bytes, 0, bufferSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
