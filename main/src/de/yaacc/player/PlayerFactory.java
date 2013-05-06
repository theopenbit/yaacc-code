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

import java.util.List;

import de.yaacc.R;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Factory for creating player instances-
 * @author Tobias Schoene (openbit)  
 * 
 */
public class PlayerFactory {
	
	public static Player createPlayer(Context context, List<PlayableItem> items){
		Player result = null;
		boolean video = false;
		boolean image = false;
		boolean music = false;
		for (PlayableItem playableItem : items) {
			image = image || playableItem.getMimeType().startsWith("image");
			video = video || playableItem.getMimeType().startsWith("video");
			music = music || playableItem.getMimeType().startsWith("audio");			
		}
		if(video && ! image && ! music){
			//use videoplayer
			//FIXME NOT JET IMPLEMENTED			
			result = new MultiContentPlayer(context);
		}else if (!video &&  image && ! music) {
			//use imageplayer
			result = createImagePlayer(context);
		}else if (!video &&  !image &&  music) {
			//use musicplayer
			result = createMusicPlayer(context);
		} else {
			//use multiplayer
			result = new MultiContentPlayer(context);
		}
		if(result != null){
			result.setItems(items.toArray(new PlayableItem[items.size()]));
		}
		return result;
	}

	private static Player createImagePlayer(Context context) {
		
		return new LocalImagePlayer(context);
	}

	private static Player createMusicPlayer(Context context) {
		boolean background = PreferenceManager
				.getDefaultSharedPreferences(context).getBoolean(
				context.getString(R.string.settings_audio_app), true);
		if (background) {
			return new LocalBackgoundMusicPlayer(context);
		}
		return new LocalThirdPartieMusicPlayer(context);
		
	}
	
	

}
