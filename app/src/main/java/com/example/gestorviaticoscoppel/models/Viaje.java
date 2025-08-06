package com.example.gestorviaticoscoppel.models;

public class Viaje {
    private int idViaje;
    private int idSolicitud;
    private String idUsuario;
    private String folioViaje;

    private double montoHotelAutorizado;
    private double montoComidaAutorizado;
    private double montoTransporteAutorizado;
    private double montoGasolinaAutorizado;

    private double montoHotelGastado;
    private double montoComidaGastado;
    private double montoTransporteGastado;
    private double montoGasolinaGastado;
    private double montoOtrosGastado;

    private String estado;

    private String fechaInicio;
    private String fechaEnvioRevision;
    private String fechaFinalizacion;

    private String folioTicketSobrante;
    private String idRevisorFinal;
    private String motivoRechazo;

    public Viaje() {}

    public Viaje(int idSolicitud, String idUsuario, String folioViaje,
                 double montoHotelAutorizado, double montoComidaAutorizado,
                 double montoTransporteAutorizado, double montoGasolinaAutorizado) {
        this.idSolicitud = idSolicitud;
        this.idUsuario = idUsuario;
        this.folioViaje = folioViaje;
        this.montoHotelAutorizado = montoHotelAutorizado;
        this.montoComidaAutorizado = montoComidaAutorizado;
        this.montoTransporteAutorizado = montoTransporteAutorizado;
        this.montoGasolinaAutorizado = montoGasolinaAutorizado;
        this.estado = "En_Curso";
    }

    public int getIdViaje() { return idViaje; }
    public void setIdViaje(int idViaje) { this.idViaje = idViaje; }

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getFolioViaje() { return folioViaje; }
    public void setFolioViaje(String folioViaje) { this.folioViaje = folioViaje; }

    public double getMontoHotelAutorizado() { return montoHotelAutorizado; }
    public void setMontoHotelAutorizado(double montoHotelAutorizado) { this.montoHotelAutorizado = montoHotelAutorizado; }

    public double getMontoComidaAutorizado() { return montoComidaAutorizado; }
    public void setMontoComidaAutorizado(double montoComidaAutorizado) { this.montoComidaAutorizado = montoComidaAutorizado; }

    public double getMontoTransporteAutorizado() { return montoTransporteAutorizado; }
    public void setMontoTransporteAutorizado(double montoTransporteAutorizado) { this.montoTransporteAutorizado = montoTransporteAutorizado; }

    public double getMontoGasolinaAutorizado() { return montoGasolinaAutorizado; }
    public void setMontoGasolinaAutorizado(double montoGasolinaAutorizado) { this.montoGasolinaAutorizado = montoGasolinaAutorizado; }

    public double getMontoHotelGastado() { return montoHotelGastado; }
    public void setMontoHotelGastado(double montoHotelGastado) { this.montoHotelGastado = montoHotelGastado; }

    public double getMontoComidaGastado() { return montoComidaGastado; }
    public void setMontoComidaGastado(double montoComidaGastado) { this.montoComidaGastado = montoComidaGastado; }

    public double getMontoTransporteGastado() { return montoTransporteGastado; }
    public void setMontoTransporteGastado(double montoTransporteGastado) { this.montoTransporteGastado = montoTransporteGastado; }

    public double getMontoGasolinaGastado() { return montoGasolinaGastado; }
    public void setMontoGasolinaGastado(double montoGasolinaGastado) { this.montoGasolinaGastado = montoGasolinaGastado; }

    public double getMontoOtrosGastado() { return montoOtrosGastado; }
    public void setMontoOtrosGastado(double montoOtrosGastado) { this.montoOtrosGastado = montoOtrosGastado; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getFechaEnvioRevision() { return fechaEnvioRevision; }
    public void setFechaEnvioRevision(String fechaEnvioRevision) { this.fechaEnvioRevision = fechaEnvioRevision; }

    public String getFechaFinalizacion() { return fechaFinalizacion; }
    public void setFechaFinalizacion(String fechaFinalizacion) { this.fechaFinalizacion = fechaFinalizacion; }

    public String getFolioTicketSobrante() { return folioTicketSobrante; }
    public void setFolioTicketSobrante(String folioTicketSobrante) { this.folioTicketSobrante = folioTicketSobrante; }

    public String getIdRevisorFinal() { return idRevisorFinal; }
    public void setIdRevisorFinal(String idRevisorFinal) { this.idRevisorFinal = idRevisorFinal; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public double getMontoTotalAutorizado() {
        return montoHotelAutorizado + montoComidaAutorizado + montoTransporteAutorizado + montoGasolinaAutorizado;
    }

    public double getMontoTotalGastado() {
        return montoHotelGastado + montoComidaGastado + montoTransporteGastado + montoGasolinaGastado + montoOtrosGastado;
    }

    public double getSaldoRestante() {
        return getMontoTotalAutorizado() - getMontoTotalGastado();
    }
}