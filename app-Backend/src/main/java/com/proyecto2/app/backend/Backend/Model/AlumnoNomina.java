package com.proyecto2.app.backend.Backend.Model;

public class AlumnoNomina {
    private int idInscripcion;
    private int idEstudiante;
    private String nombre;
    private String apellido;

    public AlumnoNomina(int idInscripcion, int idEstudiante, String nombre, String apellido) {
        this.idInscripcion = idInscripcion;
        this.idEstudiante = idEstudiante;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public int getIdInscripcion() { return idInscripcion; }
    public int getIdEstudiante() { return idEstudiante; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
}
