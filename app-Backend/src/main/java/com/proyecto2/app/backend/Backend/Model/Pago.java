/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Model;

import java.time.LocalDate;

/**
 *
 * @author wilian
 */
public class Pago {

    private int idPago;
    private int idEstudiante;
    private int idEmpleado;
    private String concepto;
    private double monto;
    private LocalDate fecha;

    public Pago(int idPago, int idEstudiante, int idEmpleado,
            String concepto, double monto, LocalDate fecha) {

        this.idPago = idPago;
        this.idEstudiante = idEstudiante;
        this.idEmpleado = idEmpleado;
        this.concepto = concepto;
        this.monto = monto;
        this.fecha = fecha;
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

}
