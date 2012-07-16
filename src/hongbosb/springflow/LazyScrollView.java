package hongbosb.springflow;

import android.widget.ScrollView;
import android.util.AttributeSet;
import android.content.Context;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class LazyScrollView extends ScrollView {

    private int mFallCnt;
    private int mFallWidth;

    private String[] mFiles;
    private ItemLoader mLoader;
    private ViewGroup[] mFalls;

    static public final int EXPECTED_WIDTH = 150;

    public LazyScrollView (Context context, AttributeSet attr) {
        super(context, attr);
        initDivideInfo(getContext());
    }

    @Override
    protected void onFinishInflate() {
        addVerticalLayouts();
        mFiles = LoadUtils.listAssets(getContext());
        mLoader = new ItemLoader(getContext());
    }

    private void addVerticalLayouts() {
        ViewGroup container = (ViewGroup)this.findViewById(R.id.container);
        mFalls = new ViewGroup[mFallCnt];

        for (int i = 1; i <= mFallCnt; i ++)  {
            ViewGroup fall = 
                (ViewGroup)LayoutInflater.from(getContext()).inflate(R.layout.fall, null);
            LayoutParams param = new LayoutParams(mFallWidth, LayoutParams.MATCH_PARENT);
            fall.setLayoutParams(param);
            fall.setId(i);
            mFalls[i - 1] = fall;
            container.addView(fall);
        }
    }

    public void addImage(ImageView view, int index) {
        setImageWidth(view);

        ViewGroup parent = getFall(index);        
        parent.addView(view);

        mLoader.loadImage(view, mFiles[index]);
    }

    private void setImageWidth(ImageView view) {
        LayoutParams lp = new LayoutParams(mFallWidth, LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
    }

    private ViewGroup getFall(int pos) {
        return mFalls[pos%mFallCnt];
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
