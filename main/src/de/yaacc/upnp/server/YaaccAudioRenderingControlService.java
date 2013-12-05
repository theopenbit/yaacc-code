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

import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.model.types.UnsignedIntegerTwoBytes;
import org.teleal.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.teleal.cling.support.renderingcontrol.RenderingControlException;

import android.util.Log;

import de.yaacc.upnp.UpnpClient;



/**
 * @author Tobias Schöne (openbit)
 */
public class YaaccAudioRenderingControlService extends
		AbstractAudioRenderingControl {

	
	private final UpnpClient upnpClient;

	public YaaccAudioRenderingControlService(UpnpClient upnpClient) {
		this.upnpClient = upnpClient;
	}

	@Override
	public boolean getMute(UnsignedIntegerFourBytes instanceId, String channelName)
			throws RenderingControlException {
		Log.d(getClass().getName(), "getMute() - not yet implemented");
		return false;
	}

	@Override
	public UnsignedIntegerTwoBytes getVolume(UnsignedIntegerFourBytes instanceId,
			String channelName) throws RenderingControlException {
		Log.d(getClass().getName(), "getVolume() - not yet implemented");
		return null;
	}

	@Override
	public void setMute(UnsignedIntegerFourBytes instanceId, String channelName, boolean desiredMute)
			throws RenderingControlException {
		Log.d(getClass().getName(), "setMute() - not yet implemented");

	}

	@Override
	public void setVolume(UnsignedIntegerFourBytes instanceId, String channelName,
			UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException {
		Log.d(getClass().getName(), "setVolume() - not yet implemented");

	}

}
