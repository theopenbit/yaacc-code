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

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import de.yaacc.browser.BrowseActivity;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class de.yaacc.MainActivityTest \
 * de.yaacc.tests/android.test.InstrumentationTestRunner
 */

public class MainActivityTest extends ActivityUnitTestCase<BrowseActivity> {

	private BrowseActivity activity;
	
    public MainActivityTest() {
        super(BrowseActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
      super.setUp();
      Intent intent = new Intent(getInstrumentation().getTargetContext(),
    		  BrowseActivity.class);
      startActivity(intent, null, null);
      activity = getActivity();
    }
    
    @SmallTest
    public void testStartSettingsMenu() {
      getInstrumentation().invokeMenuActionSync(activity, 0, 0);
     
    }

    
    @Override
    protected void tearDown() throws Exception {
      
      super.tearDown();
    }

    
}
