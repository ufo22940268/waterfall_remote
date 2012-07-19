package hongbosb.springflow;

import android.widget.ImageView;
import android.content.Context;
import android.util.AttributeSet;

public class ItemView extends ImageView {

    private boolean mDirty = false;
    private String mPath;

    public ItemView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public ItemView(Context context) {
        super(context);
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String getPath() {
        return mPath;
    }

    public boolean isDirty() {
        return mDirty;
    }

    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }
}
