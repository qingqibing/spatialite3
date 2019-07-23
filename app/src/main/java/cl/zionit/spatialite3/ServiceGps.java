package cl.zionit.spatialite3;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

import static android.app.NotificationChannel.DEFAULT_CHANNEL_ID;

public class ServiceGps extends IntentService {

    private static int NOTIFY_ID=1337;
    private static int FOREGROUND_ID=1338;

    private Integer[] id = new Integer[1];
    private Integer[] repeticiones = new Integer[1];
    private Double[] distanciaAnterior = new Double[1];

    private LocationManager locationMangaer = null;
    private LocationListener locationListener = null;
    private GeoDatabaseHandler gdbHandler = null;
    private TextToSpeech textToSpeech;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ServiceGps(String name) {
        super(name);
    }


    public ServiceGps() {
        super("ServiceGps");
    }

    @Override
    public void onCreate() {
        Log.i("*****", "On CREATE");
        Log.i("*****", "On CREATE");

        if (gdbHandler == null){
            try {
                gdbHandler = new GeoDatabaseHandler(this);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        locationMangaer = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListManager();

        repeticiones[0] = 0;
        distanciaAnterior[0] = 0.0;




        try{

            Thread.sleep(1000);
            if (textToSpeech != null){
                if(!textToSpeech.isSpeaking()) {
                    textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
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
                textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        Log.i("*****", "On Init");
                        if (status == TextToSpeech.SUCCESS) {
                            textToSpeech.setLanguage(new Locale("es", "ES"));
                        }
                    }
                });
            }

        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }


        startForeground(FOREGROUND_ID,buildForegroundNotification("please please", ""));


        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Servicio inicialiandose", Toast.LENGTH_SHORT).show();




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
        if (locationMangaer != null){
            locationMangaer.removeUpdates(locationListener);
        }

    }

    @Override
    protected void onHandleIntent(Intent intent) {




    }

    @TargetApi(Build.VERSION_CODES.O)
    private Notification buildForegroundNotification(String filename, String texto) {

        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("my_service", "My Background Service");
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            channelId = "";
        }


        if (locationMangaer != null && locationListener != null){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationMangaer.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            }
        }






        NotificationCompat.Builder b = new NotificationCompat.Builder(this,channelId);
        b.setContentTitle("TITULO NOTIFICACION")
                .setContentText(filename)
                .setSound(null)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setTicker("PARA QUE NO SE CIERRE LA NOTIFICACION");


        speak(texto);

        return(b.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        chan.setSound(null,null);

        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);

        return channelId;
    }

/*    private void raiseNotification(Intent inbound, File output, Exception e) {
        NotificationCompat.Builder b=new NotificationCompat.Builder(this);

        b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setWhen(System.currentTimeMillis());

        if (e == null) {
            b.setContentTitle(getString(R.string.download_complete))
                    .setContentText(getString(R.string.fun))
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setTicker(getString(R.string.download_complete));

            Intent outbound=new Intent(Intent.ACTION_VIEW);
            Uri outputUri=
                    FileProvider.getUriForFile(this, AUTHORITY, output);

            outbound.setDataAndType(outputUri, inbound.getType());
            outbound.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            PendingIntent pi=PendingIntent.getActivity(this, 0,
                    outbound, PendingIntent.FLAG_UPDATE_CURRENT);

            b.setContentIntent(pi);
        }
        else {
            b.setContentTitle(getString(R.string.exception))
                    .setContentText(e.getMessage())
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setTicker(getString(R.string.exception));
        }

        NotificationManager mgr=
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        mgr.notify(NOTIFY_ID, b.build());
    }*/


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
                            startForeground(FOREGROUND_ID,buildForegroundNotification("please please", s));
//                            speak(s);
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
                            startForeground(FOREGROUND_ID,buildForegroundNotification("please please", entrando));
//                            speak(entrando);
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
                textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, null);
        }
    }
}
