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
import java.util.Collections;
import java.util.List;

import android.preference.PreferenceManager;
import de.yaacc.R;
import de.yaacc.upnp.UpnpClient;

/**
 * Factory for creating player instances-
 * 
 * @author Tobias Schoene (openbit)
 * 
 */
public class PlayerFactory {

	private static List<Player> currentPlayers = new ArrayList<Player>();
	
	

	/**
	 * Creates a player for the given content. Based on the configuration
	 * settings in the upnpClient the player may be a player to play on a remote
	 * device.
	 * 
	 * @param upnpClient
	 *            the upnpClient
	 * @param items
	 *            the items to be played
	 * @return the player
	 */
	public static Player createPlayer(UpnpClient upnpClient,
			List<PlayableItem> items) {
		Player result = null;
		boolean video = false;
		boolean image = false;
		boolean music = false;
		if (!upnpClient.getReceiverDeviceId().equals(UpnpClient.LOCAL_UID)) {
			result = new AVTransportPlayer(upnpClient);
		} else {
			for (PlayableItem playableItem : items) {
				image = image || playableItem.getMimeType().startsWith("image");
				video = video || playableItem.getMimeType().startsWith("video");
				music = music || playableItem.getMimeType().startsWith("audio");
			}
			if (video && !image && !music) {
				// use videoplayer
				// FIXME NOT JET IMPLEMENTED
				result = new MultiContentPlayer(upnpClient);
			} else if (!video && image && !music) {
				// use imageplayer
				result = createImagePlayer(upnpClient);
			} else if (!video && !image && music) {
				// use musicplayer
				result = createMusicPlayer(upnpClient);
			} else {
				// use multiplayer
				result = new MultiContentPlayer(upnpClient);
			}			
		}
		if (result != null) {
			currentPlayers.add(result);
			result.setItems(items.toArray(new PlayableItem[items.size()]));
		}
		return result;
	}

	private static Player createImagePlayer(UpnpClient upnpClient) {

		return new LocalImagePlayer(upnpClient);
	}

	private static Player createMusicPlayer(UpnpClient upnpClient) {
		boolean background = PreferenceManager.getDefaultSharedPreferences(
				upnpClient.getContext()).getBoolean(
				upnpClient.getContext().getString(R.string.settings_audio_app),
				false);
		if (background) {
			return new LocalBackgoundMusicPlayer(upnpClient);
		}
		return new LocalThirdPartieMusicPlayer(upnpClient);

	}

	/**
	 * returns all current players
	 * @return the currentPlayer
	 */
	public static List<Player> getCurrentPlayers() {
		return Collections.unmodifiableList(currentPlayers);
	}
	
	/**
	 * Kills the given Player
	 * @param player
	 */
	public static void kill(Player player){
		assert(player != null);
		currentPlayers.remove(player);
		player.onDestroy();
	}
	
}
