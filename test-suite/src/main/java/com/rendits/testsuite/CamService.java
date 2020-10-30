/* Copyright 2018 Rendits
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/* Service used to periodically send CAM messages to the router.
 */

package com.rendits.testsuite;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import com.rendits.router.SimpleCam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.taimos.gpsd4java.api.ObjectListener;
import de.taimos.gpsd4java.backend.GPSdEndpoint;
import de.taimos.gpsd4java.backend.ResultParser;
import de.taimos.gpsd4java.types.ATTObject;
import de.taimos.gpsd4java.types.DeviceObject;
import de.taimos.gpsd4java.types.DevicesObject;
import de.taimos.gpsd4java.types.SATObject;
import de.taimos.gpsd4java.types.SKYObject;
import de.taimos.gpsd4java.types.TPVObject;
import de.taimos.gpsd4java.types.subframes.SUBFRAMEObject;

public class CamService {
    private final static Logger logger = LoggerFactory.getLogger(CamService.class);

    private int period;
    private DatagramSocket socket;
    private InetAddress routerAddress;
    private int routerPort;

    private Vehicle vehicle;

    private SimpleCam simpleCam;
    private byte[] buffer;

    private ConcurrentHashMap highResTimestamps;

    final gpsdata data = new gpsdata();

    CamService(int period,
               DatagramSocket socket,
               InetAddress routerAddress,
               int routerPort,
               Vehicle vehicle,
               ScheduledExecutorService scheduler,
               ConcurrentHashMap highResTimestamps) {

        this.period = period;
        this.socket = socket;
        this.routerAddress = routerAddress;
        this.routerPort = routerPort;
        this.vehicle = vehicle;
        this.highResTimestamps = highResTimestamps;

        /* Add the runner to the thread pool */
        scheduler.scheduleAtFixedRate(runner, period, 100, TimeUnit.MILLISECONDS); //Run at 100 miliseconds

        logger.info("Starting CAM service...");

        try{
            final GPSdEndpoint ep = new GPSdEndpoint("localhost", 2947, new ResultParser());
            logger.info("started gps");
            ep.addListener(new ObjectListener() {
                @Override
                public void handleTPV(final TPVObject tpv) {
                    logger.info("TPV: {}", tpv);
                    data.longitude = tpv.getLongitude();
                    data.latitude = tpv.getLatitude();
                    data.course = tpv.getCourse();
                    data.altitude = tpv.getAltitude();
                    data.longitudeerror = tpv.getLongitudeError();
                    data.latitudeerror = tpv.getLatitudeError();
                    data.courseerror = tpv.getCourseError();
                    data.speed = tpv.getSpeed();
                    data.speederror = tpv.getSpeedError();
                    data.altitudeerror = tpv.getLatitudeError();
                    
                }
            });
            logger.info("before start");
    
            ep.start();
            }catch (final Exception e){
                System.out.println("Não deu");
            }
    }

    private Runnable runner = new Runnable() {
            @Override
            public void run() {
                try{

                    int longitude = (int)(data.longitude * Math.pow(10, 7));
                    int latitude = (int)(data.latitude * Math.pow(10, 7));
                    int altitude = (int)(data.altitude*100);
                    int speed = (int)(data.speed*100);
                    int course = (int)(data.course * 10);
                    double speederror_temp = data.speederror * 100;
                    int speederror;

                    if (speederror_temp <= 1.0){
                        speederror = 1;
                    }else if(speederror_temp < 1.0 && speederror_temp >= 125.0){
                        speederror = (int)(speederror_temp);
                    }else if (speederror_temp > 125.0){
                        speederror = 126;
                    }else{
                        speederror = 127;
                    }


                    /* Record a high-res timestamp and map it to
                     * the timestamp included in the message. */
                    long nanoTime = System.nanoTime();
                    int generationDeltaTime = vehicle.getGenerationDeltaTime();
                    highResTimestamps.put(generationDeltaTime, nanoTime);

                    /* Create a simple CAM manually */
                    SimpleCam simpleCam = new SimpleCam(vehicle.getStationID(),
                                                        generationDeltaTime,
                                                        (byte) 128, //containerMask
                                                        5, //stationType
                                                        latitude, //latitude
                                                        longitude, //longitude
                                                        0, //semiMajorConfidence
                                                        0, //semiMinorConfidence
                                                        0, //semiMajorOrientation
                                                        altitude, //altitude
                                                        course, //heading value
                                                        1, //headingConfidence
                                                        speed, //speedValue
                                                        speederror, //speedConfidence
                                                        40, //vehicleLength
                                                        20, //vehicleWidth
                                                        159, //longitudinalAcc
                                                        1, //longitudinalAccConf
                                                        2, //yawRateValue
                                                        1, //yawRateConfidence
                                                        0); //vehicleRole

                    /* Format it as a byte array */
                    byte[] buffer = simpleCam.asByteArray();

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    packet.setPort(routerPort);
                    packet.setAddress(routerAddress);
                    socket.send(packet);
                } catch(IOException e) {
                    logger.error("IOException in CAM Service.");
                }
            }
        };
}
