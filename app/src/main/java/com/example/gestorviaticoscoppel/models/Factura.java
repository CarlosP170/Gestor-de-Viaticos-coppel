package com.example.gestorviaticoscoppel.models;

public class Factura {
    private int idFactura;
    private int idViaje;
    private String categoria;
    private double monto;
    private String archivoNombre;
    private String archivoRuta;
    private String archivoTipo;
    private String archivoContenidoBase64;
    private String fechaSubida;

    public Factura() {}

    public Factura(int idViaje, String categoria, double monto, String archivoNombre,
                   String archivoTipo, String archivoContenidoBase64) {
        this.idViaje = idViaje;
        this.categoria = categoria;
        this.monto = monto;
        this.archivoNombre = archivoNombre;
        this.archivoTipo = archivoTipo;
        this.archivoContenidoBase64 = archivoContenidoBase64;
        this.archivoRuta = "";
    }

    public int getIdFactura() { return idFactura; }
    public void setIdFactura(int idFactura) { this.idFactura = idFactura; }

    public int getIdViaje() { return idViaje; }
    public void setIdViaje(int idViaje) { this.idViaje = idViaje; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getArchivoNombre() { return archivoNombre; }
    public void setArchivoNombre(String archivoNombre) { this.archivoNombre = archivoNombre; }

    public String getArchivoRuta() { return archivoRuta; }
    public void setArchivoRuta(String archivoRuta) { this.archivoRuta = archivoRuta; }

    public String getArchivoTipo() { return archivoTipo; }
    public void setArchivoTipo(String archivoTipo) { this.archivoTipo = archivoTipo; }

    public String getArchivoContenidoBase64() { return archivoContenidoBase64; }
    public void setArchivoContenidoBase64(String archivoContenidoBase64) { this.archivoContenidoBase64 = archivoContenidoBase64; }

    public String getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(String fechaSubida) { this.fechaSubida = fechaSubida; }
}