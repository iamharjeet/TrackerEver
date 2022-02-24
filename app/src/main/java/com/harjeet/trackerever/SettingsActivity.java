package com.harjeet.trackerever;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.MyUtils.MySharedPref;

public class SettingsActivity extends AppCompatActivity {
SeekBar seekBar;
TextView txtDistance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findIds();
handleSeekbar();
    }

    private void handleSeekbar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtDistance.setText("Distance : "+progress+" KM");
                MySharedPref.saveSharedValue(getApplicationContext(), AppConstants.NOTIFY_DISTANCE,String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void findIds() {
        seekBar=findViewById(R.id.seekbar);
        txtDistance=findViewById(R.id.txt_distance);

    }
}