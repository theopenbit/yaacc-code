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

/**
 * @author openbit
 *
 */
public enum ContentDirectoryFolder {
	PARENT_OF_ROOT("-1"),
	ROOT("0"),
	MUSIC("-10"),
	MUSIC_GENRES("-20"),
	IMAGES("-30"),
	MOVIES("-40"),
	MUSIC_ALL_TITLES("-50"),
	MUSIC_ALBUMS("-60"),
	MUSIC_ARTIST("-70");	
	
	private String id;
	
	
	ContentDirectoryFolder(String id){
		this.id=id;
	}
	
	public String getId(){
		return id;
	}
	
}
