package de.yaacc.upnp;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;

import android.widget.Toast;
import de.yaacc.UpnpBrowserActivity;



public class BrowseRegistryListener extends DefaultRegistryListener {

	private UpnpBrowserActivity upnpBrowserUi;

	public BrowseRegistryListener(UpnpBrowserActivity ui) {
		upnpBrowserUi = ui;
	}
    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        deviceAdded(device);
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
    	upnpBrowserUi.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(
                		BrowseRegistryListener.this.upnpBrowserUi,
                        "Discovery failed of '" + device.getDisplayString() + "': " +
                                (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
        deviceRemoved(device);
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        deviceAdded(device);
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        deviceRemoved(device);
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        deviceAdded(device);
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        deviceRemoved(device);
    }

    public void deviceAdded(@SuppressWarnings("rawtypes") final Device device) {
        upnpBrowserUi.runOnUiThread(new Runnable() {
            public void run() {
                DeviceDisplay d = new DeviceDisplay(device);
                int position = upnpBrowserUi.listAdapter.getPosition(d);
                if (position >= 0) {
                    // Device already in the list, re-set new value at same position
                	upnpBrowserUi.listAdapter.remove(d);
                	upnpBrowserUi.listAdapter.insert(d, position);
                } else {
                	upnpBrowserUi.listAdapter.add(d);
                }
            }
        });
    }

    public void deviceRemoved(final Device device) {
    	upnpBrowserUi.runOnUiThread(new Runnable() {
            public void run() {
            	upnpBrowserUi.listAdapter.remove(new DeviceDisplay(device));
            }
        });
    }
}

