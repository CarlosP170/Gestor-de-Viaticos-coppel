package com.example.gestorviaticoscoppel.models;

public class Solicitud {
    private int idSolicitud;
    private String folioViaje;
    private String idSolicitante;
    private String idBeneficiario;
    private double montoHotel;
    private double montoComida;
    private double montoTransporte;
    private double montoGasolina;
    private String estado;
    private String fechaSolicitud;
    private String fechaRevision;
    private String idRevisor;

    public Solicitud() {}

    public Solicitud(String folioViaje, String idSolicitante, String idBeneficiario,
                     double montoHotel, double montoComida, double montoTransporte, double montoGasolina) {
        this.folioViaje = folioViaje;
        this.idSolicitante = idSolicitante;
        this.idBeneficiario = idBeneficiario;
        this.montoHotel = montoHotel;
        this.montoComida = montoComida;
        this.montoTransporte = montoTransporte;
        this.montoGasolina = montoGasolina;
        this.estado = "Pendiente";
    }

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getFolioViaje() { return folioViaje; }
    public void setFolioViaje(String folioViaje) { this.folioViaje = folioViaje; }

    public String getIdSolicitante() { return idSolicitante; }
    public void setIdSolicitante(String idSolicitante) { this.idSolicitante = idSolicitante; }

    public String getIdBeneficiario() { return idBeneficiario; }
    public void setIdBeneficiario(String idBeneficiario) { this.idBeneficiario = idBeneficiario; }

    public double getMontoHotel() { return montoHotel; }
    public void setMontoHotel(double montoHotel) { this.montoHotel = montoHotel; }

    public double getMontoComida() { return montoComida; }
    public void setMontoComida(double montoComida) { this.montoComida = montoComida; }

    public double getMontoTransporte() { return montoTransporte; }
    public void setMontoTransporte(double montoTransporte) { this.montoTransporte = montoTransporte; }

    public double getMontoGasolina() { return montoGasolina; }
    public void setMontoGasolina(double montoGasolina) { this.montoGasolina = montoGasolina; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(String fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(String fechaRevision) { this.fechaRevision = fechaRevision; }

    public String getIdRevisor() { return idRevisor; }
    public void setIdRevisor(String idRevisor) { this.idRevisor = idRevisor; }

    public double getMontoTotal() {
        return montoHotel + montoComida + montoTransporte + montoGasolina;
    }
}