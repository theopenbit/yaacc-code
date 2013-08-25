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
package de.yaacc.upnp.server;

import java.net.URI;
import java.util.List;

import org.teleal.cling.support.avtransport.impl.state.AbstractState;
import org.teleal.cling.support.avtransport.impl.state.Playing;
import org.teleal.cling.support.avtransport.lastchange.AVTransportVariable;
import org.teleal.cling.support.model.AVTransport;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.SeekMode;

import android.util.Log;
import de.yaacc.player.Player;
import de.yaacc.upnp.UpnpClient;

public class AvTransportMediaRendererPlaying extends Playing<AVTransport> {

	private UpnpClient upnpClient;

	/**
	 * Constructor.
	 * 
	 * @param transport
	 *            the state holder
	 * @param upnpClient
	 *            the upnpclient to use
	 */
	public AvTransportMediaRendererPlaying(AVTransport transport,
			UpnpClient upnpClient) {
		super(transport);
		this.upnpClient = upnpClient;
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Playing#onEntry()
	 */
	@Override
	public void onEntry() {
		Log.d(this.getClass().getName(), "On Entry");
		super.onEntry();
		// Start playing now!		
		List<Player> players = upnpClient.initializePlayers(getTransport());
		for (Player player : players) {
			player.play();			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Playing#setTransportURI(java.net.URI, java.lang.String)
	 */
	@Override
	public Class<? extends AbstractState> setTransportURI(URI uri,
			String metaData) {
		Log.d(this.getClass().getName(), "Set TransportURI");
		getTransport().setMediaInfo(new MediaInfo(uri.toString(), metaData));		
		// If you can, you should find and set the duration of the track here!
		getTransport().setPositionInfo(
				new PositionInfo(1, metaData, uri.toString()));

		// It's up to you what "last changes" you want to announce to event
		// listeners
		getTransport().getLastChange().setEventedValue(
				getTransport().getInstanceId(),
				new AVTransportVariable.AVTransportURI(uri),
				new AVTransportVariable.CurrentTrackURI(uri));

		return AvTransportMediaRendererStopped.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Playing#stop()
	 */
	@Override
	public Class<? extends AbstractState> stop() {
		Log.d(this.getClass().getName(), "Stop");
		// Stop playing!
		return AvTransportMediaRendererStopped.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Playing#play(java.lang.String)
	 */
	@Override
	public Class<? extends AbstractState> play(String speed) {
		Log.d(this.getClass().getName(), "play");				
		return AvTransportMediaRendererPlaying.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Playing#pause()
	 */
	@Override
	public Class<? extends AbstractState> pause() {
		Log.d(this.getClass().getName(), "pause");
		return AvTransportMediaRendererPaused.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Playing#next()
	 */
	@Override
	public Class<? extends AbstractState> next() {
		Log.d(this.getClass().getName(), "next");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Playing#previous()
	 */
	@Override
	public Class<? extends AbstractState> previous() {
		Log.d(this.getClass().getName(), "previous");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Playing#seek(org.teleal.cling.support.model.SeekMode, java.lang.String)
	 */
	@Override
	public Class<? extends AbstractState> seek(SeekMode unit, String target) {
		Log.d(this.getClass().getName(), "seek");
		return null;
	}
}
