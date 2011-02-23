package com.myapps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import de.mjpegsample.MjpegView.MjpegInputStream;
import de.mjpegsample.MjpegView.MjpegView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RemoteViews;

public class Video extends Activity {
    private String url;
    private Camera cam;
    private CameraControl camC;
    public Activity activity;
    private MjpegView mv;
    private boolean pause;
    protected static final int GUIUPDATEIDENTIFIER = 0x101;
    static final String[] SIZE = new String[] { "1280x1024", "1280x960",
	    "1280x720", "768x576", "4CIF", "704x576", "704x480", "VGA",
	    "640x480", "640x360", "2CIFEXP", "2CIF", "704x288", "704x240",
	    "480x360", "CIF", "384x288", "352x288", "352x240", "320x240",
	    "240x180", "QCIF", "192x144", "176x144", "176x120", "160x120" };
    static Bitmap newBMP;

    private String fileNameURL = "/sdcard/com.myapps.camera/";
    private NotificationManager notificationManager;

    public void movePanTilt(final String direction) {
	String command;

	if (direction == "horizontalstart")
	    command = "/axis-cgi/com/ptz.cgi?camera=1" + "&pan=-180"
		    + "&tilt=0";
	else if (direction == "horizontalend")
	    command = "/axis-cgi/com/ptz.cgi?camera=1" + "&pan=180" + "&tilt=0";
	else if (direction == "verticalstart")
	    command = "/axis-cgi/com/ptz.cgi?camera=1" + "&pan=0" + "&tilt=180";
	else if (direction == "verticalend")
	    command = "/axis-cgi/com/ptz.cgi?camera=1" + "&pan=0"
		    + "&tilt=-180";

	else {
	    command = "/axis-cgi/com/ptz.cgi?camera=1";
	    command = command + "&move=";
	    command = command + direction;
	}

	try {
	    String url = "http://" + cam.ip + command;
	    Log.i(getString(R.string.logTag), url);
	    URL addr = new URL(url);
	    HttpURLConnection con = (HttpURLConnection) addr.openConnection();
	    con.setRequestProperty("Authorization",
		    base64Encoder.userNamePasswordBase64(cam.login, cam.pass));
	    con.connect();
	    Log.i(getString(R.string.logTag), ("" + con.getResponseCode()));
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.video);
	setRequestedOrientation(0);
	activity = this;

	/*
	 * Récupération des arguments
	 */

	Bundle extras = getIntent().getExtras();
	cam = (Camera) extras.getSerializable(getString(R.string.camTag));
	camC = new CameraControl(cam);

	// Only update if WiFi or 3G is connected and not roaming
	ConnectivityManager mConnectivity = (ConnectivityManager) activity
		.getApplicationContext().getSystemService(
			Context.CONNECTIVITY_SERVICE);
	NetworkInfo info = mConnectivity.getActiveNetworkInfo();
	int netType = info.getType();
	int netSubtype = info.getSubtype();
	if (netType == ConnectivityManager.TYPE_WIFI) {
	    Log.i("AppLog", "Wifi detecte");
	    url = "http://" + cam.ip + ":" + cam.port
		    + "/axis-cgi/mjpg/video.cgi?resolution=320x240";
	} else {
	    Log.i("AppLog", "Reseau detecte");
	    url = "http://" + cam.ip + ":" + cam.port
		    + "/axis-cgi/mjpg/video.cgi?resolution=160x120";
	}

	/*
	 * Buttons Listener
	 */

	Button buttonSnap = (Button) findViewById(R.id.Snap);
	buttonSnap.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("SnapShot Format");
		builder.setSingleChoiceItems(SIZE, -1,
			new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
				try {

				    File f = new File(fileNameURL);
				    if (!f.exists()) {
					f.mkdir();
				    }
				    String fileName = fileNameURL
					    + System.currentTimeMillis()
					    + ".jpeg";
				    Log.i(getString(R.string.logTag), fileName);
				    Bitmap bmp = camC.takeSnapshot(SIZE[item]);
				    Log.i(getString(R.string.logTag),
					    "Snap ok !!");
				    FileOutputStream fichier = new FileOutputStream(
					    fileName);
				    bmp.compress(Bitmap.CompressFormat.JPEG,
					    80, fichier);
				    fichier.flush();
				    fichier.close();
				    Log.i(getString(R.string.logTag),
					    "Snap Save !!");
				    statusBarNotification(activity, bmp,
					    ("Snap save : " + fileName),
					    fileName);
				} catch (IOException e) {
				    Log.i(getString(R.string.logTag),
					    "Snap I/O exception !!");
				    e.printStackTrace();
				}
				dialog.dismiss();
			    }
			});
		AlertDialog alert = builder.create();
		alert.show();
	    }
	});

	Button buttonright = (Button) findViewById(R.id.arrow_right);
	buttonright.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		movePanTilt("right");
	    }
	});

	Button buttonleft = (Button) findViewById(R.id.arrow_left);
	buttonleft.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		movePanTilt("left");
	    }
	});

	Button buttonup = (Button) findViewById(R.id.arrow_up);
	buttonup.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		movePanTilt("up");
	    }
	});

	Button buttondown = (Button) findViewById(R.id.arrow_down);
	buttondown.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		movePanTilt("down");
	    }
	});

	/*
	 * Contrôle du PTZ par déplacement sur l'écran
	 */
	/*
	 * img.setOnTouchListener(new OnTouchListener() { float startX, startY;
	 * 
	 * @Override public boolean onTouch(View v, MotionEvent event) { if
	 * (event.getAction() == MotionEvent.ACTION_DOWN) { startX =
	 * event.getX(); startY = event.getY(); return true; } return false; }
	 * });
	 */

	/*
	 * Affichage video
	 */

	mv = (MjpegView) findViewById(R.id.surfaceView1);
	start_connection(mv, url, cam);

    }

    private void statusBarNotification(Activity activity, Bitmap bmp,
	    String text, String path) {
	notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	Notification notification = new Notification(R.drawable.camera,
		"Camera-Axis", System.currentTimeMillis());
	notification.contentView = new RemoteViews(activity.getPackageName(),
		R.layout.notification);
	/* Action lors d'un clic sur la notification */
	Intent intentNotification = new Intent();
	intentNotification.setAction(android.content.Intent.ACTION_VIEW);
	intentNotification.setDataAndType(Uri.fromFile(new File(path)),
		"image/png");
	PendingIntent pendingIntent = PendingIntent.getActivity(
		activity.getApplicationContext(), 0, intentNotification, 0);

	notification.defaults |= Notification.DEFAULT_VIBRATE;
	notification.contentIntent = pendingIntent;
	notification.contentView.setImageViewBitmap(R.id.Nimage, bmp);
	notification.contentView.setTextViewText(R.id.Ntext, text);
	notificationManager.notify(1, notification);
    }

    private void start_connection(MjpegView mv, String url, Camera cam) {
	try {
	    URL addr = new URL(url);
	    Log.i("AppLog", addr.toString());
	    HttpURLConnection con = (HttpURLConnection) addr.openConnection();
	    con.setRequestProperty("Authorization",
		    base64Encoder.userNamePasswordBase64(cam.login, cam.pass));
	    con.connect();
	    InputStream stream;
	    stream = con.getInputStream();
	    mv.setSource(new MjpegInputStream(stream));
	    mv.setDisplayMode(MjpegView.SIZE_FULLSCREEN);
	    mv.showFps(true);
	    pause = false;
	    Log.i("AppLog", "onCreate");

	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void onResume() {
	super.onResume();
	if (pause) {
	    mv.resumePlayback();
	    pause = false;
	}

    }

    public void onPause() {
	pause = true;
	super.onPause();
    }

    public void onDestroy() {
	super.onDestroy();
	mv.stopPlayback();
    }
}
