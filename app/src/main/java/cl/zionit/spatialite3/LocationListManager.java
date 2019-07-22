package cl.zionit.spatialite3;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;

import static cl.zionit.spatialite3.Utilidad.formatearNumerosMiles;

public class LocationListManager implements LocationListener{

    private Context context;
    private GeoDatabaseHandler gdbHandler;
    private TextToSpeech textToSpeech;

    private Integer[] id = new Integer[1];
    private Integer[] repeticiones  = new Integer[1];
    private Double[] distanciaAnterior = new Double[1];

    LocationListManager(Context context, GeoDatabaseHandler gdbHandler, TextToSpeech textToSpeech) {
        this.context = context;
        this.gdbHandler = gdbHandler;
        this.textToSpeech = textToSpeech;

        repeticiones[0] = 0;
        distanciaAnterior[0] = 0.0;
    }

    @Override
    public void onLocationChanged(Location loc) {

       /* TextView velocity = (TextView) ((MainActivity) context.getApplicationContext()).findViewById(R.id.velocity);
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
                    try{
                        valor = Double.parseDouble(response[2]);
                    }catch (Exception e){
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

                    if (valor == 0.0){
                        String s = "Estás en " + response[1].toLowerCase() + " y el maximo de velocidad es " + response[3] + " kilómetros por hora";
                        speak(s);
//                        if (!textToSpeech.isSpeaking()) {
//                            textToSpeech = new TextToSpeech(MainActivity.this, MainActivity.this);
//                        }
                    }

                    if (distanciaAnterior[0] > 0.0 && valor < distanciaAnterior[0]){
                        String entrando = "Estás a "+Utilidad.redondeoDecimales(Double.parseDouble(response[2]), 2) + " metros de  " +" entrar a " + response[1].toLowerCase() + " y el maximo de velocidad es " + response[3] + " kilómetros por hora";
                        speak(entrando);
                    }

                }

               /* if (velocity != null) velocity.setText(response[3]);
                if (card_info != null) card_info.setText(response[4]);
                if (communicateTextView != null)communicateTextView.setText(response[1]);*/

                id[0] = Integer.parseInt(response[0]);
                distanciaAnterior[0] = Double.parseDouble(response[2]);
            }else{
                id[0] = 0;
              /*if (velocity != null) velocity.setText(context.getResources().getString(R.string.empty_velocity));
                if (card_info != null) card_info.setText("Ninguna geocerca encontrada");
                if (communicateTextView != null)communicateTextView.setText("Ninguna geocerca encontrada");*/
            }
        }

       /* if (lbl_lat != null)lbl_lat.setText(String.valueOf(formatearNumerosMiles(loc.getLatitude())));
        if (lbl_long != null)lbl_long.setText(String.valueOf(formatearNumerosMiles(loc.getLongitude())));*/







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


}
