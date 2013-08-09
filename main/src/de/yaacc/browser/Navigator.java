package de.yaacc.browser;

import java.util.LinkedList;

import org.teleal.cling.model.meta.Device;

import android.util.Log;

public class Navigator {
	
	public final static String DEVICE_OVERVIEW_OBJECT_ID = "-1";
	public final static String PROVIDER_DEVICE_SELECT_LIST_OBJECT_ID = "-2";
	public final static String RECEIVER_DEVICE_SELECT_LIST_OBJECT_ID = "-3";
	public final static String ITEM_ROOT_OBJECT_ID = "0";
	private final static Position DEVICE_LIST_POSIOTION = new Position(DEVICE_OVERVIEW_OBJECT_ID, null);
	
	
	public Navigator(){		
		navigationPath = new LinkedList<Position>();
		Log.d(getClass().getName(), "pushNavigation: " + DEVICE_LIST_POSIOTION.getObjectId());
		navigationPath.add(DEVICE_LIST_POSIOTION);
	}

	private LinkedList<Position> navigationPath;
	
	public Position getCurrentPosition(){
		if (navigationPath.isEmpty()){
			return DEVICE_LIST_POSIOTION;
		}
		return navigationPath.peekLast();
	}
	
	public void pushPosition(Position pos){
		Log.d(getClass().getName(), "pushNavigation: " + pos.getObjectId());
		navigationPath.add(pos);
	}
	
	public Position popPosition(){
		Position result = null;
		if (navigationPath.isEmpty()){
			result = DEVICE_LIST_POSIOTION;
		}else{
			result = navigationPath.removeLast();
		}
		Log.d(getClass().getName(), "popNavigation: " + result.getObjectId());
		return result;
	}

}
