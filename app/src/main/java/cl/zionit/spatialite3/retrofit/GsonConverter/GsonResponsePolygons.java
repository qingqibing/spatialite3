package cl.zionit.spatialite3.retrofit.GsonConverter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import cl.zionit.spatialite3.clases.Polygonos;

public class GsonResponsePolygons {
    @SerializedName("estado")
    @Expose
    private int estado;

    @SerializedName("mensaje")
    @Expose
    private List<Polygonos> query;

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public List<Polygonos> getQuery() {
        return query;
    }

    public void setQuery(List<Polygonos> query) {
        this.query = query;
    }
}
