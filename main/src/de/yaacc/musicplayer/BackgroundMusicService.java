/*
 *
 * Copyright (C) 2013 www.yaacc.de 
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.yaacc.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple service for playing music in background.
 *
 * @author Tobias Schoene (openbit)
 */
public class BackgroundMusicService extends Service {

    public static final String URIS = "URIS_PARAM"; // String Intent parameter
    private MediaPlayer player;
    private IBinder binder = new BackgroundMusicServiceBinder();
    private BackgroundMusicBroadcastReceiver backgroundMusicBroadcastReceiver;
    //private boolean prepared  = false;

    public BackgroundMusicService() {
        super();
    }

    public class BackgroundMusicServiceBinder extends Binder {
        public BackgroundMusicService getService() {
            return BackgroundMusicService.this;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(this.getClass().getName(), "On Create");

    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        Log.d(this.getClass().getName(), "On Destroy");
        if (player != null) {
            player.stop();
            player.release();
        }
        unregisterReceiver(backgroundMusicBroadcastReceiver);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(this.getClass().getName(), "On Bind");
        return binder;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onStart(android.content.Intent, int)
     */
    @Override
    public void onStart(Intent intent, int startid) {
        Log.d(this.getClass().getName(), "On Start");
        initialize(intent);
    }

    private void initialize(Intent intent) {
        backgroundMusicBroadcastReceiver = new BackgroundMusicBroadcastReceiver(this);
        backgroundMusicBroadcastReceiver.registerReceiver();
        if (player != null) {
            player.stop();
            player.release();
        }
        player = new MediaPlayer();
        try {
            if (intent != null && intent.getData() != null) {
                player.setDataSource(this, intent.getData());
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Exception while changing datasource uri", e);

        }
        if (player != null) {
            player.setVolume(100, 100);
            Log.i(this.getClass().getName(), "is Playing:" + player.isPlaying());
        }
    }

    /**
     * stop current music play
     */
    public void stop() {
        if (player != null) {
            player.stop();
        }
    }

    /**
     * start current music play
     */
    public void play() {
        //final ActionState actionState = new ActionState();
        if (player != null && !player.isPlaying() ) {
            /*new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    actionState.watchdogFlag = true;
                }
            }, 30000L); // 30sec. Watchdog
            while (!(actionState.actionFinished || actionState.watchdogFlag)) {
                // wait for player preparation
                actionState.actionFinished = prepared;
            }*/
            //if(actionState.actionFinished){
                player.start();
            /*}else {
                Log.d(getClass().getName(),"Preparation of music player failed!");
            }*/
        }
    }

    /**
     * pause current music play
     */
    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    /**
     * change music uri
     *
     * @param uri
     */
    public void setMusicUri(Uri uri) {
        Log.d(this.getClass().getName(), "changing datasource uri to:" + uri.toString());
        if (player != null) {
            player.release();
        }
        player = new MediaPlayer();
        player.setVolume(100, 100);
        //prepared= false;
        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, uri);
            player.prepare();
            //player.prepareAsync();
          /*  player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    prepared = true;
                }
            });*/
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Exception while changing datasource uri", e);

        }

    }

    /**
     * returns the duration of the current track
     *
     * @return the duration
     */
    public int getDuration() {
        int duration = 0;

        if (player != null && player.isPlaying()) {
            try {
                duration = player.getDuration();
            } catch (Exception ex) {
                Log.d(getClass().getName(), "Caught player exception", ex);
            }
        }

        return duration;
    }

    /**
     * return the current position in the playing track
     *
     * @return the position
     */
    public int getCurrentPosition() {
        int currentPosition = 0;
        if (player != null) {
            try {
                currentPosition = player.getCurrentPosition();
            } catch (Exception ex) {
                Log.d(getClass().getName(), "Caught player exception", ex);
            }
        }

        return currentPosition;
    }


    private static class ActionState {
        public boolean actionFinished = false;
        public boolean watchdogFlag = false;
    }
}