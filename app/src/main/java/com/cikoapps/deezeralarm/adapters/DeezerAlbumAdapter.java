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
import com.cikoapps.deezeralarm.Fragments.DeezerAlbumFragment;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.models.DeezerAlbum;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class DeezerAlbumAdapter extends RecyclerView.Adapter<DeezerAlbumAdapter.DeezerAlbumViewHolder> {

    public static int selectedPosition = -1;
    private static Typeface robotoRegular;
    private final ArrayList<DeezerAlbum> albumsList;
    private final LayoutInflater inflater;
    private final ArrayList<Bitmap> images;

    public DeezerAlbumAdapter(Context mContext, ArrayList<DeezerAlbum> deezerAlbums) {
        albumsList = deezerAlbums;
        deezerAlbums.add(null);
        inflater = LayoutInflater.from(mContext);
        images = new ArrayList<>();
        for (int i = 0; i < albumsList.size(); i++) {
            images.add(null);
        }
        robotoRegular = Typeface.createFromAsset(mContext.getAssets(), "Roboto-Regular.ttf");
    }

    @Override
    public DeezerAlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.deezer_albums_item_layout, parent, false);
        return new DeezerAlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeezerAlbumViewHolder holder, final int position) {
        final DeezerAlbum deezerAlbum = albumsList.get(position);
        if (deezerAlbum == null) {
            holder.albumRadioButton.setWillNotDraw(true);
            holder.albumImageView.setWillNotDraw(true);
            holder.albumArtistTextView.setWillNotDraw(true);
            holder.albumTitleTextView.setWillNotDraw(true);
            holder.albumImageView.setImageBitmap(null);
        } else {
            holder.albumRadioButton.setWillNotDraw(false);
            holder.albumImageView.setWillNotDraw(false);
            holder.albumArtistTextView.setWillNotDraw(false);
            holder.albumTitleTextView.setWillNotDraw(false);
            holder.albumTitleTextView.setText(deezerAlbum.title);
            holder.albumTitleTextView.setTypeface(robotoRegular);
            holder.albumArtistTextView.setText(deezerAlbum.artist);
            holder.albumArtistTextView.setTypeface(robotoRegular);
            if (deezerAlbum.imageUrlMedium.length() > 1) {
                holder.albumImageView.setImageResource(R.drawable.ic_album);
                if (deezerAlbum.selected && RingtoneActivity.selectedRingtone.type == 2) {
                    holder.albumRadioButton.setChecked(true);
                } else holder.albumRadioButton.setChecked(false);

                if (images.get(position) != null) {
                    holder.albumImageView.setImageBitmap(images.get(position));
                } else {
                    new ImageLoadTask(deezerAlbum.imageUrlMedium, holder.albumImageView, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
            holder.albumRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPosition == -1) {
                        deezerAlbum.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerAlbumFragment.updateSelectedRingtone(deezerAlbum.id, deezerAlbum.title, deezerAlbum.artist);
                    } else if (selectedPosition == position) {
                        deezerAlbum.selected = false;
                        selectedPosition = -1;
                        notifyItemChanged(position);
                        DeezerAlbumFragment.updateSelectedRingtone(-1, "", "");
                    } else {
                        albumsList.get(selectedPosition).selected = false;
                        notifyItemChanged(selectedPosition);
                        deezerAlbum.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerAlbumFragment.updateSelectedRingtone(deezerAlbum.id, deezerAlbum.title, deezerAlbum.artist);
                    }

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return albumsList.size();
    }

    class DeezerAlbumViewHolder extends RecyclerView.ViewHolder {


        final ImageView albumImageView;
        final TextView albumTitleTextView;
        final TextView albumArtistTextView;
        final RadioButton albumRadioButton;

        public DeezerAlbumViewHolder(View itemView) {
            super(itemView);
            albumImageView = (ImageView) itemView.findViewById(R.id.albumImage);
            albumTitleTextView = (TextView) itemView.findViewById(R.id.albumTitleTextView);
            albumArtistTextView = (TextView) itemView.findViewById(R.id.albumArtistTextView);
            albumRadioButton = (RadioButton) itemView.findViewById(R.id.albumRadioButton);

        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private final String url;
        private final ImageView imageView;
        private final int position;

        public ImageLoadTask(String url, ImageView imageView, int position) {
            this.url = url;
            this.position = position;
            this.imageView = imageView;
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
