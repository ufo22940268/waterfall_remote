package hongbosb.springflow;

import android.widget.ImageView;
import android.os.HandlerThread;
import android.os.Handler.Callback;
import android.os.Handler;
import android.os.*;
import android.graphics.*;
import android.content.Context;
import android.widget.LinearLayout.LayoutParams;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.util.*;

public class ItemLoader implements Callback{
    static public final int MESSAGE_REQUEST_LOAD = 0;
    static public final int MESSAGE_REFRESH_VIEW = 1;

    private ConcurrentHashMap<ImageView, String> mPendingMap;
    private ConcurrentHashMap<String, SoftReference<Bitmap>> mCacheMap;

    private LoaderThread mLoaderThread;

    private Handler mMainHandler;

    private Context mContext;

    public ItemLoader(Context context) {
        mCacheMap = new ConcurrentHashMap<String, SoftReference<Bitmap>>();
        mPendingMap = new ConcurrentHashMap<ImageView, String>();
        mContext = context;

        mMainHandler = new Handler(this);
    }
    
    public void loadImage(ImageView view, String path) {
        Bitmap bitmap = getBitmapFromCache(path);
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
            setImageParams(view, bitmap);
        } else {
            mPendingMap.put(view, path);
            requestLoading();
        }
    }

    private Bitmap getBitmapFromCache(String path) {
        if (mCacheMap.get(path) != null && mCacheMap.get(path).get() != null) {
            return mCacheMap.get(path).get();
        } else {
            return null;
        }
    }

    private void requestLoading() {
        mMainHandler.sendEmptyMessage(MESSAGE_REQUEST_LOAD);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_REQUEST_LOAD:
                if (mLoaderThread == null) {
                    mLoaderThread = new LoaderThread();
                    mLoaderThread.start();
                }

                mLoaderThread.requestLoading();
                break;
            case MESSAGE_REFRESH_VIEW:
                refreshViews();
                break;
        }
        return true;
    }

    private void refreshViews() {
        for (Map.Entry<ImageView, String> entry : mPendingMap.entrySet()) {
            String path = entry.getValue();
            ImageView view = entry.getKey();
            Bitmap bitmap = getBitmapFromCache(path);
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                setImageParams(view, bitmap);
            }
        }
        
    }

    private void setImageParams(ImageView view, Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int fallWidth = view.getWidth();

        LayoutParams lp = new LayoutParams(fallWidth, height/width*fallWidth);
        view.setLayoutParams(lp);
    }

    private class LoaderThread extends HandlerThread implements Callback {

        private Handler mLoaderHandler;

        public LoaderThread() {
            super("hongbosbthread");
        }

        @Override
        public boolean handleMessage(Message msg) {
            for (Map.Entry<ImageView, String> entry : mPendingMap.entrySet()) {
                String path = entry.getValue();
                if (mCacheMap.get(path) == null) {
                    mCacheMap.put(path, new SoftReference(decodeBitmap(path)));
                }
            }

            mMainHandler.sendEmptyMessage(MESSAGE_REFRESH_VIEW);
            return true;
        }

        private void requestLoading() {
            if (mLoaderHandler == null) {
                mLoaderHandler = new Handler(getLooper(), this);
            }

            mLoaderHandler.sendEmptyMessage(0);
        }

        private Bitmap decodeBitmap(String path) {
            try {
                InputStream in = mContext.getResources().getAssets().open(path);
                return BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}

