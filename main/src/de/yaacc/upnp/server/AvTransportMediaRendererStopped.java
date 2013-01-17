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
import org.teleal.cling.support.avtransport.impl.state.Stopped;
import org.teleal.cling.support.model.AVTransport;
import org.teleal.cling.support.model.SeekMode;

import android.util.Log;

import de.yaacc.upnp.UpnpClient;

/**
 * @author Tobias Schöne (openbit)
 * 
 */
public class AvTransportMediaRendererStopped extends Stopped<AVTransport> {

	private UpnpClient upnpClient;

	/**
	 * Constructor.
	 * @param transport the state holder
	 * @param upnpClient the upnpclient to use
	 */
	public AvTransportMediaRendererStopped(AVTransport transport, UpnpClient upnpClient) {
		super(transport);
		this.upnpClient = upnpClient;
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Stopped#onEntry()
	 */
	@Override
	public void onEntry() {
		Log.d(this.getClass().getName(), "On Entry");
		super.onEntry();
		// Optional: Stop playing, release resources, etc.
	}

	

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Stopped#setTransportURI(java.net.URI, java.lang.String)
	 */
	@Override
   public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
		Log.d(this.getClass().getName(), "setTransportURI");
		         // This operation can be triggered in any state, you should think
		         // about how you'd want your player to react. If we are in Stopped
		         // state nothing much will happen, except that you have to set
		         // the media and position info, just like in MyRendererNoMediaPresent.
		         // However, if this would be the MyRendererPlaying state, would you
		         // prefer stopping first?
		         return AvTransportMediaRendererStopped.class;
		     }

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Stopped#stop()
	 */
	@Override
	public Class<? extends AbstractState> stop() {
		Log.d(this.getClass().getName(), "stop");
		// / Same here, if you are stopped already and someone calls STOP,
		// well...
		return AvTransportMediaRendererStopped.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Stopped#play(java.lang.String)
	 */
	@Override
	public Class<? extends AbstractState> play(String speed) {
		Log.d(this.getClass().getName(), "play");
		// It's easier to let this classes' onEntry() method do the work
		return AvTransportMediaRendererStopped.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Stopped#next()
	 */
	@Override
	public Class<? extends AbstractState> next() {
		Log.d(this.getClass().getName(), "next");
		return AvTransportMediaRendererStopped.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Stopped#previous()
	 */
	@Override
	public Class<? extends AbstractState> previous() {
		Log.d(this.getClass().getName(), "previous");
		return AvTransportMediaRendererStopped.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.teleal.cling.support.avtransport.impl.state.Stopped#seek(org.teleal.cling.support.model.SeekMode, java.lang.String)
	 */
	@Override
	public Class<? extends AbstractState> seek(SeekMode unit, String target) {
		Log.d(this.getClass().getName(), "seek");
		// Implement seeking with the stream in stopped state!
		return AvTransportMediaRendererStopped.class;
	}

}
