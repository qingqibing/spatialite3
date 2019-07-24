package cl.zionit.spatialite3.Servicios;

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

import cl.zionit.spatialite3.bd.GeoDatabaseHandler;
import cl.zionit.spatialite3.utilidad.Utilidad;

public class ServiceGps extends IntentService {

    private static int FOREGROUND_ID=1338;

    private Integer[] id = new Integer[1];
    private Integer[] repeticiones = new Integer[1];
    private Integer[] repeticionesSaliendo = new Integer[1];
    private Integer[] repeticionesEntrando = new Integer[1];
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
        if (gdbHandler == null){
            try {
                gdbHandler = new GeoDatabaseHandler(this);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        locationMangaer = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListManager();

        id[0] = 0;
        repeticiones[0] = 0;
        distanciaAnterior[0] = 0.0;




        try{

            Thread.sleep(1000);
            if (textToSpeech != null){
                if(!textToSpeech.isSpeaking()) {
                    textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
//                            Log.i("*****", "On Init");
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
//                        Log.i("*****", "On Init");
                        if (status == TextToSpeech.SUCCESS) {
                            textToSpeech.setLanguage(new Locale("es", "ES"));
                        }
                    }
                });
            }

        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }


        startForeground(FOREGROUND_ID,buildForegroundNotification("Inicializando busqueda...", "", 1));


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
        Toast.makeText(this, "Finalizando Servicio", Toast.LENGTH_SHORT).show();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }

        if (locationMangaer != null){
            locationMangaer.removeUpdates(locationListener);
        }

    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @TargetApi(Build.VERSION_CODES.O)
    private Notification buildForegroundNotification(String filename, String texto, int accion) {

        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("my_service", "My Background Service");
        } else {
            channelId = "";
        }


        if (locationMangaer != null && locationListener != null){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationMangaer.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            }
        }


        NotificationCompat.Builder b = new NotificationCompat.Builder(this,channelId);
        b.setContentTitle("Geocercas")
                .setContentText(filename)
                .setSound(null)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setTicker("Obteniendo posición...");


        speak(texto);

        return(b.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){

        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        chan.setSound(null,null);


        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);

        return channelId;
    }

    public class LocationListManager implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            loc.getLatitude();
            loc.getLongitude();
            String point = "POINT(" + loc.getLatitude() + " " + loc.getLongitude() + ")";
            String[] response = gdbHandler.queryPointInPolygon(point);
            if (textToSpeech != null) {
                if (response[0] != null && response[1] != null && response[2] != null) {
                    if (id[0] != null && id[0] != Integer.parseInt(response[0])) {
                        repeticiones[0] = 0;
                        repeticionesEntrando[0] = 0;
                        repeticionesSaliendo[0] = 0;
                    }

                    double valor = 0.0;
                    try {
                        valor = Double.parseDouble(response[2]);
                    } catch (Exception e) {
                        valor = 0.0;
                    }


                    if (valor == 0.0 && repeticiones[0] < 1) {
                        repeticiones[0]++;
                        final String s = "Estás en " + response[1].toLowerCase() + " y el maximo de velocidad es " + response[3] + " kilómetros por hora";
                        startForeground(FOREGROUND_ID,buildForegroundNotification(s, s,1));
                    }else if ( ((id[0] != null && id[0] == 0 || id[0] != null && id[0] != Integer.parseInt(response[0])) && valor > 0.0 && distanciaAnterior[0] > 0.0) && repeticionesEntrando[0] < 1) {
                        repeticionesEntrando[0]++;
                        final String entrando = "Estás a " + Utilidad.redondeoDecimales(Double.parseDouble(response[2]), 2) + " metros de  " + " entrar a " + response[1].toLowerCase() + " y el maximo de velocidad es " + response[3] + " kilómetros por hora";
                        startForeground(FOREGROUND_ID, buildForegroundNotification(entrando, entrando, 1));
                    }else if ((id[0] != null && id[0] == Integer.parseInt(response[0]) ) && (distanciaAnterior[0] == 0.0 && valor > 0.0) && repeticionesSaliendo[0] < 1) {
                        repeticionesSaliendo[0]++;
                        String condicion = "";
                        if (id[0] > 0){
                            condicion = "saliendo de "+ response[1].toLowerCase();
                        }
                        String saliendo = "Estás "+ condicion+" y el maximo de velocidad es " + response[3] + " kilómetros por hora";
                        startForeground(FOREGROUND_ID,buildForegroundNotification(saliendo, saliendo,1));
                    }else{
                        startForeground(FOREGROUND_ID,buildForegroundNotification(point, "",1));
                    }

                    id[0] = Integer.parseInt(response[0]);
                    distanciaAnterior[0] = Double.parseDouble(response[2]);
                } else {
                    id[0] = 0;
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            Toast.makeText(ServiceGps.this, "status changed " + provider + " status: " + status, Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(ServiceGps.this, "Provider enabled " + provider, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(ServiceGps.this, "Provider disabled " + provider, Toast.LENGTH_SHORT).show();
        }
    }

    private void speak(String text) {
        //textToSpeech.setPitch(2); graves y agudos
        if (textToSpeech != null) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, null);
        }
    }
}
