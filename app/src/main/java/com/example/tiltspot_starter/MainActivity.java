package com.example.tiltspot_starter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity
            implements SensorEventListener {

        // System sensor manager instance.
        private SensorManager mSensorManager;

        // Accelerometer and magnetometer sensors, as retrieved from the
        // sensor manager.
        private Sensor mSensorAccelerometer;
        private Sensor mSensorMagnetometer;

        // TextViews to display current sensor values.
        private TextView mTextSensorAzimuth;
        private TextView mTextSensorPitch;
        private TextView mTextSensorRoll;

        // Very small values for the accelerometer (on all three axes) should
        // be interpreted as 0. This value is the amount of acceptable
        // non-zero drift.
        private static final float VALUE_DRIFT = 0.05f;

        // Add member variables to hold copies of the accelerometer and magnetometer data
        private float[] mAccelerometerData = new float[3];
        private float[] mMagnetometerData = new float[3];

        // ImageView drawables to display spots.
        private ImageView mBubble;

        private Display mDisplay;

        private float azimuth, pitch, roll;

        private String timestamp;

        int recordId;

        private Boolean isSave;

        ImageButton btSave, btView;

        DatabaseReference db;

        public List<Record> lstRecords;
        public String _pitch, _roll, _timestamp;

        private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel"; //Channel ID
        private static final int NOTIFICATION_ID = 0;
        private NotificationManager mNotificationManager;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            btSave = (ImageButton) findViewById(R.id.btSave);
            btView = (ImageButton) findViewById(R.id.btView);

            // Lock the orientation to portrait (for now)
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            db = FirebaseDatabase.getInstance().getReference("records");

            mTextSensorAzimuth = (TextView) findViewById(R.id.value_azimuth);
            mTextSensorPitch = (TextView) findViewById(R.id.value_pitch);
            mTextSensorRoll = (TextView) findViewById(R.id.value_roll);
            mBubble = (ImageView) findViewById(R.id.bubble);


            // Get accelerometer and magnetometer sensors from the sensor manager.
            // The getDefaultSensor() method returns null if the sensor
            // is not available on the device.
            mSensorManager = (SensorManager) getSystemService(
                    Context.SENSOR_SERVICE);
            mSensorAccelerometer = mSensorManager.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER);
            mSensorMagnetometer = mSensorManager.getDefaultSensor(
                    Sensor.TYPE_MAGNETIC_FIELD);

            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            mDisplay = wm.getDefaultDisplay();

            btSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pushToDB();
                }
            });

            btView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewData();
                }
            });

            createNotificationChannel();

        }

    private void viewData() {


        final ArrayList<Record> records = new ArrayList<>();

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lstRecords = new ArrayList<Record>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    _pitch = postSnapshot.getValue(Record.class).pitch;
                    _roll = postSnapshot.getValue(Record.class).roll;
                    _timestamp = postSnapshot.getValue(Record.class).timestamp;

                        Record record = new Record();

                        record.setPitch(_pitch);
                        record.setRoll(_roll);
                        record.setTimestamp(_timestamp);

                        lstRecords.add(record);
                }

                Intent intent = new Intent(MainActivity.this, ViewRecords.class);
                intent.putExtra("Records", (Serializable) lstRecords);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
         * Listeners for the sensors are registered in this callback so that
         * they can be unregistered in onStop().
         */
        @Override
        protected void onStart() {
            super.onStart();

            // Listeners for the sensors are registered in this callback and
            // can be unregistered in onStop().
            //
            // Check to ensure sensors are available before registering listeners.
            // Both listeners are registered with a "normal" amount of delay
            // (SENSOR_DELAY_NORMAL).
            if (mSensorAccelerometer != null) {
                mSensorManager.registerListener(this, mSensorAccelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (mSensorMagnetometer != null) {
                mSensorManager.registerListener(this, mSensorMagnetometer,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        @Override
        protected void onStop() {
            super.onStop();

            // Unregister all sensor listeners in this callback so they don't
            // continue to use resources when the app is stopped.
            mSensorManager.unregisterListener(this);
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            int sensorType = sensorEvent.sensor.getType();

            switch (sensorType) {
                case Sensor.TYPE_ACCELEROMETER:
                    mAccelerometerData = sensorEvent.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mMagnetometerData = sensorEvent.values.clone();
                    break;
                default:
                    return;
            }

            float[] rotationMatrix = new float[9];
            boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix,
                    null, mAccelerometerData, mMagnetometerData);

            float orientationValues[] = new float[3];
            float[] rotationMatrixAdjusted = new float[9];

            switch (mDisplay.getRotation()) {
                case Surface.ROTATION_0:
                    rotationMatrixAdjusted = rotationMatrix.clone();
                    break;
                case Surface.ROTATION_90:
                    SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
                            rotationMatrixAdjusted);
                    break;
                case Surface.ROTATION_180:
                    SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y,
                            rotationMatrixAdjusted);
                    break;
                case Surface.ROTATION_270:
                    SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X,
                            rotationMatrixAdjusted);
                    break;
            }

            if (rotationOK) {
                SensorManager.getOrientation(rotationMatrix, orientationValues);
                SensorManager.getOrientation(rotationMatrixAdjusted,
                        orientationValues);

            }

            azimuth = orientationValues[0];
            pitch = orientationValues[1];
            roll = orientationValues[2];

            mTextSensorAzimuth.setText(getResources().getString(
                    R.string.value_format, azimuth));
            mTextSensorPitch.setText(getResources().getString(
                    R.string.value_format, pitch));
            mTextSensorRoll.setText(getResources().getString(
                    R.string.value_format, roll));

            if (Math.abs(pitch) < VALUE_DRIFT) {
                pitch = 0;
            }
            if (Math.abs(roll) < VALUE_DRIFT) {
                roll = 0;
            }

            //Reset all spot values to 0. Without this animation artifacts happen with fast tilts.
            moveBubble(pitch,roll);

            mTextSensorAzimuth.setText(getResources().getString(R.string.value_format, azimuth));
            mTextSensorPitch.setText(getResources().getString(R.string.value_format, pitch));
            mTextSensorRoll.setText(getResources().getString(R.string.value_format, roll));

            if(pitch == 0 && roll == 0){
                Notify();
            }

        }

    public void createNotificationChannel(){
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            //Create a notificationChannel
            NotificationChannel notificationChannel = new NotificationChannel(
                    PRIMARY_CHANNEL_ID, //Channel ID
                    "Test Notification Channel", //name for the channel
                    NotificationManager.IMPORTANCE_HIGH); //Importance Level

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from test Notification Channel");

            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private NotificationCompat.Builder getNotificationBuilder() {

        //Create an intent that will launch when a user taps on the notification
        Intent notificationIntent = new Intent(this,MainActivity.class);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                this,
                NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //create and instantiate our notification builder
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(
                this, //application context
                PRIMARY_CHANNEL_ID) //notification channel ID
                .setContentTitle("Congratulations!!!")
                .setContentText("You have set the bubble on the target" )
                .setSmallIcon(R.drawable.bubbly)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);

        return notifyBuilder;
    }

    private void Notify() {
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
        mNotificationManager.notify(NOTIFICATION_ID,notifyBuilder.build());
    }

    private void pushToDB() {
        recordId+=1;
        timestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        String id = Integer.toString(recordId);//db.push().getKey();
        Record record = new Record(id, String.valueOf(pitch), String.valueOf(roll), timestamp);
        db.child(id).setValue(record);
    }

    public void moveBubble(float pitch1, float roll1){
            View v = mBubble;
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator xAxis = ObjectAnimator.ofFloat(v,"X",4000*roll1+570);
            ObjectAnimator yAxis = ObjectAnimator.ofFloat(v,"Y",4000*pitch1+970);
            animatorSet.playTogether(xAxis,yAxis);
            animatorSet.setDuration(400);
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.start();
        }

        /**
         * Must be implemented to satisfy the SensorEventListener interface;
         * unused in this app.
         */
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

}
