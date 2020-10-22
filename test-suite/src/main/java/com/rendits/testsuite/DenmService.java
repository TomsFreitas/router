
/* Service used to periodically send DENM messages to the router.
 */

package com.rendits.testsuite;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
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


public class DenmService {
    private final static Logger logger = LoggerFactory.getLogger(DenmService.class);

    private int period;
    private DatagramSocket socket;
    private InetAddress routerAddress;
    private int routerPort;

    private Vehicle vehicle;

    private SimpleCam simpleCam;
    private byte[] buffer;

    private ConcurrentHashMap highResTimestamps;
    final gpsdata data = new gpsdata();


    DenmService(int period,
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
        scheduler.scheduleAtFixedRate(runner, period, period, TimeUnit.MILLISECONDS); // Are denms periodic?

        logger.info("Starting DENM service...");

        try{
            final GPSdEndpoint ep = new GPSdEndpoint("localhost", 2947, new ResultParser());
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
    
            ep.start();
            }catch (final Exception e){
                System.out.println("Não deu");
            }

    }

    private Runnable runner = new Runnable() {
            @Override
            public void run() {
                try{

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
                                                        2, //latitude
                                                        48, //longitude
                                                        0, //semiMajorConfidence
                                                        0, //semiMinorConfidence
                                                        0, //semiMajorOrientation
                                                        400, //altitude
                                                        1, //heading value
                                                        1, //headingConfidence
                                                        0, //speedValue
                                                        1, //speedConfidence
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

class gpsdata{

    public double latitude;
    public double longitude;
    public double altitude;
    public double latitudeerror;
    public double longitudeerror;
    public double altitudeerror;
    public double course;
    public double speed;
    public double courseerror;
    public double speederror;

    public gpsdata(double latitude, double longitude, double altitude, double latitudeerror,
    double longitudeerror, double altitudeerror, double course, double speed, double courseerror,
    double speederror ){
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.latitudeerror = latitudeerror;
        this.longitudeerror = longitudeerror;
        this.altitudeerror = altitudeerror;
        this.course = course;
        this.speed = speed;
        this.courseerror = courseerror;  
        this.speederror = speederror;


    }

    public gpsdata(){}
}

