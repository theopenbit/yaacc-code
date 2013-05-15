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
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import de.yaacc.R;
import de.yaacc.imageviewer.ImageViewerActivity;
import de.yaacc.upnp.UpnpClient;

/**
 * @author Tobias Schoene (openbit)
 * 
 */
public abstract class AbstractPlayer implements Player {

	private List<PlayableItem> items = new ArrayList<PlayableItem>();
	private int currentIndex = 0;
	private Timer playerTimer;
	private boolean isPlaying = false;
	private boolean isProcessingCommand = false;

	private UpnpClient upnpClient;

	/**
	 * @param context
	 */
	public AbstractPlayer(UpnpClient upnpClient) {
		super();
		this.upnpClient = upnpClient;
	}

	/**
	 * @return the context
	 */
	public Context getContext() {
		return upnpClient.getContext();
	}

	/**
	 * @return the upnpClient
	 */
	public UpnpClient getUpnpClient() {
		return upnpClient;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#next()
	 */
	@Override
	public void next() {
		if (isProcessingCommand)
			return;
		isProcessingCommand = true;
		cancleTimer();
		currentIndex++;
		if (currentIndex > items.size() - 1) {
			currentIndex = 0;
		}
		Context context = getUpnpClient().getContext();
		if (context instanceof Activity) {
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					Toast toast = Toast.makeText(getContext(), getContext()
							.getResources().getString(R.string.next)
							+ getPositionString(), Toast.LENGTH_SHORT);

					toast.show();
				}
			});
		}
		loadItem();
		isProcessingCommand = false;
	}

	//

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#previous()
	 */
	@Override
	public void previous() {
		if (isProcessingCommand)
			return;
		isProcessingCommand = true;
		cancleTimer();
		currentIndex--;
		if (currentIndex < 0) {
			if (items.size() > 0) {
				currentIndex = items.size() - 1;
			} else {
				currentIndex = 0;
			}
		}
		Context context = getUpnpClient().getContext();
		if (context instanceof Activity) {
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					Toast toast = Toast.makeText(getContext(), getContext()
							.getResources().getString(R.string.previous)
							+ getPositionString(), Toast.LENGTH_SHORT);
					toast.show();
				}
			});
		}
		loadItem();
		isProcessingCommand = false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#pause()
	 */
	@Override
	public void pause() {
		if (isProcessingCommand)
			return;
		isProcessingCommand = true;
		cancleTimer();
		Context context = getUpnpClient().getContext();
		if (context instanceof Activity) {
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					Toast toast = Toast.makeText(getContext(), getContext()
							.getResources().getString(R.string.pause)
							+ getPositionString(), Toast.LENGTH_SHORT);
					toast.show();
				}
			});
		}
		isPlaying = false;
		isProcessingCommand = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#start()
	 */
	@Override
	public void play() {
		if (isProcessingCommand)
			return;
		isProcessingCommand = true;
		if (currentIndex < items.size()) {
			Context context = getUpnpClient().getContext();
			if (context instanceof Activity) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						Toast toast = Toast.makeText(getContext(), getContext()
								.getResources().getString(R.string.play)
								+ getPositionString(), Toast.LENGTH_SHORT);
						toast.show();
					}
				});
			}
			// Start the pictureShow
			isPlaying = true;
			loadItem();
			isProcessingCommand = false;

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#stop()
	 */
	@Override
	public void stop() {
		if (isProcessingCommand)
			return;
		isProcessingCommand = true;
		cancleTimer();
		currentIndex = 0;
		Context context = getUpnpClient().getContext();
		if (context instanceof Activity) {
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					Toast toast = Toast.makeText(getContext(), getContext()
							.getResources().getString(R.string.stop)
							+ getPositionString(), Toast.LENGTH_SHORT);
					toast.show();
				}
			});
		}
		stopItem(items.get(currentIndex));
		isPlaying = false;
		isProcessingCommand = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#setItems(de.yaacc.player.PlayableItem[])
	 */
	@Override
	public void setItems(PlayableItem... playableItems) {
		items.addAll(Arrays.asList(playableItems));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#addItem(de.yaacc.player.PlayableItem)
	 */
	@Override
	public void addItem(PlayableItem playableItems) {
		items.add(playableItems);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.Player#clear()
	 */
	@Override
	public void clear() {
		items.clear();
	}

	private void cancleTimer() {
		if (playerTimer != null) {
			playerTimer.cancel();
		}
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	private String getPositionString() {
		return " (" + (currentIndex + 1) + "/" + items.size() + ")";
	}

	private void loadItem() {

		PlayableItem playableItem = items.get(currentIndex);
		Object loadedItem = loadItem(playableItem);
		startItem(playableItem, loadedItem);
		if (isPlaying()) {
			startTimer(playableItem.getDuration() + getSilenceDuration());
		}
	}

	/**
	 * returns the duration between two items
	 * 
	 * @return dureaton in millis
	 */
	protected long getSilenceDuration() {
		return 2000L; // Default 2 sec.
	}

	/**
	 * Start a timer for the next item change
	 * 
	 * @param duration
	 *            in millis
	 */
	public void startTimer(final long duration) {

		playerTimer = new Timer();
		playerTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				Log.d(getClass().getName(), "TimerEvent" + this);
				AbstractPlayer.this.next();

			}
		}, duration);

	}

	protected abstract void stopItem(PlayableItem playableItem);

	protected abstract Object loadItem(PlayableItem playableItem);

	protected abstract void startItem(PlayableItem playableItem,
			Object loadedItem);

}
