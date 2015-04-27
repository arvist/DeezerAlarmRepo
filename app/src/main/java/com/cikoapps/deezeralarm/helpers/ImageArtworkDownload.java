package com.cikoapps.deezeralarm.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.cikoapps.deezeralarm.activities.DeezerBase;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class ImageArtworkDownload {

    private static final String TAG = "ImageArtworkDownload";
    private static final String APP_ID = DeezerBase.APP_ID;
    private final Context context;
    private ImageLoadTask imageLoadTask;

    public ImageArtworkDownload(Context context) {
        this.context = context;
    }

    public void cancelImageLoadTask() {
        if (imageLoadTask != null) {
            imageLoadTask.cancel(true);
            imageLoadTask = null;
        }
    }

    public void setAlbumArtworkImage(final ImageView imageView, int type, long id) {
        RequestListener requestListener = new JsonRequestListener() {
            public void onResult(Object result, Object requestId) {
                com.deezer.sdk.model.Album album = (com.deezer.sdk.model.Album) result;
                try {
                    imageLoadTask = new ImageLoadTask((album.getCoverUrl()), imageView);
                    imageLoadTask.execute();
                } catch (Exception ignored) {
                }
            }

            public void onUnparsedResult(String requestResponse, Object requestId) {
            }

            public void onException(Exception e, Object requestId) {
            }
        };

        DeezerRequest currUserAlbumRequest = DeezerRequestFactory.requestAlbum(id);
        currUserAlbumRequest.setId("albumById");
        DeezerConnect deezerConnect = new DeezerConnect(context, APP_ID);
        deezerConnect.requestAsync(currUserAlbumRequest, requestListener);
    }


    public void setPlaylistArtworkImage(String url, ImageView imageview) {
        imageLoadTask = new ImageLoadTask(url, imageview);
        imageLoadTask.execute();

    }

    private class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
        final String url;
        final ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
            Log.e(TAG, url);
        }

        // Lai neaizkavētu lietotāja interfeisu
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
            imageView.setImageBitmap(result);
        }


    }

}
