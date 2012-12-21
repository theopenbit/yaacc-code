/**
 *
 * Copyright (C) 2012 Tobias Schoene www.schoenesnetz.de kontakt@schoenesnetz.de
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

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceConfiguration;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.registry.Registry;

import android.net.wifi.WifiManager;

/*
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 3
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/**
 * This is an android service to provide access to an upnp registry. 
 * @author Tobias Schöne (openbit)  
 * 
 */
public class UpnpRegistryService extends AndroidUpnpServiceImpl{

	public Registry getRegistry(){
		return upnpService.getRegistry();
	}
	
	public UpnpService getUpnpService(){
		return upnpService;
	}
	
//FIXME Example for further use
//	@Override
//    protected AndroidUpnpServiceConfiguration createConfiguration(WifiManager wifiManager) {
//        return new AndroidUpnpServiceConfiguration(wifiManager) {
//
//            @Override
//            public int getRegistryMaintenanceIntervalMillis() {
//            	
//                return 7000;
//            }
//
//        };
//    }


}
