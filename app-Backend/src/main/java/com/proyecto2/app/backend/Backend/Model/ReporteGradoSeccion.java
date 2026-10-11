/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Model;

/**
 *
 * @author wilian
 */
public class ReporteGradoSeccion {

    private int idEstudiante;
    private String estudiante;
    private int idGrado;
    private String grado;
    private int idSeccion;
    private String seccion;
    private int idCiclo;

    public ReporteGradoSeccion(int idEstudiante, String estudiante, int idGrado, String grado, int idSeccion, String seccion, int idCiclo) {
        this.idEstudiante = idEstudiante;
        this.estudiante = estudiante;
        this.idGrado = idGrado;
        this.grado = grado;
        this.idSeccion = idSeccion;
        this.seccion = seccion;
        this.idCiclo = idCiclo;
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

    public int getIdGrado() {
        return idGrado;
    }

    public void setIdGrado(int idGrado) {
        this.idGrado = idGrado;
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }

    public int getIdSeccion() {
        return idSeccion;
    }

    public void setIdSeccion(int idSeccion) {
        this.idSeccion = idSeccion;
    }

    public String getSeccion() {
        return seccion;
    }

    public void setSeccion(String seccion) {
        this.seccion = seccion;
    }

    public int getIdCiclo() {
        return idCiclo;
    }

    public void setIdCiclo(int idCiclo) {
        this.idCiclo = idCiclo;
    }
}
