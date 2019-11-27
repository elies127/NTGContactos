package com.naranjatradicionaldegandia.elias.agregadordecontactosntg;

public class Correo {

    private String asunto;
    private String contenido;
    private String fecha;
    private String dedonde;

    public Correo (String asunto, String contenido, String fecha, String dedonde){
        this.asunto = asunto;
        this.contenido =contenido;
        this.fecha = fecha;
        this.dedonde = dedonde;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDedonde() {
        return dedonde;
    }

    public void setDedonde(String dedonde) {
        this.dedonde = dedonde;
    }
}
