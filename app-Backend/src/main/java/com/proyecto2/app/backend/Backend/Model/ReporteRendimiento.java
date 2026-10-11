/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Model;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author wilian
 */
public class ReporteRendimiento {

    private int idGrado;
    private String grado;
    private int idSeccion;
    private String seccion;
    private int idCiclo;
    private Collection<ReportePromedioCurso> promediosCursos;
    private ReportePromedioCurso cursoMayorDificultad;
    private List<ReportePromedioEstudiante> mejoresPromedios;

    public ReporteRendimiento(int idGrado, String grado, int idSeccion, String seccion, int idCiclo, Collection<ReportePromedioCurso> promediosCursos, ReportePromedioCurso cursoMayorDificultad, List<ReportePromedioEstudiante> mejoresPromedios) {
        this.idGrado = idGrado;
        this.grado = grado;
        this.idSeccion = idSeccion;
        this.seccion = seccion;
        this.idCiclo = idCiclo;
        this.promediosCursos = promediosCursos;
        this.cursoMayorDificultad = cursoMayorDificultad;
        this.mejoresPromedios = mejoresPromedios;
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

    public Collection<ReportePromedioCurso> getPromediosCursos() {
        return promediosCursos;
    }

    public void setPromediosCursos(Collection<ReportePromedioCurso> promediosCursos) {
        this.promediosCursos = promediosCursos;
    }

    public ReportePromedioCurso getCursoMayorDificultad() {
        return cursoMayorDificultad;
    }

    public void setCursoMayorDificultad(ReportePromedioCurso cursoMayorDificultad) {
        this.cursoMayorDificultad = cursoMayorDificultad;
    }

    public List<ReportePromedioEstudiante> getMejoresPromedios() {
        return mejoresPromedios;
    }

    public void setMejoresPromedios(List<ReportePromedioEstudiante> mejoresPromedios) {
        this.mejoresPromedios = mejoresPromedios;
    }
}
