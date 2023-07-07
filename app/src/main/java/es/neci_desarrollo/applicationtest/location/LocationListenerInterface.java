package es.neci_desarrollo.applicationtest.location;

import android.location.Location;
import android.telephony.SignalStrength;

public interface LocationListenerInterface {
    public void onLocationChanged (Location location);
}

