package hongbosb.springflow;

import android.widget.ScrollView;
import android.util.AttributeSet;
import android.content.Context;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class LazyScrollView extends ScrollView {

    private int mFallCnt;
    private int mFallWidth;

    static public final int EXPECTED_WIDTH = 150;

    public LazyScrollView (Context context, AttributeSet attr) {
        super(context, attr);
    }

    @Override
    protected void onFinishInflate() {
        initDivideInfo(getContext());
        addVerticalLayouts();
    }

    private void addVerticalLayouts() {
        ViewGroup container = (ViewGroup)this.findViewById(R.id.container);

        for (int i = 1; i <= mFallCnt; i ++)  {
            ViewGroup fall = 
                (ViewGroup)LayoutInflater.from(getContext()).inflate(R.layout.fall, null);
            LayoutParams param = new LayoutParams(mFallWidth, LayoutParams.MATCH_PARENT);
            fall.setLayoutParams(param);
            fall.setId(i);
            container.addView(fall);
        }
    }

    private void initDivideInfo(Context context) {
        int width = getScreenWidth(context);
        if (width%EXPECTED_WIDTH == 0) {
            mFallWidth = EXPECTED_WIDTH;
            mFallCnt = width/EXPECTED_WIDTH;
        } else {
            mFallCnt = width/EXPECTED_WIDTH + 1;
            mFallWidth = width/mFallCnt;
        }
    }

    private int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getWidth();
    }

    public int getFallCnt() {
        return mFallCnt;
    }

    public int getFallWidth() {
        return mFallWidth;
    }

}
