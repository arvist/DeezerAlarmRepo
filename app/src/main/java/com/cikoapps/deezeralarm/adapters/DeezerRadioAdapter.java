package com.cikoapps.deezeralarm.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cikoapps.deezeralarm.Activities.RingtoneActivity;
import com.cikoapps.deezeralarm.Fragments.DeezerRadioFragment;
import com.cikoapps.deezeralarm.R;

import com.cikoapps.deezeralarm.models.DeezerRadio;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DeezerRadioAdapter extends RecyclerView.Adapter<DeezerRadioAdapter.DeezerRadioViewHolder> {
    private static final String TAG = "DeezerRadioAdapter";
    public static int selectedPosition = -1;
    private static Typeface robotoRegular;
    private final ArrayList<DeezerRadio> deezerRadioList;
    private final LayoutInflater inflater;
    private final ArrayList<Bitmap> images;

    public DeezerRadioAdapter(Context mContext, ArrayList<DeezerRadio> deezerRadio) {
        deezerRadioList = new ArrayList<>(deezerRadio);
        deezerRadioList.add(null);
        inflater = LayoutInflater.from(mContext);
        images = new ArrayList<>();
        for (int i = 0; i < deezerRadioList.size(); i++) {
            images.add(null);
        }
        robotoRegular = Typeface.createFromAsset(mContext.getAssets(), "Roboto-Regular.ttf");
    }

    @Override
    public DeezerRadioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.deezer_radio_item_layout, parent, false);
        return new DeezerRadioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeezerRadioViewHolder holder, final int position) {
        final DeezerRadio deezerRadio = deezerRadioList.get(position);
        if (deezerRadio == null) {
            holder.radioRadioButton.setWillNotDraw(true);
            holder.radioTitleTextView.setWillNotDraw(true);
            holder.radioImageView.setWillNotDraw(true);
            holder.radioImageView.setImageBitmap(null);
        } else {
            holder.radioRadioButton.setWillNotDraw(false);
            holder.radioTitleTextView.setWillNotDraw(false);
            holder.radioImageView.setWillNotDraw(false);
            holder.radioTitleTextView.setText(deezerRadio.title);
            holder.radioTitleTextView.setTypeface(robotoRegular);

            if (deezerRadio.imageUrlMedium.length() > 1) {
                holder.radioImageView.setImageResource(R.drawable.ic_radio);
                if (deezerRadio.selected && RingtoneActivity.selectedRingtone.type == 4) {
                    holder.radioRadioButton.setChecked(true);
                } else holder.radioRadioButton.setChecked(false);
                if (images.get(position) != null) {
                    holder.radioImageView.setImageBitmap(images.get(position));
                } else {
                    new ImageLoadTask(deezerRadio.imageUrlMedium, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
            holder.radioRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPosition == -1) {
                        deezerRadio.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerRadioFragment.updateSelectedRingtone(deezerRadio.id, deezerRadio.title);
                    } else if (selectedPosition == position) {
                        deezerRadio.selected = false;
                        selectedPosition = -1;
                        notifyItemChanged(position);
                        DeezerRadioFragment.updateSelectedRingtone(-1, "");
                    } else {
                        deezerRadioList.get(selectedPosition).selected = false;
                        notifyItemChanged(selectedPosition);
                        deezerRadio.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerRadioFragment.updateSelectedRingtone(deezerRadio.id, deezerRadio.title);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return deezerRadioList.size();
    }


    class DeezerRadioViewHolder extends RecyclerView.ViewHolder {


        final ImageView radioImageView;
        final TextView radioTitleTextView;
        final RadioButton radioRadioButton;

        public DeezerRadioViewHolder(View itemView) {
            super(itemView);
            radioImageView = (ImageView) itemView.findViewById(R.id.radioImage);
            radioTitleTextView = (TextView) itemView.findViewById(R.id.radioTitleTextView);
            radioRadioButton = (RadioButton) itemView.findViewById(R.id.radioRadioButton);

        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private final String url;
        private final int position;

        public ImageLoadTask(String url, int position) {
            this.url = url;
            this.position = position;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            images.set(position, result);
            notifyItemChanged(position);
        }

    }
}
