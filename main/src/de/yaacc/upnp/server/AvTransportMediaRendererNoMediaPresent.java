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

import org.teleal.cling.support.avtransport.impl.state.AbstractState;
import org.teleal.cling.support.avtransport.impl.state.NoMediaPresent;
import org.teleal.cling.support.avtransport.lastchange.AVTransportVariable;
import org.teleal.cling.support.model.AVTransport;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;

import android.util.Log;
import de.yaacc.upnp.UpnpClient;

/**
 * @author Tobias Schöne (openbit)
 * 
 */
public class AvTransportMediaRendererNoMediaPresent extends
		NoMediaPresent<AVTransport> {

	private UpnpClient upnpClient;

	/**
	 * Constructor.
	 * 
	 * @param transport
	 *            the state holder
	 * @param upnpClient
	 *            the upnpClient to use
	 */
	public AvTransportMediaRendererNoMediaPresent(AVTransport transport,
			UpnpClient upnpClient) {
		super(transport);
		this.upnpClient = upnpClient;
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.NoMediaPresent#setTransportURI(java.net.URI, java.lang.String)
	 */
	@Override
	public Class<? extends AbstractState> setTransportURI(URI uri,
			String metaData) {
		Log.d(this.getClass().getName(), "set Transport: " + uri + " metaData: " + metaData);
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
}
