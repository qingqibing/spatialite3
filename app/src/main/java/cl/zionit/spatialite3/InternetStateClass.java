package cl.zionit.spatialite3;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetStateClass {

    private Context activityMain;
    private Boolean tipoConexion1 = false, tipoConexion2 = true;


    public InternetStateClass(Context activityMain) {
        this.activityMain = activityMain;
    }


    public boolean getConection(){

        ProgressDialog dialog = new ProgressDialog((activityMain));
        dialog.setMessage(activityMain.getResources().getString(R.string.dialog_message_verify_conexion));
        dialog.show();
        ConnectivityManager cm = (ConnectivityManager) activityMain.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm != null){
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null){

                ConnectivityManager connManager1 = (ConnectivityManager) activityMain.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mwifi = connManager1.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                ConnectivityManager connManager2 = (ConnectivityManager) activityMain.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mMobile = connManager2.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                if (mwifi != null){
                    if (mwifi.isConnected()){
                        tipoConexion1 = true;
                    }
                }

                if (mMobile != null){
                    if (mMobile.isConnected()){
                        tipoConexion2 = true;
                    }
                }
                dialog.dismiss();
                return tipoConexion1 || tipoConexion2;

            }else{
                dialog.dismiss();
                return false;
            }

        }else{
            dialog.dismiss();
            return false;
        }

    }



}
