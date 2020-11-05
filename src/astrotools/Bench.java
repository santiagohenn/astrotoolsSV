package astrotools;

public class Bench {

    /* Test code */
    public static void main(String[] args) {

        // Set the scenario start and end date & time
        String startTime = "2020-03-20T11:00:00.000";
        String endTime = "2020-03-21T11:00:00.000";

        // Create a facility, provide latitude and longitude in degrees and height in meters
        Facility facility = new Facility(0, -45, 0);

        // Create your satellite, either from a TLE two string format, or providing the orbital elements
        //        String card1 = "1 00000U 20000A   20080.45833333  .00000000  00000-0  00000-0 0    18";
        //        String card2 = "2 00000  97.8877  80.1454 0000000 267.4896 279.9910 14.82153416    15";

        //        Satellite satellite = new Satellite(card1, card2);
        Satellite satellite = new Satellite(7000672.074930292, 0, 97.8877, 80.1454, 267.4896, 279.9910);

        // Create an instance of the access calculator, with a facility and a satellite, simulation time in seconds
        // height over the horizon in degrees.
        AccessCalculator accessCalculator = new AccessCalculator(facility, satellite, 120, 5, startTime, endTime);

        // Compute the access times
        accessCalculator.compute();

        // Print the report
        accessCalculator.printAccessReport();

        System.out.println("The process took: " + accessCalculator.getLastComputeTime() + " ms");

    }

}
