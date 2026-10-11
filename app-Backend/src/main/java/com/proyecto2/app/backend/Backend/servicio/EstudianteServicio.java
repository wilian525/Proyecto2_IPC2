/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.servicio;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Dao.EstudianteDao;
import com.proyecto2.app.backend.Backend.Dao.UsuarioDao;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import com.proyecto2.app.backend.Backend.seguridad.SeguridadContrasena;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class EstudianteServicio {

    private EstudianteDao estudianteDAO;
    private UsuarioDao usuarioDAO;
    private SeguridadContrasena seguridadContrasena;

    public EstudianteServicio() {
        estudianteDAO = new EstudianteDao();
        usuarioDAO = new UsuarioDao();
        seguridadContrasena = new SeguridadContrasena();
    }

    public boolean crearEstudiante(Estudiante estudiante, Usuario usuario) {

        if (estudiante == null || estudiante.getNombre() == null || estudiante.getNombre().trim().isEmpty() || estudiante.getApellido() == null || estudiante.getApellido().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del estudiante no son válidos");
        }

        if (estudiante.getFechaNacimiento() == null) {
            throw new ServicioException("DATOS_INVALIDOS", "La fecha de nacimiento no puede estar vacía");
        }

        if (usuario == null || usuario.getNombreUsuario() == null || usuario.getNombreUsuario().trim().isEmpty() || usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del usuario del estudiante no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            for (Usuario actual : usuarioDAO.consultar(conexion)) {
                if (actual.getNombreUsuario().equalsIgnoreCase(usuario.getNombreUsuario().trim())) {
                    throw new ServicioException("CONFLICTO", "El nombre de usuario ya está registrado");
                }
            }

            usuario.setRol("ESTUDIANTE");
            usuario.setEstado("ACTIVO");
            usuario.setIdEmpleado(0);
            usuario.setContrasena(seguridadContrasena.hashear(usuario.getContrasena()));

            boolean usuarioCreado = usuarioDAO.insertar(usuario, conexion);

            if (!usuarioCreado || usuario.getIdUsuario() <= 0) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible crear el usuario del estudiante");
            }

            estudiante.setIdUsuario(usuario.getIdUsuario());
            estudiante.setEstado("ACTIVO");

            boolean estudianteCreado = estudianteDAO.insertar(estudiante, conexion);

            if (!estudianteCreado) {
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
