/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.servicio;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Dao.EmpleadoDao;
import com.proyecto2.app.backend.Backend.Dao.EstudianteDao;
import com.proyecto2.app.backend.Backend.Dao.UsuarioDao;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Empleado;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import com.proyecto2.app.backend.Backend.seguridad.JwtUtil;
import com.proyecto2.app.backend.Backend.seguridad.SeguridadContrasena;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wilian
 */
public class AutenticacionServicio {

     private UsuarioDao usuarioDAO;
    private EmpleadoDao empleadoDAO;
    private EstudianteDao estudianteDAO;
    private SeguridadContrasena seguridadContrasena;
    private JwtUtil jwtUtil;

    public AutenticacionServicio() {
        usuarioDAO = new UsuarioDao();
        empleadoDAO = new EmpleadoDao();
        estudianteDAO = new EstudianteDao();
        seguridadContrasena = new SeguridadContrasena();
        jwtUtil = new JwtUtil();
    }

    public boolean cerrarSesion(String token) {
        if (token == null || token.trim().isEmpty()) throw new ServicioException("NO_AUTORIZADO", "El token de sesión no puede estar vacío");
        if (!jwtUtil.validarToken(token)) throw new ServicioException("NO_AUTORIZADO", "La sesión no es válida o ya expiró");
        return true;
    }

    public boolean recuperarContraseña(String nombreUsuario, String nombre, String apellido, LocalDate fechaVerificacion, String nuevaContrasena) {
        validarDatos(nombreUsuario, nombre, apellido, fechaVerificacion, nuevaContrasena);

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Usuario usuario = usuarioDAO.consultarPorNombreUsuario(nombreUsuario.trim(), conexion);

            if (usuario == null || usuario.getEstado() == null || !"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
                throw new ServicioException("NO_AUTORIZADO", "No fue posible validar la identidad del usuario");
            }

            if (!validarIdentidad(usuario, nombre, apellido, fechaVerificacion, conexion)) {
                throw new ServicioException("NO_AUTORIZADO", "No fue posible validar la identidad del usuario");
            }

            if (seguridadContrasena.verificar(nuevaContrasena, usuario.getContrasena())) {
                throw new ServicioException("CONFLICTO", "La nueva contraseña debe ser diferente a la actual");
            }

            if (!usuarioDAO.cambiarContraseña(usuario.getIdUsuario(), seguridadContrasena.hashear(nuevaContrasena), conexion)) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible actualizar la contraseña");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;
        } catch (daoException e) {
            ServicioException error = new ServicioException("BD_ERROR", "Error de base de datos al recuperar la contraseña", e);
            rollback(conexion, error);
            throw error;
        } catch (SQLException e) {
            ServicioException error = new ServicioException("TRANSACCION_ERROR", "Error en la transacción al recuperar la contraseña", e);
            rollback(conexion, error);
            throw error;
        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    private boolean validarIdentidad(Usuario usuario, String nombre, String apellido, LocalDate fecha, Connection conexion) {
        if ("ESTUDIANTE".equalsIgnoreCase(usuario.getRol())) {
            for (Estudiante estudiante : estudianteDAO.consultar(conexion)) {
                if (estudiante.getIdUsuario() == usuario.getIdUsuario() && iguales(estudiante.getNombre(), nombre) && iguales(estudiante.getApellido(), apellido) && fecha.equals(estudiante.getFechaNacimiento())) return true;
            }
            return false;
        }

        if (usuario.getIdEmpleado() != null && usuario.getIdEmpleado() > 0) {
            for (Empleado empleado : empleadoDAO.consultar(conexion)) {
                if (empleado.getIdEmpleado() == usuario.getIdEmpleado() && iguales(empleado.getNombre(), nombre) && iguales(empleado.getApellido(), apellido) && fecha.equals(empleado.getFechaContratacion())) return true;
            }
        }

        return false;
    }

    private boolean iguales(String valorBD, String valorIngresado) {
        return valorBD != null && valorIngresado != null && valorBD.trim().equalsIgnoreCase(valorIngresado.trim());
    }

    private void validarDatos(String nombreUsuario, String nombre, String apellido, LocalDate fecha, String nuevaContrasena) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty() || nombre == null || nombre.trim().isEmpty() || apellido == null || apellido.trim().isEmpty() || fecha == null) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos de recuperación son obligatorios");
        }
        if (nuevaContrasena == null || nuevaContrasena.trim().isEmpty() || nuevaContrasena.length() < 8) {
            throw new ServicioException("DATOS_INVALIDOS", "La nueva contraseña debe contener al menos 8 caracteres");
        }
    }

    private void rollback(Connection conexion, Exception causa) {
        if (conexion == null) return;
        try {
            conexion.rollback();
        } catch (SQLException e) {
            causa.addSuppressed(e);
        }
    }

    private void restaurarAutoCommit(Connection conexion, boolean autoCommitAnterior) {
    if (conexion != null) {
        try {
            conexion.setAutoCommit(autoCommitAnterior);
        } catch (SQLException e) {
            Logger.getLogger(AutenticacionServicio.class.getName()).log(Level.SEVERE, "No fue posible restaurar el autoCommit", e);
        }
    }
}

private void cerrarConexion(Connection conexion) {
    if (conexion != null) {
        try {
            conexion.close();
        } catch (SQLException e) {
            Logger.getLogger(AutenticacionServicio.class.getName()).log(Level.SEVERE, "No fue posible cerrar la conexión", e);
        }
    }
}
}
