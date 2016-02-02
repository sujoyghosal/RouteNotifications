package com.route.sujoy.routenotifications;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.apigee.sdk.data.client.DataClient;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;


public class MainActivity extends Activity {

    private Context context = this;
    private static boolean registered = false;
    private static ProgressDialog progressDialog;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RoutesUtils.showStops = true;
        new RegisterPush().execute("");
        progressDialog = ProgressDialog.show(this, "Status", "Getting Routes..please wait", true, true);
        if (RoutesUtils.allRoutesArray == null || RoutesUtils.allRoutesArray.isEmpty()) {
            new GetAllRoutes().execute("");
        } else
            loadRouteNames();
        linkUserToDevice();
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void startActivityCreateRoute(View v) {
        Intent in = new Intent(this, CreateRoute.class);
        startActivity(in);
    }

    private void linkUserToDevice() {
        String urls[] = new String[2];
        DataClient dc = AppServices.getClient(context);
        if (dc != null) {
            urls[0] = "http://sujoyghosal-test.apigee.net/busroute/addusertodevice?uuid=" + RoutesUtils.loggedinUser.getUuid()
                    + "&deviceid=" + dc.getUniqueDeviceID().toString();
        }
        Log.d("#linkUserToDevice#", urls[0]);
        new LinkUserToDevice().execute(urls);
    }

    private void loadRouteNames() {
        Spinner spinnerRoutes = (Spinner) findViewById(R.id.spinnerRoutes);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, RoutesUtils.routeNames); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoutes.setAdapter(spinnerArrayAdapter);
        spinnerRoutes.setOnItemSelectedListener(new CustomSpinnerSelectionListener());

    }

    public void getStopsForARoute(View view) {
        RoutesUtils.routeStopsArray.clear();
        RoutesUtils.showStops = true;
        if (!RoutesUtils.allRoutesArray.isEmpty()) {
            for (int i = 0; i < RoutesUtils.allRoutesArray.size(); i++) {
                RouteObject aObject = RoutesUtils.allRoutesArray.get(i);
                if (aObject.getRouteName().equalsIgnoreCase(RoutesUtils.routeName)) {
                    for (int k = 0; k < aObject.getBusStopsArray().size(); k++) {
                        RoutesUtils.routeStopsArray.add(aObject.getBusStopsArray().get(k));
                    }
                    break;
                }
            }
            startActivity(new Intent(context, RouteListActivity.class));
        }
    }

    public void trackBusesNearMe(View v) {
        RoutesUtils.routeStopsArray.clear();
        String[] urls = new String[2];
        urls[0] = "http://sujoyghosal-test.apigee.net/busroute/getbusesnearme?radius=15000&latitude=" +
                RoutesUtils.getCurrentDeviceLatitude() + "&longitude=" + RoutesUtils.getCurrentDeviceLongitude();

        Log.d("####", "Get Buses Near Me URL=" + urls[0]);
        new GetCurrentBusLocations().execute(urls);
    }

    public void trackBusesOnARoute(View v) {
        RoutesUtils.routeStopsArray.clear();

        String[] urls = new String[2];
        urls[0] = "http://sujoyghosal-test.apigee.net/busroute/getcurrentbuslocations?route=" + Uri.encode(RoutesUtils.routeName);
        urls[1] = "BUSES";
        Log.d("####", "Get Bus Locations URL=" + urls[0]);
        new GetCurrentBusLocations().execute(urls);

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.route.sujoy.routenotifications/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.route.sujoy.routenotifications/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class RegisterPush extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";

            try {
                if (!registered) {
                    GCMRegistrar.checkDevice(context);
                    GCMRegistrar.checkManifest(context);
                    AppServices.loginAndRegisterForPush(context);
                    registered = true;
                }

            } catch (Exception e) {
                Log.e("Error!!!!", e.toString());

                return null;
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {


        }
    }


    private class GetCurrentBusLocations extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            RoutesUtils.showStops = false;
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";

            try {
                URL routeurl = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) routeurl.openConnection();
                conn.connect();

                InputStream content = conn.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s;
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }

            } catch (Exception e) {
                Log.e("Error!!!!", e.toString());

                return null;
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                RoutesUtils.displayDialog(context, "Status", "Could not get bus locations.");
                return;
            }
            RoutesUtils.routeStopsArray.clear();
            try {
                JSONArray ja = new JSONArray(result);
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    RouteListActivity.RouteStopsObject bus = new RouteListActivity.RouteStopsObject();
                    bus.setStopName(jo.getString("routeName"));
                    if (jo.has("id")) {
                        bus.setAddress(jo.getString("id"));
                        bus.setStopName(jo.getString("routeName") + "-" + jo.getString("id"));
                    }
                    if (jo.has("uuid"))
                        bus.setUUID(jo.getString("uuid"));
                    if (jo.has("location")) {
                        JSONObject location = jo.getJSONObject("location");
                        if (location != null && location.has("latitude") && location.has("longitude"))
                            bus.setCoordinates(new RoutesUtils.Coordinates(location.getString("latitude"), location.getString("longitude"), ""));

/*                        if(location.getString("latitude")!=null && !location.getString("latitude").isEmpty()
                                && location.getString("longitude")!=null && !location.getString("longitude").isEmpty()) {
                            Double dist = RoutesUtils.getDistanceFromLatLonInKm(Double.valueOf(RoutesUtils.getCurrentDeviceLatitude()),
                                    Double.valueOf(RoutesUtils.getCurrentDeviceLongitude()),
                                    Double.valueOf(location.getString("latitude")),
                                    Double.valueOf(location.getString("longitude")));
                            DecimalFormat myFormatter = new DecimalFormat("###.#");
                            bus.setDistance(myFormatter.format(dist));
                            Log.d("####", "Distance=" + myFormatter.format(dist));
                        }*/
                        RoutesUtils.routeStopsArray.add(bus);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            startActivity(new Intent(context, RouteListActivity.class));

        }
    }


    public void performShare(String shareMesg) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareMesg);
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private class LinkUserToDevice extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            System.out.println("GetUserByEmail URL=" + urls[0]);

            try {
                URL routeurl = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) routeurl.openConnection();
                conn.connect();

                InputStream content = conn.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s;
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }

            } catch (Exception e) {
                Log.e("Error!!!!", e.toString());

                return null;
            }
            Log.d("LinkUserToDevice Call Response Received:", response);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null || result.equalsIgnoreCase("[]")) {
                Log.e("Could not link:", "Device already linked possibly..");
                return;
            }

            Log.i("LinkUserToDevice", "Success. Result = " + result);

        }

    }

    private class GetAllRoutes extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            RoutesUtils.showStops = true;

        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";

            String url = "http://sujoyghosal-test.apigee.net/busroute/allroutes";

            try {
                URL routeurl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) routeurl.openConnection();
                conn.connect();

                InputStream content = conn.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s;
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }

            } catch (Exception e) {
                Log.e("Error!!!!", e.toString());
                return null;
            }

            Log.d("GetAllRoutes Call Response Received:", response);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {

            if (progressDialog != null)
                progressDialog.dismiss();
            if (result == null)
                return;
            Log.i("GetAllRoutes", "Success. Result = " + result);
            try {
                RoutesUtils.allRoutesArray.clear();
                JSONArray ar = new JSONArray(result);
                Log.d("##Number of routes found: ", "" + ar.length());
                for (int i = 0; i < ar.length(); i++) {
                    JSONObject oe = ar.getJSONObject(i);
                    RouteObject routeObject = new RouteObject();
                    routeObject.setRouteName(oe.getString("name"));
//                    RouteListActivity.RouteStopsObject rsObject = new RouteListActivity.RouteStopsObject();

                    if (oe.has("description"))
                        routeObject.setRouteDesc(oe.getString("description"));
                    if (oe.has("uuid"))
                        routeObject.setUUID(oe.getString("uuid"));
                    if (oe.has("bus_locations")) {
                        JSONArray bus_locations = oe.getJSONArray("bus_locations");

                        if (bus_locations != null && bus_locations.length() > 0) {
                            for (int j = 0; j < bus_locations.length(); j++) {
                                JSONObject bl = bus_locations.getJSONObject(j);
                                String bus_id = "";
                                String lat;
                                String lng;
                                if (bl.has("busID"))
                                    bus_id = bl.getString("busID");
                                if (bl.getJSONObject("location") != null && bl.getJSONObject("location").has("latitude")
                                        && bl.getJSONObject("location").has("longitude")) {
                                    lat = bl.getJSONObject("location").getString("bus_latitude");
                                    lng = bl.getJSONObject("location").getString("bus_longitude");
                                    routeObject.busLocationsArray.add(new RoutesUtils.Coordinates(bus_id, lat, lng));
                                }
                            }
                        }
                    }

                    if (oe.has("bus_stops")) {
                        JSONArray bus_stops = oe.getJSONArray("bus_stops");
                        if (bus_stops != null && bus_stops.length() > 0) {
                            for (int j = 0; j < bus_stops.length(); j++) {
                                JSONObject bs = bus_stops.getJSONObject(j);
                                RouteListActivity.RouteStopsObject busStop = new RouteListActivity.RouteStopsObject();
                                if (bs.has("stop_name"))
                                    busStop.setStopName(bs.getString("stop_name"));
                                if (bs.has("street"))
                                    busStop.setStreet(bs.getString("street"));
                                if (bs.has("address_line2"))
                                    busStop.setAddress2(bs.getString("address_line2"));
                                if (bs.has("city"))
                                    busStop.setCity(bs.getString("city"));
                                if (bs.has("state"))
                                    busStop.setState(bs.getString("state"));
                                if (bs.has("country"))
                                    busStop.setCountry(bs.getString("country"));
                                if (bs.has("postal_code"))
                                    busStop.setPC(bs.getString("postal_code"));

                                busStop.setAddress(busStop.getStreet() + "," + busStop.getCity());

                                if (bs.has("location")) {
                                    JSONObject location = bs.getJSONObject("location");
                                    if (location != null && location.has("latitude") && location.has("longitude")) {
                                        busStop.setCoordinates(new RoutesUtils.Coordinates(location.getString("latitude"), location.getString("longitude"), ""));
                                        if (location.getString("latitude") != null && RoutesUtils.isDouble(location.getString("latitude"))
                                                && location.getString("longitude") != null && RoutesUtils.isDouble(location.getString("longitude"))) {
                                            try {
                                                Double dist = RoutesUtils.getDistanceFromLatLonInKm(Double.valueOf(RoutesUtils.getCurrentDeviceLatitude()),
                                                        Double.valueOf(RoutesUtils.getCurrentDeviceLongitude()),
                                                        Double.valueOf(location.getString("latitude")),
                                                        Double.valueOf(location.getString("longitude")));
                                                DecimalFormat myFormatter = new DecimalFormat("###.#");
                                                busStop.setDistance(myFormatter.format(dist));
                                            } catch (Exception e) {
                                                Log.d("####", "Could not set distance");
                                                routeObject.busStopsArray.add(busStop);
                                                continue;
                                            }
                                        }
                                    }

                                }
                                routeObject.busStopsArray.add(busStop);
//                                RoutesUtils.allStopsArray.add(busStop);
                            }
                        }
                    }

                    RoutesUtils.allRoutesArray.add(routeObject);
                }
                Log.d("####", "Populating Route Names - All Stops Array Size Right Now is " + RoutesUtils.allRoutesArray.size());
                if (RoutesUtils.allRoutesArray != null && RoutesUtils.allRoutesArray.size() > 0) {
                    for (int i = 0; i < RoutesUtils.allRoutesArray.size(); i++) {
                        if (!RoutesUtils.routeNames.contains(RoutesUtils.allRoutesArray.get(i).getRouteName()))
                            RoutesUtils.routeNames.add(RoutesUtils.allRoutesArray.get(i).getRouteName());
                    }
                }
                loadRouteNames();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        RoutesUtils.showStops = true;
        if (RoutesUtils.allRoutesArray == null || RoutesUtils.allRoutesArray.isEmpty()) {
            progressDialog = ProgressDialog.show(context, "Status", "Getting Routes..please wait", true, true);
            new GetAllRoutes().execute("");
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

}
