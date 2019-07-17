package cl.zionit.spatialite3;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Utilidad {


    public static double redondeoDecimales(double numero, int numeroDecimales) {
        BigDecimal redondeado = new BigDecimal(numero)
                .setScale(numeroDecimales, RoundingMode.HALF_EVEN);
        return redondeado.doubleValue();
    }
}
