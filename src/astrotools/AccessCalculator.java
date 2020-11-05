package astrotools;

import org.hipparchus.ode.events.Action;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.AbstractPropagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.propagation.events.EventDetector;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AccessCalculator {

    private Facility facility;
    private Satellite satellite;
    private Date contact = new Date();
    private List<Access> accessIntervals = new ArrayList<>();
    private double step = 120;
    private double timeTresHold = 30;
    private double elevationThreshold = Math.toRadians(5);
    private final String startTime;
    private final String endTime;
    private long lastComputeTime = 0;
    private String propagatorMode = "sgp4";

    private final BodyShape earth;
    private static final double MU = 3.986004415e+14;

    /**
     * Initializes the AccessCalculator with a Facility and a Satellite objects
     * the step for the propagation in seconds, the elevation threshold (height
     * above the horizon provided in degrees minimum to establish access) and
     * the start and end time for the access calculation in ISO8601 format:
     * yyyy-MM-ddTHH:mm:ss.SSS
     **/

    public AccessCalculator(Facility groundStation, Satellite satellite,
                            double step, double elevationThreshold, String startTime, String endTime) {
        this.facility = groundStation;
        this.satellite = satellite;
        this.step = step;
        this.elevationThreshold = Math.toRadians(elevationThreshold);
        this.startTime = startTime;
        this.endTime = endTime;

        // Orekit configurations:
        File orekitData = new File("orekit-data");
        if (!orekitData.exists()) {
            System.err.format(Locale.US, "Failed to find %s folder%n",
                    orekitData.getAbsolutePath());
            System.exit(1);
        }

        DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));
        Frame earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING,
                earthFrame);
    }

    /**
     * Sets the propagation mode, by default on SGP4
     * implemented also: keplerian
     **/

    public void setPropagatorMode(String mode) {
        this.propagatorMode = mode;
    }

    /**
     * Computes access times, updates the accessInterval List with obtained
     * intervals of Access
     **/

    public void compute() {

        // Toc
        lastComputeTime = System.currentTimeMillis();

//        Date contact = new Date();                  // FIXME pretty sure I'm going back and forward
//        contact.setTime(stamp2unix(startTime));
        AbsoluteDate startAbsolutDate = stamp2AD(startTime);
        AbsoluteDate endAbsoluteDate = stamp2AD(endTime);
        double scenarioTime = endAbsoluteDate.durationFrom(startAbsolutDate);

        accessIntervals.clear();

        GeodeticPoint station = new GeodeticPoint(facility.getLat(), facility.getLon(), facility.getHeight());
        TopocentricFrame topoFrame = new TopocentricFrame(earth, station, "");

        EventDetector elevDetector = new ElevationDetector(step, timeTresHold, topoFrame).
                withConstantElevation(elevationThreshold).
                withHandler(
                        (s, detector, increasing) -> {
                            addInterval(s, increasing);
                            return Action.CONTINUE;
                        });

        AbstractPropagator propagator = getPropagator();
        propagator.addEventDetector(elevDetector);
        propagator.propagate(startAbsolutDate, startAbsolutDate.shiftedBy(scenarioTime));

        // Toc
        lastComputeTime = System.currentTimeMillis() - lastComputeTime;

    }

    /**
     * Gets the AbstractPropagator instance according to the propagatorMode
     * selected
     **/

    private AbstractPropagator getPropagator() {

        switch (propagatorMode) {
            case "keplerian":
                Frame inertialFrame = FramesFactory.getEME2000();
                Orbit initialOrbit = new KeplerianOrbit(satellite.getSemMajAxis(), satellite.getEcc(), satellite.getInc(),
                        satellite.getArgOfPerigee(), satellite.getRaan(), satellite.getMeanAnomaly(), PositionAngle.MEAN,
                        inertialFrame, stamp2AD(startTime), MU);
                return new KeplerianPropagator(initialOrbit);

            case "sgp4":
            default:
                TLE tle;
                if (satellite.getLine1().isEmpty()) {
                    tle = elements2TLE(satellite);
                } else {
                    tle = new TLE(satellite.getLine1(), satellite.getLine2());
                }
                return TLEPropagator.selectExtrapolator(tle);
        }
    }

    /**
     * Sets the minimum elevation threshold (height over horizon) that permits
     * access between assets. Takes the value in degrees.
     **/

    public void setElevationThreshold(double elevationThreshold) {
        this.elevationThreshold = Math.toRadians(elevationThreshold);
    }

    /**
     * Sets the time threshold, this is an internal variable from the Orekit
     * package, used in the ElevatorDetector as a kind of sensibility in the
     * access interval detections. 30, By default.
     **/

    public void setTimeTresHold(double timeTresHold) {
        this.timeTresHold = timeTresHold;
    }

    /**
     * Provides the computation time elapsed in the last computation
     **/

    public long getLastComputeTime() {
        return lastComputeTime;
    }

    /**
     * Provides the last obtained access intervals
     **/

    public List<Access> getAccessIntervals() {
        return accessIntervals;
    }

    /**
     * Returns the currently configured facility
     **/

    public Facility getFacility() {
        return facility;
    }

    /**
     * Sets the facility
     **/

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    /**
     * returns the currently configured satellite
     **/

    public Satellite getSatellite() {
        return satellite;
    }

    /**
     * returns the satellite
     **/

    public void setSatellite(Satellite satellite) {
        this.satellite = satellite;
    }

    /**
     * Adds a detected Access interval to the accessIntervals List
     **/

    // private void addInterval(SpacecraftState s, ElevationDetector detector, boolean dir) {
    private void addInterval(SpacecraftState s, boolean dir) {

        try {
            if (dir) {
                contact = s.getDate().toDate(TimeScalesFactory.getUTC());
            } else {
                accessIntervals.add(new Access(contact.getTime(), s.getDate().toDate(TimeScalesFactory.getUTC()).getTime()));
            }
        } catch (NullPointerException e) {
            System.out.println("Error obtaining access interval: " + contact.toString() + " " + e.getMessage());
        }

    }

    /**
     * Get a TLE instance to perform a SGP4 propagation from common Keplerian elements
     * using mock up data for irrelevant parameters on mission design (such as launch
     * year, or  launch number).
     **/

    private TLE elements2TLE(Satellite sat) {

        return new TLE(0, 'U', 2020, 0, "A", 0, 1,
                stamp2AD(startTime), Math.sqrt(MU / Math.pow(sat.getSemMajAxis(), 3)), 0, 0,
                sat.getEcc(), sat.getInc(), sat.getArgOfPerigee(), sat.getRaan(), sat.getMeanAnomaly(), 1, 0d);
    }

    /**
     * Useful transformations between date formats
     **/

    private AbsoluteDate stamp2AD(String stamp) {
        return new AbsoluteDate(stamp, TimeScalesFactory.getUTC());
    }

    private String unix2stamp(long unix) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTCG"));
        Date date = new Date(unix);
        return dateFormat.format(date);
    }

    private long stamp2unix(String dateStamp) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS"); // FIXME
        Date parsedDate = new Date();

        try {
            parsedDate = dateFormat.parse(dateStamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return parsedDate.getTime();
    }

    /**
     * Prints access intervals in a readable form
     **/

    public void printAccessReport() {

        if (accessIntervals.isEmpty()) {
            System.out.println("No access intervals detected");
            return;
        }

        int contact = 1;
        for (Access access : accessIntervals) {
            System.out.println(contact++ + "\t" + unix2stamp(access.getStart()) + "\t" + unix2stamp(access.getEnd()));
        }

    }

}


