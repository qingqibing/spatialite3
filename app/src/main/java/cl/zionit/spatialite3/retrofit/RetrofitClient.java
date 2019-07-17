package cl.zionit.spatialite3.retrofit;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build();



/*    private static String getConection(){
        String ruta = "";

        Configuracion configuracion = MainActivity.myAppDB.myDao().getConfiguracion();
        if (configuracion != null){
            String ip="";
            if (configuracion.getIpSelected() == 1){
                ip = configuracion.getIpPrimaria();
            }else{
                ip = configuracion.getIpSecundaria();
            }
            ruta =  ip + configuracion.getRutaFile();
        }

        return ruta;
    }*/



    public static Retrofit getClient(){
        if(retrofit==null){
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://190.13.170.27/spatialite/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
