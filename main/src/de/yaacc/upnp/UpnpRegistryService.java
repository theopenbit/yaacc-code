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
package de.yaacc.upnp;

import org.teleal.cling.android.AndroidUpnpServiceConfiguration;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.transport.impl.apache.StreamClientConfigurationImpl;
import org.teleal.cling.transport.impl.apache.StreamClientImpl;
import org.teleal.cling.transport.spi.StreamClient;

import android.net.wifi.WifiManager;

/**
 * This is an android service to provide access to an upnp registry.
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class UpnpRegistryService extends AndroidUpnpServiceImpl {

	@Override
	protected AndroidUpnpServiceConfiguration createConfiguration(
			Object wifiManager) {
		return new AndroidUpnpServiceConfiguration(wifiManager) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.teleal.cling.android.AndroidUpnpServiceConfiguration#
			 * createStreamClient()
			 */
			@Override
			public StreamClient<?> createStreamClient() {
				return new StreamClientImpl(
						new StreamClientConfigurationImpl() {
							public int getConnectionTimeoutSeconds() {
								return 20;
							}

							public int getDataReadTimeoutSeconds() {
								return 20;
							}

							public boolean getStaleCheckingEnabled() {

								return false;
							}

							public int getRequestRetryCount() {

								return 1;
							}

						});
			}

		};
	}

}
