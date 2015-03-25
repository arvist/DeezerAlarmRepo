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
import com.cikoapps.deezeralarm.Fragments.DeezerPlaylistsFragment;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.models.Playlist;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class DeezerPlaylistAdapter extends RecyclerView.Adapter<DeezerPlaylistAdapter.DeezerPlaylistViewHolder> {

    public static int selectedPosition = -1;
    private static Typeface robotoRegular;
    private final ArrayList<Playlist> playlistList;
    private final LayoutInflater inflater;
    private final ArrayList<Bitmap> images;

    public DeezerPlaylistAdapter(Context mContext, ArrayList<Playlist> playlist) {
        playlistList = playlist;
        if (playlistList.size() > 0) {
            if (playlistList.get(playlist.size() - 1) != null)
                playlistList.add(null);
        }
        inflater = LayoutInflater.from(mContext);
        images = new ArrayList<>();
        for (int i = 0; i < playlistList.size(); i++) {
            images.add(null);
        }
        robotoRegular = Typeface.createFromAsset(mContext.getAssets(), "Roboto-Regular.ttf");
    }

    @Override
    public DeezerPlaylistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.deezer_playlists_item_layout, parent, false);
        return new DeezerPlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeezerPlaylistViewHolder holder, final int position) {
        final Playlist playlist = playlistList.get(position);
        if (playlist == null) {
            holder.playlistChecked.setWillNotDraw(true);
            holder.playListImage.setWillNotDraw(true);
            holder.playListImage.setImageBitmap(null);
            holder.playListInfoTextView.setWillNotDraw(true);
            holder.playListTitleTextView.setWillNotDraw(true);
        } else {
            holder.playlistChecked.setWillNotDraw(false);
            holder.playListImage.setWillNotDraw(false);
            holder.playListInfoTextView.setWillNotDraw(false);
            holder.playListTitleTextView.setWillNotDraw(false);
            holder.playListTitleTextView.setText(playlist.title);
            holder.playListTitleTextView.setTypeface(robotoRegular);
            holder.playListInfoTextView.setText(playlist.info);
            holder.playListInfoTextView.setTypeface(robotoRegular);
            if (playlist.imageUrlMedium.length() > 1) {
                holder.playListImage.setImageResource(R.drawable.ic_playlist);
                if (playlist.selected && RingtoneActivity.selectedRingtone.type == 1) {
                    holder.playlistChecked.setChecked(true);
                } else holder.playlistChecked.setChecked(false);
                if (images.size() > 0) {
                    if (images.get(position) != null) {
                        holder.playListImage.setImageBitmap(images.get(position));
                    } else {
                        new ImageLoadTask(playlist.imageUrlMedium, holder.playListImage, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }
            holder.playlistChecked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPosition == -1) {
                        playlist.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerPlaylistsFragment.updateSelectedRingtone(playlist.id, playlist.title);
                        DeezerPlaylistsFragment.updateSelectedRingtone(playlist.id, playlist.title);
                    } else if (selectedPosition == position) {
                        playlist.selected = false;
                        selectedPosition = -1;
                        notifyItemChanged(position);
                        DeezerPlaylistsFragment.updateSelectedRingtone(-1, "");
                    } else {
                        playlistList.get(selectedPosition).selected = false;
                        notifyItemChanged(selectedPosition);
                        playlist.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerPlaylistsFragment.updateSelectedRingtone(playlist.id, playlist.title);
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    class DeezerPlaylistViewHolder extends RecyclerView.ViewHolder {


        final ImageView playListImage;
        final TextView playListTitleTextView;
        final TextView playListInfoTextView;
        final RadioButton playlistChecked;

        public DeezerPlaylistViewHolder(View itemView) {
            super(itemView);
            playListImage = (ImageView) itemView.findViewById(R.id.playListImage);
            playListTitleTextView = (TextView) itemView.findViewById(R.id.playtListTitleTextView);
            playListInfoTextView = (TextView) itemView.findViewById(R.id.playListInfoTextView);
            playlistChecked = (RadioButton) itemView.findViewById(R.id.playlistChecked);

        }
    }

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
