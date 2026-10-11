/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Model;

/**
 *
 * @author wilian
 */
public class ReportePromedioCurso {

    private int idCurso;
    private String curso;
    private int idSeccion;
    private int idCiclo;
    private double promedio;
    private int estudiantesEvaluados;

    public ReportePromedioCurso(int idCurso, String curso, int idSeccion, int idCiclo, double promedio, int estudiantesEvaluados) {
        this.idCurso = idCurso;
        this.curso = curso;
        this.idSeccion = idSeccion;
        this.idCiclo = idCiclo;
        this.promedio = promedio;
        this.estudiantesEvaluados = estudiantesEvaluados;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public int getIdSeccion() {
        return idSeccion;
    }

    public void setIdSeccion(int idSeccion) {
        this.idSeccion = idSeccion;
    }

    public int getIdCiclo() {
        return idCiclo;
    }

    public void setIdCiclo(int idCiclo) {
        this.idCiclo = idCiclo;
    }

    public double getPromedio() {
        return promedio;
    }

    public void setPromedio(double promedio) {
        this.promedio = promedio;
    }

    public int getEstudiantesEvaluados() {
        return estudiantesEvaluados;
    }

    public void setEstudiantesEvaluados(int estudiantesEvaluados) {
        this.estudiantesEvaluados = estudiantesEvaluados;
    }
}
