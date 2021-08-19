package com.home.broadcastreceivers.musicmachine;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.home.broadcastreceivers.R;
import com.home.broadcastreceivers.musicmachine.models.Song;

import static android.app.NotificationManager.IMPORTANCE_HIGH;


public class PlayerService extends Service {
    private static final String TAG = PlayerService.class.getSimpleName();
    private static final int REQUEST_OPEN = 99;

    private MediaPlayer mPlayer;
    public Messenger mMessenger = new Messenger(new PlayerHandler(this));

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mPlayer = MediaPlayer.create(this, R.raw.jingle);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String title = "";
        String artist = "";

        if (intent.getParcelableExtra(MainActivity.EXTRA_SONG) != null) {
            Song song = intent.getParcelableExtra(MainActivity.EXTRA_SONG);
            title = song.getTitle();
            artist = song.getArtist();
        }

        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                REQUEST_OPEN, mainIntent, 0);


        String CHANNEL_ONE_ID = "com.app.N1";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }


        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setChannelId(CHANNEL_ONE_ID)
                .setSmallIcon(R.drawable.ic_queue_music_white)
                .setContentTitle(title)
                .setContentText(artist)
                .setContentIntent(pendingIntent);
        Notification notification = notificationBuilder.build();
        startForeground(11, notification);

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopSelf();
                stopForeground(true);
            }
        });
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mPlayer.release();
    }

    // Client Methods
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public void play() {
        mPlayer.start();
    }

    public void pause() {
        mPlayer.pause();
    }
}
