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
    private ConcurrentHashMap<String, BitmapRef> mCacheMap;

    private LoaderThread mLoaderThread;

    private Handler mMainHandler;

    private Context mContext;

    public ItemLoader(Context context) {
        mCacheMap = new ConcurrentHashMap<String, BitmapRef>();
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
            mCacheMap.put(path, new BitmapRef());
            requestLoading();
        }
    }

    private Bitmap getBitmapFromCache(String path) {
        BitmapRef ref = mCacheMap.get(path);
        if (ref != null) {
            return ref.get();
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
                mPendingMap.remove(view);
            } else {
                view.setImageResource(R.drawable.loading);
                requestLoading();
            }
        }
    }

    private void setImageParams(ImageView view, Bitmap bitmap) {
        float height = bitmap.getHeight();
        float width = bitmap.getWidth();
        float fallWidth = view.getWidth();

        LayoutParams lp = new LayoutParams((int)fallWidth, (int)(height/width*fallWidth));
        view.setLayoutParams(lp);
    }

    private class BitmapRef {
        static public final int PENDING = 0;
        static public final int LOADING = 1;
        static public final int LOADED = 2;
        public int status;

        public SoftReference<Bitmap> bitmapRef;

        public BitmapRef() {
            status = PENDING;
        }

        public void set(Bitmap bitmap) {
            bitmapRef = new SoftReference<Bitmap>(bitmap);
        }

        //If the bitmap is null, then it may has been recycled. So we
        //assume that there is no picture that bitmap is null and set status
        //to pending.
        public Bitmap get() {
            if (status == LOADED && bitmapRef != null && bitmapRef.get() != null) {
                return bitmapRef.get();
            } else {
                status = PENDING;
                return null;
            }
        }

        private boolean loaded() {
            if (status == LOADED && bitmapRef != null && bitmapRef.get() != null) {
                return true;
            } else {
                return false;
            }
        }
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
                    BitmapRef cacheRef = mCacheMap.get(path);
                    if (!cacheRef.loaded()) {
                        try {
                            Bitmap bitmap = decodeBitmap(path);
                            cacheRef.set(bitmap);
                            cacheRef.status = BitmapRef.LOADED;
                        } catch (OutOfMemoryError e) {
                            e.printStackTrace();
                        }
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
                Bitmap result = BitmapFactory.decodeStream(in);

                if (in != null) {
                    in.close();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}

