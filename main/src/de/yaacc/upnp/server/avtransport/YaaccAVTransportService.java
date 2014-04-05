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
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.impl.AVTransportService;
import org.fourthline.cling.support.avtransport.impl.AVTransportStateMachine;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;
import org.seamless.statemachine.StateMachineBuilder;
import org.seamless.statemachine.TransitionException;

import java.net.URI;

import de.yaacc.upnp.UpnpClient;
import de.yaacc.upnp.model.types.SyncOffset;
import de.yaacc.upnp.server.avtransport.AvTransportMediaRendererNoMediaPresent;
import de.yaacc.upnp.server.avtransport.AvTransportStateMachine;


/**
 * @author Tobias Schöne (openbit)
 */
@org.fourthline.cling.binding.annotations.UpnpService(serviceId = @org.fourthline.cling.binding.annotations.UpnpServiceId("AVTransport"), serviceType = @org.fourthline.cling.binding.annotations.UpnpServiceType(value = "AVTransport", version = 3), stringConvertibleTypes = {org.fourthline.cling.support.lastchange.LastChange.class})
public class YaaccAVTransportService extends AVTransportService<AvTransport> {

    private final UpnpClient upnpClient;
    private AvTransport avTransport;

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
                AvTransport.class, UpnpClient.class}, new Object[]{
                createTransport(instanceId, getLastChange()),
                upnpClient});
    }

    @UpnpAction(name = "GetSyncOffset",
            out = {@UpnpOutputArgument(name = "CurrentSyncOffset", stateVariable = "SyncOffset", getterName = "toString")})
    public SyncOffset getSyncOffset(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return avTransport.getSynchronizationInfo().getOffset();
    }

    @UpnpAction(name = "SetSyncOffset")
    public void setSyncOffset(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                              @UpnpInputArgument(name = "NewSyncOffset", stateVariable = "SyncOffset") String offset) throws AVTransportException {
        avTransport.getSynchronizationInfo().setOffset(new SyncOffset(offset));
    }

    @UpnpAction(name = "AdjustSyncOffset")
    public void adjustSyncOffset(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                                 @UpnpInputArgument(name = "Adjustment", stateVariable = "SyncOffset") String offset) throws AVTransportException {
        avTransport.getSynchronizationInfo().setOffset(avTransport.getSynchronizationInfo().getOffset().add(new SyncOffset(offset)));
    }


    @UpnpAction(name = "SyncPlay")
    public void syncPlay(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                         @UpnpInputArgument(name = "Speed", stateVariable = "TransportPlaySpeed") String speed,
                         @UpnpInputArgument(name = "ReferencePositionUnits", stateVariable = "A_ARG_TYPE_SeekMode") String referencedPositionUnits,
                         @UpnpInputArgument(name = "ReferencePosition",  stateVariable = "A_ARG_TYPE_SeekTarget") String referencedPosition,
                         @UpnpInputArgument(name = "ReferencePresentationTime", stateVariable = "A_ARG_TYPE_PresentationTime") String referencedPresentationTime,
                         @UpnpInputArgument(name = "ReferenceClockId", stateVariable = "A_ARG_TYPE_ClockId") String referencedClockId) throws  AVTransportException{
        try {
            ((AvTransportStateMachine)findStateMachine(instanceId)).syncPlay(speed, referencedPositionUnits, referencedPosition, referencedPresentationTime, referencedClockId);
        } catch (TransitionException ex) {
            throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
        }
    }


    @UpnpAction(name = "SyncStop")
    public void syncStop(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                         @UpnpInputArgument(name = "StopTime", stateVariable = "A_ARG_TYPE_PresentationTime") String referencedPresentationTime,
                         @UpnpInputArgument(name = "ReferenceClockId", stateVariable = "A_ARG_TYPE_ClockId") String referencedClockId) throws  AVTransportException {
        try {
            ((AvTransportStateMachine)findStateMachine(instanceId)).syncStop(referencedPresentationTime, referencedClockId);
        } catch (TransitionException ex) {
            throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
        }
    }

    @UpnpAction(name = "SyncPause")
    public void syncPause(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                          @UpnpInputArgument(name = "PauseTime", stateVariable = "A_ARG_TYPE_PresentationTime") String referencedPresentationTime,
                          @UpnpInputArgument(name = "ReferenceClockId", stateVariable = "A_ARG_TYPE_ClockId") String referencedClockId) throws  AVTransportException {
        try {
            ((AvTransportStateMachine)findStateMachine(instanceId)).syncStop(referencedPresentationTime,referencedClockId);
        } catch (TransitionException ex) {
            throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
        }
    }


    @UpnpAction(name = "GetCurrentTransportActions", out = @UpnpOutputArgument(name = "Actions", stateVariable = "CurrentTransportActions"))
    public String getCurrentTransportActionsString(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId)
            throws AVTransportException {
        try {
            return ModelUtil.toCommaSeparatedList(getCurrentTransportActions(instanceId));
        } catch (Exception ex) {
            return ""; // TODO: Empty string is not defined in spec but seems reasonable for no available action?
        }
    }


    @Override
    public void appendCurrentState(LastChange lc, UnsignedIntegerFourBytes instanceId) throws Exception {

        MediaInfo mediaInfo = getMediaInfo(instanceId);
        TransportInfo transportInfo = getTransportInfo(instanceId);
        TransportSettings transportSettings = getTransportSettings(instanceId);
        PositionInfo positionInfo = getPositionInfo(instanceId);
        DeviceCapabilities deviceCaps = getDeviceCapabilities(instanceId);

        lc.setEventedValue(
                instanceId,
                new AVTransportVariable.AVTransportURI(URI.create(mediaInfo.getCurrentURI())),
                new AVTransportVariable.AVTransportURIMetaData(mediaInfo.getCurrentURIMetaData()),
                new AVTransportVariable.CurrentMediaDuration(mediaInfo.getMediaDuration()),
                new AVTransportVariable.CurrentPlayMode(transportSettings.getPlayMode()),
                new AVTransportVariable.CurrentRecordQualityMode(transportSettings.getRecQualityMode()),
                new AVTransportVariable.CurrentTrack(positionInfo.getTrack()),
                new AVTransportVariable.CurrentTrackDuration(positionInfo.getTrackDuration()),
                new AVTransportVariable.CurrentTrackMetaData(positionInfo.getTrackMetaData()),
                new AVTransportVariable.CurrentTrackURI(URI.create(positionInfo.getTrackURI())),
                new AvTransportVariable.CurrentTransportActions(getPossibleTransportActions(instanceId)),
                new AVTransportVariable.NextAVTransportURI(URI.create(mediaInfo.getNextURI())),
                new AVTransportVariable.NextAVTransportURIMetaData(mediaInfo.getNextURIMetaData()),
                new AVTransportVariable.NumberOfTracks(mediaInfo.getNumberOfTracks()),
                new AVTransportVariable.PossiblePlaybackStorageMedia(deviceCaps.getPlayMedia()),
                new AVTransportVariable.PossibleRecordQualityModes(deviceCaps.getRecQualityModes()),
                new AVTransportVariable.PossibleRecordStorageMedia(deviceCaps.getRecMedia()),
                new AVTransportVariable.RecordMediumWriteStatus(mediaInfo.getWriteStatus()),
                new AVTransportVariable.RecordStorageMedium(mediaInfo.getRecordMedium()),
                new AVTransportVariable.TransportPlaySpeed(transportInfo.getCurrentSpeed()),
                new AVTransportVariable.TransportState(transportInfo.getCurrentTransportState()),
                new AVTransportVariable.TransportStatus(transportInfo.getCurrentTransportStatus())
        );
    }

    protected TransportAction[] getPossibleTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception{
        AVTransportStateMachine stateMachine = findStateMachine(instanceId);
        try {
            return ((YaaccState)stateMachine.getCurrentState()).getPossibleTransportActions();
        } catch (TransitionException ex) {
            return new TransportAction[0];
        }
    }

    protected AVTransport createTransport(UnsignedIntegerFourBytes instanceId, LastChange lastChange) {
        avTransport = new AvTransport(instanceId, lastChange, StorageMedium.NETWORK);
        return avTransport;
    }

}
