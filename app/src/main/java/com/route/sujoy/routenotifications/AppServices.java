package com.route.sujoy.routenotifications;

import android.content.Context;
import android.util.Log;

import com.apigee.sdk.ApigeeClient;
import com.apigee.sdk.data.client.DataClient;
import com.apigee.sdk.data.client.callbacks.ApiResponseCallback;
import com.apigee.sdk.data.client.callbacks.DeviceRegistrationCallback;
import com.apigee.sdk.data.client.entities.Device;
import com.apigee.sdk.data.client.push.GCMDestination;
import com.apigee.sdk.data.client.push.GCMPayload;
import com.apigee.sdk.data.client.response.ApiResponse;
import com.google.android.gcm.GCMRegistrar;

public final class AppServices {

  private static DataClient client;
  private static Device device;


  static synchronized DataClient getClient(Context context) {
    if (client == null) {
    	if (Settings.ORG.equals("<<your org name here>>")) {
    		Log.e(RoutesUtils.TAG, "ORG value has not been set.");
    	} else {
    		ApigeeClient apigeeClient = new ApigeeClient(Settings.ORG, Settings.APP, Settings.API_URL,context);
    		client = apigeeClient.getDataClient();
    	}
    }
    return client;
  }

  public static void loginAndRegisterForPush(final Context context) {

    RoutesUtils.deviceID = AppServices.getClient(context).getUniqueDeviceID().toString();
    if ((Settings.USER.length() > 0)) {
    	DataClient dataClient = getClient(context);
    	if (dataClient != null) {
    		dataClient.authorizeAppUserAsync(Settings.USER, Settings.PASSWORD, new ApiResponseCallback() {

    			@Override
    			public void onResponse(ApiResponse apiResponse) {
    				Log.i(RoutesUtils.TAG, "login response: " + apiResponse);
    				registerPush(context);
    			}

    			@Override
    			public void onException(Exception e) {
    				RoutesUtils.displayMessage(context, "Login Exception: " + e);
    				Log.i(RoutesUtils.TAG, "login exception: " + e);
    			}
    		});
    	} else {
    		Log.e(RoutesUtils.TAG, "Data client is null, did you set ORG value in Settings.java?");
    	}
    } else {
      registerPush(context);
    }
  }
  
  static void registerPush(Context context) {
    GCMRegistrar.register(context, Settings.GCM_SENDER_ID);
    String regId = GCMRegistrar.getRegistrationId(context);
//    regId = "";
    if ("".equals(regId)) {
      GCMRegistrar.register(context, Settings.GCM_SENDER_ID);
    } else {
      if (GCMRegistrar.isRegisteredOnServer(context)) {
        Log.i(RoutesUtils.TAG, "Already registered with GCM");
      } else {
        AppServices.register(context, regId);
      }
    }
  }


  /**
   * Register this user/device pair on App Services.
   */
  static void register(final Context context, final String regId) {


	DataClient dataClient = getClient(context);
//    RoutesUtils.deviceID = dataClient.getUniqueDeviceID().toString();
	if (dataClient != null) {

        Log.i(RoutesUtils.TAG, "registering device: " + dataClient.getUniqueDeviceID() + ",Registration iD:" + regId);

		dataClient.registerDeviceForPushAsync(dataClient.getUniqueDeviceID(), Settings.NOTIFIER, regId, null, new DeviceRegistrationCallback() {

            @Override
            public void onResponse(Device device) {
                Log.i(RoutesUtils.TAG, "register response: " + device);
                AppServices.device = device;
//        displayMessage(context, "Device registered as: " + regId);
                DataClient dataClient = getClient(context);

                if (dataClient != null) {
                    // connect Device to current User - if there is one
                    if (dataClient.getLoggedInUser() != null) {
                        dataClient.connectEntitiesAsync("users", dataClient.getLoggedInUser().getUuid().toString(),
                                "devices", device.getUuid().toString(),
                                new ApiResponseCallback() {
                                    @Override
                                    public void onResponse(ApiResponse apiResponse) {
                                        Log.i(RoutesUtils.TAG, "connect response: " + apiResponse);

                                    }

                                    @Override
                                    public void onException(Exception e) {
                                        RoutesUtils.displayMessage(context, "Connect Exception: " + e);
                                        Log.i(RoutesUtils.TAG, "connect exception: " + e);
                                    }
                                });
                    }
                } else {
                    Log.e(RoutesUtils.TAG, "data client is null, did you set ORG value in Settings.java?");
                }
            }

            @Override
            public void onException(Exception e) {
                RoutesUtils.displayMessage(context, "Register Exception: " + e);
                Log.i(RoutesUtils.TAG, "register exception: " + e);
            }

            @Override
            public void onDeviceRegistration(Device device) { /* this won't ever be called */ }
        });
	} else {
		Log.e(RoutesUtils.TAG, "Data client is null, did you set ORG value in Settings.java?");
	}
  }

      static void sendMyselfANotification(final Context context, String message) {
	  if (device == null) {
		  RoutesUtils.displayMessage(context, "Device not registered. Do you want to enable Push Notification?");
	  } else {
		  DataClient dataClient = getClient(context);
		  if (dataClient != null) {
			  GCMDestination destination = GCMDestination.destinationSingleDevice(device.getUuid());
//              GCMDestination destination = GCMDestination.destinationAllDevices();
			  GCMPayload payload = new GCMPayload();
			  payload.setAlertText(message);

			  dataClient.pushNotificationAsync(payload, destination, Settings.NOTIFIER, new ApiResponseCallback() {
            	  @Override
				  public void onResponse(ApiResponse apiResponse) {
					  Log.i(RoutesUtils.TAG, "send response: " + apiResponse);
				  }

				  @Override
				  public void onException(Exception e) {
					  RoutesUtils.displayMessage(context, "Send Exception: " + e);
					  Log.i(RoutesUtils.TAG, "send exception: " + e);
				  }
			  });
		  } else {
			  Log.e(RoutesUtils.TAG, "data client is null, did you set ORG value in Settings.java?");
		  }
	  }
  }
    static void sendNotificationToAll(final Context context, String message) {
        if (device == null) {
            RoutesUtils.displayMessage(context, "Device not registered. Do you want to enable Push Notification?");
        } else {
            DataClient dataClient = getClient(context);
            if (dataClient != null) {
              GCMDestination destination = GCMDestination.destinationAllDevices();
                GCMPayload payload = new GCMPayload();
                payload.setAlertText(message);

                dataClient.pushNotificationAsync(payload, destination, Settings.NOTIFIER, new ApiResponseCallback() {
                    @Override
                    public void onResponse(ApiResponse apiResponse) {
                        Log.i(RoutesUtils.TAG, "send response: " + apiResponse);
                    }

                    @Override
                    public void onException(Exception e) {
                        RoutesUtils.displayMessage(context, "Send Exception: " + e);
                        Log.i(RoutesUtils.TAG, "send exception: " + e);
                    }
                });
            } else {
                Log.e(RoutesUtils.TAG, "data client is null, did you set ORG value in Settings.java?");
            }
        }
    }



    static void sendNotificationToGroups(final Context context, String message,String group) {
        if (device == null) {
            RoutesUtils.displayMessage(context, "Device not registered. Do you want to enable Push Notification?");
        } else {
            DataClient dataClient = getClient(context);

            if (dataClient != null) {
                GCMDestination destination = GCMDestination.destinationSingleGroup(group);
                GCMPayload payload = new GCMPayload();
                payload.setAlertText(message);

                dataClient.pushNotificationAsync(payload, destination, Settings.NOTIFIER, new ApiResponseCallback() {
                    @Override
                    public void onResponse(ApiResponse apiResponse) {
                        Log.i(RoutesUtils.TAG, "send response: " + apiResponse);
                    }

                    @Override
                    public void onException(Exception e) {
                        RoutesUtils.displayMessage(context, "Send Exception: " + e);
                        Log.i(RoutesUtils.TAG, "send exception: " + e);
                    }
                });
            } else {
                Log.e(RoutesUtils.TAG, "data client is null, did you set ORG value in Settings.java?");
            }
        }
    }
  /**
   * Unregister this device within the server.
   */
  static void unregister(final Context context, final String regId) {
    Log.i(RoutesUtils.TAG, "unregistering device: " + regId);
    register(context, "");
  }

}
