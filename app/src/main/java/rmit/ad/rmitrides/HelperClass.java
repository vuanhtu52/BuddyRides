package rmit.ad.rmitrides;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

public class HelperClass {
    public static String NotificationChannel = "RMITRides";

    public static String getApiKeyInManifest(Context context){
        //https://blog.iangclifton.com/2010/10/08/using-meta-data-in-an-androidmanifest/
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            String val = bundle.getString("com.google.android.geo.API_KEY");
            return val;
        }
        catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static GeoPoint LatLng2GeoPoint(LatLng latLng){
        return new GeoPoint(latLng.latitude, latLng.longitude);
    }

    public static LatLng GeoPoint2LatLng(GeoPoint geoPoint){
        return new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
    }
}
