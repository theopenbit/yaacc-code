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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.Item;

import android.net.Uri;
import android.util.Log;

/**
 * representation of an item which is to be played
 * @author Tobias Schoene (openbit)  
 * 
 */
public class PlayableItem {

	private String mimeType;
	private String title;
	private Uri uri;
	private long duration;	
	private Item item;
	
	
	public PlayableItem(Item item, int defaultDuration){
		this.item =item;
		setTitle(item.getTitle());		
		Res resource = item.getFirstResource();
		if (resource != null) {
			setUri(Uri.parse(resource.getValue()));
			setMimeType(resource.getProtocolInfo().getContentFormat());
			
			// calculate duration
			SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
			long millis = defaultDuration;
			if (resource.getDuration() != null) {
				try {
					Date date = dateFormat.parse(resource.getDuration());
					millis = (date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds()) * 1000;
				} catch (ParseException e) {
					Log.d(getClass().getName(), "bad duration format", e);
				}
			}
			setDuration(millis);			
		}
	}
	
	/**
	 * 
	 */
	public PlayableItem() {
		mimeType="";
		title="";
		uri=null;
		duration=0;		
		item = null;
	}


	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}


	/**
	 * @param mimeType the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}


	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}


	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}


	/**
	 * @return the uri
	 */
	public Uri getUri() {
		return uri;
	}


	/**
	 * @param uri the uri to set
	 */
	public void setUri(Uri uri) {
		this.uri = uri;
	}


	/**
	 * Duration in milliseconds.
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}


	/**
	 * Duration in milliseconds.
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}


	
	public Item getItem(){
		return item;
	      
	}
}
