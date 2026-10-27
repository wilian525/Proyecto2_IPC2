package com.proyecto2.app.backend.Backend.Model;

// Préstamo con el título del libro para las consultas del estudiante.
public class PrestamoConsulta extends Prestamo {
    private String tituloLibro;

    public PrestamoConsulta() { }

    public PrestamoConsulta(Prestamo prestamo, String tituloLibro) {
        setIdPrestamo(prestamo.getIdPrestamo());
        setIdLibro(prestamo.getIdLibro());
        setIdUsuario(prestamo.getIdUsuario());
        setFechaPrestamo(prestamo.getFechaPrestamo());
        setFechaDevolucionEsperada(prestamo.getFechaDevolucionEsperada());
        setFechaDevolucion(prestamo.getFechaDevolucion());
        setDiasAtraso(prestamo.getDiasAtraso());
        setMontoMulta(prestamo.getMontoMulta());
        setFechaAviso(prestamo.getFechaAviso());
        this.tituloLibro = tituloLibro;
    }

    public String getTituloLibro() { return tituloLibro; }
    public void setTituloLibro(String tituloLibro) { this.tituloLibro = tituloLibro; }
}
