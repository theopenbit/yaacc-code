package de.yaacc.browser;

import java.util.LinkedList;

import org.teleal.cling.model.meta.Device;

public class Navigator {
	
	private final static int POS_JUMP_TWO = 2;
	private final static String LEVEL_DEVICE_OVERVIEW = "-1";
	private final static String LEVEL_DEVICE_ROOT = "0";
	
	
	public Navigator(){
		Position pos = new Position(LEVEL_DEVICE_OVERVIEW, null);
		navigationPath = new LinkedList<Position>();
		navigationPath.add(pos);
	}

	private LinkedList<Position> navigationPath;
	
	public Position getCurrentPosition(){
		if (navigationPath.isEmpty()){
			//TODO: return default device
		}
		return navigationPath.peekLast();
	}
	
	public Position getLastPosition(){
		int listSize = navigationPath.size();
		if(listSize < 2 || navigationPath.isEmpty()){
			//TODO: Error handling
		}
		return navigationPath.get(listSize - POS_JUMP_TWO);
	}
	
	public void moveTo(String objectId){
		//TODO: Load the sender device
		Device dev = null;
		this.moveTo(dev,objectId);
	}
	
	private void moveTo(Device dev, String objectId){
		Position newPos = new Position(objectId, dev);
		//TODO: Check whether we already were at this position but somebody didn't use the method moveToLastPosition
		navigationPath.add(newPos);
		//TODO: Navigation stuff
	}
	
	public void moveToLastPosition(){
		int listSize = navigationPath.size();
		if(listSize < 2 || navigationPath.isEmpty()){
			//TODO: Error handling
		}
		Position last = navigationPath.get(listSize - POS_JUMP_TWO);
		this.moveTo(last.getCurrentDevice(), last.getCurrentObjectId());
	}

	public void addNewPosition(Position pos){
		navigationPath.add(pos);
	}
	
	public void addNewPositionOnSameDevice(String objectId){
		Position pos = new Position(objectId, navigationPath.peekLast().getCurrentDevice());
		navigationPath.add(pos);
	}
	
	public void addNewPosition(Device device, String objectId){
		navigationPath.add(new Position(objectId, device));
	}
	
	public boolean isLevelDeviceOverview(){
		if (LEVEL_DEVICE_OVERVIEW.equals(getCurrentPosition().getCurrentObjectId())){
			return true;
		}
		return false;
	}
	
	public boolean isLevelDeviceRoot(){
		if (LEVEL_DEVICE_ROOT.equals(getCurrentPosition().getCurrentObjectId())){
			return true;
		}
		return false;
	}
	

}
