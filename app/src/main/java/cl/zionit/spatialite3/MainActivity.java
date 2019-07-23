package cl.zionit.spatialite3;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.IOException;
import java.util.Locale;

import cl.zionit.spatialite3.retrofit.ApiService;
import cl.zionit.spatialite3.retrofit.GsonConverter.GsonResponsePolygons;
import cl.zionit.spatialite3.retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static cl.zionit.spatialite3.Utilidad.formatearNumerosMiles;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private GeoDatabaseHandler gdbHandler;
    private TextView communicateTextView, lbl_long, lbl_lat, velocity, card_info;
    private Button run_point_in_polygon;

    private Integer[] id = new Integer[1];
    private Integer[] repeticiones = new Integer[1];
    private Double[] distanciaAnterior = new Double[1];

    private LocationManager locationMangaer = null;
    private LocationListener locationListener = null;


    private static final int INITIAL_REQUEST = 1337;

    private TextToSpeech textToSpeech;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }


        communicateTextView = (TextView) findViewById(R.id.communicate);
        lbl_long = (TextView) findViewById(R.id.lbl_long);
        lbl_lat = (TextView) findViewById(R.id.lbl_lat);
        card_info = (TextView) findViewById(R.id.card_info);
        velocity = (TextView) findViewById(R.id.velocity);
        run_point_in_polygon = (Button) findViewById(R.id.run_point_in_polygon);



        locationMangaer = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        //Note: GeoDatabaseHandler here isn't doing too much work since this is a simple example
        // if in your app, copying the DB and/or doing queries requires a lot of processing time
        //then you probably want to do this in a thread.
        try {
            gdbHandler = new GeoDatabaseHandler(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        id[0] = 0;
        repeticiones[0] = 0;
        distanciaAnterior[0] = 0.0;




        run_point_in_polygon.setOnClickListener(this);


    }


    @Override
    protected void onResume() {
        super.onResume();

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.i("*****", "On Init ACTIVITY");
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(new Locale("es", "ES"));
                }
            }
        });

        Intent intent = new Intent(this, ServiceGps.class);
        stopService(intent);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    INITIAL_REQUEST);
        } else {
            locationMangaer.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();


    if (locationMangaer != null && locationListener != null){
        locationMangaer.removeUpdates(locationListener);
    }

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }


        Intent intent = new Intent(MainActivity.this, ServiceGps.class);
        startService(intent);

    }



    /*----------Listener class to get coordinates ------------- */
    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(final Location loc) {
            if (gdbHandler != null) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String point = "POINT(" + loc.getLatitude() + " " + loc.getLongitude() + ")";
                                String[] response = gdbHandler.queryPointInPolygon(point);

                                if (response[0] != null && response[1] != null) {
                                    if (id[0] != null && id[0] != Integer.parseInt(response[0])) {
                                        repeticiones[0] = 0;
                                    }
                                    if (repeticiones[0] < 1) {

                                        repeticiones[0]++;

                                        if (distanciaAnterior[0] == 0.0 && Double.parseDouble(response[2]) > 0.0) {
                                            String condicion = "";
                                            if (id[0] > 0) {
                                                condicion = "saliendo de " + response[1].toLowerCase();
                                            } else {
                                                condicion = " á " + Utilidad.redondeoDecimales(Double.parseDouble(response[2]), 2) + "de " + response[1].toLowerCase();
                                            }
                                            String saliendo = "Estás " + condicion + " y el maximo de velocidad es " + response[3] + " kilómetros por hora";
                                            speak(saliendo);

                                        }

                                        if (Double.parseDouble(response[2]) == 0.0) {
                                            String s = "Estás en " + response[1].toLowerCase() + " y el maximo de velocidad es " + response[3] + " kilómetros por hora";
                                            speak(s);
                                        }

                                        if (distanciaAnterior[0] > 0.0 && Double.parseDouble(response[2]) < distanciaAnterior[0]) {
                                            String entrando = "Estás a " + Utilidad.redondeoDecimales(Double.parseDouble(response[2]), 2) + " metros de  " + " entrar a " + response[1].toLowerCase() + " y el maximo de velocidad es " + response[3] + " kilómetros por hora";
                                            speak(entrando);
                                        }

                                    }

                                    if (velocity != null) velocity.setText(response[3]);
                                    if (card_info != null) card_info.setText(response[4]);
                                    if (communicateTextView != null)communicateTextView.setText(response[1]);

                                    id[0] = Integer.parseInt(response[0]);
                                    distanciaAnterior[0] = Double.parseDouble(response[2]);
                                } else {
                                    id[0] = 0;
                                    if (velocity != null)
                                        velocity.setText(getResources().getString(R.string.empty_velocity));
                                    if (card_info != null)card_info.setText("Ninguna geocerca encontrada");
                                    if (communicateTextView != null)communicateTextView.setText("Ninguna geocerca encontrada");
                                }


                                if (lbl_lat != null)
                                    lbl_lat.setText(String.valueOf(formatearNumerosMiles(loc.getLatitude())));
                                if (lbl_long != null)
                                    lbl_long.setText(String.valueOf(formatearNumerosMiles(loc.getLongitude())));
                            }
                        });

                    }
                });
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    void descarga() {

        final ProgressDialog dialog4 = new ProgressDialog(this);
        dialog4.setTitle("espere...");
        dialog4.setMessage("descargando datos desde el servidor");
        dialog4.setCancelable(false);
        dialog4.show();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<GsonResponsePolygons> callVersion = apiService.descargarDatos();
        callVersion.enqueue(new Callback<GsonResponsePolygons>() {
            @Override
            public void onResponse(@NonNull Call<GsonResponsePolygons> call, @NonNull Response<GsonResponsePolygons> response) {
                GsonResponsePolygons ss = response.body();
                if (ss != null) {
                    switch (ss.getEstado()) {
                        case 1:
                            if (gdbHandler != null) {
                                gdbHandler.deleteAll();
                                for (Polygonos dd : ss.getQuery()) {
                                    gdbHandler.insertPolygon(dd.getQuery());
                                }
                            }
                            dialog4.dismiss();
                            break;
                        case 2:
                        default:
                            System.out.println("PROBLEMAS ,NO SE HA PODIDO SINCRONIZAR  NO POSEE LA ULTIMA VERSION , POR FAVOR , COMUNIQUESE CON SU SUPERVISOR  ");

                            dialog4.dismiss();
                            break;

                        case 3:
                            System.out.println("EMPRESA BLOQUEADA POR SISTEMA");

                            dialog4.dismiss();
                            break;
                    }
                }else{
//                    Utilidad.snackbackLong(getView(), , R.color.colorTextosRed).show();
                    System.out.println("Problemas obteniendo datos del servidor ");
                    dialog4.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GsonResponsePolygons> call, @NonNull Throwable t) {
//                Utilidad.snackbackLong(getView(), , R.color.colorTextosRed).show();
                System.out.println("Problemas obteniendo datos del servidor (FAILURE)");
                dialog4.dismiss();
            }
        });
    }

 private void speak(String text){
        //textToSpeech.setPitch(2); graves y agudos
        if (textToSpeech != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, null);
            }else{
                textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        gdbHandler.cleanup();
        Intent intent = new Intent(MainActivity.this,GpsService.class);
        stopService(intent);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.run_point_in_polygon:
                descarga();
                break;
        }
    }
}
