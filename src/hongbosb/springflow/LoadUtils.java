package hongbosb.springflow;

import android.content.res.AssetManager;
import android.content.Context;

public class LoadUtils {
    public static String[] listAssets(Context context) {
        try {
            String[] tmp = context.getResources().getAssets().list("images");
            for (int i = 0; i < tmp.length; i ++) {
                tmp[i] = "images/" + tmp[i];
            }
            return tmp;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

