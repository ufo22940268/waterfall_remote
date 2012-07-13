package hongbosb.springflow;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.view.LayoutInflater;

public class MainActivity extends Activity
{
    LazyScrollView mContainerView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mContainerView = (LazyScrollView)findViewById(R.id.lazy_scroll);
        addAllImages();
    }

    private void addAllImages() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < 30; i ++) {
            ImageView view = (ImageView)inflater.inflate(R.layout.item, null);
            mContainerView.addImage(view, i);
        }
    }
}
