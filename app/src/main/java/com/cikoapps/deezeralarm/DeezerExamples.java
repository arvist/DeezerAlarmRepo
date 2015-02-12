//package com.cikoapps.deezeralarm;
//
//import android.support.v7.app.ActionBarActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.TextView;
//
//import com.deezer.sdk.model.Album;
//import com.deezer.sdk.model.Permissions;
//import com.deezer.sdk.model.Playlist;
//import com.deezer.sdk.model.User;
//import com.deezer.sdk.network.connect.DeezerConnect;
//import com.deezer.sdk.network.connect.SessionStore;
//import com.deezer.sdk.network.connect.event.DialogListener;
//import com.deezer.sdk.network.request.DeezerRequest;
//import com.deezer.sdk.network.request.DeezerRequestFactory;
//import com.deezer.sdk.network.request.event.DeezerError;
//import com.deezer.sdk.network.request.event.JsonRequestListener;
//import com.deezer.sdk.network.request.event.RequestListener;
//import com.deezer.sdk.player.PlaylistPlayer;
//import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
//import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
//
//import java.util.List;
//
//
//public class DeezerExamples extends ActionBarActivity {
//
//
//
//    DeezerConnect deezerConnect;
//    SessionStore sessionStore; String applicationID = "151831";
//    String userId;
//    User currUser;
//    List<Playlist> playlistList;
//    PlaylistPlayer albumPlayer = null;
//    Long  playListId;
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Initialize Deezer SDK
//
//        deezerConnect = new DeezerConnect(this, applicationID);
//
//
//        // The set of Deezer Permissions needed by the app
//        String[] permissions = new String[] {
//                Permissions.BASIC_ACCESS,
//                Permissions.MANAGE_LIBRARY,
//                Permissions.LISTENING_HISTORY };
//
//        // The listener for authentication events
//        DialogListener listener = new DialogListener() {
//            public void onComplete(Bundle values) {
//                getAuthUser();
//                getUserPlayLists();
//
//            }
//            public void onCancel() {}
//            public void onException(Exception e) {}
//        };
//        deezerConnect.authorize(this, permissions, listener);
//        sessionStore = new SessionStore();
//        sessionStore.save(deezerConnect, getApplicationContext());
//
//
//    }
//
//    public void getAuthUser(){
//        RequestListener requestListener = new JsonRequestListener() {
//
//            StringBuilder stringBuilder = new StringBuilder();
//            public void onResult(Object result, Object requestId) {
//                User currentUser = (User) result;
//                TextView testTextView = (TextView) findViewById(R.id.test);
//                currUser = currentUser;
//            }
//
//            public void onUnparsedResult(String requestResponse, Object requestId) {}
//
//            public void onException(Exception e, Object requestId) {}
//        };
//
//        DeezerRequest currUserRequest =  DeezerRequestFactory.requestCurrentUser();
//        currUserRequest.setId("currUserRequest");
//        deezerConnect.requestAsync(currUserRequest, requestListener);
//    }
//    public void getUserPlayLists(){
//        RequestListener requestListener = new JsonRequestListener() {
//
//            StringBuilder stringBuilder = new StringBuilder();
//            public void onResult(Object result, Object requestId) {
//
//                playlistList = (List<Playlist>) result;
//                for(Playlist playlist : playlistList){
//                    playListId = playlist.getId();
//                    Log.e("Playlist", playlist.getId() + "");
//                }
//                Log.e("Finished getting playlists", "");
//
//                playFavorites(playlistList.get(0));
//            }
//
//            public void onUnparsedResult(String requestResponse, Object requestId) {}
//
//            public void onException(Exception e, Object requestId) {}
//        };
//
//        DeezerRequest currUserPlaylistRequest =  DeezerRequestFactory.requestCurrentUserPlaylists();
//        currUserPlaylistRequest.setId("currUserPlayListRequest");
//        deezerConnect.requestAsync(currUserPlaylistRequest, requestListener);
//    }
//
//    public void playFavorites(Playlist playList){
//
//
//        try {
//            albumPlayer = new PlaylistPlayer(getApplication(), deezerConnect, new WifiAndMobileNetworkStateChecker());
//        } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
//            tooManyPlayersExceptions.printStackTrace();
//        } catch (DeezerError deezerError) {
//            deezerError.printStackTrace();
//        }
//        albumPlayer.playPlaylist(playList.getId());
//    }
//
//    @Override
//    protected void onPause() {
//
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        albumPlayer.stop();
//        albumPlayer.release();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//}
