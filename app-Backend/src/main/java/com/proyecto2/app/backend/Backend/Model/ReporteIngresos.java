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
public class ReporteIngresos {
    
     private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Collection<ReporteIngresoConcepto> detalle;
    private double totalIngresos;

    public ReporteIngresos(LocalDate fechaInicio, LocalDate fechaFin, Collection<ReporteIngresoConcepto> detalle, double totalIngresos) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.detalle = detalle;
        this.totalIngresos = totalIngresos;
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

    public Collection<ReporteIngresoConcepto> getDetalle() {
        return detalle;
    }

    public void setDetalle(Collection<ReporteIngresoConcepto> detalle) {
        this.detalle = detalle;
    }

    public double getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(double totalIngresos) {
        this.totalIngresos = totalIngresos;
    }
}
