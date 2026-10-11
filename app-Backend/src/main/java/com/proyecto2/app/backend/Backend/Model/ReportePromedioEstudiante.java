/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Model;

/**
 *
 * @author wilian
 */
public class ReportePromedioEstudiante {
    
     private int idEstudiante;
    private String estudiante;
    private double promedioGeneral;

    public ReportePromedioEstudiante(int idEstudiante, String estudiante, double promedioGeneral) {
        this.idEstudiante = idEstudiante;
        this.estudiante = estudiante;
        this.promedioGeneral = promedioGeneral;
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

    public double getPromedioGeneral() {
        return promedioGeneral;
    }

    public void setPromedioGeneral(double promedioGeneral) {
        this.promedioGeneral = promedioGeneral;
    }
}
