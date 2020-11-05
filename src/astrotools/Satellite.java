package astrotools;

public class Satellite {

    private String line1 = "";
    private String line2 = "";

    private double semMajAxis;
    private double ecc;
    private double inc;
    private double raan;
    private double argOfPerigee;
    private double meanAnomaly;

    public Satellite(String tle1, String tle2) {
        this.line1 = tle1;
        this.line2 = tle2;
    }

    public Satellite(double semMajAxis, double ecc, double inc,
                     double raan, double argOfPerigee, double anomaly) {
        this.semMajAxis = semMajAxis;
        this.ecc = ecc;
        this.inc = Math.toRadians(inc);
        this.raan = Math.toRadians(raan);
        this.argOfPerigee = Math.toRadians(argOfPerigee);
        this.meanAnomaly = Math.toRadians(anomaly);
    }

    public void setElements(double semMajAxis, double ecc, double inc,
                            double raan, double argOfPerigee, double anomaly) {
        this.semMajAxis = semMajAxis;
        this.ecc = ecc;
        this.inc = Math.toRadians(inc);
        this.raan = Math.toRadians(raan);
        this.argOfPerigee = Math.toRadians(argOfPerigee);
        this.meanAnomaly = Math.toRadians(anomaly);
    }

    public void setTLE(String tle1, String tle2) {
        setLine1(tle1);
        setLine2(tle2);
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public double getSemMajAxis() {
        return semMajAxis;
    }

    public void setSemMajAxis(double semMajAxis) {
        this.semMajAxis = semMajAxis;
    }

    public double getEcc() {
        return ecc;
    }

    public void setEcc(double ecc) {
        this.ecc = ecc;
    }

    public double getInc() {
        return inc;
    }

    public void setInc(double inc) {
        this.inc = inc;
    }

    public double getRaan() {
        return raan;
    }

    public void setRaan(double raan) {
        this.raan = raan;
    }

    public double getArgOfPerigee() {
        return argOfPerigee;
    }

    public void setArgOfPerigee(double argOfPerigee) {
        this.argOfPerigee = argOfPerigee;
    }

    public double getMeanAnomaly() {
        return meanAnomaly;
    }

    public void setMeanAnomaly(double meanAnomaly) {
        this.meanAnomaly = meanAnomaly;
    }

}
