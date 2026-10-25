package com.proyecto2.app.backend.Backend.Model;

public class CuotaMensual {
    private int idCuota;
    private int idInscripcion;
    private int anio;
    private int mes;
    private double monto;
    private Integer idPago;

    public CuotaMensual() { }
    public CuotaMensual(int idCuota, int idInscripcion, int anio, int mes, double monto, Integer idPago) {
        this.idCuota = idCuota;
        this.idInscripcion = idInscripcion;
        this.anio = anio;
        this.mes = mes;
        this.monto = monto;
        this.idPago = idPago;
    }
    public int getIdCuota() { return idCuota; }
    public void setIdCuota(int valor) { idCuota = valor; }
    public int getIdInscripcion() { return idInscripcion; }
    public void setIdInscripcion(int valor) { idInscripcion = valor; }
    public int getAnio() { return anio; }
    public void setAnio(int valor) { anio = valor; }
    public int getMes() { return mes; }
    public void setMes(int valor) { mes = valor; }
    public double getMonto() { return monto; }
    public void setMonto(double valor) { monto = valor; }
    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer valor) { idPago = valor; }
}
