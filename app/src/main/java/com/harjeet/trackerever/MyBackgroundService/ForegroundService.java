package com.harjeet.trackerever.MyBackgroundService;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.harjeet.trackerever.HomeActivity;
import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.MyUtils.DistanceAlgorithm;
import com.harjeet.trackerever.MyUtils.MySharedPref;
import com.harjeet.trackerever.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES;

import java.util.Locale;

public class ForegroundService extends Service {
    DatabaseReference locationsReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_LOCATIONS);
    MediaPlayer mediaPlayer;
    double otherUserLat;
    double otherUserLong;
    private Geocoder geocoder;
    String otherUserMobile, myMobileNumber;
    int MIN_TIME_BW_UPDATES = 100;
    int MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private double myLat, myLong;
    int notify_distance = 5;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        otherUserMobile = intent.getStringExtra(AppConstants.OTHERMOBILENUMBER);
        myMobileNumber = intent.getStringExtra(AppConstants.MYMOBILENUMBER);

        Log.d("mylog",myMobileNumber+" "+otherUserMobile);

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        @SuppressLint("ResourceAsColor") Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SERVICE")
                .setContentText("Tracking in background.......")
                .setSmallIcon(R.drawable.ic_location)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        String distance = MySharedPref.getSharedValue(getApplicationContext(), AppConstants.NOTIFY_DISTANCE);
        if (distance != null) {
            notify_distance = Integer.valueOf(distance);
        } else {
            notify_distance = 5;
        }
        startLocating();

        return START_STICKY;
    }

    private void startLocating() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500, 0, listener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);


    }

    public LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.e("Google", "Location Changed");
            myLat = location.getLatitude();
            myLong = location.getLongitude();
            locationsReference.child(myMobileNumber).child("lat").setValue(myLat);
            locationsReference.child(myMobileNumber).child("long").setValue(myLong);
            checkLocationsDifference();

        }
    };
    private void createNotificationChannel() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkLocationsDifference() {
//        if (TrackingList != null) {
//            if (TrackingList.size() > 0) {
                locationsReference.child(otherUserMobile).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        otherUserLat = snapshot.child("lat").getValue(Double.class);
                        otherUserLong = snapshot.child("long").getValue(Double.class);
                        Location locationA = new Location("LocationA");
                        locationA.setLatitude(myLat);
                        locationA.setLongitude(myLong);
                        Location locationB = new Location("LocationB");
                        locationB.setLatitude(otherUserLat);
                        locationB.setLongitude(otherUserLong);
                        double distance = DistanceAlgorithm.DistanceBetweenPlaces(myLat, myLong, otherUserLat, otherUserLong);
                        int kmDistance = (int) distance;

                        if (otherUserMobile!=null) {
                            if ((kmDistance <= notify_distance)) {
                                    if (mediaPlayer != null) {
                                        mediaPlayer.stop();
                                        mediaPlayer.release();
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alert);
                                        mediaPlayer.start();
                                    } else {
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alert);
                                        mediaPlayer.start();
                                    }
                                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            mediaPlayer.start();
                                        }
                                    });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });
            }



        }


