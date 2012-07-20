package hongbosb.springflow;

import android.widget.ImageView;
import android.os.HandlerThread;
import android.os.Handler.Callback;
import android.os.Handler;
import android.os.*;
import android.graphics.*;
import android.content.Context;
import android.widget.LinearLayout.LayoutParams;

import java.net.HttpURLConnection;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.util.*;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.net.URL;

public class ItemLoader implements Callback{
    static public final int MESSAGE_REQUEST_LOAD = 0;
    static public final int MESSAGE_REFRESH_VIEW = 1;

    private ConcurrentHashMap<ImageView, String> mPendingMap;
    private ConcurrentHashMap<String, BitmapHolder> mCacheMap;

    private LoaderThread mLoaderThread;
    private boolean mIdle;

    private Handler mMainHandler;
    private int mItemWidth;

    private Context mContext;

    public ItemLoader(Context context, int itemWidth) {
        mCacheMap = new ConcurrentHashMap<String, BitmapHolder>();
        mPendingMap = new ConcurrentHashMap<ImageView, String>();
        mContext = context;

        mMainHandler = new Handler(this);
        mItemWidth = itemWidth;
    }
    
    public void loadImage(ItemView view) {
        mIdle = false;

        String url = view.getUrl();
        boolean loaded = loadPhotoFromCache(view, url);
        if (loaded) {
            mPendingMap.remove(view);
        } else {
            mPendingMap.put(view, url);
            requestLoading();
        }

        if (mPendingMap.size() == 0 && !mMainHandler.hasMessages(MESSAGE_REQUEST_LOAD)) {
            mIdle = true;
        }
    }

    private Bitmap getBitmapFromCache(String url) {
        BitmapHolder ref = mCacheMap.get(url);
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
                mIdle = false;

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
        Iterator<ImageView> iter = mPendingMap.keySet().iterator();
        while (iter.hasNext()) {
            ImageView view = iter.next();
            String url = mPendingMap.get(view);

            boolean loaded = loadPhotoFromCache(view, url);
            if (loaded) {
                iter.remove();
            }
        }

        if (mPendingMap.size() != 0) {
            requestLoading();
        } else if (!mMainHandler.hasMessages(MESSAGE_REQUEST_LOAD)) {
            mIdle = true;
        }
    }

    public boolean isIdle() {
        return mIdle;
    }

    private boolean loadPhotoFromCache(ImageView view, String url) {
        BitmapHolder holder = mCacheMap.get(url);
        if (holder == null) {
            holder = new BitmapHolder();
            mCacheMap.put(url, holder);
        } else if (holder.status == BitmapHolder.LOADED) {
            if (holder.bitmapRef == null) {
                setDrawable(view, R.drawable.loading);
                return true;
            }

            Bitmap bitmap = holder.bitmapRef.get();
            if (bitmap != null) {
                setBitmap(view, bitmap);
                return true;
            }

            holder.bitmapRef = null;
        }

        setDrawable(view, R.drawable.loading);
        holder.status = BitmapHolder.PENDING;
        return false;
    }

    private void setBitmap(ImageView view, Bitmap bitmap) {
        view.setImageBitmap(bitmap);
        setImageParams(view, bitmap);
    }

    private void setDrawable(ImageView view, int res) {
        view.setImageResource(R.drawable.loading);
        //TODO Resize the image to meet requirement.
    }

    private void setImageParams(ImageView view, Bitmap bitmap) {
        float height = bitmap.getHeight();
        float width = bitmap.getWidth();

        LayoutParams lp = new LayoutParams((int)mItemWidth, (int)(height/width*mItemWidth));
        view.setLayoutParams(lp);
    }

    private class BitmapHolder {
        static public final int PENDING = 0;
        static public final int LOADING = 1;
        static public final int LOADED = 2;
        public int status;

        public SoftReference<Bitmap> bitmapRef;

        public BitmapHolder() {
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
                String url = entry.getValue();
                BitmapHolder cacheRef = mCacheMap.get(url);
                try {
                    Bitmap bitmap = getBitmapFromWeb(url);
                    cacheRef.set(bitmap);
                    cacheRef.status = BitmapHolder.LOADED;
                    mCacheMap.put(url, cacheRef);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
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

        private Bitmap getBitmapFromWeb(String url) {
            System.out.println("++++++++++++++++++++" + url + "++++++++++++++++++++");
            File file = null;
            HttpURLConnection conn = null;
            try {
                URL conUrl = new URL(url);
                conn = (HttpURLConnection)conUrl.openConnection();
                InputStream in = new BufferedInputStream(conn.getInputStream());

                FileCache fileCache = new FileCache();
                file = fileCache.getFile(url);
                FileOutputStream out = new FileOutputStream(file);
                Utils.copyStream(in, out);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
            }

            if (file != null) {
                return BitmapFactory.decodeFile(file.getPath());
            } else {
                return null;
            }
        }

        private Bitmap decodeBitmap(String url) {
            try {
                InputStream in = mContext.getResources().getAssets().open(url);
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

