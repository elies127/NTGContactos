package com.naranjatradicionaldegandia.elias.agregadordecontactosntg;

import android.util.Log;

public class Correo {

    private String asunto;
    private String contenido;
    private String fecha;
    private String dedonde;

    public Correo(String asunto, String contenido, String fecha, String dedonde) {
        this.asunto = asunto;
        this.contenido = contenido;
        this.fecha = fecha;
        this.dedonde = dedonde;
    }

    public String getAsunto() {
        return asunto;
    }


    public String getContenido() {
        return contenido;
    }


    public String getFecha() {
        return fecha;
    }


    public String getDedonde() {
        return dedonde;
    }

}
