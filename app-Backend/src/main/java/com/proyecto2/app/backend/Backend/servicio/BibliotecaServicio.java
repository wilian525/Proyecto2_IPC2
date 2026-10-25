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
import java.util.logging.Level;
import java.util.logging.Logger;

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

            Libro libroEncontrado = libroDAO.consultarPorId(prestamo.getIdLibro(), conexion);

            if (libroEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El libro no existe");
            }

            if (!"ACTIVO".equalsIgnoreCase(libroEncontrado.getEstado()) || libroEncontrado.getCantidadDisponible() <= 0) {
                throw new ServicioException("CONFLICTO", "El libro no se encuentra disponible");
            }

            Usuario usuarioEncontrado = usuarioDAO.consultarPorId(prestamo.getIdUsuario(), conexion);

            if (usuarioEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El usuario no existe");
            }

            if (!"ACTIVO".equalsIgnoreCase(usuarioEncontrado.getEstado())) {
                throw new ServicioException("CONFLICTO", "El usuario no se encuentra activo");
            }

            if (!"ESTUDIANTE".equalsIgnoreCase(usuarioEncontrado.getRol()) && !"MAESTRO".equalsIgnoreCase(usuarioEncontrado.getRol())) {
                throw new ServicioException("NO_AUTORIZADO", "Los préstamos únicamente pueden realizarse a estudiantes o maestros");
            }

            prestamo.setFechaPrestamo(LocalDate.now());
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

            Prestamo prestamoEncontrado = prestamoDAO.consultarPorId(idPrestamo, conexion);

            if (prestamoEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El préstamo no existe");
            }

            if (prestamoEncontrado.getFechaDevolucion() != null) {
                throw new ServicioException("CONFLICTO", "El préstamo ya fue devuelto");
            }

            if (fechaDevolucion.isBefore(prestamoEncontrado.getFechaPrestamo())) {
                throw new ServicioException("DATOS_INVALIDOS", "La fecha de devolución no puede ser anterior a la fecha del préstamo");
            }

            fechaDevolucion = LocalDate.now();
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

            Libro libroEncontrado = libroDAO.consultarPorId(prestamoEncontrado.getIdLibro(), conexion);

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

    public boolean crearLibro(Libro libro) {
        if (libro == null || libro.getTitulo() == null || libro.getTitulo().trim().isEmpty() || libro.getAutor() == null || libro.getAutor().trim().isEmpty() || libro.getCantidadDisponible() < 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del libro no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            libro.setEstado("ACTIVO");

            if (!libroDAO.insertar(libro, conexion)) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible crear el libro");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;
        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al crear el libro", e);
        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al crear el libro", e);
        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean modificarLibro(Libro libro) {

        if (libro == null || libro.getIdLibro() <= 0 || libro.getTitulo() == null || libro.getTitulo().trim().isEmpty()
                || libro.getAutor() == null || libro.getAutor().trim().isEmpty() || libro.getCantidadDisponible() < 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del libro no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Libro encontrado = null;

            for (Libro actual : libroDAO.consultar(conexion)) {
                if (actual.getIdLibro() == libro.getIdLibro()) {
                    encontrado = actual;
                    break;
                }
            }

            if (encontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El libro no existe");
            }

            libro.setEstado(encontrado.getEstado());

            if (!libroDAO.actualizar(libro, conexion)) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible modificar el libro");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al modificar el libro", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al modificar el libro", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean eliminarLibro(int idLibro) {

        if (idLibro <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "El id del libro no es válido");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            boolean encontrado = false;

            for (Libro libro : libroDAO.consultar(conexion)) {
                if (libro.getIdLibro() == idLibro) {
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                throw new ServicioException("NO_ENCONTRADO", "El libro no existe");
            }

            for (Prestamo prestamo : prestamoDAO.consultar(conexion)) {
                if (prestamo.getIdLibro() == idLibro) {
                    throw new ServicioException("CONFLICTO", "El libro tiene préstamos registrados y no puede eliminarse");
                }
            }

            if (!libroDAO.eliminar(idLibro, conexion)) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible eliminar el libro");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al eliminar el libro", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al eliminar el libro", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean cambiarEstadoLibro(int idLibro, boolean activar) {
        if (idLibro <= 0) throw new ServicioException("DATOS_INVALIDOS", "Selecciona un libro válido");
        try (Connection conexion = ConexionDB.getInstance().getConnection()) {
            Libro libro = libroDAO.consultarPorId(idLibro, conexion);
            if (libro == null) throw new ServicioException("NO_ENCONTRADO", "El libro no existe");
            if (activar ? "ACTIVO".equals(libro.getEstado()) : "INACTIVO".equals(libro.getEstado()))
                throw new ServicioException("CONFLICTO", "El libro ya tiene ese estado");
            return activar ? libroDAO.activar(idLibro, conexion) : libroDAO.desactivar(idLibro, conexion);
        } catch (SQLException e) {
            throw new ServicioException("BD_ERROR", "No se pudo cambiar el estado del libro", e);
        }
    }

    public boolean registrarAviso(int idPrestamo) {
        if (idPrestamo <= 0) throw new ServicioException("DATOS_INVALIDOS", "Selecciona un préstamo válido");
        if (!prestamoDAO.registrarAviso(idPrestamo))
            throw new ServicioException("CONFLICTO", "Solo puedes avisar sobre préstamos atrasados y no devueltos");
        return true;
    }

    private void restaurarAutoCommit(Connection conexion, boolean autoCommitAnterior) {
    if (conexion != null) {
        try {
            conexion.setAutoCommit(autoCommitAnterior);
        } catch (SQLException e) {
            Logger.getLogger(BibliotecaServicio.class.getName()).log(Level.SEVERE, "No fue posible restaurar el autoCommit", e);
        }
    }
}

private void cerrarConexion(Connection conexion) {
    if (conexion != null) {
        try {
            conexion.close();
        } catch (SQLException e) {
            Logger.getLogger(BibliotecaServicio.class.getName()).log(Level.SEVERE, "No fue posible cerrar la conexión", e);
        }
    }
}
}
