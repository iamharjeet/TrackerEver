package com.harjeet.trackerever;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.MyUtils.MySharedPref;

public class SplashScreenActivity extends AppCompatActivity {
ImageView logo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        logo = findViewById(R.id.img_logo);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.shaking);
        logo.setAnimation(animation);

   startTimer();
}
    public void startTimer() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                SharedPreferences preferences = getSharedPreferences("myPref", MODE_PRIVATE);
//                String user = preferences.getString("user_phone", null);
           String user= MySharedPref.getSharedValue(getApplicationContext(), AppConstants.USER_PHONE);
                if (user != null && !user.equals("0")) {
                    Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(getApplicationContext(), PhoneVerificationActivity.class);
                    startActivity(i);
                }

            }
        });
        t.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.shaking);
        logo.setAnimation(animation);
        startTimer();
    }
}
