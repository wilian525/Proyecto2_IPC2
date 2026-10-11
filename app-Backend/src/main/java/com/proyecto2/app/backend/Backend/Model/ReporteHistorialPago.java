/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Model;

import java.time.LocalDate;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class ReporteHistorialPago {

    private int idEstudiante;
    private String estudiante;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Collection<Pago> pagos;
    private double totalPagado;
    private double saldoPendiente;
    private String estadoCuenta;

    public ReporteHistorialPago(int idEstudiante, String estudiante, LocalDate fechaInicio, LocalDate fechaFin, Collection<Pago> pagos, double totalPagado, double saldoPendiente, String estadoCuenta) {
        this.idEstudiante = idEstudiante;
        this.estudiante = estudiante;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.pagos = pagos;
        this.totalPagado = totalPagado;
        this.saldoPendiente = saldoPendiente;
        this.estadoCuenta = estadoCuenta;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public String getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(String estudiante) {
        this.estudiante = estudiante;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Collection<Pago> getPagos() {
        return pagos;
    }

    public void setPagos(Collection<Pago> pagos) {
        this.pagos = pagos;
    }

    public double getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(double totalPagado) {
        this.totalPagado = totalPagado;
    }

    public double getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(double saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    public String getEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(String estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }
}
