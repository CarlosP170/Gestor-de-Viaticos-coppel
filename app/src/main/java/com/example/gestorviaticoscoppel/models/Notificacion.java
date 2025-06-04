package com.example.gestorviaticoscoppel.models;

public class Notificacion {
    private int idNotificacion;
    private String idUsuario;
    private String tipo;
    private String titulo;
    private String mensaje;
    private String fechaCreacion;
    private boolean leida;
    private String datosRelacionados;

    public static final String TIPO_SOLICITUD_RECHAZADA = "SOLICITUD_RECHAZADA";
    public static final String TIPO_VIATICOS_AUTORIZADOS = "VIATICOS_AUTORIZADOS";
    public static final String TIPO_VIAJE_FINALIZADO = "VIAJE_FINALIZADO";
    public static final String TIPO_VIAJE_RECHAZADO = "VIAJE_RECHAZADO";
    public static final String TIPO_SOLICITUDES_PENDIENTES = "SOLICITUDES_PENDIENTES";
    public static final String TIPO_VIAJES_PENDIENTES = "VIAJES_PENDIENTES";

    public Notificacion() {}

    public Notificacion(String idUsuario, String tipo, String titulo, String mensaje, String datosRelacionados) {
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.datosRelacionados = datosRelacionados;
        this.leida = false;
    }

    public int getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(int idNotificacion) { this.idNotificacion = idNotificacion; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public String getDatosRelacionados() { return datosRelacionados; }
    public void setDatosRelacionados(String datosRelacionados) { this.datosRelacionados = datosRelacionados; }

    public String getFechaFormateada() {
        if (fechaCreacion == null) return "";
        try {
            if (fechaCreacion.length() >= 10) {
                return fechaCreacion.substring(0, 10);
            }
            return fechaCreacion;
        } catch (Exception e) {
            return fechaCreacion;
        }
    }
}