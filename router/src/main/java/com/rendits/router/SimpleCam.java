/* Copyright 2016 Albin Severinson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rendits.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;
import net.gcdc.camdenm.CoopIts.*;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.ProtocolVersion;
import java.lang.IllegalArgumentException;
import net.gcdc.asn1.datatypes.IntRange;

public class SimpleCam{
    private final static Logger logger = LoggerFactory.getLogger(Router.class);
    private final int SIMPLE_CAM_LENGTH = 82;

    byte messageID = MessageId.cam;
    int stationID;
    int genDeltaTimeMillis;
    byte containerMask;
    int stationType;
    int latitude;
    int longitude;
    int semiMajorAxisConfidence;
    int semiMinorAxisConfidence;
    int semiMajorOrientation;
    int altitude;
    int heading;
    int headingConfidence;
    int speed;
    int speedConfidence;
    int vehicleLength;
    int vehicleWidth;
    int longitudinalAcceleration;
    int longitudinalAccelerationConfidence;
    int yawRate;
    int yawRateConfidence;
    int vehicleRole;

    /* Create a simple CAM by supplying the values manually. */
    public SimpleCam(int stationID,
                     int genDeltaTimeMillis,
                     byte containerMask,
                     int stationType,
                     int latitude,
                     int longitude,
                     int semiMajorAxisConfidence,
                     int semiMinorAxisConfidence,
                     int semiMajorOrientation,
                     int altitude,
                     int heading,
                     int headingConfidence,
                     int speed,
                     int speedConfidence,
                     int vehicleLength,
                     int vehicleWidth,
                     int longitudinalAcceleration,
                     int longitudinalAccelerationConfidence,
                     int yawRate,
                     int yawRateConfidence,
                     int vehicleRole) {

        this.stationID = stationID;
        this.genDeltaTimeMillis = genDeltaTimeMillis;
        this.containerMask = containerMask;
        this.stationType = stationType;
        this.latitude = latitude;
        this.longitude =  longitude;
        this.semiMajorAxisConfidence = semiMajorAxisConfidence;
        this.semiMinorAxisConfidence = semiMinorAxisConfidence;
        this.semiMajorOrientation = semiMajorAxisConfidence;
        this.altitude = altitude;
        this.heading = heading;
        this.headingConfidence = headingConfidence;
        this.speed = speed;
        this.speedConfidence = speedConfidence;
        this.vehicleLength = vehicleLength;
        this.vehicleWidth = vehicleWidth;
        this.longitudinalAcceleration = longitudinalAcceleration;
        this.longitudinalAccelerationConfidence = longitudinalAccelerationConfidence;
        this.yawRate = yawRate;
        this.yawRateConfidence = yawRateConfidence;
        this.vehicleRole = vehicleRole;
    }

    /* For creating a simple CAM from a UDP message as received from the vehicle control system. */
    public SimpleCam(byte[] receivedData){
        if(receivedData.length < SIMPLE_CAM_LENGTH){
            logger.error("Simple CAM is too short. Is: {} Should be: {}",
                         receivedData.length, SIMPLE_CAM_LENGTH);
            throw new IllegalArgumentException();
        }

        ByteBuffer buffer = ByteBuffer.wrap(receivedData);
        messageID = buffer.get();
        if(messageID != MessageId.cam){
            logger.error("Simple CAM has incorrect id. Id: {} Should be: {}",
                         messageID, MessageId.cam);
        }
        stationID = buffer.getInt();
        genDeltaTimeMillis = buffer.getInt();
        containerMask = buffer.get();
        stationType = buffer.getInt();
        latitude = buffer.getInt();
        longitude = buffer.getInt();
        semiMajorAxisConfidence = buffer.getInt();
        semiMinorAxisConfidence = buffer.getInt();
        semiMajorOrientation = buffer.getInt();
        altitude = buffer.getInt();
        heading = buffer.getInt();
        headingConfidence = buffer.getInt();
        speed = buffer.getInt();
        speedConfidence = buffer.getInt();
        vehicleLength = buffer.getInt();
        vehicleWidth = buffer.getInt();
        longitudinalAcceleration = buffer.getInt();
        longitudinalAccelerationConfidence = buffer.getInt();
        yawRate = buffer.getInt();
        yawRateConfidence = buffer.getInt();
        vehicleRole = buffer.getInt();

        /* Verify that the values are correct and attempt to replace
         * any errors with default values. */
        if(messageID != MessageId.cam){
            logger.error("MessageID is: {} Should be: {}",
                         messageID, MessageId.cam);
            throw new IllegalArgumentException();
        }

        if(!checkInt(StationID.class, stationID, "StationID")){
            throw new IllegalArgumentException();
        }
        if(!checkInt(GenerationDeltaTime.class, genDeltaTimeMillis, "GenerationDeltaTime")){
            throw new IllegalArgumentException();
        }
        if(!checkInt(StationType.class, stationType, "StationType")){
            stationType = StationType.unknown;
        }
        if(!checkInt(Latitude.class, latitude, "Latitude")){
            latitude = Latitude.unavailable;
        }
        if(!checkInt(Longitude.class, longitude, "Longitude")){
            longitude = Longitude.unavailable;
        }
        if(!checkInt(SemiAxisLength.class, semiMajorAxisConfidence, "SemiMajorConfidence")){
            semiMajorAxisConfidence = SemiAxisLength.unavailable;
        }
        if(!checkInt(SemiAxisLength.class, semiMinorAxisConfidence, "SemiMinorConfidence")){
            semiMinorAxisConfidence = SemiAxisLength.unavailable;
        }
        if(!checkInt(HeadingValue.class, semiMajorOrientation, "SemiMajorOrientation")){
            semiMajorOrientation = HeadingValue.unavailable;
        }
        if(!checkInt(AltitudeValue.class, altitude, "Altitude")){
            altitude = AltitudeValue.unavailable;
        }
        if(!checkInt(HeadingValue.class, heading, "Heading")){
            heading = HeadingValue.unavailable;
        }
        if(!checkInt(HeadingConfidence.class, headingConfidence, "HeadingConfidence")){
            headingConfidence = HeadingConfidence.unavailable;
        }
        if(!checkInt(SpeedValue.class, speed, "Speed")){
            speed = SpeedValue.unavailable;
        }
        if(!checkInt(SpeedConfidence.class, speedConfidence, "SpeedConfidence")){
            speedConfidence = SpeedConfidence.unavailable;
        }
        if(!checkInt(VehicleLengthValue.class, vehicleLength, "VehicleLength")){
            vehicleLength = VehicleLengthValue.unavailable;
        }
        if(!checkInt(VehicleWidth.class, vehicleWidth, "VehicleWidth")){
            vehicleWidth = VehicleWidth.unavailable;
        }
        if(!checkInt(LongitudinalAccelerationValue.class, longitudinalAcceleration, "LongitudinalAcceleration")){
            longitudinalAcceleration = LongitudinalAccelerationValue.unavailable;
        }
        if(!checkInt(AccelerationConfidence.class,
                     longitudinalAccelerationConfidence,
                     "LongitudinalAccelerationConfidence")){
            longitudinalAccelerationConfidence = AccelerationConfidence.unavailable;
        }
        if(!checkInt(YawRateValue.class, yawRate, "YawRate")){
            yawRate = YawRateValue.unavailable;
        }

        /* TODO: Find a cleaner way to check enums. Also, this
         * approach is not very informative.*/
        if(!YawRateConfidence.isMember(yawRateConfidence)){
            logger.warn("YawRateConfidence is not valid. Value={}", yawRateConfidence);
            yawRateConfidence = (int) YawRateConfidence.unavailable.value();
        }

        if(this.hasLowFrequencyContainer()){
            if(!VehicleRole.isMember(vehicleRole)){
                logger.warn("VehicleRole is not valid. Value={}", vehicleRole);
                vehicleRole = (int) VehicleRole.default_.value();
            }
        }
    }

    /* For creating a simple CAM from a CAM message as received from another ITS station. */
    public SimpleCam(Cam camPacket){
        CoopAwareness cam = camPacket.getCam();
        ItsPduHeader header = camPacket.getHeader();
        GenerationDeltaTime generationDeltaTime = cam.getGenerationDeltaTime();
        CamParameters camParameters = cam.getCamParameters();

        messageID = (byte) header.getMessageID().value;
        stationID = (int) header.getStationID().value;
        genDeltaTimeMillis = (int) generationDeltaTime.value;
        byte containerMask = 0;

        if(messageID != MessageId.cam){
            logger.warn("Malformed message on BTP port 2001 from station with ID {}", stationID);
            throw new IllegalArgumentException("Malformed message on BTP port 2001");
        }

        /* BasicContainer */
        BasicContainer basicContainer = camParameters.getBasicContainer();
        stationType = (int) basicContainer.getStationType().value;
        latitude = (int) basicContainer.getReferencePosition().getLatitude().value;
        longitude = (int) basicContainer.getReferencePosition().getLongitude().value;
        semiMajorAxisConfidence = (int) basicContainer.getReferencePosition().getPositionConfidenceEllipse().getSemiMajorConfidence().value;
        semiMinorAxisConfidence = (int) basicContainer.getReferencePosition().getPositionConfidenceEllipse().getSemiMinorConfidence().value;
        semiMajorOrientation = (int) basicContainer.getReferencePosition().getPositionConfidenceEllipse().getSemiMajorOrientation().value;
        altitude = (int) basicContainer.getReferencePosition().getAltitude().getAltitudeValue().value;

        /* HighFrequencyContainer */
        HighFrequencyContainer highFrequencyContainer = camParameters.getHighFrequencyContainer();
        BasicVehicleContainerHighFrequency basicVehicleContainerHighFrequency = highFrequencyContainer.getBasicVehicleContainerHighFrequency();

        heading = (int) basicVehicleContainerHighFrequency.getHeading().getHeadingValue().value;
        headingConfidence = (int) basicVehicleContainerHighFrequency.getHeading().getHeadingConfidence().value;
        speed = (int) basicVehicleContainerHighFrequency.getSpeed().getSpeedValue().value;
        speedConfidence = (int) basicVehicleContainerHighFrequency.getSpeed().getSpeedConfidence().value;
        vehicleLength = (int) basicVehicleContainerHighFrequency.getVehicleLength().getVehicleLengthValue().value;
        vehicleWidth = (int) basicVehicleContainerHighFrequency.getVehicleWidth().value;
        longitudinalAcceleration = (int) basicVehicleContainerHighFrequency.getLongitudinalAcceleration().getLongitudinalAccelerationValue().value();
        longitudinalAccelerationConfidence = (int) basicVehicleContainerHighFrequency.getLongitudinalAcceleration().getLongitudinalAccelerationConfidence().value();
        yawRate = (int) basicVehicleContainerHighFrequency.getYawRate().getYawRateValue().value;
        yawRateConfidence = (int) basicVehicleContainerHighFrequency.getYawRate().getYawRateConfidence().value();


        /* LowFrequencyContainer */
        LowFrequencyContainer lowFrequencyContainer = null;
        if(camParameters.hasLowFrequencyContainer()){
            containerMask += (1<<7);
            lowFrequencyContainer = camParameters.getLowFrequencyContainer();
            BasicVehicleContainerLowFrequency basicVehicleContainerLowFrequency = lowFrequencyContainer.getBasicVehicleContainerLowFrequency();
            vehicleRole = (int) basicVehicleContainerLowFrequency.getVehicleRole().value();
        }

        this.containerMask = containerMask;
    }

    /* Return the IntRange min and max value as a nice string. */
    String getIntRangeString(IntRange intRange){
        String string = "minValue=" + intRange.minValue() + ", maxValue=" + intRange.maxValue();
        return string;
    }

    /* Return true if value is within the IntRange, and false
       otherwise. */
    boolean compareIntRange(int value, IntRange intRange){
        return value <= intRange.maxValue() && value >= intRange.minValue();
    }

    public boolean checkInt(Class<?> classOfT, int value, String name){
        IntRange intRange = (IntRange) classOfT.getAnnotation(IntRange.class);
        if(intRange == null){
            logger.error("{} does not have an IntRange!", classOfT);
            return false;
        }
        if(!compareIntRange(value, intRange)){
            logger.warn("{} is outside of range. Value={}, {}",
                        name, value, getIntRangeString(intRange));
            return false;
        }else return true;
    }

    /*
      public boolean checkEnum(Enum classOfT, int value, String name){
      boolean valid = classOfT.isMember(value);
      if(!valid) logger.error("{} is not valid. Value={}",
      name, value);
      return valid;
      }
    */

    public int getLatitude(){
        return this.latitude;
    }

    public int getLongitude(){
        return this.longitude;
    }

    public int getGenerationDeltaTime(){
        return this.genDeltaTimeMillis;
    }

    @Override
    public boolean equals(Object o) {
            // self check
            if (this == o) {
                    return true;
            }

            // null check
            if (o == null) {
                    return false;
            }

            // type check and cast
            if (getClass() != o.getClass()) {
                    return false;
            }

            SimpleCam simpleCam = (SimpleCam) o;

            // field comparison
            return messageID == simpleCam.messageID
                    && stationID == simpleCam.stationID
                    && genDeltaTimeMillis == simpleCam.genDeltaTimeMillis
                    && containerMask == simpleCam.containerMask
                    && stationType == simpleCam.stationType
                    && latitude == simpleCam.latitude
                    && longitude == simpleCam.longitude
                    && semiMajorAxisConfidence == simpleCam.semiMajorAxisConfidence
                    && semiMinorAxisConfidence == simpleCam.semiMinorAxisConfidence
                    && semiMajorOrientation == simpleCam.semiMajorOrientation
                    && altitude == simpleCam.altitude
                    && heading == simpleCam.heading
                    && headingConfidence == simpleCam.headingConfidence
                    && speed == simpleCam.speed
                    && speedConfidence == simpleCam.speedConfidence
                    && vehicleLength == simpleCam.vehicleLength
                    && vehicleWidth == simpleCam.vehicleWidth
                    && longitudinalAcceleration == simpleCam.longitudinalAcceleration
                    && longitudinalAccelerationConfidence == simpleCam.longitudinalAccelerationConfidence
                    && yawRate == simpleCam.yawRate
                    && yawRateConfidence == simpleCam.yawRateConfidence
                    && vehicleRole == simpleCam.vehicleRole;
    }


    /* Check if the simple CAM is valid. */
    boolean isValid(){
        boolean valid = true;

        if(messageID != MessageId.cam){
            logger.error("MessageID is: {} Should be: {}",
                         messageID, MessageId.cam);
            valid = false;
        }

        if(!checkInt(StationID.class, stationID, "StationID")) valid = false;
        if(!checkInt(GenerationDeltaTime.class, genDeltaTimeMillis, "GenerationDeltaTime")) valid = false;
        if(!checkInt(StationType.class, stationType, "StationType")) valid = false;
        if(!checkInt(Latitude.class, latitude, "Latitude")) valid = false;
        if(!checkInt(Longitude.class, longitude, "Longitude")) valid = false;
        if(!checkInt(SemiAxisLength.class, semiMajorAxisConfidence, "SemiMajorConfidence")) valid = false;
        if(!checkInt(SemiAxisLength.class, semiMinorAxisConfidence, "SemiMinorConfidence")) valid = false;
        if(!checkInt(HeadingValue.class, semiMajorOrientation, "SemiMajorOrientation")) valid = false;
        if(!checkInt(AltitudeValue.class, altitude, "Altitude")) valid = false;
        if(!checkInt(HeadingValue.class, heading, "Heading")) valid = false;
        if(!checkInt(HeadingConfidence.class, headingConfidence, "HeadingConfidence")) valid = false;
        if(!checkInt(SpeedValue.class, speed, "Speed")) valid = false;
        if(!checkInt(SpeedConfidence.class, speedConfidence, "SpeedConfidence")) valid = false;
        if(!checkInt(VehicleLengthValue.class, vehicleLength, "VehicleLength")) valid = false;
        if(!checkInt(VehicleWidth.class, vehicleWidth, "VehicleWidth")) valid = false;
        if(!checkInt(LongitudinalAccelerationValue.class, longitudinalAcceleration, "LongitudinalAcceleration")) valid = false;
        if(!checkInt(AccelerationConfidence.class,
                     longitudinalAccelerationConfidence,
                     "LongitudinalAccelerationConfidence")) valid = false;
        if(!checkInt(YawRateValue.class, yawRate, "YawRate")) valid = false;

        /* TODO: Find a cleaner way to check enums. Also, this
         * approach is not very informative.*/
        if(!YawRateConfidence.isMember(yawRateConfidence)){
            logger.error("YawRateConfidence is not valid. Value={}", yawRateConfidence);
            valid = false;
        }

        if(this.hasLowFrequencyContainer()){
            if(!VehicleRole.isMember(vehicleRole)){
                logger.error("VehicleRole is not valid. Value={}", vehicleRole);
                valid = false;
            }
        }

        return valid;
    }

    /* Return true if the simple CAM has a low frequency container. */
    boolean hasLowFrequencyContainer(){
        return (containerMask & (1<<7)) != 0;
    }

    /* Return values as a byte array for sending as a simple CAM UDP message. */
    public byte[] asByteArray(){
        byte[] packetBuffer = new byte[SIMPLE_CAM_LENGTH];
        ByteBuffer buffer = ByteBuffer.wrap(packetBuffer);
        try{
            buffer.put(messageID);
            buffer.putInt(stationID);
            buffer.putInt(genDeltaTimeMillis);
            buffer.put(containerMask);
            buffer.putInt(stationType);
            buffer.putInt(latitude);
            buffer.putInt(longitude);
            buffer.putInt(semiMajorAxisConfidence);
            buffer.putInt(semiMinorAxisConfidence);
            buffer.putInt(semiMajorOrientation);
            buffer.putInt(altitude);
            buffer.putInt(heading);
            buffer.putInt(headingConfidence);
            buffer.putInt(speed);
            buffer.putInt(speedConfidence);
            buffer.putInt(vehicleLength);
            buffer.putInt(vehicleWidth);
            buffer.putInt(longitudinalAcceleration);
            buffer.putInt(longitudinalAccelerationConfidence);
            buffer.putInt(yawRate);
            buffer.putInt(yawRateConfidence);
            buffer.putInt(vehicleRole);
        }catch(BufferOverflowException e){
            logger.error("Error converting simple CAM to byte array.", e);
            /* Return an empty byte array as the vehicle control
             * system has to deal with those anyway.
             */
            return new byte[SIMPLE_CAM_LENGTH];
        }
        return packetBuffer;
    }

    /* Return values as a proper CAM message for sending to another ITS station. */
    public Cam asCam(){
        LowFrequencyContainer lowFrequencyContainer = (containerMask & (1<<7)) != 0 ?
            new LowFrequencyContainer(
                                      new BasicVehicleContainerLowFrequency(
                                                                            VehicleRole.fromCode(vehicleRole),
                                                                            ExteriorLights.builder()
                                                                            .set(false, false, false, false, false, false, false, false)
                                                                            .create(),
                                                                            new PathHistory()
                                                                            ))
            :
            null;

        //Not used for participating vehicles
        SpecialVehicleContainer specialVehicleContainer = null;

        BasicContainer basicContainer =
            new BasicContainer(new StationType(stationType),
                               new ReferencePosition(new Latitude(latitude),
                                                     new Longitude(longitude),
                                                     new PosConfidenceEllipse(new SemiAxisLength(semiMajorAxisConfidence),
                                                                              new SemiAxisLength(semiMinorAxisConfidence),
                                                                              new HeadingValue(semiMajorOrientation)),
                                                     new Altitude(new AltitudeValue(altitude),
                                                                  AltitudeConfidence.unavailable)));
        HighFrequencyContainer highFrequencyContainer =
            new HighFrequencyContainer(BasicVehicleContainerHighFrequency.builder()
                                       .heading(new Heading(new HeadingValue(heading),
                                                            new HeadingConfidence(headingConfidence)))
                                       .speed(new Speed(new SpeedValue(speed),
                                                        new SpeedConfidence(speedConfidence)))
                                       //DriveDirection isn't part of the GCDC spec. Set to unavailable.
                                       .driveDirection(DriveDirection.values()[2])
                                       .vehicleLength(new VehicleLength(new VehicleLengthValue(vehicleLength), VehicleLengthConfidenceIndication.unavailable))
                                       .vehicleWidth(new VehicleWidth(vehicleWidth))
                                       .longitudinalAcceleration(new LongitudinalAcceleration(new LongitudinalAccelerationValue(longitudinalAcceleration),
                                                                                              new AccelerationConfidence(longitudinalAccelerationConfidence)))
                                       //Curvature and CurvatureCalculationMode isn't part of the GCDC spec. Set to unavailable.
                                       .curvature(new Curvature())
                                       .curvatureCalculationMode(CurvatureCalculationMode.values()[2])
                                       .yawRate(new YawRate(new YawRateValue(yawRate),
                                                            //TODO: This code is slow. Cache YawRateConfidence.values() if it's a problem.
                                                            YawRateConfidence.values()[yawRateConfidence]))
                                       .create()
                                       );

        return new Cam(
                       new ItsPduHeader(new ProtocolVersion(1),
                                        new MessageId(MessageId.cam),
                                        new StationID(stationID)),
                       new CoopAwareness(
                                         new GenerationDeltaTime(genDeltaTimeMillis * GenerationDeltaTime.oneMilliSec),
                                         new CamParameters(basicContainer,
                                                           highFrequencyContainer,
                                                           lowFrequencyContainer,
                                                           specialVehicleContainer)));
    }
}
