package es.neci_desarrollo.applicationtest.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;

import es.neci_desarrollo.applicationtest.location.LocationListenerInterface;

public class MyLocationListener implements LocationListener {

    private LocationListenerInterface locationListenerInterface;

    @Override
    public void onLocationChanged(@NonNull Location location) {
        locationListenerInterface.onLocationChanged(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    public void setLocationListenerInterface(LocationListenerInterface locationListenerInterface) {
        this.locationListenerInterface = locationListenerInterface;
    }
}
