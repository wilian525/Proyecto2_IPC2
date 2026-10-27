package com.proyecto2.app.backend.Backend.Model;

// Nota con nombres legibles para el historial del estudiante.
public class NotaHistorial extends Nota {
    private String curso;
    private String actividad;
    private String grado;
    private String seccion;
    private int anioInicio;
    private int anioFin;
    private double porcentaje;

    public NotaHistorial() { }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
    public String getActividad() { return actividad; }
    public void setActividad(String actividad) { this.actividad = actividad; }
    public String getGrado() { return grado; }
    public void setGrado(String grado) { this.grado = grado; }
    public String getSeccion() { return seccion; }
    public void setSeccion(String seccion) { this.seccion = seccion; }
    public int getAnioInicio() { return anioInicio; }
    public void setAnioInicio(int anioInicio) { this.anioInicio = anioInicio; }
    public int getAnioFin() { return anioFin; }
    public void setAnioFin(int anioFin) { this.anioFin = anioFin; }
    public double getPorcentaje() { return porcentaje; }
    public void setPorcentaje(double porcentaje) { this.porcentaje = porcentaje; }
}
