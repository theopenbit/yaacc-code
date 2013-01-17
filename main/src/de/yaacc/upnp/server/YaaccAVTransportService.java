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

import java.beans.PropertyChangeSupport;

import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.AVTransportException;
import org.teleal.cling.support.avtransport.AbstractAVTransportService;
import org.teleal.cling.support.avtransport.impl.AVTransportService;
import org.teleal.cling.support.avtransport.impl.AVTransportStateMachine;
import org.teleal.cling.support.igd.callback.GetStatusInfo;
import org.teleal.cling.support.lastchange.LastChange;
import org.teleal.cling.support.model.AVTransport;
import org.teleal.cling.support.model.DeviceCapabilities;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.support.model.TransportSettings;
import org.teleal.common.statemachine.StateMachineBuilder;

import de.yaacc.upnp.UpnpClient;


/**
 * @author Tobias Schöne (openbit)
 * 
 */
public class YaaccAVTransportService extends AVTransportService<AVTransport> {

	private final UpnpClient upnpClient;

	/**
	 * 
	 */
	public YaaccAVTransportService(UpnpClient upnpClient) {
		super(AvTransportStateMachine.class,
				AvTransportMediaRendererNoMediaPresent.class);
		this.upnpClient = upnpClient;
	}

	/**
	 * Create a 
	 */
	protected AVTransportStateMachine createStateMachine(
			UnsignedIntegerFourBytes instanceId) {
		return (AVTransportStateMachine) StateMachineBuilder.build(
				AvTransportStateMachine.class,
				AvTransportMediaRendererNoMediaPresent.class, new Class[] {
						AVTransport.class, UpnpClient.class }, new Object[] {
						createTransport(instanceId, getLastChange()),
						upnpClient });
	}

}
