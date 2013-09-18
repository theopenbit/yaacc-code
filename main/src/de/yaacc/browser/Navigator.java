/*
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
package de.yaacc.browser;

import java.util.LinkedList;

import android.util.Log;

/**
 * Manages navigation path inside device and folder hierarchy.
 * @author Christoph Hähnel (eyeless)
 */
public class Navigator {
	
	public final static String DEVICE_OVERVIEW_OBJECT_ID = "-1";
	public final static String PROVIDER_DEVICE_SELECT_LIST_OBJECT_ID = "-2";
	public final static String RECEIVER_DEVICE_SELECT_LIST_OBJECT_ID = "-3";
	public final static String ITEM_ROOT_OBJECT_ID = "0";
	public final static Position DEVICE_LIST_POSIOTION = new Position(DEVICE_OVERVIEW_OBJECT_ID, null);
	
	
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
