package com.example.mymp3player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "com.example.mymp3player.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.mymp3player.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.mymp3player.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.mymp3player.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.mymp3player.ACTION_STOP";

    public static final String CHANNEL_ID = "channel_id";
    public static final String CHANNEL_NAME = "channel_name";

    private MediaSessionManager mediaSessionManager;
    private MediaSession mediaSession;
    private MediaController.TransportControls transportControls;

    public static final int NOTIFICATION_ID = 101;

    private static final String TAG = "MediaPlayerService";

    private final IBinder iBinder = new LocalBinder();

    private MediaPlayer mediaPlayer;
    private String mediaFile;
    private int resumePosition;

    private AudioManager audioManager;

    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    private ArrayList<Song> songsList;
    private int audioIndex = -1;
    private Song activeSong;


    @Override
    public void onCreate() {
        super.onCreate();

        callStateListener();
        registerBecomingNoisyReceiver();
        register_playNewAudio();
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(activeSong.getData());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            StorageUtil storage = new StorageUtil(getApplicationContext());
            songsList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex!=-1 && audioIndex<songsList.size()) {
                activeSong = songsList.get(audioIndex);
            } else {
                stopSelf();
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
            stopSelf();
        }

        if (!requestAudioFocus()) {
            stopSelf();
        }

        if (mediaSession==null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d(TAG, "Media error not valid for progressive playback");
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d(TAG, "Media error server died");
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d(TAG, "Media error unknown");
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();

        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener,
                    PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);

        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {

            return MediaPlayerService.this;
        }
    }

    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private void callStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (audioIndex != -1 && audioIndex < songsList.size()) {
                activeSong = songsList.get(audioIndex);
            } else {
                stopSelf();
            }

            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void register_playNewAudio() {
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    private void initMediaSession() throws RemoteException {
        if (mediaPlayer != null) return;

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSession(getApplicationContext(), "AudioPlayer");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        updateMetaData();

        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                onSkipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                onSkipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                stopSelf();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }
        });
    }

    private void skipToNext() {
        if (audioIndex == songsList.size() - 1) {
            audioIndex = 0;
            activeSong = songsList.get(audioIndex);
        } else {
            activeSong = songsList.get(++audioIndex);
        }

        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToPrevious() {
        if (audioIndex == 0) {
            audioIndex = songsList.size() - 1;
            activeSong = songsList.get(audioIndex);
        } else {
            activeSong = songsList.get(--audioIndex);
        }

        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void buildNotification(PlaybackStatus playbackStatus) {
        int notificationAction = android.R.drawable.ic_media_pause;
        PendingIntent play_pauseAction = null;

        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.cat);

        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setShowWhen(false)
                .setStyle(new Notification.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setColor(getResources().getColor(R.color.light_purple))
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setContentText(activeSong.getArtist())
                .setContentTitle(activeSong.getData())
                .setContentInfo(activeSong.getTitle())
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel(
                            CHANNEL_ID,
                            CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_DEFAULT
                    ));
        }
        notificationManager.notify(NOTIFICATION_ID,
                notificationBuilder.build());
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.cat);

        mediaSession.setMetadata(new MediaMetadata.Builder()
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, activeSong.getArtist())
                .putString(MediaMetadata.METADATA_KEY_TITLE, activeSong.getTitle())
                .build());
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            case 1:
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            case 2:
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            case 3:
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction==null || playbackAction.getAction()==null) {
            return;
        }

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }
}
