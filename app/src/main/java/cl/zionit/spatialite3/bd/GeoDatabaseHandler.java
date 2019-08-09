package cl.zionit.spatialite3.bd;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cl.zionit.spatialite3.R;
import jsqlite.*;
import pimp.spatialite_database_driver.Credits;

/**
 * Created by kristina on 9/2/15.
 */
public class GeoDatabaseHandler {

    private static final String TAG = "GEODBH";
    private static final String TAG_SL = TAG + "_JSQLITE";

    private static String DB_PATH = "/data/data/cl.zionit.spatialite3/databases";
    private static String DB_NAME = "geocerca.sqlite";
    private Database spatialiteDb;


    public GeoDatabaseHandler(Context context) throws IOException {

        File cacheDatabase = new File(DB_PATH, DB_NAME);
        if (!cacheDatabase.getParentFile().exists()) {
            File dirDb = cacheDatabase.getParentFile();
            if (!dirDb.mkdir()) {
                throw new IOException(TAG_SL + "Could not create dirDb: " + dirDb.getAbsolutePath());
            }

            InputStream inputStream = context.getResources().openRawResource(R.raw.geocerca);
            copyDatabase(inputStream, DB_PATH + File.separator + DB_NAME);
        }

        try {
            spatialiteDb = new Database();
            spatialiteDb.open(cacheDatabase.getAbsolutePath(), Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE);

            try {
                Stmt stmt = spatialiteDb.prepare("ALTER TABLE geocerca ADD COLUMN aviso_cercania_geo INTEGER default 1");
                stmt.step();
            } catch (SQLiteException ex) {
                Log.w(TAG, "Altering geocerca : " + ex.getMessage());
            }

        } catch (jsqlite.Exception e) {
            e.printStackTrace();
//            Log.e(TAG_SL, e.getMessage());
        }

    }

    public void cleanup() {
        try {
            spatialiteDb.close();
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }
    }

    private void copyDatabase(InputStream inputStream, String dbFilename) throws IOException {

        OutputStream outputStream = new FileOutputStream(dbFilename);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer,0,length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public String[] queryPointInPolygon(String gpsPoint) {
        String query = "SELECT id,nombre, ST_Distance(`polygon`, GeomFromText('"+gpsPoint+"'))*111319 AS distancia, limite,descripcion,aviso_cercania_geo  FROM geocerca ORDER BY distancia ASC LIMIT 1;";
        String[] respuesta = new String[6];
        try {
            Stmt stmt = spatialiteDb.prepare(query);
            int maxColumns = stmt.column_count() ;
            int rowIndex = 0;
            while (stmt.step()) {
                for (int i = 0; i < maxColumns; i++) {
                    respuesta[i] = stmt.column_string(i);
                }
                if (rowIndex++ > 2) break;
            }
            stmt.close();
        } catch (jsqlite.Exception e) {
            for (int i = 0; i < 6; i++){
                respuesta[i] = null;
            }
        }

        return respuesta;
    }

    public void deleteAll(){
        try {
            String query = "DELETE FROM geocerca";
            Stmt stmt = spatialiteDb.prepare(query);

            stmt.step();
            stmt.close();

        } catch (jsqlite.Exception e) {
//            Log.e(TAG_SL,e.getMessage());
        }
    }

    public void insertPolygon(String query){
        try {
            Stmt stmt = spatialiteDb.prepare(query);
            stmt.step();
            stmt.close();
        } catch (jsqlite.Exception e) {
           Log.e(TAG_SL,e.getMessage());
        }
    }
}
