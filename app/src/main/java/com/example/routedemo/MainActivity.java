package com.example.routedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final int REQUEST_CODE = 1;

    GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    double latitude, longitude;
    final int RADIUS = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        initmap();

        getUserLocation();
        if(!checkPermission())
        {
            requestPermission();
        }
        else
        {
            fusedLocationProviderClient.requestLocationUpdates( locationRequest, locationCallback, Looper.myLooper() );
        }


    }

    private Boolean checkPermission()
    {
        int permissionState = ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION );
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission()
    {
        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE );

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                setHomeMarker();
                fusedLocationProviderClient.requestLocationUpdates( locationRequest, locationCallback, Looper.myLooper() );
            }
        }
    }

    private void initmap()
    {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById( R.id.map );
        mapFragment.getMapAsync( this );

    }

    private void getUserLocation()
    {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient( this );
        locationRequest = new LocationRequest();
        locationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );
        locationRequest.setInterval( 5000 );
        locationRequest.setFastestInterval( 3000 );
        locationRequest.setSmallestDisplacement( 10 );
        setHomeMarker();

    }
    private void setHomeMarker(){
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations())
                {
                    //new 
                    LatLng userLocation = new LatLng( location.getLatitude(), location.getLongitude());
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    CameraPosition cameraPosition = CameraPosition.builder()
                            .target( userLocation )
                            .zoom( 15 )
                            .bearing( 0 )
                            .tilt( 45 )
                            .build();
                    mMap.animateCamera( CameraUpdateFactory.newCameraPosition( cameraPosition ) );
                    mMap.addMarker( new MarkerOptions().position( userLocation )
                    .title( "Your Location" ));



                   // .icon( BitmapDescriptorFactory.fromResource( R.drawable.icon ) ));


                }
            }
        };
    }


   // https://maps.googleapis.com/maps/api/place/findplacefromtext/output?parameters




private String getUrl(double latitude, double longitude, String nearbyPlace)
{
    StringBuilder placeUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?"  );
    placeUrl.append( "location=" + latitude + "," + longitude );
    placeUrl.append( "&radius=" + RADIUS );
    placeUrl.append( "&type=" + nearbyPlace );
    //placeUrl.append( "&keyword=cruise" );
    placeUrl.append( "&key=" + getString(R.string.api_key_class ));
    return placeUrl.toString();
}
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    public void btnClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_restaurant:
                // get the url from place api
                String url = getUrl( latitude, longitude, "restaurant" );
                Log.i("MainActivity", url);
               // setmarkers( url );
                Object[] dataTransfer = new Object[2];
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                GetNearbyPlaceData getNearbyPlaceData = new GetNearbyPlaceData();
                getNearbyPlaceData.execute(dataTransfer);
                Toast.makeText( this, "Restautants", Toast.LENGTH_SHORT ).show();
                break;
        }
    }

    private void setmarkers (String url) {
        String placeData = "";

        FetchURL fetchURL = new FetchURL();
        try {

            placeData = fetchURL.readUrl( url );
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<HashMap<String, String>> nearByPlaceList = null;
        DataParser parser = new DataParser();
        nearByPlaceList = parser.parse( placeData );
        for (int i = 0; i < nearByPlaceList.size(); i++) {
            // Log.i("MainActivity", String.valueOf( nearbyList.size()));
            HashMap<String, String> place = nearByPlaceList.get( i );

            String placeName = place.get( "placeName" );
            String vicinity = place.get( "vicinity" );
            double lat = Double.parseDouble( place.get( "lat" ) );
            double lng = Double.parseDouble( place.get( "lng" ) );
            String reference = place.get( "reference" );

            LatLng location = new LatLng( lat, lng );
            MarkerOptions marker = new MarkerOptions().position( location )
                    .title( placeName )

                    .icon( BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_GREEN ) );
            mMap.addMarker( marker );

//            CameraPosition cameraPosition = CameraPosition.builder()
//                    .target( location )
//                    .zoom( 15 )
//                    .bearing( 0 )
//                    .tilt( 45 )
//                    .build();
//            mMap.animateCamera( CameraUpdateFactory.newCameraPosition( cameraPosition ) );


        }
    }
}
