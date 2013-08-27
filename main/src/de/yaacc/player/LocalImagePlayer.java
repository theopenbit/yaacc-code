/*
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
package de.yaacc.player;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import de.yaacc.R;
import de.yaacc.imageviewer.ImageViewerActivity;
import de.yaacc.imageviewer.ImageViewerBroadcastReceiver;
import de.yaacc.upnp.UpnpClient;
import de.yaacc.util.NotificationId;

/**
 * Player for local image viewing activity
 * 
 * @author Tobias Schoene (openbit)
 * 
 */
public class LocalImagePlayer implements Player {

	private Context context;
	private Timer commandExecutionTimer;
	private String name;

	/**
	 * @param context
	 * @param name
	 *            playerName
	 * 
	 */
	public LocalImagePlayer(UpnpClient upnpClient, String name) {
		this(upnpClient);
		setName(name);
	}

	/**
	 * @param context
	 */
	public LocalImagePlayer(UpnpClient upnpClient) {
		this.context = upnpClient.getContext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#next()
	 */
	@Override
	public void next() {
		// Communicating with the activity is only possible after the activity
		// is started
		// if we send an broadcast event to early the activity won't be up
		// in order there is no known way to query the activity state
		// we are sending the command delayed
		commandExecutionTimer = new Timer();
		commandExecutionTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				Intent intent = new Intent();
				intent.setAction(ImageViewerBroadcastReceiver.ACTION_NEXT);
				context.sendBroadcast(intent);

			}
		}, 500L);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#previous()
	 */
	@Override
	public void previous() {
		// Communicating with the activity is only possible after the activity
		// is started
		// if we send an broadcast event to early the activity won't be up
		// in order there is no known way to query the activity state
		// we are sending the command delayed
		commandExecutionTimer = new Timer();
		commandExecutionTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				Intent intent = new Intent();
				intent.setAction(ImageViewerBroadcastReceiver.ACTION_PREVIOUS);
				context.sendBroadcast(intent);

			}
		}, 500L);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#pause()
	 */
	@Override
	public void pause() {
		// Communicating with the activity is only possible after the activity
		// is started
		// if we send an broadcast event to early the activity won't be up
		// in order there is no known way to query the activity state
		// we are sending the command delayed
		commandExecutionTimer = new Timer();
		commandExecutionTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				Intent intent = new Intent();
				intent.setAction(ImageViewerBroadcastReceiver.ACTION_PAUSE);
				context.sendBroadcast(intent);

			}
		}, 500L);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#play()
	 */
	@Override
	public void play() {
		// Communicating with the activity is only possible after the activity
		// is started
		// if we send an broadcast event to early the activity won't be up
		// in order there is no known way to query the activity state
		// we are sending the command delayed
		commandExecutionTimer = new Timer();
		commandExecutionTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				Log.d(this.getClass().getName(), "send play");
				Intent intent = new Intent();
				intent.setAction(ImageViewerBroadcastReceiver.ACTION_PLAY);
				context.sendBroadcast(intent);

			}
		}, 500L);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#stop()
	 */
	@Override
	public void stop() {
		// Communicating with the activity is only possible after the activity
		// is started
		// if we send an broadcast event to early the activity won't be up
		// in order there is no known way to query the activity state
		// we are sending the command delayed
		commandExecutionTimer = new Timer();
		commandExecutionTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				Intent intent = new Intent();
				intent.setAction(ImageViewerBroadcastReceiver.ACTION_STOP);
				context.sendBroadcast(intent);

			}
		}, 500L);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#setItems(de.yaacc.player.PlayableItem[])
	 */
	@Override
	public void setItems(PlayableItem... items) {
		Intent intent = new Intent(context, ImageViewerActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ArrayList<Uri> uris = new ArrayList<Uri>();
		for (int i = 0; i < items.length; i++) {
			uris.add(items[i].getUri());
		}
		intent.putExtra(ImageViewerActivity.URIS, uris);				
		context.startActivity(intent);
		showNotification(uris);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#addItem(de.yaacc.player.PlayableItem)
	 */
	@Override
	public void addItem(PlayableItem item) {
		// FIXME not yet implemented

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#getName()
	 */
	@Override
	public String getName() {

		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#exit()
	 */
	@Override
	public void exit() {
		PlayerFactory.shutdown(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#clear()
	 */
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#onDestroy()
	 */
	@Override
	public void onDestroy() {
		cancleNotification();
		// Communicating with the activity is only possible after the activity
		// is started
		// if we send an broadcast event to early the activity won't be up
		// in order there is no known way to query the activity state
		// we are sending the command delayed
		commandExecutionTimer = new Timer();
		commandExecutionTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				Intent intent = new Intent();
				intent.setAction(ImageViewerBroadcastReceiver.ACTION_EXIT);
				context.sendBroadcast(intent);

			}
		}, 500L);

	}

	/**
	 * Displays the notification.
	 * 
	 * @param uris
	 */
	private void showNotification(ArrayList<Uri> uris) {

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context)
				.setOngoing(false)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(
						"Yaacc player " + (getName() == null ? "" : getName()));
		// .setContentText("Current Title");
		PendingIntent contentIntent = getNotificationIntent(uris);
		if (contentIntent != null) {
			mBuilder.setContentIntent(contentIntent);
		}
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(getNotificationId(), mBuilder.build());
	}

	/**
	 * Cancels the notification.
	 */
	private void cancleNotification() {
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.cancel(getNotificationId());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.AbstractPlayer#getNotificationIntent()
	 */
	private PendingIntent getNotificationIntent(ArrayList<Uri> uris) {
		Intent notificationIntent = new Intent(context,
				ImageViewerActivity.class);
		notificationIntent.putExtra(ImageViewerActivity.URIS, uris);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		return contentIntent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.AbstractPlayer#getNotificationId()
	 */
	private int getNotificationId() {

		return NotificationId.LOCAL_IMAGE_PLAYER.getId();
	}

	/* (non-Javadoc)
	 * @see de.yaacc.player.Player#getId()
	 */
	@Override
	public int getId() {		
		return getNotificationId();
	}
	
	

}
