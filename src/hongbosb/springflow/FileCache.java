package hongbosb.springflow;

import android.os.Environment;

import java.io.File;

public class FileCache {

    public static File getFile(String url) {
        String fileName = "a" + String.valueOf(url.hashCode());
        File directory = getDirectory();
        return new File(directory, fileName);   
    }

    public static File getDirectory() {
        return new File("/tmp/");
    }
}   
