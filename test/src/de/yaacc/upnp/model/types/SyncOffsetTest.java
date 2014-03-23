package de.yaacc.upnp.model.types;/*
* Copyright (C) 2014 www.yaacc.de
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
*/

import android.test.AndroidTestCase;

import junit.framework.TestCase;

/**
 * @author Tobias Schoene (TheOpenBit)
 */
public class SyncOffsetTest extends AndroidTestCase {

    public void  testOffsetFormat(){
        SyncOffset syncOffset = new SyncOffset(true, 0, 0, 0, 0, 0, 0);
        assertEquals("P00:00:00.000 000 000", syncOffset.toString());
        assertEquals(syncOffset, new SyncOffset(syncOffset.toString()));
        syncOffset = new SyncOffset(true, 10, 0, 0, 0, 0, 0);
        assertEquals("P10:00:00.000 000 000", syncOffset.toString());
        assertEquals(syncOffset, new SyncOffset(syncOffset.toString()));
        syncOffset = new SyncOffset(false, 10, 0, 0, 0, 0, 0);
        assertEquals("-P10:00:00.000 000 000", syncOffset.toString());
        assertEquals(syncOffset, new SyncOffset(syncOffset.toString()));
        syncOffset = new SyncOffset(true, 0, 21, 0, 0, 0, 0);
        assertEquals("P00:21:00.000 000 000", syncOffset.toString());
        assertEquals(syncOffset, new SyncOffset(syncOffset.toString()));
        syncOffset = new SyncOffset(true, 0, 0, 45, 0, 0, 0);
        assertEquals("P00:00:45.000 000 000", syncOffset.toString());
        assertEquals(syncOffset, new SyncOffset(syncOffset.toString()));
        syncOffset = new SyncOffset(true, 0, 0, 0, 600, 0, 0);
        assertEquals("P00:00:00.600 000 000", syncOffset.toString());
        assertEquals(syncOffset, new SyncOffset(syncOffset.toString()));
        syncOffset = new SyncOffset(true, 0, 0, 0, 0, 600, 0);
        assertEquals("P00:00:00.000 600 000", syncOffset.toString());
        assertEquals(syncOffset, new SyncOffset(syncOffset.toString()));
        syncOffset = new SyncOffset(true, 0, 0, 0, 0, 0, 800);
        assertEquals("P00:00:00.000 000 800", syncOffset.toString());
        assertEquals(syncOffset, new SyncOffset(syncOffset.toString()));
    }

    public void  testOffsetWrongFormat(){
        //Parsingerror mus produce zero offset
        try{
            new SyncOffset(true, 100, 0, 0, 0, 0, 0);
           fail("No exception thrown");
        }catch(IllegalArgumentException ex){
            //expected
        }
        SyncOffset syncOffset = new SyncOffset("P100:00:00.000 000 000");
        assertEquals("P00:00:00.000 000 000", syncOffset.toString());
        try{
            new SyncOffset(true, 0, 1000, 0, 0, 0, 0);
            fail("No exception thrown");
        }catch(IllegalArgumentException ex){
            //expected
        }
        syncOffset = new SyncOffset("P00:1000:00.000 000 000");
        assertEquals("P00:00:00.000 000 000", syncOffset.toString());
        try{
            new SyncOffset(false, 0, 0, 1000, 0,  0, 0);
            fail("No exception thrown");
        }catch(IllegalArgumentException ex){
            //expected
        }
        syncOffset = new SyncOffset("P00:00:1000.000 000 000");
        assertEquals("P00:00:00.000 000 000", syncOffset.toString());


        try{
            new SyncOffset(true, 0, 0, 0, 10000, 0, 0);
            fail("No exception thrown");
        }catch(IllegalArgumentException ex){
            //expected
        }
        syncOffset = new SyncOffset("P00:00:00.10000 000 000");
        assertEquals("P00:00:00.000 000 000", syncOffset.toString());

        try{
            new SyncOffset(true, 0, 0, 0, 0, 10000, 0);
            fail("No exception thrown");
        }catch(IllegalArgumentException ex){
            //expected
        }
        syncOffset = new SyncOffset("P00:00:00.000 10000 000");
        assertEquals("P00:00:00.000 000 000", syncOffset.toString());

        try{
            new SyncOffset(true, 0, 0, 0, 0, 0, 10000);
            fail("No exception thrown");
        }catch(IllegalArgumentException ex){
            //expected
        }
        syncOffset = new SyncOffset("P00:00:00.000 000 10000");
        assertEquals("P00:00:00.000 000 000", syncOffset.toString());

        try{
            new SyncOffset(true, -100, 0, 0, 0, 0, 0);
            fail("No exception thrown");
        }catch(IllegalArgumentException ex){
            //expected
        }
        syncOffset = new SyncOffset("P-100:00:00.000 000 000");
        assertEquals("P00:00:00.000 000 000", syncOffset.toString());
        try{
            new SyncOffset(true, 0, -1000, 0, 0, 0, 0);
            fail("No exception thrown");
        }catch(IllegalArgumentException ex){
            //expected
        }
        syncOffset = new SyncOffset("P00:-1000:00.000 000 000");
        assertEquals("P00:00:00.000 000 000", syncOffset.toString());
        try{
            new SyncOffset(false, 0, 0, -1000, 0,  0, 0);
            fail("No exception thrown");
        }catch(IllegalArgumentException ex){
            //expected
        }
        syncOffset = new SyncOffset("P00:00:-1000.000 000 000");
        assertEquals("P00:00:00.000 000 000", syncOffset.toString());

        try{
            new SyncOffset(true, 0, 0, 0, -10000, 0, 0);
            fail("No exception thrown");
        }catch(IllegalArgumentException ex){
            //expected
        }
        syncOffset = new SyncOffset("P00:00:00.-10000 000 000");
        assertEquals("P00:00:00.000 000 000", syncOffset.toString());

        try{
            new SyncOffset(true, 0, 0, 0, 0, -10000, 0);
            fail("No exception thrown");
        }catch(IllegalArgumentException ex){
            //expected
        }
        syncOffset = new SyncOffset("P00:00:00.000 -10000 000");
        assertEquals("P00:00:00.000 000 000", syncOffset.toString());

        try{
            new SyncOffset(true, 0, 0, 0, 0, 0, -10000);
            fail("No exception thrown");
        }catch(IllegalArgumentException ex){
            //expected
        }
        syncOffset = new SyncOffset("P00:00:00.000 000 -10000");
        assertEquals("P00:00:00.000 000 000", syncOffset.toString());

    }


    public void  testAdjustOffset(){
        SyncOffset syncOffset = new SyncOffset("P10:00:00.000 000 000");
        syncOffset = syncOffset.add(new SyncOffset("P10:00:00.000 999 000"));
        assertEquals("P20:00:00.000 999 000", syncOffset.toString());
        syncOffset = new SyncOffset("P10:00:00.000 000 000");
        syncOffset = syncOffset.add(new SyncOffset("P10:00:00.000 999 999"));
        assertEquals("P20:00:00.000 999 999", syncOffset.toString());
        syncOffset = new SyncOffset("P10:00:00.000 001 001");
        syncOffset = syncOffset.add(new SyncOffset("P10:00:00.000 999 999"));
        assertEquals("P20:00:00.001 001 000", syncOffset.toString());
        syncOffset = new SyncOffset("P10:00:00.999 000 001");
        syncOffset = syncOffset.add(new SyncOffset("P10:00:00.000 999 999"));
        assertEquals("P20:00:01.000 000 000", syncOffset.toString());
        syncOffset = new SyncOffset("P10:00:00.999 000 001");
        syncOffset = syncOffset.add(new SyncOffset("P10:59:59.000 999 999"));
        assertEquals("P21:00:00.000 000 000", syncOffset.toString());

        syncOffset = new SyncOffset("P20:00:00.000 999 000");
        syncOffset = syncOffset.add(new SyncOffset("-P10:00:00.000 999 000"));
        assertEquals("P10:00:00.000 000 000", syncOffset.toString());
        syncOffset = new SyncOffset("P20:00:00.000 999 999");
        syncOffset = syncOffset.add(new SyncOffset("-P10:00:00.000 999 999"));
        assertEquals("P10:00:00.000 000 000", syncOffset.toString());
        syncOffset = new SyncOffset("P20:00:00.001 001 000");
        syncOffset = syncOffset.add(new SyncOffset("-P10:00:00.000 999 999"));
        assertEquals("P10:00:00.000 001 001", syncOffset.toString());
        syncOffset = new SyncOffset("P20:00:01.000 000 000");
        syncOffset = syncOffset.add(new SyncOffset("-P10:00:00.000 999 999"));
        assertEquals("P10:00:00.999 000 001", syncOffset.toString());
        syncOffset = new SyncOffset("P21:00:00.000 000 000");
        syncOffset = syncOffset.add(new SyncOffset("-P10:59:59.000 999 999"));
        assertEquals("P10:00:00.999 000 001", syncOffset.toString());
    }



}
