package com.example.registrodebaches;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {


    private TextView accelerationView;
    private TextView textViewLocation;
    private int FINELOCATIONCODE = 1;

    private static LocationManager locationManager;
    private MediaPlayer mp;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean isRecording;
    private Button recordingBtn, btnDisplayCoordenadas;
    private int coordenadaId;
    private Map<Date, Coordenada> coordenadas;
    public static Context parentContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parentContext = getApplicationContext();
        //sensor = new MySensor();
        //myLocationService = new MyLocationService();
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        locationManager = (LocationManager)MainActivity.parentContext.getSystemService(MainActivity.parentContext.LOCATION_SERVICE);
        //request permissions ?
        if (ContextCompat.checkSelfPermission(MainActivity.parentContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.parentContext, "You have already  coarse location permission ;)", Toast.LENGTH_SHORT).show();
        } else {
            requestLocationPermissions();
        }
        mp = MediaPlayer.create(MainActivity.this, R.raw.beep);

        this.findViewById(android.R.id.content);

        TextView textViewHasAccelerometer = (TextView) findViewById(R.id.textViewHasAccelerometer);
        textViewHasAccelerometer.setText("");
        if (mAccelerometer != null) {
            //Device has accelerometer
            textViewHasAccelerometer.setText("Tu dispositivo tiene acelerometro \n" + mAccelerometer.getName());

        } else {
            textViewHasAccelerometer.setText("Tu dispositivo no tiene acelerómetro y no es posible grabar altitudes en esta aplicación");
        }
        accelerationView = (TextView) findViewById(R.id.textViewLinearAcceleration);
        accelerationView.setText("Inicializado");
        textViewLocation = (TextView) findViewById(R.id.textViewLocation);
        textViewLocation.setText("Location: Inicializado");

        //inicializa lista de coordenadas a grabar
        coordenadas = new HashMap<>();

        isRecording = false;
        coordenadaId = 0;
        recordingBtn = findViewById(R.id.recordBtn);

        recordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String btnText = ((Button) view).getText().toString();
                if (!isRecording) {
                    btnText = "STOP..";
                    isRecording = true;
                } else {
                    btnText = "RECORD";
                    isRecording = false;
                }
                ((Button) view).setText(btnText);
            }
        });
        btnDisplayCoordenadas = findViewById(R.id.btnDisplayCoordenadas);
        btnDisplayCoordenadas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printCoordenadas();
            }
        });

    }

    //LOCATION
    private void requestLocationPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) MainActivity.parentContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(MainActivity.parentContext)
                    .setTitle("Permission needed")
                    .setMessage("La app necesita acceder a tu ubicación para registrar desniveles en el mapa")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions((Activity) MainActivity.parentContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINELOCATIONCODE);
                        }
                    })
                    .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions((Activity) MainActivity.parentContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINELOCATIONCODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINELOCATIONCODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted: " + grantResults[0], Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission NOT granted", Toast.LENGTH_SHORT).show();

            }
        }
    }


    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);


    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

    }


    private void printCoordenadas() {
        //LinearLayout2
        //ScrollView
        //--constraint layout
        //  --LinearLayout


        LinearLayout listaCoordenadas = findViewById(R.id.listaCoordenadas);

        listaCoordenadas.removeAllViewsInLayout();

        coordenadas.forEach((key, value) -> {
            TextView newLine = new TextView(this);
            newLine.setText("ID: " + value.getId() + " lat : " + value.getLat() + " lng : " + value.getLng());

            listaCoordenadas.addView(newLine);
        });
    }


    ////////////SENSOR
    public void onSensorChanged(@NonNull SensorEvent event) {


        //accelerationView.setText("Acceleration minus Gz on the z-axis: " + String.valueOf(event.values[1]));

        System.out.println("Sensor changed! " + new Date());


        if (event.values[2] > 14 && isRecording) {

            accelerationView.setText("jump counts!" + new Date());
            //play beep sensor detect irregularity
            mp.start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION);

                    textViewLocation.setText("LOCATION SIN PERMISOS");
                    return;
                }
                locationManager.getCurrentLocation(
                        LocationManager.GPS_PROVIDER,
                        null,
                        this.getMainExecutor(),
                        new Consumer<Location>() {
                            @Override
                            public void accept(Location location) {
                                double lat, lng;

                                lat = location.getLatitude();
                                lng = location.getLongitude();
                                // code
                                textViewLocation.setText("Lat:" + lat + "  Long: " + lng);
                                coordenadaId++;
                                //agrega coordenada al array list
                                coordenadas.put(new Date(), new Coordenada(coordenadaId, lat, lng));

                            }
                        });

            }


        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}