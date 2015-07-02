package com.route.sujoy.routenotifications;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class CreateRoute extends Activity {

    private final Context context = this;
    private EditText etName;
    private EditText etStopName;
    private EditText etStreet;
    private EditText etAddress2;
    private EditText etCity;
    private EditText etState;
    private EditText etCountry;
    private EditText etPC;
    private TextView tvResp;
    private EditText etDesc;

    private String validateError = "";
    private static String shopLatitude ="";
    private static String shopLongitude = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_route);
        RoutesUtils.loadDeviceLocation(this);
        etName = (EditText)findViewById(R.id.editTextName);
        etStopName = (EditText)findViewById(R.id.editTextStopName);
        etStreet = (EditText)findViewById(R.id.editTextStreet);
        etCity = (EditText)findViewById(R.id.editTextCity);
        etState = (EditText)findViewById(R.id.editTextState);
        etCountry = (EditText)findViewById(R.id.editTextCountry);
        etPC = (EditText)findViewById(R.id.editTextPC);
        etDesc = (EditText)findViewById(R.id.editTextDesc);
        etAddress2 = (EditText)findViewById(R.id.editTextAddress2);


        tvResp = (TextView)findViewById(R.id.textViewResp);
    }

    public void sendCreateRouteRequest(View v){
        if(!validateInputs()){
            RoutesUtils.displayDialog(context,"Input Issues",validateError);
            return;
        }
        geoCodeBusStopAddress();
    }

    private void geoCodeBusStopAddress(){
        String base = "https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyBVkYvETl2eSWet_AkIVaLRGJaN1U95sa4&address=";
        String url = etStreet.getText().toString() + ","
                + etCity.getText().toString() + ","
                + etState.getText().toString() + ","
                + etCountry.getText().toString();

        String params[] = new String[2];
        params[0] = base + Uri.encode(url);
                new GeoCodeAddress().execute(params);
    }
    private boolean validateInputs(){
        validateError = "";
        if(etName.length()<2){
            validateError = "Name cannot be empty or less than 2 characters";
            return false;
        }
        if(etStopName.length()<2){
            validateError = "Stop Name cannot be empty or less than 2 characters";
            return false;
        }
        if(etStreet.length()<2){
            validateError = "Street cannot be empty or less than 2 characters";
            return false;
        }
        if(etCity.length()<2){
            validateError = "City cannot be empty or less than 2 characters";
            return false;
        }

        if(etCountry.length()<2){
            validateError = "Country cannot be empty or less than 2 characters";
            return false;
        }
        if(validateError!=null && validateError.isEmpty()) {
            validateError = "Submitting Create Checkin Request";
            tvResp.setText(validateError);
            return true;
        } else {

            return false;
        }
    }


    private class CallCreateRouteAPI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            String url = urls[0];
            System.out.println("Create Route URL=" + url);

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
                e.printStackTrace();
                return null;
            }
            Log.d("Response:", response);
            return response;
        }


        protected void onPostExecute(String result){
            if(result==null){
                Log.e("##Error!!!!", "Could not create route.");
                RoutesUtils.displayDialog(context, "Create Route Status: ", "Failed to complete  request.");
                return;
            } else if(result.trim().equalsIgnoreCase("true")){
                Log.e("##Error!!!!", "Could not create route. Response = true.");
                RoutesUtils.displayDialog(context, "Create Route Status: ", "Failed to complete  request. Route Exists With Same Name, need to call Update.");
                return;
            }
            Log.d("####Create Route response received=", result);
            tvResp.setText("Create Route Status:" + result);
            RoutesUtils.displayDialog(context, "Create Route Status", "You have successfully created route/stops.");
            AppServices.sendNotificationToAll(context, "The bus route named '" + etName.getText().toString() +
                    "'  has been created/updated in " + etCity.getText().toString() + ", " + etCountry.getText().toString());
/*            try {
                JSONObject j = new JSONObject(result);
                if(j!=null && j.has("_data")){
                    JSONObject jd = j.getJSONObject("_data");
                    if(jd!=null && jd.has("uuid"))

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }*/

                String urls[] = new String[2];
                urls[0] = "http://sujoyghosal-test.apigee.net/busroutenocache/creategroup?group=" +
                        etName.getText().toString().trim().toUpperCase().replace(" ","-") +
                        "-" + etStopName.getText().toString().trim().toUpperCase().replace(" ","-");
                new CallCreateGroupAPI().execute(urls);
//                new MainActivity.GetAllRoutes().execute("");
                RoutesUtils.allRoutesArray.clear();
        }
    }
    private class CallCreateGroupAPI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            String url = urls[0];
            System.out.println("Create Group URL=" + url);

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
                e.printStackTrace();
                return null;
            }
            Log.d("Response:", response);
            return response;
        }


        protected void onPostExecute(String result){
            if(result==null || result.equalsIgnoreCase("true")){
                Log.e("##Error!!!!", "Group Exists");
                return;
            }
            Log.d("####Create Group response received=", result);
        }
    }
    private class CallUpdateRouteAPI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            String url = urls[0];
            Log.d("Update Route URL=",url);

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
                e.printStackTrace();
                return null;
            }
            Log.d("Response:", response);
            return response;
        }


        protected void onPostExecute(String result){

            if(result!=null && result.equalsIgnoreCase("true")){
                Log.e("##Error!!!!", "Stop Exists");
                RoutesUtils.displayDialog(context,"Status","Error Adding New Bus Stop to Route :(");
                return;
            }
            Log.d("####Update Route response received=", result);
            RoutesUtils.displayDialog(context, "Status", "Success adding bus stop to route :)");
            AppServices.sendNotificationToAll(context, "A new bus stop " + etStopName.getText().toString() + "has been added to route "
                    + etName.getText().toString() + " in " + etCity.getText().toString() + ", " + etCountry.getText().toString());
//                new MainActivity.GetAllRoutes().execute();
            RoutesUtils.allRoutesArray.clear();

                String urls[] = new String[2];
                urls[0] = "http://sujoyghosal-test.apigee.net/busroutenocache/creategroup?group=" +
                        etName.getText().toString().trim().toUpperCase().replace(" ","-") +
                        "-" + etStopName.getText().toString().trim().toUpperCase().replace(" ","-");
                new CallCreateGroupAPI().execute(urls);
        }
    }
    private class GeoCodeAddress extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            Log.d("Geocode URL=",urls[0]);

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
            }
            Log.d("GeoCode Call Response Received:", response);
            return response;
        }

        @Override
        protected void onPostExecute(String result){

            if (result==null)
                return;

            try {
                JSONObject j = new JSONObject(result);
                JSONArray jr = j.getJSONArray("results");
                for(int i=0; i<jr.length(); i++){
                    JSONObject location = jr.getJSONObject(i).getJSONObject("geometry").getJSONObject("location");
                    shopLatitude = location.getString("lat");
                    shopLongitude = location.getString("lng");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Error Parsing JSON Response for GeoCode call", Toast.LENGTH_LONG);
                return;
            }
                Log.i("####GeoCoded address, latitude/longitude=", shopLatitude + "/" + shopLongitude);
            createOrUpdateRoute();
        }
    }

    private void createOrUpdateRoute(){
        RouteObject currRoute = null;
        for(int i=0; i<RoutesUtils.allRoutesArray.size(); i++){
            RouteObject r = RoutesUtils.allRoutesArray.get(i);
            if(r.getRouteName().equalsIgnoreCase(etName.getText().toString())){
                currRoute = r;
                break;
            }
        }
        if(currRoute!=null){ //route exists
            Log.d("##Curr Route Found:", currRoute.getRouteName());
            for(int i=0; i<currRoute.getBusStopsArray().size();i++){
                RouteListActivity.RouteStopsObject rs = currRoute.getBusStopsArray().get(i);
                Log.d("rs.getStopName,etStopName.getText()",rs.getStopName() + "=" + etStopName.getText().toString());
                if(rs.getStopName().trim().equalsIgnoreCase(etStopName.getText().toString().trim())){
                    Log.d("##Received request for Stop:", "Route and Stop name are same. Returning..");
                    RoutesUtils.displayDialog(context,"Status","This stopname already exists on this route");
                    return;
                }
            }
            //To do - new stop on existing route - update route
            Log.d("##Calling Route Update:", currRoute.getRouteName() + "-" + etStopName.getText().toString());
            RouteListActivity.RouteStopsObject o = new RouteListActivity.RouteStopsObject();
            o.setStopName(etStopName.getText().toString());
            o.setCity(etCity.getText().toString());
            o.setStreet(etStreet.getText().toString());
            o.setAddress2(etAddress2.getText().toString());
            o.setState(etState.getText().toString());
            o.setCountry(etCountry.getText().toString());
            o.setPC(etPC.getText().toString());
            o.setCoordinates(new RoutesUtils.Coordinates(shopLatitude, shopLongitude,""));
            currRoute.busStopsArray.add(o);
            Log.d("##Curr Object After adding New Stop:", currRoute.toString());
            JSONArray ja = new JSONArray();
            JSONObject mainObj = new JSONObject();
            try {
                for(int i=0; i<currRoute.busStopsArray.size();i++) {
                    JSONObject jo = new JSONObject();
                    jo.put("route_name", currRoute.getRouteName());
                    jo.put("description", currRoute.getRouteDesc());
                    jo.put("stop_name", currRoute.busStopsArray.get(i).getStopName());
                    jo.put("street", currRoute.busStopsArray.get(i).getStreet());
                    jo.put("address_line2", currRoute.busStopsArray.get(i).getAddress2());
                    jo.put("city", currRoute.busStopsArray.get(i).getCity());
                    jo.put("state", currRoute.busStopsArray.get(i).getState());
                    jo.put("postal_code", currRoute.busStopsArray.get(i).getPC());
                    jo.put("country", currRoute.busStopsArray.get(i).getCountry());
                    JSONObject locationOb = new JSONObject();
                    locationOb.put("latitude",currRoute.busStopsArray.get(i).getCoordinates().getLatitude());
                    locationOb.put("longitude",currRoute.busStopsArray.get(i).getCoordinates().getLongitude());
                    jo.put("location", locationOb);
                    ja.put(jo);
                }
                mainObj.put("bus_stops", ja);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("##Error!!!!", "Could not create routes object for update.");
                RoutesUtils.displayDialog(context, "Status: ", "Failed to add new bus stop.");
                return;
            }
            String urls[] = new String[2];
            String base = "http://sujoyghosal-test.apigee.net/busroutenocache/updateroute?";
            base += "&uuid=" + currRoute.getUUID() + "&bus_stops=" + Uri.encode(mainObj.toString());
            urls[0] = base;
            new CallUpdateRouteAPI().execute(urls);
        }else {
            Log.d("##Curr Route is null", "");
            String urls[] = new String[2];
            String base = "http://sujoyghosal-test.apigee.net/busroutenocache/createroute?";

            urls[0] = base + "routename=" + Uri.encode(etName.getText().toString())
                    + "&street=" + Uri.encode(etStreet.getText().toString())
                    + "&city=" + Uri.encode(etCity.getText().toString())
                    + "&address_line2=" + Uri.encode(etAddress2.getText().toString())
                    + "&state=" + Uri.encode(etState.getText().toString())
                    + "&postalcode=" + Uri.encode(etPC.getText().toString())
                    + "&country=" + Uri.encode(etCountry.getText().toString())
                    + "&stopname=" + Uri.encode(etStopName.getText().toString())
                    + "&latitude=" + shopLatitude + "&longitude=" + shopLongitude
                    + "&description=" + Uri.encode(etDesc.getText().toString());
            new CallCreateRouteAPI().execute(urls);
        }
    }

}
