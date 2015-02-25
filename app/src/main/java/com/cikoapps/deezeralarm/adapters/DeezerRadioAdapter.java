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
import com.cikoapps.deezeralarm.models.Radio;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by arvis.taurenis on 2/16/2015.
 */
public class DeezerRadioAdapter extends RecyclerView.Adapter<DeezerRadioAdapter.DeezerRadioViewHolder> {
    Context context;
    ArrayList<Radio> radioList;
    LayoutInflater inflater;
    ArrayList<Bitmap> images;
    static Typeface notoRegular;
    public static int selectedPosition = -1;

    public DeezerRadioAdapter(Context mContext, ArrayList<Radio> radio) {
        context = mContext;
        radioList = radio;
        radioList.add(null);
        inflater = LayoutInflater.from(context);
        images = new ArrayList<>();
        for (int i = 0; i < radioList.size(); i++) {
            images.add(null);
        }
        notoRegular = Typeface.createFromAsset(context.getAssets(), "NotoSerif-Regular.ttf");
    }

    @Override
    public DeezerRadioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.deezer_radio_item_layout, parent, false);
        return new DeezerRadioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeezerRadioViewHolder holder, final int position) {
        final Radio radio = radioList.get(position);
        if (radio == null) {
            holder.radioRadioButton.setWillNotDraw(true);
            holder.radioTitleTextView.setWillNotDraw(true);
            holder.radioImageView.setWillNotDraw(true);
            holder.radioImageView.setImageBitmap(null);
        } else {
            holder.radioRadioButton.setWillNotDraw(false);
            holder.radioTitleTextView.setWillNotDraw(false);
            holder.radioImageView.setWillNotDraw(false);


            holder.radioTitleTextView.setText(radio.title);
            holder.radioTitleTextView.setTypeface(notoRegular);

            if (radio.imageUrlMedium.length() > 1) {
                holder.radioImageView.setImageResource(R.drawable.weather_sunny);
                if (radio.selected && RingtoneActivity.selectedRingtone.type == 4) {
                    holder.radioRadioButton.setChecked(true);
                } else holder.radioRadioButton.setChecked(false);
                if (images.get(position) != null) {
                    holder.radioImageView.setImageBitmap(images.get(position));
                } else {
                    new ImageLoadTask(radio.imageUrlMedium, holder.radioImageView, position).execute();
                }

            }
            holder.radioRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPosition == -1) {
                        radio.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerRadioFragment.updateSelectedRingtone(radio.id, radio.title);
                    } else if (selectedPosition != -1 && selectedPosition == position) {
                        radio.selected = false;
                        selectedPosition = -1;
                        notifyItemChanged(position);
                        DeezerRadioFragment.updateSelectedRingtone(-1, "");
                    } else {
                        radioList.get(selectedPosition).selected = false;
                        notifyItemChanged(selectedPosition);
                        radio.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerRadioFragment.updateSelectedRingtone(radio.id, radio.title);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return radioList.size();
    }


    class DeezerRadioViewHolder extends RecyclerView.ViewHolder {


        ImageView radioImageView;
        TextView radioTitleTextView;
        RadioButton radioRadioButton;

        public DeezerRadioViewHolder(View itemView) {
            super(itemView);
            radioImageView = (ImageView) itemView.findViewById(R.id.radioImage);
            radioTitleTextView = (TextView) itemView.findViewById(R.id.radioTitleTextView);
            radioRadioButton = (RadioButton) itemView.findViewById(R.id.radioRadioButton);

        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;
        private int position;

        public ImageLoadTask(String url, ImageView imageView, int position) {
            this.url = url;
            this.imageView = imageView;
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
