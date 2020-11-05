package astrotools;

public class Bench {

    /* Test code */
    public static void main(String[] args) {

        //        String card1 = "1 00000U 20000A   20080.45833333  .00000000  00000-0  00000-0 0    18";
        //        String card2 = "2 00000  97.8877  80.1454 0000000 267.4896 279.9910 14.82153416    15";

        String time1 = "2020-03-20T11:00:00.000";
        String time2 = "2020-03-21T11:00:00.000";

        //  Create a facility, provide latitude and longitude in degrees and height in meters
        Facility groundStation = new Facility(0, -45, 0);

        //        double elements[] = {7000672.074930292, 1.478E-4, 97.8877, 80.1454, 267.4896, 279.9910};
        //        Satellite satellite = new Satellite(card1, card2);

        Satellite satellite = new Satellite(7000672.074930292,0,97.8877,80.1454,267.4896,279.9910);

        AccessCalculator accessCalculator = new AccessCalculator(groundStation, satellite, 120, 5, time1, time2);
        accessCalculator.compute();
        accessCalculator.printAccessReport();

        System.out.println("The process took: " + accessCalculator.getLastComputeTime() + " ms");

    }

}
