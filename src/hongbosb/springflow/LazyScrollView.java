package hongbosb.springflow;

import android.widget.ScrollView;
import android.util.AttributeSet;
import android.content.Context;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.graphics.Rect;
import android.view.View;

public class LazyScrollView extends ScrollView {

    private int mFallCnt;
    private int mFallWidth;

    private String[] mFiles;
    private ItemLoader mLoader;
    private ViewGroup[] mFalls;
    private int mItemCnt;

    static public final int EXPECTED_WIDTH = 150;

    public LazyScrollView (Context context, AttributeSet attr) {
        super(context, attr);
        initDivideInfo(getContext());
    }

    @Override
        protected void onFinishInflate() {
            addVerticalLayouts();
            mFiles = LoadUtils.listAssets(getContext());
            mLoader = new ItemLoader(getContext(), mFallWidth);

            startLoadingImages();
        }

    private void startLoadingImages() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < 12; i ++) {
            ItemView view = (ItemView)inflater.inflate(R.layout.item, null);
            addImage(view, mItemCnt);
            mItemCnt += 1;
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
        //recycle or reloadview abnormal. Because when the numbers of image doesn't comes very
        //large, it won't crash. So just don't recycle bitmap temperory.
        //
        //recycle(t, ot);
        //reloadViews();

        if (bottomMeeted(t) && mLoader.isIdle()) {
            startLoadingImages();
        }
    }

    //
    private void reloadViews() {
        for (int i = 0; i < mFallCnt; i ++) {
            ViewGroup parent = mFalls[i];
            for (int j = parent.getChildCount() - 1; j >= 0; j --) {
                ItemView child = (ItemView)parent.getChildAt(j);
                //if (child.isDirty() && isVisible(child)) {
                if (child.isDirty()) {
                    reloadView(child);
                } else {
                    break;
                }
            }
        }
    }

    private void reloadView(ItemView view) {
        mLoader.loadImage(view);
    }

    private boolean isVisible(View view) {
        Rect inner = new Rect();
        view.getHitRect(inner);
        Rect outer = new Rect();
        getDrawingRect(outer);
        return Rect.intersects(inner, outer);
    }

    private boolean bottomMeeted(int top) {
        int maxHeight = getMaxFallHeight();
        if (maxHeight == top + getScreenHeight()) {
            return true;
        } else {
            return false;
        }
    }


    //Recycle the view one screen distance from visible area.
    private void recycle(int top, int oldTop) {
        boolean scrollDown = top > oldTop ? true : false;
        int recUp = top - getScreenHeight();   
        int recDown = top + 2*getScreenHeight();
        for (int i = 0; i < mFalls.length; i ++) {
            ViewGroup parent = mFalls[i];
            int sum = 0;
            for (int j = 0; j < parent.getChildCount(); j ++) {
                ItemView view = (ItemView)parent.getChildAt(j);
                sum += view.getHeight();
                if (sum <= recUp && recUp > 0 && scrollDown) {
                    recycleBitmap(view);
                } else if (sum >= recDown && !scrollDown) {
                    recycleBitmap(view);
                }
            }
        }
    }

    private void recycleBitmap(ItemView item) {
        item.setImageBitmap(null); 
        item.setDirty(true); 
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

    private void addImage(ItemView view, int index) {
        ViewGroup parent = getFall();        
        parent.addView(view);
        view.setPath(mFiles[index%(mFiles.length)]);
        mLoader.loadImage(view);
    }

    private ViewGroup getFall() {
        return mFalls[mItemCnt%mFallCnt];
    }

    private int getChildsHeight(ViewGroup parent) {
        int sum = 0;
        for (int i = 0; i < parent.getChildCount(); i ++) {
            sum += parent.getChildAt(i).getHeight();
        }
        return sum;
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
