package com.route.sujoy.routenotifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

  public GCMIntentService() {
    super(Settings.GCM_SENDER_ID);
  }

  /**
   * Method called on device registered
   **/
  @Override
  protected void onRegistered(Context context, String registrationId) {
    Log.i(TAG, "Device registered For Push Notification" + registrationId);
//    displayMessage(context, getString(R.string.gcm_registered, ""));
//      displayMessage(context, "Registered for Push Scores");
    AppServices.register(context, registrationId);
  }

  /**
   * Method called on device unregistered
   * */
  @Override
  protected void onUnregistered(Context context, String registrationId) {
    Log.i(TAG, "Device unregistered");
    RoutesUtils.displayMessage(context, "Device Unregistered");
    AppServices.unregister(context, registrationId);
  }

  /**
   * Method called on receiving a new message
   * */
  @Override
  protected void onMessage(Context context, Intent intent) {
    String message = intent.getExtras().getString("data");
    Log.i(TAG, "Received message: " + message);
    generateNotification(context, message);
  }

  /**
   * Method called on receiving a deleted message
   * */
  @Override
  protected void onDeletedMessages(Context context, int total) {
    Log.i(TAG, "Received deleted messages notification");
    String message = "Deleted Messages";
    RoutesUtils.displayMessage(context, message);
    generateNotification(context, message);
  }

  /**
   * Method called on Error
   * */
  @Override
  public void onError(Context context, String errorId) {
    Log.i(TAG, "Received error: " + errorId);
    RoutesUtils.displayMessage(context, "Error: " + errorId);
  }

  @Override
  protected boolean onRecoverableError(Context context, String errorId) {
    Log.i(TAG, "Received recoverable error: " + errorId);
    RoutesUtils.displayMessage(context, "Recoverable error: " + errorId);
    return super.onRecoverableError(context, errorId);
  }

  /**
   * Issues a Notification to inform the user that server has sent a message.
   */
  private static void generateNotification(Context context, String message) {
  int icon = R.drawable.bus_small_clipped_rev_2;
  long when = System.currentTimeMillis();
  NotificationManager notificationManager = (NotificationManager)
          context.getSystemService(Context.NOTIFICATION_SERVICE);

  Intent notificationIntent = new Intent(context, MainActivity.class);
  // set intent so it does not start a new activity
  notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
  PendingIntent intent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);

  Notification notification = new Builder(context)
          .setContentText(message)
          .setContentTitle(context.getString(R.string.app_name))
          .setSmallIcon(icon)
          .setWhen(when)
          .setContentIntent(intent)
          .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
          .build();

  notification.flags |= Notification.FLAG_AUTO_CANCEL;

  // Play default notification sound
  notification.defaults |= Notification.DEFAULT_SOUND;

  // Vibrate if vibrate is enabled
//    notification.defaults |= Notification.DEFAULT_VIBRATE;
  notificationManager.notify(0, notification);
}
}
