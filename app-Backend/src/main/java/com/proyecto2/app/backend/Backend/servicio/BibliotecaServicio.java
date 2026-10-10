/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.servicio;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Dao.LibroDao;
import com.proyecto2.app.backend.Backend.Dao.PrestamoDao;
import com.proyecto2.app.backend.Backend.Dao.UsuarioDao;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Libro;
import com.proyecto2.app.backend.Backend.Model.Prestamo;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class BibliotecaServicio {

    public static final int PRECIO_MULTA = 20;

    private LibroDao libroDAO;
    private PrestamoDao prestamoDAO;
    private UsuarioDao usuarioDAO;

    public BibliotecaServicio() {
        libroDAO = new LibroDao();
        prestamoDAO = new PrestamoDao();
        usuarioDAO = new UsuarioDao();
    }

    public boolean registrarPrestamo(Prestamo prestamo) {

        if (prestamo == null || prestamo.getIdLibro() <= 0 || prestamo.getIdUsuario() <= 0 || prestamo.getFechaPrestamo() == null || prestamo.getFechaDevolucionEsperada() == null) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del préstamo no son válidos");
        }

        if (prestamo.getFechaDevolucionEsperada().isBefore(prestamo.getFechaPrestamo())) {
            throw new ServicioException("DATOS_INVALIDOS", "La fecha esperada de devolución no puede ser anterior a la fecha del préstamo");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Libro libroEncontrado = null;
            Collection<Libro> libros = libroDAO.consultar(conexion);

            for (Libro libro : libros) {
                if (libro.getIdLibro() == prestamo.getIdLibro()) {
                    libroEncontrado = libro;
                    break;
                }
            }

            if (libroEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El libro no existe");
            }

            if (!"ACTIVO".equalsIgnoreCase(libroEncontrado.getEstado()) || libroEncontrado.getCantidadDisponible() <= 0) {
                throw new ServicioException("CONFLICTO", "El libro no se encuentra disponible");
            }

            Usuario usuarioEncontrado = null;
            Collection<Usuario> usuarios = usuarioDAO.consultar(conexion);

            for (Usuario usuario : usuarios) {
                if (usuario.getIdUsuario() == prestamo.getIdUsuario()) {
                    usuarioEncontrado = usuario;
                    break;
                }
            }

            if (usuarioEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El usuario no existe");
            }

            if (!"ACTIVO".equalsIgnoreCase(usuarioEncontrado.getEstado())) {
                throw new ServicioException("CONFLICTO", "El usuario no se encuentra activo");
            }

            if (!"ESTUDIANTE".equalsIgnoreCase(usuarioEncontrado.getRol()) && !"MAESTRO".equalsIgnoreCase(usuarioEncontrado.getRol())) {
                throw new ServicioException("NO_AUTORIZADO", "Los préstamos únicamente pueden realizarse a estudiantes o maestros");
            }

            prestamo.setFechaDevolucion(null);
            prestamo.setDiasAtraso(0);
            prestamo.setMontoMulta(0);

            boolean registrado = prestamoDAO.insertar(prestamo, conexion);

            if (!registrado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible registrar el préstamo");
            }

            libroEncontrado.setCantidadDisponible(libroEncontrado.getCantidadDisponible() - 1);

            boolean libroActualizado = libroDAO.actualizar(libroEncontrado, conexion);

            if (!libroActualizado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible actualizar la disponibilidad del libro");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al registrar el préstamo", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al registrar el préstamo", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean registrarDevolucion(int idPrestamo, LocalDate fechaDevolucion) {

        if (idPrestamo <= 0 || fechaDevolucion == null) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos de la devolución no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Prestamo prestamoEncontrado = null;
            Collection<Prestamo> prestamos = prestamoDAO.consultar(conexion);

            for (Prestamo prestamo : prestamos) {
                if (prestamo.getIdPrestamo() == idPrestamo) {
                    prestamoEncontrado = prestamo;
                    break;
                }
            }

            if (prestamoEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El préstamo no existe");
            }

            if (prestamoEncontrado.getFechaDevolucion() != null) {
                throw new ServicioException("CONFLICTO", "El préstamo ya fue devuelto");
            }

            if (fechaDevolucion.isBefore(prestamoEncontrado.getFechaPrestamo())) {
                throw new ServicioException("DATOS_INVALIDOS", "La fecha de devolución no puede ser anterior a la fecha del préstamo");
            }

            int diasAtraso = 0;

            if (fechaDevolucion.isAfter(prestamoEncontrado.getFechaDevolucionEsperada())) {
                diasAtraso = (int) ChronoUnit.DAYS.between(prestamoEncontrado.getFechaDevolucionEsperada(), fechaDevolucion);
            }

            double montoMulta = diasAtraso * PRECIO_MULTA;

            prestamoEncontrado.setFechaDevolucion(fechaDevolucion);
            prestamoEncontrado.setDiasAtraso(diasAtraso);
            prestamoEncontrado.setMontoMulta(montoMulta);

            boolean actualizado = prestamoDAO.actualizar(prestamoEncontrado, conexion);

            if (!actualizado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible registrar la devolución");
            }

            Libro libroEncontrado = null;
            Collection<Libro> libros = libroDAO.consultar(conexion);

            for (Libro libro : libros) {
                if (libro.getIdLibro() == prestamoEncontrado.getIdLibro()) {
                    libroEncontrado = libro;
                    break;
                }
            }

            if (libroEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El libro relacionado con el préstamo no existe");
            }

            libroEncontrado.setCantidadDisponible(libroEncontrado.getCantidadDisponible() + 1);

            boolean libroActualizado = libroDAO.actualizar(libroEncontrado, conexion);

            if (!libroActualizado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible actualizar la disponibilidad del libro");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al registrar la devolución", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al registrar la devolución", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    private void rollback(Connection conexion, Exception causa) {
        if (conexion != null) {
            try {
                conexion.rollback();
            } catch (SQLException e) {
                causa.addSuppressed(e);
            }
        }
    }

    private void restaurarAutoCommit(Connection conexion, boolean autoCommitAnterior) {
        if (conexion != null) {
            try {
                conexion.setAutoCommit(autoCommitAnterior);
            } catch (SQLException e) {
                System.err.println("No fue posible restaurar el autoCommit: " + e.getMessage());
            }
        }
    }

    private void cerrarConexion(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                System.err.println("No fue posible cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
