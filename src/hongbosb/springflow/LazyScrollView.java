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

            startLoadingImages();
        }

    private void startLoadingImages() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < 10; i ++) {
            ImageView view = (ImageView)inflater.inflate(R.layout.item, null);
            addImage(view, i);
        }
    }

    private int getMaxFallHeight() {
        int max = 0;
        for (int i = 0; i < mFalls.length; i ++) {
            max = Math.max(mFalls[i].getHeight(), max);
        }
        return max;
    }

    @Override
    protected void onScrollChanged(int l, int t, int ol, int ot) {
        if (bottomMeeted(t)) {
            //recycle(t);
            startLoadingImages();
        }
    }

    private boolean bottomMeeted(int top) {
        int maxHeight = getMaxFallHeight();
        if (maxHeight == top + getScreenHeight()) {
            return true;
        } else {
            return false;
        }
    }


    private void recycle(int top) {
        int recY = top - getScreenHeight();   
        if (recY < 0) {
            return;
        } else {
            for (int i = 0; i < mFalls.length; i ++) {
                ViewGroup parent = mFalls[i];
                int sum = 0;
                for (int j = 0; j < parent.getChildCount(); j ++) {
                    ImageView view = (ImageView)parent.getChildAt(j);
                    sum += view.getHeight();
                    if (sum <= recY) {
                        view.setImageBitmap(null);
                    } else {
                        break;
                    }
                }
            }
        }
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

    private void addImage(ImageView view, int index) {
        setImageWidth(view);

        ViewGroup parent = getFall(index);        
        parent.addView(view);

        mLoader.loadImage(view, mFiles[index%(mFiles.length)]);
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

    private int getScreenHeight() {
        WindowManager manager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getHeight();
    }

    public int getFallCnt() {
        return mFallCnt;
    }

    public int getFallWidth() {
        return mFallWidth;
    }

}
