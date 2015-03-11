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
import com.cikoapps.deezeralarm.Fragments.DeezerArtistFragment;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.models.Artist;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DeezerArtistAdapter extends RecyclerView.Adapter<DeezerArtistAdapter.DeezerArtistViewHolder> {


    Context context;
    ArrayList<Artist> artistList;
    LayoutInflater inflater;
    ArrayList<Bitmap> images;
    static Typeface robotoRegular;
    public static int selectedPosition = -1;

    public DeezerArtistAdapter(Context mContext, ArrayList<Artist> artists) {
        context = mContext;
        artistList = artists;
        artistList.add(null);
        inflater = LayoutInflater.from(context);
        images = new ArrayList<>();
        for (int i = 0; i < artistList.size(); i++) {
            images.add(null);
        }
        robotoRegular = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
    }

    @Override
    public DeezerArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.deezer_artists_item_layout, parent, false);
        return new DeezerArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeezerArtistViewHolder holder, final int position) {
        final Artist artist = artistList.get(position);
        if (artist == null) {
            holder.artistRadioButton.setWillNotDraw(true);
            holder.artistImageView.setWillNotDraw(true);
            holder.artistTextView.setWillNotDraw(true);
            holder.artistImageView.setImageBitmap(null);
        } else {
            holder.artistRadioButton.setWillNotDraw(false);
            holder.artistImageView.setWillNotDraw(false);
            holder.artistTextView.setWillNotDraw(false);
            holder.artistTextView.setText(artist.name);
            holder.artistTextView.setTypeface(robotoRegular);
            if (artist.imageUrlMedium.length() > 1) {
                holder.artistImageView.setImageResource(R.drawable.ic_artist);
                if (artist.selected && RingtoneActivity.selectedRingtone.type == 3) {
                    holder.artistRadioButton.setChecked(true);
                } else holder.artistRadioButton.setChecked(false);
                if (images.get(position) != null) {
                    holder.artistImageView.setImageBitmap(images.get(position));
                } else {
                    new ImageLoadTask(artist.imageUrlMedium, holder.artistImageView, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
            holder.artistRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPosition == -1) {
                        artist.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerArtistFragment.updateSelectedRingtone(artist.id, artist.name);
                    } else if (selectedPosition == position) {
                        artist.selected = false;
                        selectedPosition = -1;
                        notifyItemChanged(position);
                        DeezerArtistFragment.updateSelectedRingtone(-1, "");
                    } else {
                        artistList.get(selectedPosition).selected = false;
                        notifyItemChanged(selectedPosition);
                        artist.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerArtistFragment.updateSelectedRingtone(artist.id, artist.name);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    class DeezerArtistViewHolder extends RecyclerView.ViewHolder {


        ImageView artistImageView;
        TextView artistTextView;
        RadioButton artistRadioButton;

        public DeezerArtistViewHolder(View itemView) {
            super(itemView);
            artistImageView = (ImageView) itemView.findViewById(R.id.artistImage);
            artistTextView = (TextView) itemView.findViewById(R.id.artistTextView);
            artistRadioButton = (RadioButton) itemView.findViewById(R.id.artistRadioButton);

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
