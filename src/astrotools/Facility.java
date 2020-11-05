package astrotools;

public class Facility {

    private double lat;
    private double lon;
    private double height;

    public Facility(double lat, double lon, double height) {
        this.lat = Math.toRadians(lat);
        this.lon = Math.toRadians(lon);
        this.height = height;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

}
