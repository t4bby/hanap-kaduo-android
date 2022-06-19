package corp.amq.hkd.ui.fragments.profile.adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import corp.amq.hkd.R;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private String[] urls;

    public ImageAdapter(Context c, String[] urls) {
        this.mContext = c;
        this.urls = urls;
    }

    public int getCount() {
        return urls.length;
    }

    public Object getItem(int position) {
        return urls[position];
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView = new ImageView(mContext);
        imageView.setLayoutParams(new GridView.LayoutParams(250, 500));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(8, 8, 8, 8);

        // Picasso.get().load(urls[position]).into(imageView);
        Picasso.get().load(R.drawable.sample2).into(imageView);

        return imageView;
    }

}
