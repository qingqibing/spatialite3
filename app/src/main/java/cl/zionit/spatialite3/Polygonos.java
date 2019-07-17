package cl.zionit.spatialite3;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Polygonos {

    @SerializedName("geocerca")
    @Expose
    private int geocerca;

    @SerializedName("query")
    @Expose
    private String query;


    public int getGeocerca() {
        return geocerca;
    }

    public void setGeocerca(int geocerca) {
        this.geocerca = geocerca;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
