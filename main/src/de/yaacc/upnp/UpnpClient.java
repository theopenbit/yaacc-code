package de.yaacc.upnp;

import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.UpnpService;
import org.teleal.cling.model.message.header.STAllHeader;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;

public class UpnpClient {

	 // UPnP discovery is asynchronous, we need a callback
    RegistryListener listener = new RegistryListener() {
				

        public void remoteDeviceDiscoveryStarted(Registry registry,
                                                 RemoteDevice device) {
            System.out.println(
                    "Discovery started: " + device.getDisplayString()
            );
        }

        public void remoteDeviceDiscoveryFailed(Registry registry,
                                                RemoteDevice device,
                                                Exception ex) {
            System.out.println(
                    "Discovery failed: " + device.getDisplayString() + " => " + ex
            );
        }

        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            System.out.println(
                    "Remote device available: " + device.getDisplayString()
            );
        }

        public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
            System.out.println(
                    "Remote device updated: " + device.getDisplayString()
            );
        }

        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            System.out.println(
                    "Remote device removed: " + device.getDisplayString()
            );
        }

        public void localDeviceAdded(Registry registry, LocalDevice device) {
            System.out.println(
                    "Local device added: " + device.getDisplayString()
            );
        }

        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            System.out.println(
                    "Local device removed: " + device.getDisplayString()
            );
        }

        public void beforeShutdown(Registry registry) {
            System.out.println(
                    "Before shutdown, the registry has devices: "
                    + registry.getDevices().size()
            );
        }

        public void afterShutdown() {
            System.out.println("Shutdown of registry complete!");

        }
    };
	private UpnpService upnpService;
	
	
    
    public UpnpClient(){

    }
    
    public void start() {
        // This will create necessary network resources for UPnP right away
        System.out.println("Starting Cling...");
        upnpService = new UpnpServiceImpl(listener);

        // Send a search message to all devices and services, they should respond soon
        upnpService.getControlPoint().search(new STAllHeader());
        
    }
    
    public void stop(){
    	// Release all resources and advertise BYEBYE to other UPnP devices
        System.out.println("Stopping Cling...");
        upnpService.shutdown();
    }


}
