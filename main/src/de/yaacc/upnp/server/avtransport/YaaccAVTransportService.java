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
package de.yaacc.upnp.server.avtransport;

import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpOutputArgument;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.impl.AVTransportService;
import org.fourthline.cling.support.avtransport.impl.AVTransportStateMachine;
import org.fourthline.cling.support.model.AVTransport;
import org.seamless.statemachine.StateMachineBuilder;

import de.yaacc.upnp.UpnpClient;
import de.yaacc.upnp.model.types.SyncOffset;
import de.yaacc.upnp.server.avtransport.AvTransportMediaRendererNoMediaPresent;
import de.yaacc.upnp.server.avtransport.AvTransportStateMachine;


/**
 * @author Tobias Schöne (openbit)
 */
@org.fourthline.cling.binding.annotations.UpnpService(serviceId = @org.fourthline.cling.binding.annotations.UpnpServiceId("AVTransport"), serviceType = @org.fourthline.cling.binding.annotations.UpnpServiceType(value = "AVTransport", version = 3), stringConvertibleTypes = {org.fourthline.cling.support.lastchange.LastChange.class})
public class YaaccAVTransportService extends AVTransportService<AVTransport> {

    private final UpnpClient upnpClient;

    /**
     *
     */
    public YaaccAVTransportService(UpnpClient upnpClient) {
        super(AvTransportStateMachine.class,
                AvTransportMediaRendererNoMediaPresent.class);
        this.upnpClient = upnpClient;
    }

    /**
     * Create a
     */
    protected AVTransportStateMachine createStateMachine(
            UnsignedIntegerFourBytes instanceId) {
        return (AVTransportStateMachine) StateMachineBuilder.build(
                AvTransportStateMachine.class,
                AvTransportMediaRendererNoMediaPresent.class, new Class[]{
                AVTransport.class, UpnpClient.class}, new Object[]{
                createTransport(instanceId, getLastChange()),
                upnpClient});
    }

    @UpnpAction(name = "GetSyncOffset",
            out = {@UpnpOutputArgument(name = "CurrentSyncOffset", stateVariable = "SyncOffset", getterName = "toString")})
    public SyncOffset getSyncOffset(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return upnpClient.getOffset();
    }

    @UpnpAction(name = "SetSyncOffset")
    public void setSyncOffset(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                              @UpnpInputArgument(name = "NewSyncOffset", stateVariable = "SyncOffset") String offset) throws AVTransportException {
        upnpClient.setOffset(new SyncOffset(offset));
    }

    @UpnpAction(name = "AdjustSyncOffset")
    public void adjustSyncOffset(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                              @UpnpInputArgument(name = "Adjustment", stateVariable = "SyncOffset") String offset) throws AVTransportException {
        upnpClient.setOffset(upnpClient.getOffset().add(new SyncOffset(offset)));
    }


    @UpnpAction(name = "SyncPlay")
    public void syncPlay(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                                 @UpnpInputArgument(name = "Speed", stateVariable = "TransportPlaySpeed") String speed,
                                 @UpnpInputArgument(name = "ReferencePositionUnits", stateVariable = "A_ARG_TYPE_SeekMode") String referencedPositionUnits,
                                 @UpnpInputArgument(name = "ReferencePosition",  stateVariable = "A_ARG_TYPE_SeekTarget") String referencedPosition,
                                 @UpnpInputArgument(name = "ReferencePresentationTime", stateVariable = "A_ARG_TYPE_PresentationTime") String referencedPresentationTime,
                                 @UpnpInputArgument(name = "ReferenceClockId", stateVariable = "A_ARG_TYPE_ClockId") String referencedClockId){
            //TODO Fill me
    }


    @UpnpAction(name = "SyncStop")
    public void syncStop(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                         @UpnpInputArgument(name = "StopTime", stateVariable = "A_ARG_TYPE_PresentationTime") String referencedPresentationTime,
                         @UpnpInputArgument(name = "ReferenceClockId", stateVariable = "A_ARG_TYPE_ClockId") String referencedClockId){
            //TODO Fill me
    }

    @UpnpAction(name = "SyncPause")
    public void syncPause(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                         @UpnpInputArgument(name = "PauseTime", stateVariable = "A_ARG_TYPE_PresentationTime") String referencedPresentationTime,
                         @UpnpInputArgument(name = "ReferenceClockId", stateVariable = "A_ARG_TYPE_ClockId") String referencedClockId){
        //TODO Fill me
    }


}
