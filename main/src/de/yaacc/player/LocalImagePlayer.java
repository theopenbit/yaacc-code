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
import java.util.List;

import de.yaacc.imageviewer.ImageViewerActivity;
import de.yaacc.imageviewer.ImageViewerBroadcastReceiver;
import de.yaacc.upnp.UpnpClient;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Player for local image viewing activity
 * @author Tobias Schoene (openbit)  
 * 
 */
public class LocalImagePlayer implements Player {

	private Context context;

	/**
	 * @param context
	 */
	public LocalImagePlayer(UpnpClient upnpClient) {
		this.context = upnpClient.getContext();
	}
	
	
	/* (non-Javadoc)
	 * @see de.yaacc.player.Player#next()
	 */
	@Override
	public void next() {
		Intent intent = new Intent();
		 intent.setAction(ImageViewerBroadcastReceiver.ACTION_NEXT);
		 context.sendBroadcast(intent);

	}

	/* (non-Javadoc)
	 * @see de.yaacc.player.Player#previous()
	 */
	@Override
	public void previous() {
		Intent intent = new Intent();
		 intent.setAction(ImageViewerBroadcastReceiver.ACTION_PREVIOUS);
		 context.sendBroadcast(intent);
	}

	/* (non-Javadoc)
	 * @see de.yaacc.player.Player#pause()
	 */
	@Override
	public void pause() {
		Intent intent = new Intent();
		intent.setAction(ImageViewerBroadcastReceiver.ACTION_PAUSE);
		context.sendBroadcast(intent);

	}

	/* (non-Javadoc)
	 * @see de.yaacc.player.Player#play()
	 */
	@Override
	public void play() {
		 Intent intent = new Intent();
		 intent.setAction(ImageViewerBroadcastReceiver.ACTION_PLAY);
		 context.sendBroadcast(intent);
	
	}

	/* (non-Javadoc)
	 * @see de.yaacc.player.Player#stop()
	 */
	@Override
	public void stop() {
		 Intent intent = new Intent();
		 intent.setAction(ImageViewerBroadcastReceiver.ACTION_STOP);
		 context.sendBroadcast(intent);
		

	}

	/* (non-Javadoc)
	 * @see de.yaacc.player.Player#setItems(de.yaacc.player.PlayableItem[])
	 */
	@Override
	public void setItems(PlayableItem... items) {
		Intent intent = new Intent(context, ImageViewerActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	
		ArrayList<Uri> uris = new ArrayList<Uri>();
		for (int i = 0; i <items.length; i++) {
			uris.add(items[i].getUri());
		}
		intent.putExtra(ImageViewerActivity.URIS, uris);
		context.startActivity(intent);

	}

	/* (non-Javadoc)
	 * @see de.yaacc.player.Player#addItem(de.yaacc.player.PlayableItem)
	 */
	@Override
	public void addItem(PlayableItem item) {
		// FIXME not yet implemented

	}

	/* (non-Javadoc)
	 * @see de.yaacc.player.Player#clear()
	 */
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

}
