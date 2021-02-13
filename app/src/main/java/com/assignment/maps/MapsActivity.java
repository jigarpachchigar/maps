package com.assignment.maps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Geocoder geo;
    TextView txtMarkers;

    private static final int REQUEST_CODE = 1;
    private Marker homeMarker;
    private Marker destMarker;
    private String[] cities = {"A", "B", "C", "D"};

    Polyline line;
    Polygon shape;
    private static final int POLYGON_SIDES = 4;
    List<Marker> markerList = new ArrayList<>();
    LocationManager locationManager;
    LocationListener locationListener;

    Polygon polygon = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        txtMarkers = findViewById(R.id.txtMarkerText);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        geo = new Geocoder(MapsActivity.this, Locale.getDefault());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //set home marker
                setHomeMarker(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

        };

        if (!isGrantedPermission())
            requestLocationPermission();
        else
            startUpdateLocation();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //set the marker
                setMarker(latLng);
                geo = new Geocoder(MapsActivity.this, Locale.getDefault());

                try {
                    List<Address> address = geo.getFromLocation(latLng.latitude, latLng.longitude, 1);

                    if (address.size() < 4) {
                        mMap.addMarker(new MarkerOptions().position(latLng).title("Country:" + address.get(0).getCountryName()
                                + ". Address:" + address.get(0).getAddressLine(0)));
                        txtMarkers.setText("Name:" + address.get(0).getCountryName()
                                + ". Address:" + address.get(0).getAddressLine(0));
                    }

                } catch (IOException e) {
                    if (e != null)
                        Toast.makeText(MapsActivity.this, "Error:" + e.getMessage().toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }

        });

    }

    private void setMarker(LatLng latLng) {

        MarkerOptions options = new MarkerOptions().position(latLng)
                .title("Your Destination");


//        if(destMarker != null)
//            clearMap();
//        destMarker = mMap.addMarker(options);
//        drawLine();
        if(markerList.size() == POLYGON_SIDES) {
            clearMap();
        }
        markerList.add(mMap.addMarker(options));
        if(markerList.size() == POLYGON_SIDES) {
            drawShape();

        }

    }

    private void drawShape() {
        PolygonOptions options = new PolygonOptions()
                .fillColor(Color.GREEN)
                .strokeColor(Color.RED)
                .strokeWidth(5);

        for(int i=0; i<POLYGON_SIDES; i++)
            options.add(markerList.get(i).getPosition());

        shape = mMap.addPolygon(options);
    }

    private void drawLine() {
        PolylineOptions options = new PolylineOptions()
                .color(Color.BLACK)
                .width(10)
                .add(homeMarker.getPosition(),destMarker.getPosition());

        line = mMap.addPolyline(options);
    }

    private void clearMap() {
//        if(destMarker != null){
//            destMarker.remove();
//            destMarker = null;
//        }

        //line.remove();

        for(Marker marker: markerList)
            marker.remove();
        markerList.clear();
        shape.remove();
        shape = null;
    }

    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
    }

    private boolean isGrantedPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    public void onPolygonClick(Polygon polygon) {
        Toast.makeText(this, "Total Location", Toast.LENGTH_SHORT).show();
        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                Toast.makeText(MapsActivity.this, "HHHHH", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setHomeMarker(Location location) {
        LatLng usrLoc = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions options = new MarkerOptions().position(usrLoc)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Your Location");

        homeMarker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(usrLoc,15));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(REQUEST_CODE == requestCode){
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0,locationListener);
            }
        }
    }
}

