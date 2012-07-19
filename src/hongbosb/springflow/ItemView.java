package hongbosb.springflow;

import android.widget.ImageView;
import android.content.Context;
import android.util.AttributeSet;

public class ItemView extends ImageView {

    private boolean mDirty = false;

    public ItemView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public boolean isDirty() {
        return mDirty;
    }

    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }
}
