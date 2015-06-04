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

import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.activities.RingtoneActivity;
import com.cikoapps.deezeralarm.fragments.DeezerArtistFragment;
import com.cikoapps.deezeralarm.models.DeezerArtist;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DeezerArtistAdapter extends RecyclerView.Adapter<DeezerArtistAdapter.DeezerArtistViewHolder> {


    public static int selectedPosition = -1;
    private static Typeface robotoRegular;
    private final ArrayList<DeezerArtist> deezerArtistList;
    private final LayoutInflater inflater;
    private final ArrayList<Bitmap> images;

    public DeezerArtistAdapter(Context mContext, ArrayList<DeezerArtist> deezerArtists) {
        deezerArtistList = deezerArtists;
        // Add null object to list to prevent confirm button overlapping with last object
        deezerArtistList.add(null);
        inflater = LayoutInflater.from(mContext);
        images = new ArrayList<>();
        for (int i = 0; i < deezerArtistList.size(); i++) {
            images.add(null);
        }
        robotoRegular = Typeface.createFromAsset(mContext.getAssets(), "Roboto-Regular.ttf");
    }

    @Override
    public DeezerArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.deezer_artists_item_layout, parent, false);
        return new DeezerArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeezerArtistViewHolder holder, final int position) {
        final DeezerArtist deezerArtist = deezerArtistList.get(position);
        if (deezerArtist == null) {
            // Make last object invisible
            holder.artistRadioButton.setWillNotDraw(true);
            holder.artistImageView.setWillNotDraw(true);
            holder.artistTextView.setWillNotDraw(true);
            holder.artistImageView.setImageBitmap(null);
        } else {
            holder.artistRadioButton.setWillNotDraw(false);
            holder.artistImageView.setWillNotDraw(false);
            holder.artistTextView.setWillNotDraw(false);
            holder.artistTextView.setText(deezerArtist.name);
            holder.artistTextView.setTypeface(robotoRegular);
            if (deezerArtist.imageUrlMedium.length() > 1) {
                holder.artistImageView.setImageResource(R.drawable.ic_artist);
                if (deezerArtist.selected && RingtoneActivity.selectedRingtone.type == 3) {
                    holder.artistRadioButton.setChecked(true);
                } else holder.artistRadioButton.setChecked(false);
                if (images.get(position) != null) {
                    holder.artistImageView.setImageBitmap(images.get(position));
                } else {
                    try {
                        new ImageLoadTask(deezerArtist.imageUrlMedium, holder.artistImageView, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } catch (Exception ignored) {
                        /* Can occur when list is being scrolled up and down  app creates
                         a thread for every new image load task  if image is not in downloaded
                          image list, and id thread pool is full exception is thrown
                        */
                    }
                }
            }
            holder.artistRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPosition == -1) {
                        deezerArtist.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerArtistFragment.updateSelectedRingtone(deezerArtist.id, deezerArtist.name);
                    } else if (selectedPosition == position) {
                        deezerArtist.selected = false;
                        selectedPosition = -1;
                        notifyItemChanged(position);
                        DeezerArtistFragment.updateSelectedRingtone(-1, "");
                    } else {
                        deezerArtistList.get(selectedPosition).selected = false;
                        notifyItemChanged(selectedPosition);
                        deezerArtist.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerArtistFragment.updateSelectedRingtone(deezerArtist.id, deezerArtist.name);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return deezerArtistList.size();
    }

    class DeezerArtistViewHolder extends RecyclerView.ViewHolder {


        final ImageView artistImageView;
        final TextView artistTextView;
        final RadioButton artistRadioButton;

        public DeezerArtistViewHolder(View itemView) {
            super(itemView);
            artistImageView = (ImageView) itemView.findViewById(R.id.artistImage);
            artistTextView = (TextView) itemView.findViewById(R.id.artistTextView);
            artistRadioButton = (RadioButton) itemView.findViewById(R.id.artistRadioButton);

        }
    }

    // Async task to load artist image to list object
    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private final String url;
        private final ImageView imageView;
        private final int position;

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
