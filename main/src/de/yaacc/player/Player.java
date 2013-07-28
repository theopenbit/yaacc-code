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
package de.yaacc.player;


/**
 * A Player is able to play stop a couple of MediaObjects  
 * @author Tobias Schoene (openbit)  
 * 
 */
public interface Player {
	
	/**
	 * play the next item
	 */
	void next();
	
	/**
	 * play the previous item
	 */
	void previous();
	
	/**
	 * Pause the current item
	 */
	void pause();
	
	/**
	 * start playing the current item 
	 */
	void play();
	
	/**
	 * stops playing. And returns to the beginning of the item list.
	 */
	void stop();
	
	/**
	 * Set a List of Items 
	 * @param items the items to be played
	 */
	void setItems(PlayableItem... items);
	
	/**
	 * Adds an Item at the end of the player items 
	 */
	void addItem(PlayableItem item);
	
    /**
     * Drops all Items.
     */
	void clear();
	
	/**
	 * Kill the  player.
	 */
	void onDestroy();
	
	/**
	 * Set the name of the player.
	 * @param name the name
	 */
	void setName(String name);
	
	/**
	 * Get the player name.
	 * @return the name
	 */
	String getName();

	/**
	 * Exit the player.
	 */
	void exit();
	
	/**
	 * Returns the id of the Player.
	 * @return the id
	 */
	int getId();

	
}
