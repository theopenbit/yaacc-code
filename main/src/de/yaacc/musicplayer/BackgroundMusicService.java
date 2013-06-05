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

import de.yaacc.R;
import de.yaacc.imageviewer.ImageViewerActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * A simple service for playing music in background.
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class BackgroundMusicService extends Service {

	public static final String URIS = "URIS_PARAM"; // String Intent parameter
	private MediaPlayer player;
	private BackgroundMusicBroadcastReceiver backgroundMusicBroadcastReceiver;


	public BackgroundMusicService() {
		super();

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
		showNotification();

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
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startid) {
		Log.d(this.getClass().getName(), "On Start");
		backgroundMusicBroadcastReceiver = new BackgroundMusicBroadcastReceiver(
				this);
		backgroundMusicBroadcastReceiver.registerReceiver();
		if (player == null) {
			player = new MediaPlayer();
		} else {
			player.stop();
		}
		try {
			if (intent.getData() != null) {
				player.setDataSource(this, intent.getData());
			}
		} catch (Exception e) {
			Log.e(this.getClass().getName(),
					"Exception while changing datasource uri", e);

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
		if (player != null && !player.isPlaying()) {
			player.start();
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
		Log.e(this.getClass().getName(),
				"changing datasource uri to:" + uri.toString());
		if (player != null) {
			player.release();
		}
		player = new MediaPlayer();
		try {
			if (player.isPlaying()) {
				stop();
			}
			player.setDataSource(this, uri);
			player.prepare();
		} catch (Exception e) {
			Log.e(this.getClass().getName(),
					"Exception while changing datasource uri", e);

		}

	}
	
	private void showNotification(){	
		Intent notificationIntent = new Intent(this,
			    MusicPlayerActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
			    notificationIntent, 0);
	    NotificationCompat.Builder mBuilder =
	            new NotificationCompat.Builder(this)
	            .setSmallIcon(R.drawable.ic_launcher)
	            .setContentTitle("Yaacc Music player")
	            .setContentText("Current Title").setContentIntent(contentIntent).setOngoing(true);
	    NotificationManager mNotificationManager =
	    	    (NotificationManager) getSystemService(Context
	    	    		.NOTIFICATION_SERVICE);
	    	// mId allows you to update the notification later on.
	    	mNotificationManager.notify(1, mBuilder.build());
	}

}