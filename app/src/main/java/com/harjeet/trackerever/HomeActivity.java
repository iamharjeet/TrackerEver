package com.harjeet.trackerever;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.harjeet.trackerever.MyBackgroundService.ForegroundService;
import com.harjeet.trackerever.MyBackgroundService.SharingLocationService;
import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.MyUtils.DistanceAlgorithm;
import com.harjeet.trackerever.MyUtils.MySharedPref;
import com.harjeet.trackerever.Structures.ProfileDataStructure;
import com.harjeet.trackerever.Structures.RequestStructure;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    LinearLayout count, count2;
    AppCompatButton start_track, btnStartShareLocation;
    ImageView notification, requests;
    CircularImageView userImage;
    TextView userName;
    RippleBackground rippleBackground;
    int startTrack = 0;
    ImageView map;
    int inBackground = 0;
    DrawerLayout drawer;
    TextView profile, settings, logOut;
    private double myLat, myLong;
    String userMobile;
    int MIN_TIME_BW_UPDATES = 100;
    int MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private Geocoder geocoder;
    List<RequestStructure> TrackerList = new ArrayList<>();
    List<RequestStructure> TrackingList = new ArrayList<>();
    TextView trackingCounts, trackerCounts;
    int dialogShow = 0;
    int notify_distance = 5;
    double otherUserLat;
    double otherUserLong;
    private ImageView imgDrawer;
    private FusedLocationProviderClient mFusedLocationClient;
    DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_USERS);
    DatabaseReference locationsReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_LOCATIONS);
    DatabaseReference sendRequestReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_SEND_REQUESTS);
    DatabaseReference receiveRequestReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_RECEIVE_REQUESTS);
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        stopService();
        userMobile = MySharedPref.getSharedValue(getApplicationContext(), AppConstants.USER_PHONE);
        String distance = MySharedPref.getSharedValue(getApplicationContext(), AppConstants.NOTIFY_DISTANCE);
        MySharedPref.saveSharedValue(HomeActivity.this, AppConstants.APP_IN_BACKGROUND, "0");

        if (distance != null) {
            notify_distance = Integer.valueOf(distance);
        } else {
            notify_distance = 5;
        }
        findId();
        map.setVisibility(View.INVISIBLE);
        clicks();
        tokenGenerateAndUpdate();
        getTrackersCount();
        getTrackingCount();
        setDataInSideBar();
        if (isLocationEnabled()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            geocoder = new Geocoder(this, Locale.getDefault());
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500, 0, listener);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);

        } else {
            Toast.makeText(this, "Please on your location.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService();
        stopLocationService();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
//        stopService();
//        String distance = MySharedPref.getSharedValue(getApplicationContext(), AppConstants.NOTIFY_DISTANCE);
//        String trackStatus = MySharedPref.getSharedValue(getApplicationContext(), AppConstants.TRACKING_STATUS);
//
//        if(trackStatus!=null){
//            startTrack=Integer.valueOf(trackStatus);
//        }
//        if (distance != null) {
//            notify_distance = Integer.valueOf(distance);
//        } else {
//            notify_distance = 5;
//        }


    }

    private void tokenGenerateAndUpdate() {
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null) {
            usersReference.child(userMobile).child("token").setValue(token);
        }
    }


    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    private void AlertDialogShow(String distance) {
        startTrack = 0;
        dialogShow = 1;
        rippleBackground.stopRippleAnimation();
        Dialog dialog = new Dialog(HomeActivity.this);
        dialog.setContentView(R.layout.dialog_music_start);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AppCompatButton btnStop = dialog.findViewById(R.id.dialog_btn_stop);
        TextView name = dialog.findViewById(R.id.txt_name);
        TextView number = dialog.findViewById(R.id.txt_number);
        TextView userDistance = dialog.findViewById(R.id.txt_distance);
        CircularImageView image = dialog.findViewById(R.id.img_user);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrack = 0;
                dialogShow = 0;
                start_track.setText("Start Tracking");
                start_track.setBackgroundColor(Color.parseColor("#16DD1C"));
                dialog.cancel();
                mediaPlayer.stop();
            }
        });


        usersReference.child(TrackingList.get(0).getMobile()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ProfileDataStructure otherUserData = snapshot.getValue(ProfileDataStructure.class);
                Glide.with(getApplicationContext()).load(otherUserData.getImage()).into(image);
                name.setText(otherUserData.getName());
                number.setText(otherUserData.getMobile());
                userDistance.setText(distance + " KM");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void getTrackingCount() {
        sendRequestReference.child(MySharedPref.getSharedValue(getApplicationContext(), AppConstants.USER_PHONE)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Iterable<DataSnapshot> iterator = snapshot.getChildren();
                    TrackingList.clear();
                    for (DataSnapshot data : iterator) {
                        if (data.getValue(RequestStructure.class).getStatus().equalsIgnoreCase("1")) {
                            TrackingList.add(data.getValue(RequestStructure.class));
                        }
                    }
                    trackingCounts.setText(String.valueOf(TrackingList.size()));


                } else {
                    TrackingList.clear();
                    trackingCounts.setText("0");
                }

                if (MySharedPref.getSharedValue(HomeActivity.this, AppConstants.TRACKING_STATUS)!=null &&
                        MySharedPref.getSharedValue(HomeActivity.this, AppConstants.TRACKING_STATUS).equals("1")){
                    startTrack = 1;
                    Toast.makeText(HomeActivity.this, "Locating...", Toast.LENGTH_SHORT).show();
                    rippleBackground.startRippleAnimation();
                    startTrack = 1;
                    start_track.setText("Stop Tracking");
                    start_track.setBackgroundColor(Color.parseColor("#FF0000"));
                    checkLocationsDifference();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTrackersCount() {
        receiveRequestReference.child(MySharedPref.getSharedValue(getApplicationContext(), AppConstants.USER_PHONE)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Iterable<DataSnapshot> iterator = snapshot.getChildren();
                    TrackerList.clear();
                    for (DataSnapshot data : iterator) {
                        if (data.getValue(RequestStructure.class).getStatus().equalsIgnoreCase("1")) {
                            TrackerList.add(data.getValue(RequestStructure.class));
                        }
                    }
                    trackerCounts.setText(String.valueOf(TrackerList.size()));


                } else {
                    TrackerList.clear();
                    trackerCounts.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setDataInSideBar() {
        usersReference.child(userMobile).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userName.setText(snapshot.child("name").getValue(String.class));
                MySharedPref.saveSharedValue(getApplicationContext(), AppConstants.USER_NAME, snapshot.child("name").getValue(String.class));
                Glide.with(getApplicationContext()).load(snapshot.child("image").getValue(String.class)).into(userImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLocationsDifference() {
        if (TrackingList != null) {
            if (TrackingList.size() > 0) {
                locationsReference.child(TrackingList.get(0).getMobile()).addValueEventListener(new ValueEventListener() {
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
                        map.setVisibility(View.VISIBLE);
                        double distance = DistanceAlgorithm.DistanceBetweenPlaces(myLat, myLong, otherUserLat, otherUserLong);
                        int kmDistance = (int) distance;
                        if (TrackingList.size() > 0 && startTrack == 1) {
                            Toast.makeText(HomeActivity.this, String.valueOf(kmDistance) + " km away from you.", Toast.LENGTH_SHORT).show();
                            if ((kmDistance <= notify_distance)) {
                                if (dialogShow == 0) {

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
                                    if (!MySharedPref.getSharedValue(HomeActivity.this, AppConstants.APP_IN_BACKGROUND).equals("1")) {
                                        try {
                                            AlertDialogShow(String.valueOf(kmDistance));
                                            //show dialog
                                        } catch (Exception e) {
                                        }

                                    } else {
                                        MySharedPref.saveSharedValue(HomeActivity.this, AppConstants.DIALOG_STATUS, "1");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }

                });
            }
        }

    }


    private void clicks() {
        start_track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TrackingList.size() > 0) {
                    if (MySharedPref.getSharedValue(HomeActivity.this, AppConstants.SHARING_LOCATION) == null || MySharedPref.getSharedValue(HomeActivity.this, AppConstants.SHARING_LOCATION).equals("0")) {
                        if (start_track.getText().toString().equalsIgnoreCase("Start Tracking")) {
                            Toast.makeText(HomeActivity.this, "Locating...", Toast.LENGTH_SHORT).show();
                            rippleBackground.startRippleAnimation();
                            startTrack = 1;
                            start_track.setText("Stop Tracking");
                            start_track.setBackgroundColor(Color.parseColor("#FF0000"));
                        } else {
                            rippleBackground.stopRippleAnimation();
                            startTrack = 0;
                            start_track.setText("Start Tracking");
                            start_track.setBackgroundColor(Color.parseColor("#16DD1C"));
                        }
                    } else {
                        Toast.makeText(HomeActivity.this, "Please stop sharing Location", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Snackbar.make(findViewById(android.R.id.content), "You have no any friend to Track.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        if (MySharedPref.getSharedValue(this, AppConstants.SHARING_LOCATION) != null && MySharedPref.getSharedValue(this, AppConstants.SHARING_LOCATION).equals("1")) {
            btnStartShareLocation.setText("Stop share Location");
            btnStartShareLocation.setBackgroundColor(Color.parseColor("#FF0000"));
        }

        btnStartShareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startTrack == 0) {

                    if (MySharedPref.getSharedValue(HomeActivity.this, AppConstants.SHARING_LOCATION) == null || MySharedPref.getSharedValue(HomeActivity.this, AppConstants.SHARING_LOCATION).equals("0")) {

                        btnStartShareLocation.setText("Stop share Location");
                        btnStartShareLocation.setBackgroundColor(Color.parseColor("#FF0000"));
                        MySharedPref.saveSharedValue(HomeActivity.this, AppConstants.SHARING_LOCATION, "1");

                    } else {
                        btnStartShareLocation.setText("Share Location");
                        btnStartShareLocation.setBackgroundColor(Color.parseColor("#16DD1C"));
                        MySharedPref.saveSharedValue(HomeActivity.this, AppConstants.SHARING_LOCATION, "0");
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Please stop tracking", Toast.LENGTH_SHORT).show();
                }


            }
        });

        requests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RequestsActivity.class);
                startActivity(i);
            }
        });

        imgDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(drawer.isDrawerOpen(GravityCompat.START)){
                    drawer.closeDrawer(GravityCompat.START);
                }
                else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });


        count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TrackingActivity.class);
                startActivity(i);
            }
        });

        count2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TrackersActivity.class);
                startActivity(i);
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(i);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("myPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("user_phone", "0");
                editor.commit();
                finish();
                startActivity(new Intent(HomeActivity.this,SplashScreenActivity.class));
                finishAffinity();
            }
        });
        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), NotificationsActivity.class);
                startActivity(i);
            }
        });

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strUri = "http://maps.google.com/maps?q=loc:" + otherUserLat + "," + otherUserLong + " (" + "Label which you want" + ")";
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        });
    }

    private void findId() {
        drawer = findViewById(R.id.drawer);
        imgDrawer=findViewById(R.id.img_drawer);
        btnStartShareLocation = findViewById(R.id.btn_start_sharing_location);
        count = findViewById(R.id.tacking_count);
        count2 = findViewById(R.id.tacking_count2);
        start_track = findViewById(R.id.btn_start_tracking);
        notification = findViewById(R.id.img_notification);
        profile = findViewById(R.id.nav_profile);
        map = findViewById(R.id.img_map);
        settings = findViewById(R.id.nav_settings);
        logOut = findViewById(R.id.nav_logout);
        userImage = findViewById(R.id.nav_image);
        userName = findViewById(R.id.nav_name);
        trackingCounts = findViewById(R.id.txt_tracking_counts);
        trackerCounts = findViewById(R.id.txt_trackers_count);
        requests = findViewById(R.id.img_requests);
        rippleBackground = findViewById(R.id.ripple_effect);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rippleBackground.setClipToOutline(true);
        }
    }

    public LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

            // TODO Auto-generated method stub
            Log.e("Google", "Location Changed");

            myLat = location.getLatitude();
            myLong = location.getLongitude();
            locationsReference.child(userMobile).child("lat").setValue(myLat);
            locationsReference.child(userMobile).child("long").setValue(myLong);
            if (startTrack == 1) {
                checkLocationsDifference();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };


    public double distance(Double latitude, Double longitude, double e, double f) {
        double d2r = Math.PI / 180;
        double dlong = (longitude - f) * d2r;
        double dlat = (latitude - e) * d2r;
        double a = Math.pow(Math.sin(dlat / 2.0), 2) + Math.cos(e * d2r)
                * Math.cos(latitude * d2r) * Math.pow(Math.sin(dlong / 2.0), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 6367 * c;
        return d;

    }

    public void onClickAddFriends(View view) {
        Intent i = new Intent(getApplicationContext(), StartTrackingActivity.class);
        startActivity(i);
    }


    public void startLocationService() {
        Intent serviceIntent = new Intent(this, SharingLocationService.class);
        serviceIntent.putExtra(AppConstants.MYMOBILENUMBER, userMobile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    public void stopLocationService() {
        Intent serviceIntent = new Intent(this, SharingLocationService.class);
        stopService(serviceIntent);
    }


    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra(AppConstants.OTHERMOBILENUMBER, TrackingList.get(0).getMobile()).putExtra(AppConstants.MYMOBILENUMBER, userMobile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (startTrack == 1) {
                startService();
                MySharedPref.saveSharedValue(this, AppConstants.TRACKING_STATUS, String.valueOf(startTrack));

            } else {
                MySharedPref.saveSharedValue(this, AppConstants.TRACKING_STATUS, String.valueOf(startTrack));
            }

            if (MySharedPref.getSharedValue(HomeActivity.this, AppConstants.SHARING_LOCATION) != null && MySharedPref.getSharedValue(HomeActivity.this, AppConstants.SHARING_LOCATION).equals("1")) {
               startLocationService();
            }

        } catch (Exception e) {
            Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}