package com.proyecto2.app.backend.Backend.Model;

public class AsignacionDetalle {
    private int idAsignacion;
    private int idCurso;
    private int idSeccion;
    private int idCiclo;
    private String curso;
    private String grado;
    private String seccion;
    private String ciclo;
    private String estadoCiclo;

    public AsignacionDetalle(int idAsignacion, int idCurso, int idSeccion, int idCiclo,
            String curso, String grado, String seccion, String ciclo, String estadoCiclo) {
        this.idAsignacion = idAsignacion;
        this.idCurso = idCurso;
        this.idSeccion = idSeccion;
        this.idCiclo = idCiclo;
        this.curso = curso;
        this.grado = grado;
        this.seccion = seccion;
        this.ciclo = ciclo;
        this.estadoCiclo = estadoCiclo;
    }

    public int getIdAsignacion() { return idAsignacion; }
    public int getIdCurso() { return idCurso; }
    public int getIdSeccion() { return idSeccion; }
    public int getIdCiclo() { return idCiclo; }
    public String getCurso() { return curso; }
    public String getGrado() { return grado; }
    public String getSeccion() { return seccion; }
    public String getCiclo() { return ciclo; }
    public String getEstadoCiclo() { return estadoCiclo; }
}
