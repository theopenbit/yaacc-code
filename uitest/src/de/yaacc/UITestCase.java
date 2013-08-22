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
package de.yaacc;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;


/**
 * UI Regressiontests for yaacc
 *  
 * @author Tobias Schöne openbit@schoenesnetz.de 
 *
 */
public class UITestCase extends UiAutomatorTestCase {

	
	  public void testOpenYaacc() throws UiObjectNotFoundException {   
	      
	      // Simulate a short press on the HOME button.
	      getUiDevice().pressHome();
	      
	      // We’re now in the home screen. Next, we want to simulate 
	      // a user bringing up the All Apps screen.
	      // If you use the uiautomatorviewer tool to capture a snapshot 
	      // of the Home screen, notice that the All Apps button’s 
	      // content-description property has the value “Apps”.  We can 
	      // use this property to create a UiSelector to find the button. 
	      UiObject allAppsButton = new UiObject(new UiSelector()
	         .description("Apps"));
	      
	      // Simulate a click to bring up the All Apps screen.
	      allAppsButton.clickAndWaitForNewWindow();
	      
	      // In the All Apps screen, the Settings app is located in 
	      // the Apps tab. To simulate the user bringing up the Apps tab,
	      // we create a UiSelector to find a tab with the text 
	      // label “Apps”.
	      UiObject appsTab = new UiObject(new UiSelector()
	         .text("Apps"));
	      
	      // Simulate a click to enter the Apps tab.
	      appsTab.click();

	      // Next, in the apps tabs, we can simulate a user swiping until
	      // they come to the Settings app icon.  Since the container view 
	      // is scrollable, we can use a UiScrollable object.
	      UiScrollable appViews = new UiScrollable(new UiSelector()
	         .scrollable(true));
	      
	      // Set the swiping mode to horizontal (the default is vertical)
	      appViews.setAsHorizontalList();
	      
	      // Create a UiSelector to find the Yaacc app and simulate      
	      // a user click to launch the app. 
	      UiObject yaaccApp = appViews.getChildByText(new UiSelector()
	         .className(android.widget.TextView.class.getName()), 
	         "YAACC");
	      yaaccApp.clickAndWaitForNewWindow();
	      
	      // Validate that the package name is the expected one
	      UiObject settingsValidation = new UiObject(new UiSelector()
	         .packageName("de.yaacc"));
	      assertTrue("Unable to detect Yaacc", 
	         settingsValidation.exists());  
	      
	  }   
	
}
