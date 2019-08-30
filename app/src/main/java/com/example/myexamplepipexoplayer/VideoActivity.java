package com.example.myexamplepipexoplayer;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class VideoActivity extends AppCompatActivity {
    public static final String ARG_VIDEO_URL = "VideoActivity.URL";
    public static final String ARG_VIDEO_POSITION = "VideoActivity.POSITION";

    boolean isInPipMode = false;
    String mUrl;
    SimpleExoPlayer player;
    long videoPosition;
    boolean isPIPModeeEnabled = true; //Has the user disabled PIP mode in AppOpps?
    PlayerView playerView;
    boolean returnResultOnce = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerView = findViewById(R.id.playerView);
        if (getIntent().getExtras() == null || !getIntent().hasExtra(ARG_VIDEO_URL)) {
            finish();
        }
        mUrl = getIntent().getStringExtra(ARG_VIDEO_URL);
        if (savedInstanceState != null) {
            this.videoPosition = savedInstanceState.getLong(ARG_VIDEO_POSITION);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart() {
        super.onStart();
        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        playerView.setPlayer(player);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, getApplicationInfo().loadLabel(getPackageManager()).toString()));
        switch (Util.inferContentType(Uri.parse(mUrl))) {
            case C.TYPE_HLS:
                MediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mUrl));
                player.prepare(mediaSource);
                break;
            case C.TYPE_OTHER:
                MediaSource extractorMediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mUrl));
                player.prepare(extractorMediaSource);
                break;
            default:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
        }

        returnResultOnce = true;

        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if(playbackState == Player.STATE_READY && returnResultOnce){
                    setResult(Activity.RESULT_OK);
                    returnResultOnce = false;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                setResult(Activity.RESULT_CANCELED);
                finishAndRemoveTask();
            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
        player.setPlayWhenReady(true);

        //Use Media Session Connector from the EXT library to enable MediaSession Controls in PIP.
        MediaSessionCompat mediaSession = new MediaSessionCompat(this, getPackageName());
        MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(player, null);
        mediaSession.setActive(true);
    }

    @Override
    protected void onPause() {
        videoPosition = player.getCurrentPosition();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(videoPosition > 0L && !isInPipMode){
            player.seekTo(videoPosition);
        }
        //Makes sure that the media controls pop up on resuming and when going between PIP and non-PIP states.
        playerView.setUseController(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStop() {
        super.onStop();
        playerView.setPlayer(null);
        player.release();
        //PIPmode activity.finish() does not remove the activity from the recents stack.
        //Only finishAndRemoveTask does this.
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            finishAndRemoveTask();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
        if (outState !=null) {
            outState.putLong(ARG_VIDEO_POSITION, player.getCurrentPosition());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        videoPosition = savedInstanceState.getLong(ARG_VIDEO_POSITION);
    }

    @Override
    public void onBackPressed() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                && isPIPModeeEnabled) {
            enterPIPMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (newConfig!=null) {
            videoPosition = player.getCurrentPosition();
            isInPipMode = !isInPictureInPictureMode;
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }
    //Called when the user touches the Home or Recents button to leave the app.
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        enterPIPMode();
    }
    private void enterPIPMode(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            videoPosition = player.getCurrentPosition();
            playerView.setUseController(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PictureInPictureParams.Builder params = new PictureInPictureParams.Builder();
                this.enterPictureInPictureMode(params.build());
            } else {
                this.enterPictureInPictureMode();
            }
            /* We need to check this because the system permission check is publically hidden for integers for non-manufacturer-built apps
               https://github.com/aosp-mirror/platform_frameworks_base/blob/studio-3.1.2/core/java/android/app/AppOpsManager.java#L1640

               ********* If we didn't have that problem *********
                val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                if(appOpsManager.checkOpNoThrow(AppOpManager.OP_PICTURE_IN_PICTURE, packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).uid, packageName) == AppOpsManager.MODE_ALLOWED)

                30MS window in even a restricted memory device (756mb+) is more than enough time to check, but also not have the system complain about holding an action hostage.
             */
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkPIPPermission();
                }
            }, 30);
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private void checkPIPPermission(){
        isPIPModeeEnabled = isInPictureInPictureMode();
        if(!isInPictureInPictureMode()){
            onBackPressed();
        }
    }


}
