package cl.zionit.spatialite3.retrofit;

import cl.zionit.spatialite3.retrofit.GsonConverter.GsonResponsePolygons;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("datos.php")
    Call<GsonResponsePolygons> descargarDatos();


/*    @GET("new_obtener_dispo.php")
    Call<GsonVersion> verificarVersion(@Query("imei") String imei, @Query("rut_empresa") String rut_empresa);

    @GET("busq_sis_init.php")
    Call<GsonResponseFirstTime> firsTime(@Query("imei") String imei, @Query("rut_empresa") String rut_empresa);*/



/*    @GET("comprobar_version.php")
    Call<GsonResponse> askVersion(@Query("version_app") String version, @Query("id_dispo") int idDispo, @Query("rut_login") String rutLogin, @Query("fecha_hora_movil") String fechaHora, @Query("rut_empresa") String rut_empresa);

    @POST("insertar_marca.php")
    Call<GsonResponse> saveLectura(@Body GsonSubirEmpaquetado listaLect);

    @GET("comprobar_cabecera.php")
    Call<GsonResponse> sendAsking(@Query("id_local") int idLocal, @Query("id_dispo") int idDispo, @Query("rut_login") String rutLogin, @Query("rut_empresa") String rut_empresa);

    @GET("segunda_respuesta.php")
    Call<GsonResponse> sendResponse(@Query("id_local") int idLocal, @Query("id_servidor") int idServidor, @Query("id_dispo") int idDispo, @Query("rut_login") String rutLogin, @Query("rut_empresa") String rut_empresa);

    @POST("insertar_asistencias.php")
    Call<GsonResponse> saveAsist(@Body GsonSubirAsistencias asistencias);

    @POST("insertar_cab.php")
    Call<GsonResponse> setCab(@Body Configuracion configuracions);*/

}

