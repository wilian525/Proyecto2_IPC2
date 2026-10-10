/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.servicio;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Dao.EstudianteDao;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class EstudianteServicio {

    private final EstudianteDao estudianteDAO;

    public EstudianteServicio() {
        estudianteDAO = new EstudianteDao();
    }

    public boolean crearEstudiante(Estudiante estudiante) {

        if (estudiante == null || estudiante.getNombre() == null || estudiante.getNombre().trim().isEmpty() || estudiante.getApellido() == null || estudiante.getApellido().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del estudiante no son válidos");
        }

        if (estudiante.getFechaNacimiento() == null || estudiante.getIdUsuario() < 0) {
            throw new ServicioException("DATOS_INVALIDOS", "La fecha de nacimiento o el usuario relacionado no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            estudiante.setEstado("ACTIVO");

            boolean creado = estudianteDAO.insertar(estudiante, conexion);

            if (!creado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible crear el estudiante");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al crear el estudiante", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al crear el estudiante", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean modificarEstudiante(Estudiante estudiante) {

        if (estudiante == null || estudiante.getIdEstudiante() <= 0 || estudiante.getNombre() == null || estudiante.getNombre().trim().isEmpty() || estudiante.getApellido() == null || estudiante.getApellido().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del estudiante no son válidos");
        }

        if (estudiante.getFechaNacimiento() == null) {
            throw new ServicioException("DATOS_INVALIDOS", "La fecha de nacimiento no puede estar vacía");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Collection<Estudiante> estudiantes = estudianteDAO.consultar(conexion);
            Estudiante estudianteActual = null;

            for (Estudiante actual : estudiantes) {
                if (actual.getIdEstudiante() == estudiante.getIdEstudiante()) {
                    estudianteActual = actual;
                    break;
                }
            }

            if (estudianteActual == null) {
                throw new ServicioException("NO_ENCONTRADO", "El estudiante no existe");
            }

            estudiante.setEstado(estudianteActual.getEstado());
            estudiante.setIdUsuario(estudianteActual.getIdUsuario());

            boolean actualizado = estudianteDAO.actualizar(estudiante, conexion);

            if (!actualizado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible modificar el estudiante");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al modificar el estudiante", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al modificar el estudiante", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean cambiarEstadoEstudiante(int idEstudiante, String estado) {

        if (idEstudiante <= 0 || estado == null || estado.trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "El id o el estado del estudiante no son válidos");
        }

        if (!estado.equalsIgnoreCase("ACTIVO") && !estado.equalsIgnoreCase("INACTIVO") && !estado.equalsIgnoreCase("GRADUADO")) {
            throw new ServicioException("DATOS_INVALIDOS", "El estado debe ser ACTIVO, INACTIVO o GRADUADO");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Collection<Estudiante> estudiantes = estudianteDAO.consultar(conexion);
            boolean encontrado = false;

            for (Estudiante estudiante : estudiantes) {
                if (estudiante.getIdEstudiante() == idEstudiante) {
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                throw new ServicioException("NO_ENCONTRADO", "El estudiante no existe");
            }

            boolean actualizado = estudianteDAO.cambiarEstado(idEstudiante, estado.toUpperCase(), conexion);

            if (!actualizado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible cambiar el estado del estudiante");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al cambiar el estado del estudiante", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al cambiar el estado del estudiante", e);

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
