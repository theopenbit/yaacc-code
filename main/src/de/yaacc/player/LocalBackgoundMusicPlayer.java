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

import de.yaacc.R;
import de.yaacc.musicplayer.BackgroundMusicService;
import de.yaacc.upnp.UpnpClient;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * A Player for local music playing in background
 * 
 * @author Tobias Schoene (openbit)
 * 
 */
public class LocalBackgoundMusicPlayer extends AbstractPlayer {
	private boolean background = true;
	private BackgroundMusicService musicService;

	/**
	 * @param context
	 */
	public LocalBackgoundMusicPlayer(UpnpClient upnpClient) {
		super(upnpClient);
        Context context = upnpClient.getContext();
		Log.d(getClass().getName(), "Starting background music service... ");
		Intent svc = new Intent(context, BackgroundMusicService.class);
//FIXME bindService, has a different behavior than startService  yet don't know why bind doesn't working
		context.bindService(svc, new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(getClass().getName(),
						"background music service disconnected!");
				musicService = null;

			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(getClass().getName(),
						"background music service connected!");

				musicService = (BackgroundMusicService) service;

			}
		}

		, Context.BIND_AUTO_CREATE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.yaacc.player.AbstractPlayer#stopItem(de.yaacc.player.PlayableItem)
	 */
	@Override
	protected void stopItem(PlayableItem playableItem) {

		if (musicService != null) {
			musicService.stopMusic();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.yaacc.player.AbstractPlayer#loadItem(de.yaacc.player.PlayableItem)
	 */
	@Override
	protected Object loadItem(PlayableItem playableItem) {
		Uri uri = playableItem.getUri();

		if (musicService != null) {
			musicService.setMusicUri(uri);
		}

		return uri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.yaacc.player.AbstractPlayer#startItem(de.yaacc.player.PlayableItem,
	 * java.lang.Object)
	 */
	@Override
	protected void startItem(PlayableItem playableItem, Object loadedItem) {

		if (musicService != null) {
			musicService.playMusic();
		}

	}

}
