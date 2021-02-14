package com.assignment.maps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener {


    Geocoder geo;
    TextView txtMarkers;

    private static final int REQUEST_CODE = 1;
    private static final int POLYGON_SIDES = 4;

    private GoogleMap mMap;

    private Marker homeMarker;
    private Marker destMarker;

    List<Marker> citiesList = new ArrayList<>();
    ArrayList<Character> letterList = new ArrayList<>();

    Character firstCity = 'A';
    Character[] citiesArr = {'A','B','C','D'};

    String titleStr = "";
    String snippetStr = "";

    Polyline line;
    Polygon shape;

    List<Marker> markersList = new ArrayList<>();
    LocationManager locationManager;
    LocationListener locationListener;

    ArrayList<String> addressArr = new ArrayList<>();
    ArrayList<String> snippetArr = new ArrayList<>();


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
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMapClickListener(this);

        mMap.getUiSettings().setZoomControlsEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //set home marker
                // setHomeMarker(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (!isGrantedPermission()) {
            requestLocationPermission();
        } else {
            startUpdatingLocation();
            LatLng canLatLong = new LatLng( 43.65,-79.35);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(canLatLong, 5));
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                System.out.println("clicked"+marker.isInfoWindowShown());
                if(marker.isInfoWindowShown()){
                    marker.hideInfoWindow();
                }
                else{
                    marker.showInfoWindow();
                }
                return true;
            }
        });

    }

    private void setMarker(LatLng latLng) {

        Geocoder geoCoder = new Geocoder(this);
        Address address = null;

        try
        {
            List<Address> matches = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = (matches.isEmpty() ? null : matches.get(0));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        if(address != null){
            if(address.getSubThoroughfare() != null)
            {
                addressArr.add(address.getSubThoroughfare());

            }
            if(address.getThoroughfare() != null)
            {

                addressArr.add(address.getThoroughfare());

            }
            if(address.getPostalCode() != null)
            {

                addressArr.add(address.getPostalCode());

            }
            if(addressArr.isEmpty())
            {
                addressArr.add("New City");
            }
            if(address.getLocality() != null)
            {
                snippetArr.add(address.getLocality());
            }
            if(address.getAdminArea() != null)
            {
                snippetArr.add(address.getAdminArea());
            }
        }

        titleStr = TextUtils.join(", ",addressArr);
        titleStr = (titleStr.equals("") ? "  " : titleStr);

        snippetStr = TextUtils.join(", ",snippetArr);

        MarkerOptions options = new MarkerOptions().position(latLng)
                .draggable(true)
                .title(titleStr)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .snippet(snippetStr);

        // Condition for counting markers  and clearing markers
        if (markersList.size() == POLYGON_SIDES)
        {
            clearMap();
        }

        Marker mrk = mMap.addMarker(options);
        markersList.add(mrk);

        if (markersList.size() == POLYGON_SIDES) {
            drawShape();
        }

        for(Character letter: citiesArr){
            if(letterList.contains(letter)){
                continue;
            }
            firstCity = letter;
            break;
        }

        LatLng mLatLng = new LatLng(latLng.latitude - 0.50,latLng.longitude);
        MarkerOptions optionsCityLabel = new MarkerOptions().position(mLatLng)
                .draggable(true)
                .icon(setText(firstCity.toString()))
                .snippet(snippetStr);
        Marker letterMarker = mMap.addMarker(optionsCityLabel);

    }

    private void drawShape() {
        PolygonOptions options = new PolygonOptions()
                .fillColor(Color.GREEN)
                .strokeColor(Color.RED)
                .strokeWidth(5);

        for(int i=0; i<POLYGON_SIDES; i++)
            options.add(markersList.get(i).getPosition());

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

        for(Marker marker: markersList)
            marker.remove();
        markersList.clear();
        shape.remove();
        shape = null;
    }

    private void startUpdatingLocation() {
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

    public BitmapDescriptor setText(String text) {

        Paint textFormat = new Paint();

        textFormat.setTextSize(50);
        textFormat.setColor(Color.argb(100, 0, 0, 0));
        textFormat.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        float Width = textFormat.measureText(text);
        float Height = textFormat.getTextSize();

        int txtWidth = (int) (Width);
        int txtHeight = (int) (Height);

        Bitmap bmpImg = Bitmap.createBitmap(txtWidth, txtHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpImg);
        canvas.translate(0, txtHeight);

        canvas.drawText(text, 0, 0, textFormat);
        return BitmapDescriptorFactory.fromBitmap(bmpImg);
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

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

        setMarker(latLng);

    }
}