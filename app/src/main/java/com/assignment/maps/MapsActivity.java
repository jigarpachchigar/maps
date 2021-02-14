package com.assignment.maps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,  GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {

    private static final int REQUEST_CODE = 1;
    private static final int POLYGON_SIDES = 4;

    private GoogleMap mMap;

    private Marker homeMarker;
    private Marker destMarker;

    List<Marker> markersList = new ArrayList<>();
    List<Marker> markersDistList = new ArrayList<>();
    ArrayList<Polyline> lineList = new ArrayList<>();

    List<Marker> citiesList = new ArrayList<>();
    ArrayList<Character> letterList = new ArrayList<>();

    Character firstCity = 'A';
    Character[] citiesArr = {'A','B','C','D'};

    HashMap<LatLng, Character> markerHashMap = new HashMap<>();

    ArrayList<String> addressArr = new ArrayList<>();
    ArrayList<String> snippetArr = new ArrayList<>();

    String titleStr = "";
    String snippetStr = "";

    LocationManager locationManager;
    LocationListener locationListener;

    Polygon shape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void startUpdatingLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
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

        if (!isGivenLocPermission()) {
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

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

                if (markersList.size() == POLYGON_SIDES) {
                    for(Polyline line: lineList){
                        line.remove();
                    }
                    lineList.clear();

                    shape.remove();
                    shape = null;

                    for(Marker currMarker: markersDistList){
                        currMarker.remove();
                    }
                    markersDistList.clear();
                    drawShape();
                }
            }
        });
    }

    private boolean isGivenLocPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }

    private void setMarker(LatLng latLng){

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

        citiesList.add(letterMarker);
        letterList.add(firstCity);
        markerHashMap.put(letterMarker.getPosition(), firstCity);
    }

    private void clearMap() {

        //        if(destMarker != null){
        //   destMarker.remove();
        //   destMarker = null;
        //}

        //line.remove();
        for (Marker marker : markersList) {
            marker.remove();
        }
        markersList.clear();

        for(Polyline line: lineList){
            line.remove();
        }
        lineList.clear();

        shape.remove();
        shape = null;

        for( Marker marker: citiesList){
            marker.remove();
        }
        citiesList.clear();

        for (Marker marker : markersDistList) {
            marker.remove();
        }
        markersDistList.clear();

    }

    // To Draw line
      /*  private void drawLine() {
            PolylineOptions options = new PolylineOptions()
                    .color(Color.BLACK)
                    .width(10)
                    .add(homeMarker.getPosition(),destMarker.getPosition());

            line = mMap.addPolyline(options);
        } */

    private void drawShape (){
        PolygonOptions options = new PolygonOptions()
                .fillColor(Color.argb(45, 0, 255, 0))
                .strokeColor(Color.RED);

        LatLng[] citiesConvex = new LatLng[POLYGON_SIDES];
        for (int i = 0; i < POLYGON_SIDES; i++) {
            citiesConvex[i] = new LatLng(markersList.get(i).getPosition().latitude,
                    markersList.get(i).getPosition().longitude);
        }

        Vector<LatLng> polyLatLong = MapPoints.convexHull(citiesConvex, POLYGON_SIDES);

        Vector<LatLng> resolveLatLng =  new Vector<>();

        int n = 0;
        for (int i = 0; i < markersList.size(); i++)
            if (markersList.get(i).getPosition().latitude < markersList.get(n).getPosition().latitude)
                n = i;

        Marker current = markersList.get(n);
        resolveLatLng.add(current.getPosition());

        while(resolveLatLng.size() != POLYGON_SIDES){

            double m1Distance = Double.MAX_VALUE;
            Marker closerMarker  = null;

            for(Marker marker: markersList){
                if(resolveLatLng.contains(marker.getPosition())){
                    continue;
                }

                double currentDist = CalculateDistance(current.getPosition().latitude,
                        current.getPosition().longitude,
                        marker.getPosition().latitude,
                        marker.getPosition().longitude);

                if(currentDist < m1Distance){
                    m1Distance = currentDist;
                    closerMarker = marker;
                }
            }

            if(closerMarker != null){
                resolveLatLng.add(closerMarker.getPosition());
                current = closerMarker;
            }
        }

        // Adding polygon latlong
        options.addAll(polyLatLong);
        shape = mMap.addPolygon(options);
        shape.setClickable(true);

        // draw polyline
        LatLng[] polyLinePoints = new LatLng[polyLatLong.size() + 1];
        int index = 0;
        for (LatLng x : polyLatLong) {
            polyLinePoints[index] = x;

            index++;
            if (index == polyLatLong.size()) {
                // add initial point
                polyLinePoints[index] = polyLatLong.elementAt(0);
            }
        }

        for(int i =0 ; i<polyLinePoints.length -1 ; i++){

            LatLng[] tempArr = {polyLinePoints[i], polyLinePoints[i+1] };
            Polyline currentPolyline =  mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .add(tempArr)
                    .color(Color.RED));
            currentPolyline.setClickable(true);
            lineList.add(currentPolyline);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        setMarker(latLng);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        double m2Distance = Double.MAX_VALUE;

        if(markersList.size() == 0){
            return;
        }

        Marker closeByMarker = null;

        for(Marker marker: markersList){
            double currDistance = CalculateDistance(marker.getPosition().latitude,
                    marker.getPosition().longitude,
                    latLng.latitude,
                    latLng.longitude);
            if(currDistance < m2Distance){
                m2Distance = currDistance;
                closeByMarker = marker;
            }
        }

        if(closeByMarker != null){

            final Marker finalCloserMarker = closeByMarker;
            AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
            deleteDialog
                    .setTitle("Delete Marker")
                    .setMessage("Are you sure want to delete the marker?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            finalCloserMarker.remove();
                            markersList.remove(finalCloserMarker);

                            letterList.remove(markerHashMap.get(finalCloserMarker.getPosition()));
                            markerHashMap.remove(finalCloserMarker);

                            for(Polyline polyline: lineList){
                                polyline.remove();
                            }
                            lineList.clear();

                            if(shape != null){
                                shape.remove();
                                shape = null;
                            }

                            for(Marker currMarker: markersDistList){
                                currMarker.remove();
                            }
                            markersDistList.clear();

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finalCloserMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker));

                        }
                    });
            AlertDialog dialog = deleteDialog.create();
            dialog.show();
        }
    }

    /*private void setHomeMarker(Location location) {
        LatLng usrLoc = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions options = new MarkerOptions().position(usrLoc)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Your Location");

        homeMarker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(usrLoc,15));
    }*/

    private double CalculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
//    private BitmapDescriptor bitmapDescriptorFromVector(Context context,int vectorResId){
//        Drawable vectorDrawable = ContextCompat.getDrawable(context,vectorResId);
//        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight());
//        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        vectorDrawable.draw(canvas);
//        return  BitmapDescriptorFactory.fromBitmap(bitmap);
//    }


    public String getMarkerDistance(Polyline polyline){

        List<LatLng> points = polyline.getPoints();
        LatLng firstPoint = points.remove(0);
        LatLng secondPoint = points.remove(0);

        double distance = CalculateDistance(firstPoint.latitude,firstPoint.longitude,
                secondPoint.latitude,secondPoint.longitude);
        NumberFormat formatter = new DecimalFormat("#0.0");
        return formatter.format(distance) + " Km.";
    }

    public String getTotalDistance(ArrayList<Polyline> polylines){

        double totalDistance = 0;
        for(Polyline polyline : polylines){
            List<LatLng> points = polyline.getPoints();
            LatLng firstPoint = points.remove(0);
            LatLng secondPoint = points.remove(0);


            double distance = CalculateDistance(firstPoint.latitude,firstPoint.longitude,
                    secondPoint.latitude,secondPoint.longitude);
            totalDistance += distance;

        }
        NumberFormat formatter = new DecimalFormat("#0.0");

        return formatter.format(totalDistance) + " Km.";
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        List<LatLng> points = polyline.getPoints();
        LatLng firstPoint = points.remove(0);
        LatLng secondPoint = points.remove(0);

        LatLng center = LatLngBounds.builder().include(firstPoint).include(secondPoint).build().getCenter();
        MarkerOptions options = new MarkerOptions().position(center)
                .draggable(true)
                .icon(setText(getMarkerDistance(polyline)));
        markersDistList.add(mMap.addMarker(options));
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for(LatLng point: polygon.getPoints()){
            builder.include(point);
        }
        LatLng center = builder.build().getCenter();
        MarkerOptions options = new MarkerOptions().position(center)
                .draggable(true)
                .icon(setText(getTotalDistance(lineList)));
        markersDistList.add(mMap.addMarker(options));
    }

    private static class MapPoints {

        public static int scale(LatLng p1, LatLng p2, LatLng p3)
        {

            double val = (p2.longitude - p1.longitude) * (p3.latitude - p2.latitude) - (p2.latitude - p1.latitude) * (p3.longitude - p2.longitude);

            if (val == 0) return 0;

            return (val > 0)? 1: 2;
        }

        public static Vector<LatLng> convexHull(LatLng markers[], int n)
        {
            // Initialize Result
            Vector<LatLng> covhull = new Vector<LatLng>();

            // Check for Minimum 3 points
            if (n < 3){
                covhull.addAll(Arrays.asList(markers));
                return covhull;
            }

            // Find leftmost point
            int l = 0;
            for (int i = 1; i < n; i++)
                if (markers[i].latitude < markers[l].latitude)
                    l = i;

            int x = l, y;
            do
            {
                // Add current point to result
                covhull.add(markers[x]);

                y = (x + 1) % n;

                for (int i = 0; i < n; i++)
                {
                    if (scale(markers[x], markers[i], markers[y]) == 2)
                        y = i;
                }

                // y added to result
                x = y;

            } while (x != l);

            // Return Result
            return covhull;
        }
    }
}
