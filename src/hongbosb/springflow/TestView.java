package hongbosb.springflow;

import android.view.View;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;

import java.io.*;

public class TestView extends View {
    private Bitmap mBitmap;

    public TestView(Context context, AttributeSet attr) {
        super(context, attr);
        InputStream in = context.getResources().openRawResource(R.drawable.sample_0);

        //mBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(in), 50, 200, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint p = new Paint();
        canvas.drawBitmap(mBitmap, 0, 10, p);
    }
}

