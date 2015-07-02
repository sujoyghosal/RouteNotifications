package com.route.sujoy.routenotifications;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class Map extends Activity {

    private static GoogleMap map;
    private static int counter = 0;
    private Context context = this;
    private static String group = "";
    private static boolean subscribeRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Bundle b = getIntent().getExtras();

       RoutesUtils.markerArray.clear();
        if(b!=null){
            int pos = b.getInt("POSITION");
            Log.d("########Got position as:", String.valueOf(pos));
            if(pos>=0){
                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                        .getMap();
                RoutesUtils.Coordinates c =  RoutesUtils.routeStopsArray.get(pos).getCoordinates();
                if(c.getLatitude()==null || !RoutesUtils.isDouble(c.getLatitude())){
                    Log.d("#####Null latitude in Map class", "Returning");
                    return;
                }
                LatLng l =  new LatLng(Double.valueOf(c.getLatitude()), Double.valueOf(c.getLongitude()));
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                map.getUiSettings().setZoomGesturesEnabled(true);
                map.setTrafficEnabled(true);

                Marker m = map.addMarker(new MarkerOptions()
                        .position(l)
                        .title(RoutesUtils.routeStopsArray.get(pos).getStopName())
                        .snippet(RoutesUtils.routeStopsArray.get(pos).getDistance())
                        .icon(BitmapDescriptorFactory
                                .fromResource(RoutesUtils.showStops ? R.drawable.bus_stop : R.drawable.bus_small)));
 //                     .fromResource(R.drawable.generic_business_71)));
                RoutesUtils.markerArray.add(m);
                m.showInfoWindow();
                RoutesUtils.markerArray.add(m);
                trackBusesOnARoute();
                return;
            }
        }
        Log.d("#######Loading All Locations", ";-0");
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        LatLng l =  new LatLng(22,88);
        if(RoutesUtils.routeStopsArray!=null && RoutesUtils.routeStopsArray.size()>0) {
            showAllObjectsOnMap();
        }
        else {
                Marker m = map.addMarker(new MarkerOptions()
                    .position(l)
                    .title("Kolkata")
                    .snippet("West Bengal, India")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.pin_pink)));
                RoutesUtils.markerArray.add(m);
                m.showInfoWindow();
        }

       if(map!=null) {

           map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
               @Override
               public boolean onMarkerClick(Marker marker) {
                   Log.d("####", "This is from MarkerClick Handler....");

                   if (RoutesUtils.routeName != null && marker.getTitle() != null) {
                       group = RoutesUtils.routeName.trim().toUpperCase().replace(" ", "-")
                               + "-" + marker.getTitle().trim().toUpperCase().replace(" ", "-");
                       displayDialog(context, "Action", "Do you want to subscribe to stop " + marker.getTitle() + " for notification?");
//                       if (subscribeRequest)
//                           RoutesUtils.displayDialog(context, "Status", "Successfully sent subscription request");
                   }
                   marker.showInfoWindow();
                   return true;
               }


           });


       }
    }
    private void showAllObjectsOnMap() {
        {
            Log.d("##Map:","Setting markers for " + RoutesUtils.routeStopsArray.size() + "points");
            LatLng l =  new LatLng(22,88);
//            int k = RoutesUtils.routeStopsArray.size();
            PolylineOptions po = new PolylineOptions();
            Marker m;
            for (int i = 0; i < RoutesUtils.routeStopsArray.size(); i++) {
                RoutesUtils.Coordinates c = RoutesUtils.routeStopsArray.get(i).getCoordinates();
                if(c.getLatitude()==null || !RoutesUtils.isDouble(c.getLatitude()))
                    continue;
                l = new LatLng(Double.valueOf(c.getLatitude()), Double.valueOf(c.getLongitude()));
                Log.d("Adding Marker: ", l.toString());
                m = map.addMarker(new MarkerOptions()
                        .position(l)
                        .title(RoutesUtils.routeStopsArray.get(i).getStopName())
                        .snippet(RoutesUtils.routeStopsArray.get(i).getDistance())
                        .icon(BitmapDescriptorFactory.fromResource(RoutesUtils.showStops ? R.drawable.bus_stop : R.drawable.bus_small)));
                m.showInfoWindow();
                po.add(l);
                RoutesUtils.markerArray.add(m);
                m.setVisible(true);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 100));
                map.setBuildingsEnabled(true);
                map.setMyLocationEnabled(true);
                map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);

            }

                po.color(Color.RED);
                po.width(5);
                po.geodesic(true);
                if(RoutesUtils.showStops)
                    map.addPolyline(po);
                else {
                    trackBusesOnARoute();
                }
        }
    }
    private static void loadCurrentMarkerPositions() {
        if(!RoutesUtils.showStops) {
                counter++;
//                trackBusesOnARoute(); //refresh coordinates for all markers
                LatLng lastBus = null;
                for (int i = 0; i < RoutesUtils.routeStopsArray.size(); i++) {
                    for (int j = 0; j < RoutesUtils.markerArray.size(); j++) {
                        if (RoutesUtils.routeStopsArray.get(i).getStopName().equalsIgnoreCase(RoutesUtils.markerArray.get(j).getTitle())) {
                            Log.d("####","LatLng of Current Bus Location=" + RoutesUtils.routeStopsArray.get(i).getCoordinates().getLatitude() + "'"
                                    + RoutesUtils.routeStopsArray.get(i).getCoordinates().getLongitude());
                            lastBus = new LatLng(
                                    Double.valueOf(RoutesUtils.routeStopsArray.get(i).getCoordinates().getLatitude()) - 0.000006*counter,
                                    Double.valueOf(RoutesUtils.routeStopsArray.get(i).getCoordinates().getLongitude()) - 0.000006*counter);
                            RoutesUtils.markerArray.get(j).setPosition(lastBus);
                            Double dist = RoutesUtils.getDistanceFromLatLonInKm(Double.valueOf(RoutesUtils.getCurrentDeviceLatitude()),
                                    Double.valueOf(RoutesUtils.getCurrentDeviceLongitude()),lastBus.latitude,lastBus.longitude);
                            DecimalFormat myFormatter = new DecimalFormat("###.##");
                            Log.d("####","Distance=" + myFormatter.format(dist));
                            RoutesUtils.markerArray.get(j).setSnippet(myFormatter.format(dist));
                            Log.d("####","LastBus=" + lastBus.toString());
                        }
                    }
                }
            if(map!=null && lastBus!=null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastBus, 100));
                // Zoom in, animating the camera.
//                map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
            }
         }

    }

    private void displayDialog(Context ctx, String title, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        builder.setMessage(msg)
                .setTitle(title)
                .setIcon(R.drawable.bus_stop)
                .setInverseBackgroundForced(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        subscribeRequest = false;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        subscribeRequest = true;
                        String urls[] = new String[2];
                        urls[0] = "http://sujoyghosal-test.apigee.net/busroute/addusertogroup?user=" + RoutesUtils.loggedinUser.getUserEmail()
                                + "&group=" + Uri.encode(group);
                        new RouteListActivity.AssisgnUserToGroup().execute(urls);
                        RoutesUtils.displayDialog(context, "Status", "Successfully sent subscription request");
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private static void trackBusesOnARoute(){
//        RoutesUtils.routeStopsArray.clear();
        if(!RoutesUtils.showStops) {
            String[] urls = new String[2];
            urls[0] = "http://sujoyghosal-test.apigee.net/busroute/getcurrentbuslocations?route=" + Uri.encode(RoutesUtils.routeName);
            Log.d("####", "Get Bus Locations URL=" + urls[0]);
            new GetCurrentBusLocations().execute(urls);
        }
    }
    private static class GetCurrentBusLocations extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute(){
            RoutesUtils.showStops = false;
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = null;

            try {
                Thread.sleep(1000);
/*                URL routeurl = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) routeurl.openConnection();
                conn.connect();

                InputStream content = conn.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response += s;
                } */

            } catch (Exception e) {
                Log.e("Error!!!!", e.toString());

                return null;
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result){
            if(result == null) {
//                RoutesUtils.displayDialog(context,"Status","Could not get bus locations.");
                loadCurrentMarkerPositions();
                trackBusesOnARoute();
                return;
            }
            Log.d("GetCurrentBusLocation:",result);
            RoutesUtils.routeStopsArray.clear();
            try {
                JSONArray ja = new JSONArray(result);
                for(int i=0;i<ja.length();i++){
                    JSONObject jo = ja.getJSONObject(i);
                    RouteListActivity.RouteStopsObject bus = new RouteListActivity.RouteStopsObject();

                    if(jo.has("id") && jo.has("routeName"))
                        bus.setStopName(jo.getString("name"));
                    if(jo.has("uuid"))
                        bus.setUUID(jo.getString("uuid"));
                    if(jo.has("location")) {
                        JSONObject location = jo.getJSONObject("location");
                        if (location != null && location.has("latitude") && location.has("longitude")) {
                            bus.setCoordinates(new RoutesUtils.Coordinates(location.getString("latitude"), location.getString("longitude"), ""));

                            if (location.getString("latitude") != null && !location.getString("latitude").isEmpty()
                                    && location.getString("longitude") != null && !location.getString("longitude").isEmpty()) {
                                Double dist = RoutesUtils.getDistanceFromLatLonInKm(Double.valueOf(RoutesUtils.getCurrentDeviceLatitude()),
                                        Double.valueOf(RoutesUtils.getCurrentDeviceLongitude()),
                                        Double.valueOf(location.getString("latitude")),
                                        Double.valueOf(location.getString("longitude")));
                                DecimalFormat myFormatter = new DecimalFormat("###.#");
                                bus.setDistance(myFormatter.format(dist));
                                Log.d("####", "Distance=" + myFormatter.format(dist));
                            }
                        }
                        RoutesUtils.routeStopsArray.add(bus);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            loadCurrentMarkerPositions();
            trackBusesOnARoute();
        }
    }

    public void performShare(String shareMesg){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMesg);
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

}