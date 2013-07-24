package de.yaacc.browser;

import org.teleal.cling.model.meta.Device;

public class Position {

	private String currentObjectId;
	private Device currentDevice;
	
	public Position(String objectId, Device device){
			
		this.currentDevice = device;
		this.currentObjectId = objectId;
	}
	
	public String getCurrentObjectId() {
		return currentObjectId;
	}
	public void setCurrentObjectId(String currentObjectId) {
		this.currentObjectId = currentObjectId;
	}
	public Device getCurrentDevice() {
		return currentDevice;
	}
	public void setCurrentDevice(Device currentDevice) {
		this.currentDevice = currentDevice;
	}
	
	
}
