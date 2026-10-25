package com.proyecto2.app.backend.Backend.Model;

public class LectorBiblioteca {
    private int idUsuario;
    private String nombreUsuario;
    private String rol;

    public LectorBiblioteca(int idUsuario, String nombreUsuario, String rol) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getRol() { return rol; }
}
