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

/**
 * Representation of the upnp type SyncOffset.
 *
 * @author Tobias Schoene (TheOpenBit)
 */
public class SyncOffset {


    private int hour;
    private int minute;
    private int second;
    private int millis;
    private int micros;
    private int nanos;
    private boolean increase;

    public SyncOffset(boolean increase, int hour, int minute, int second, int millis, int micros, int nanos) {
        assert (hour >= 0 && hour <= 23);
        assert (minute >= 0 && minute <= 59);
        assert (second >= 0 && second <= 59);
        assert (millis >= 0 && millis <= 999);
        assert (micros >= 0 && micros <= 999);
        assert (nanos >= 0 && nanos <= 999);
        this.increase = increase;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.millis = millis;
        this.micros = micros;
        this.nanos = nanos;


    }

    public SyncOffset(String offset){
        //TODO to be implemented
    }

    public java.lang.String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(increase ? "" : "-");
        sb.append("P");
        sb.append(hour);
        sb.append(":");
        sb.append(minute);
        sb.append(":");
        sb.append(second);
        sb.append(".");
        sb.append(String.format("%03d", millis));
        sb.append(String.format("%03d", micros));
        sb.append(String.format("%03d", nanos));
        return sb.toString();
    }
/*
    duration ::= ['-']'P' time
    time::= HH ':' MM ':' SS'.' MilliS MicroS NanoS
    HH ::= 2DIGIT (* 00 - 23 *)
    MM ::= 2DIGIT (* 00 - 59 *)
    SS ::= 2DIGIT (* 00 - 59 *)
    MilliS ::= 3DIGIT
    MicroS ::= 3DIGIT
    NanoS  ::= 3DIGIT
*/
}
