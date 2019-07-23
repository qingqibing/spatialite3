package cl.zionit.spatialite3;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

import static cl.zionit.spatialite3.Utilidad.formatearNumerosMiles;

public  class GpsService extends Service {


    private Integer[] id = new Integer[1];
    private Integer[] repeticiones = new Integer[1];
    private Double[] distanciaAnterior = new Double[1];

    private LocationManager locationMangaer = null;
    private LocationListener locationListener = null;
    private GeoDatabaseHandler gdbHandler = null;

    private Context context = this;
    private TextToSpeech textToSpeech;

    private static final int TWO_MINUTES = 1000 * 60 * 2;


    public Location previousBestLocation = null;

    @Override
    public void onCreate() {


//        intent = new Intent(BROADCAST_ACTION);
    if (textToSpeech != null){
        if(!textToSpeech.isSpeaking()) {
            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    Log.i("*****", "On Init");
                    if (status == TextToSpeech.SUCCESS) {
                        textToSpeech.setLanguage(new Locale("es", "ES"));
                    }
                }
            });
        }
    }else{
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.i("*****", "On Init");
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(new Locale("es", "ES"));
                }
            }
        });
    }




        if (gdbHandler == null){
            try {
                gdbHandler = new GeoDatabaseHandler(this);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        locationMangaer = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListManager();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        locationMangaer.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

        Log.i("*****", "On CREATE");
        Log.i("*****", "On CREATE");

        repeticiones[0] = 0;
        distanciaAnterior[0] = 0.0;



        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("entrando a startCommand");

        return START_STICKY;
    }



    @Override
    public void onDestroy() {

        Log.i("*****", "On Destroy");
        Log.i("*****", "On Destroy");

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();

            textToSpeech = null;
        }

//        onCreate();
        locationMangaer.removeUpdates(locationListener);
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }






/*    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }*/

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }



    public class LocationListManager implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {


            Log.i("*****", "Location changed");
//            if(isBetterLocation(loc, previousBestLocation)) {
                loc.getLatitude();
                loc.getLongitude();

                /*TextView velocity = (TextView) ((MainActivity) context.getApplicationContext()).findViewById(R.id.velocity);
                TextView card_info = (TextView) ((MainActivity) context).findViewById(R.id.card_info);
                TextView communicateTextView = (TextView) ((MainActivity) context).findViewById(R.id.communicate);
                TextView lbl_lat = (TextView) ((MainActivity) context).findViewById(R.id.lbl_lat);
                TextView lbl_long = (TextView) ((MainActivity) context).findViewById(R.id.lbl_long);*/


                String point = "POINT(" + loc.getLatitude() + " " + loc.getLongitude() + ")";
                String[] response = gdbHandler.queryPointInPolygon(point);
                if (textToSpeech != null) {
                    if (response[0] != null && response[1] != null && response[2] != null) {
                        if (id[0] != null && id[0] != Integer.parseInt(response[0])) {
                            repeticiones[0] = 0;
                        }
                        if (repeticiones[0] < 1) {

                            repeticiones[0]++;

                            double valor = 0.0;
                            try {
                                valor = Double.parseDouble(response[2]);
                            } catch (Exception e) {
                                valor = 0.0;
                            }

/*                    if (distanciaAnterior[0] == 0.0 && valor > 0.0 ){
                        String condicion = "";
                        if (id[0] > 0){
                            condicion = "saliendo de "+ response[1].toLowerCase();
                        }else{
                            condicion = " á "+ Utilidad.redondeoDecimales(Double.parseDouble(response[2]), 2)+ "de " + response[1].toLowerCase();
                        }
                        String saliendo = "Estás "+ condicion+" y el maximo de velocidad es " + response[3] + " kilómetros por hora";
                        speak(saliendo);
                    }*/

                            if (valor == 0.0) {
                                final String s = "Estás en " + response[1].toLowerCase() + " y el maximo de velocidad es " + response[3] + " kilómetros por hora";
                                speak(s);
/*                                if (textToSpeech != null) {
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                textToSpeech.speak(s, TextToSpeech.QUEUE_ADD, null, null);
                                            } else {
                                                textToSpeech.speak(s, TextToSpeech.QUEUE_ADD, null);
                                            }
                                        }
                                    });

                                }*/
//                        if (!textToSpeech.isSpeaking()) {
//                            textToSpeech = new TextToSpeech(MainActivity.this, MainActivity.this);
//                        }
                            }

                            if (valor > 0.0/* && valor < distanciaAnterior[0]*/) {
                                final String entrando = "Estás a " + Utilidad.redondeoDecimales(Double.parseDouble(response[2]), 2) + " metros de  " + " entrar a " + response[1].toLowerCase() + " y el maximo de velocidad es " + response[3] + " kilómetros por hora";
                                speak(entrando);
/*                                if (textToSpeech != null) {
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                textToSpeech.speak(entrando, TextToSpeech.QUEUE_ADD, null, null);
                                            } else {
                                                textToSpeech.speak(entrando, TextToSpeech.QUEUE_ADD, null);
                                            }
                                        }
                                    });

                                }*/
                            }

                        }

                       /* if (velocity != null) velocity.setText(response[3]);
                        if (card_info != null) card_info.setText(response[4]);
                        if (communicateTextView != null) communicateTextView.setText(response[1]);*/

                        id[0] = Integer.parseInt(response[0]);
                        distanciaAnterior[0] = Double.parseDouble(response[2]);
                    } else {
                        id[0] = 0;
                       /* if (velocity != null)
                            velocity.setText(context.getResources().getString(R.string.empty_velocity));
                        if (card_info != null) card_info.setText("Ninguna geocerca encontrada");
                        if (communicateTextView != null)
                            communicateTextView.setText("Ninguna geocerca encontrada");*/
                    }
                }

               /* if (lbl_lat != null)
                    lbl_lat.setText(String.valueOf(formatearNumerosMiles(loc.getLatitude())));
                if (lbl_long != null)
                    lbl_long.setText(String.valueOf(formatearNumerosMiles(loc.getLongitude())));*/

//            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }


    }

    private void speak(String text) {
        //textToSpeech.setPitch(2); graves y agudos
        if (textToSpeech != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, null);
            } else {
                textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
            }
        }
    }
}
