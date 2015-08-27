package com.route.sujoy.routenotifications;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class RouteListActivity extends ListActivity {

    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(RoutesUtils.routeStopsArray.isEmpty()){
//            RoutesUtils.displayDialog(context,"Error","Could Not Get Bus Stops List, going back to login screen.");
            finish();
            startActivity(new Intent(context,LoginActivity.class));
        }
        Log.d("#$#$#$#$", "Trying Friends List with count of friends =" + RoutesUtils.routeStopsArray.size() + "");
        RoutesAdapter customAdapter = new RoutesAdapter(this, RoutesUtils.routeStopsArray);
        ListView listView = getListView();
        LayoutInflater inflater=this.getLayoutInflater();
        View header=inflater.inflate(R.layout.response_header, null);
        TextView h = (TextView)header.findViewById(R.id.textViewHeader);
        h.setText( RoutesUtils.routeStopsArray.size() + " Stops For Route" );
        listView.addHeaderView(header);

        View footer = inflater.inflate(R.layout.response_footer, null);
        if(RoutesUtils.showStops)
            listView.addFooterView(footer);

        listView.setAdapter(customAdapter);
        listView.setTextFilterEnabled(true);
        customAdapter.notifyDataSetChanged();
//        RoutesUtils.subscriptionsArray.clear();
    }
    public void returnHomeFromList(View v){
        Intent mainAct=new Intent(getApplicationContext(),MainActivity.class);
        startActivity(mainAct);
    }



    private void showInMap(String latitude, String longitude){
        Uri uri= Uri.parse("http://maps.google.com/maps?saddr=" + RoutesUtils.getCurrentDeviceLatitude()
                + "," + RoutesUtils.getCurrentDeviceLongitude()
                + "&daddr=" + latitude
                + "," + longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void createMapForAllFriends(View v){
        Intent intent = new Intent(this, Map.class);
        intent.putExtra("POSITION", -1); //show all
        startActivity(intent);
    }

    private void createMapForOne(int position){
        Intent intent = new Intent(this, Map.class);
        intent.putExtra("POSITION", position); //show all
        startActivity(intent);
    }
    public static class RouteStopsObject {

        private String address;
        private String prevStop;
        private String UUID;
        private String description;
        private String city;
        private String state;
        private String street;
        private String PC;

        public String getUUID() {
            return UUID;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getAddress2() {
            return address2;
        }

        public void setAddress2(String address2) {
            this.address2 = address2;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        private String address2;
        private String country;



        public String getStopName() {
            return stopName;
        }

        public void setStopName(String stopName) {
            this.stopName = stopName;
        }

        private String stopName;

        public String getNextStop() {
            return nextStop;
        }

        public void setNextStop(String nextStop) {
            this.nextStop = nextStop;
        }

        public String getPrevStop() {
            return prevStop;
        }

        public void setPrevStop(String prevStop) {
            this.prevStop = prevStop;
        }

        private String nextStop;
        
        private RoutesUtils.Coordinates coordinates;

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        private String distance;


        public RouteStopsObject(){
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

       

        public RoutesUtils.Coordinates getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(RoutesUtils.Coordinates coordinates) {
            this.coordinates = coordinates;
        }


        public void setUUID(String UUID) {
            this.UUID = UUID;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public void setPC(String PC) {
            this.PC = PC;
        }

        public String getPC() {
            return PC;
        }
    }
    public class RoutesAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ArrayList<RouteStopsObject> objects;

        public RoutesAdapter(Context context, ArrayList<RouteStopsObject> objects) {
            inflater = LayoutInflater.from(context);
            this.objects = objects;
        }

        public int getCount() {
            return objects.size();
        }

        public RouteStopsObject getItem(int position) {
            return objects.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.activity_route_stops, null);
                holder.textViewName = (TextView) convertView.findViewById(R.id.textViewName);
                holder.textViewAddress = (TextView) convertView.findViewById(R.id.textViewAddress);
                holder.imgViewMap = (ImageView) convertView.findViewById(R.id.imageMap);
                holder.textViewDistance = (TextView) convertView.findViewById(R.id.tvDistance2);
                holder.checkBoxSubscribe = (CheckBox) convertView.findViewById(R.id.checkBoxSubscribe);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final CheckBox current =  holder.checkBoxSubscribe;

                    current.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RoutesUtils.Subscriptions s = new RoutesUtils.Subscriptions();
                    s.setRouteName(RoutesUtils.routeName);
                    s.setStopName(objects.get(position).getStopName());
                    s.setLatitude(objects.get(position).getCoordinates().getLatitude());
                    s.setLongitude(objects.get(position).getCoordinates().getLongitude());
                    if(current.isChecked()) {
                        RoutesUtils.subscriptionsArray.add(s);
                    }else
                        RoutesUtils.subscriptionsArray.remove(s);
                }
            });



            holder.imgViewMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(RoutesUtils.showStops)
                        showInMap(getItem(position).getCoordinates().getLatitude(),getItem(position).getCoordinates().getLongitude());
                    else{
                        createMapForOne(position);
                    }
                }
            });

            holder.textViewName.setText(objects.get(position).getStopName());
            holder.textViewAddress.setText(objects.get(position).address);
            holder.textViewDistance.setText(objects.get(position).distance + "km");

            //when showing buses remove checkbox
            if(!RoutesUtils.showStops){
                holder.checkBoxSubscribe.setVisibility(View.GONE);
            }
            return convertView;
        }

        private class ViewHolder {
            TextView textViewName;
            TextView textViewAddress;
            ImageView imgViewMap;
            TextView textViewDistance;
            CheckBox checkBoxSubscribe;
        }
    }

    public void submitSubscriptions(View v){

        JSONArray ja = new JSONArray();
        JSONObject mainObj = new JSONObject();
        try {
            for(int i=0; i<RoutesUtils.subscriptionsArray.size();i++) {
                JSONObject jo = new JSONObject();
                jo.put("route_name", RoutesUtils.subscriptionsArray.get(i).getRouteName());
                jo.put("stop_name", RoutesUtils.subscriptionsArray.get(i).getStopName());
                JSONObject locationOb = new JSONObject();
                locationOb.put("latitude",RoutesUtils.subscriptionsArray.get(i).getLatitude());
                locationOb.put("longitude",RoutesUtils.subscriptionsArray.get(i).getLongitude());
                jo.put("location", locationOb);
                ja.put(jo);
            }
            mainObj.put("subscriptions", ja);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("##Error!!!!", "Could not create subscriptions object for insert.");
            RoutesUtils.displayDialog(context, "Subscriptions Status: ", "Failed to complete subscribe request.");
            return;
        }
        String urls[] = new String[2];
        String base = "http://sujoyghosal-test.apigee.net/busroute/subscribe?";
        base += "&user=" + RoutesUtils.loggedinUser.getUserEmail() + "&subscriptions=" + Uri.encode(mainObj.toString());
        urls[0] = base;
        new CallSubscriptionsAPI().execute(urls);
    }
    public void addUserToRouteStopGroups(View v){
        String urls[] = new String[2];
        RoutesUtils.subscriptionsArrayIndex = 0;
        try {
            for(int i=0; i<RoutesUtils.subscriptionsArray.size();i++) {
                urls[0] = "http://sujoyghosal-test.apigee.net/busroute/addusertogroup?user=" + RoutesUtils.loggedinUser.getUserEmail()
                        + "&group=";

                String rName = RoutesUtils.subscriptionsArray.get(i).getRouteName().trim().replace(" ","-").toUpperCase();
                String sName = RoutesUtils.subscriptionsArray.get(i).getStopName().trim().replace(" ","-").toUpperCase();
                urls[0] += Uri.encode(rName + "-" + sName);
                RoutesUtils.subscriptionsArrayIndex = i;
                new AssisgnUserToGroup().execute(urls);
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("##Error!!!!", "Could not create add user to group.");

            return;
        }
        RoutesUtils.displayDialog(context, "Subscriptions Status", "You have successfully subscribed to bus-stops for notifications.");
    }
    private class CallSubscriptionsAPI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            String url = urls[0];
            System.out.println("Subscribe API URL is =" + url);

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
            Log.d("Response:", response);
            return response;
        }


        protected void onPostExecute(String result){
            if(result == null) {
                RoutesUtils.displayDialog(context, "Subscriptions Status", "Failed!! ");
                return;
            }
            Log.d("####Create Subscription response received=", result);
            RoutesUtils.displayDialog(context, "Subscriptions Status", "You have successfully subscribed to bus-stops for notifications.");
            AppServices.sendMyselfANotification(context, "Subscribed to bus stops successfuly");

//            addUserToRouteStopGroups();
        }
    }
    public  static class AssisgnUserToGroup extends AsyncTask<String, Void, String> {



        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            String url = urls[0];
            System.out.println("Assign user to group URL is =" + url);

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
            Log.d("Response:", response);
            return response;
        }


        protected void onPostExecute(String result){
                if(result == null || result.trim().equalsIgnoreCase("true")) {
                    Log.e("Assign group Status: ", "Failed!! ");
                    if(RoutesUtils.subscriptionsArray!=null && RoutesUtils.subscriptionsArray.size() > 0)
                        RoutesUtils.subscriptionsArray.get(RoutesUtils.subscriptionsArrayIndex).setSubscribed(false);
                    return;
                }
                if(RoutesUtils.subscriptionsArray!=null && RoutesUtils.subscriptionsArray.size() > 0)
                    RoutesUtils.subscriptionsArray.get(RoutesUtils.subscriptionsArrayIndex).setSubscribed(true);
                Log.d("####Asssign user to  group successful. ", result);
        }
    }

}
