package com.route.sujoy.routenotifications;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class LoginActivity extends Activity {

        private final Context context = this;
        private EditText etEmail;
        private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        RoutesUtils.init();
        RoutesUtils.loadDeviceLocation(context);

        etEmail = (EditText)findViewById(R.id.editTextEmail);
    }

    public void doLogin(View v) {
            String urls[] = new String[2];
            urls[0] = "http://sujoyghosal-test.apigee.net/busroute/getuser?email=" + etEmail.getText().toString().trim();
            new GetUserByEmail().execute(urls);
            progressDialog = ProgressDialog.show(this, "Status", "Authenticating..please wait", true, true);
    }

    private void displayDialog(Context ctx,String title, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        Log.d("#####In Alert Dialog with msg=", msg);

        builder.setMessage(msg)
                .setTitle(title)
                .setIcon(R.drawable.bus_small_clipped_rev_2)
                .setInverseBackgroundForced(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String urls[] = new String[2];
                        urls[0] = "http://sujoyghosal-test.apigee.net/busroute/createuser?email="
                                + etEmail.getText().toString() + "&name=" + etEmail.getText().toString();
                        new CreateUser().execute(urls);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private  class CreateUser extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            System.out.println("Create URL=" + urls[0]);

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
            Log.d("CreateUser Call Response Received:", response);
            return response;
        }

        @Override
        protected void onPostExecute(String result){

            if (result==null || result.equalsIgnoreCase("[]")) {
                Log.e("Could not create user:", result);
                displayDialog(context,"Create User", "Could Not Create User");
                return;
            }

            Log.i("Create", "Success. Result = " + result);
            String urls[] = new String[2];
            urls[0] = "http://sujoyghosal-test.apigee.net/busroute/getuser?email=" + etEmail.getText().toString();
            new GetUserByEmail().execute(urls);
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startActivity(new Intent(context, MainActivity.class));
            finish();
        }


    }

    private  class GetUserByEmail extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            System.out.println("GetUserByEmail URL=" + urls[0]);
            HttpURLConnection conn = null;
            try {
                URL routeurl = new URL(urls[0]);
                conn = (HttpURLConnection) routeurl.openConnection();
                conn.connect();

                InputStream content = conn.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s;
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }

            } catch (Exception e) {
                if(conn != null)
                    try {
                        Log.e("Error in connection is: ", conn.getResponseCode() + "," + conn.getResponseMessage());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                Log.e("Error!!!!", e.toString());

                return null;
            }
            Log.d("GetUserByEmail Call Response Received:", response);
            return response;
        }

        @Override
        protected void onPostExecute(String result){
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();
            if (result==null || result.equalsIgnoreCase("[]")) {
//                Log.e("Could not Login:", result);
                displayDialog(context,"Id not Found", "Do you want to register with this email id?");
                return;
            }

            Log.i("GetUserByEmail", "Success. Result = " + result);
            try {
                JSONArray userArray = new JSONArray(result);
                JSONObject firstUser = userArray.getJSONObject(0);
                if(firstUser != null){
                    RoutesUtils.loggedinUser.setUuid(firstUser.getString("uuid"));
                    RoutesUtils.loggedinUser.setUserEmail(firstUser.getString("email"));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RoutesUtils.init();
            startActivity(new Intent(context, MainActivity.class));
            finish();
        }

    }

    @Override
    public void onBackPressed(){
        finish();
    }
}

