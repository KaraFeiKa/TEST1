package es.neci_desarrollo.applicationtest.speed;

public interface ITrafficSpeedListener {
    void onTrafficSpeedMeasured(double upStream, double downStream);
}
