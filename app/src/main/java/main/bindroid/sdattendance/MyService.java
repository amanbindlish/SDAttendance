package main.bindroid.sdattendance;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.concurrent.TimeUnit;

import main.bindroid.sdattendance.utills.CommonUtils;

public class MyService extends Service {

	private static final String TAG = MyService.class.getSimpleName();

	private static final Integer WHOS_FANCY_SERVICE_NOTIFICATION_ID = 100;
	private static final int WHOS_FANCY_NOTIFICATION_ID = WHOS_FANCY_SERVICE_NOTIFICATION_ID + 1;

	private BeaconManager mBeaconManager;
	private NotificationManager mNotificationManager;
	private Region mRegion;
	private SharedPreferences mPreferences;
	private String loginId;

	private SharedPreferences pref;

	public MyService() {
	}

	@Override
	public void onCreate() {
		// Configure verbose debug logging, enable this to debugging
		// L.enableDebugLogging(true);
		pref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		loginId = pref.getString("key", "");

		mRegion = new Region(Globals.REGION, Globals.PROXIMITY_UUID,
				Globals.MAJOR, Globals.MINOR);

		mPreferences = getApplicationContext().getSharedPreferences(
				"preferences", Activity.MODE_PRIVATE);

		// User this to receive notification from all iBeacons
		// mRegion = new Region(Globals.WHOS_FANCY_REGION, PROXIMITY_UUID, null,
		// null);

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mBeaconManager = new BeaconManager(this);

		// Default values are 5s of scanning and 25s of waiting time to save CPU
		// cycles.
		// In order for this demo to be more responsive and immediate we lower
		// down those values.
		mBeaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);

		mBeaconManager
				.setMonitoringListener(new BeaconManager.MonitoringListener() {

					@Override
					public void onEnteredRegion(final Region region,
							List<Beacon> beacons) {
						postNotification(getString(R.string.status_entered_region));
						if (loginId.equalsIgnoreCase("") || loginId == null
								|| loginId.equalsIgnoreCase("null")) {
							ParseObject loginData = new ParseObject(
									"SDLoginData");
							loginData.put("EmpCode", CommonUtils
									.getLoggedInUser(getApplicationContext())
									.getEmpCode());
							loginData.put("LoginDate", CommonUtils
									.getDate(System.currentTimeMillis()));
							loginData.put("LoginTime",
									CommonUtils.getCurrentTime());
							loginData.put("LogoutTime", "00:00");
							loginData.saveInBackground();
							Editor edit = pref.edit();
							edit.putString("key", loginData.getObjectId());
						} else {
							ParseQuery<ParseObject> parseQuery = ParseQuery
									.getQuery("SDLoginData");
							parseQuery.whereEqualTo("objectId", loginId);
							parseQuery
									.findInBackground(new FindCallback<ParseObject>() {

										@Override
										public void done(
												List<ParseObject> objects,
												ParseException e) {
											if (e == null && objects != null
													&& objects.size() > 0) {
												objects.get(0)
														.put("LogoutTime",
																CommonUtils
																		.getTime(System
																				.currentTimeMillis()));
												objects.get(0)
														.saveInBackground();
												int dayCreated = objects.get(0)
														.getCreatedAt()
														.getDay();
												int dayUpdated = objects.get(0)
														.getUpdatedAt()
														.getDay();
												if (dayCreated - dayUpdated != 0) {
													Editor edit = pref.edit();
													edit.putString("key", "");
													edit.commit();
												}
											}
										}
									});

						}

					}

					@Override
					public void onExitedRegion(final Region region) {
						// postNotification(getString(R.string.status_exited_region));
					}
				});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		// setNotification();

		mNotificationManager.cancel(WHOS_FANCY_NOTIFICATION_ID);
		mBeaconManager.connect(new BeaconManager.ServiceReadyCallback() {

			@Override
			public void onServiceReady() {
				try {
					mBeaconManager.startMonitoring(mRegion);
				} catch (RemoteException e) {
					Log.d(TAG, "Error while starting monitoring");
				}
			}
		});

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mNotificationManager.cancel(WHOS_FANCY_NOTIFICATION_ID);
		mBeaconManager.disconnect();
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putBoolean(Globals.PREFERENCE_SERVICE_STARTED, false);
		editor.commit();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void postNotification(String msg) {
		PendingIntent pendingIntent = PendingIntent.getActivities(
				MyService.this, 0, new Intent[]{new Intent()},
				PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder notification = new NotificationCompat.Builder(
				MyService.this).setSmallIcon(R.drawable.beacon_gray)
				.setContentTitle(getString(R.string.last_post_notification))
				.setContentText(msg).setAutoCancel(true)
				.setContentIntent(pendingIntent)
				.setDefaults(Notification.DEFAULT_ALL);
		mNotificationManager.notify(WHOS_FANCY_NOTIFICATION_ID,
				notification.build());
	}

	// private void setNotification() {
	// Intent notificationIntent = new Intent(this, MainActivity.class);
	// PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
	// notificationIntent, 0);
	//
	// NotificationCompat.Builder notification = new NotificationCompat.Builder(
	// MyService.this).setSmallIcon(R.drawable.beacon_gray)
	// .setContentTitle(getString(R.string.app_name))
	// .setContentText(getString(R.string.waiting_notification))
	// .setContentIntent(pendingIntent);
	//
	// startForeground(WHOS_FANCY_SERVICE_NOTIFICATION_ID,
	// notification.build());
	// }

}
