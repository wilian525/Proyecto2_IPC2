/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.servicio;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Dao.GradoDao;
import com.proyecto2.app.backend.Backend.Dao.EmpleadoDao;
import com.proyecto2.app.backend.Backend.Dao.UsuarioDao;
import com.proyecto2.app.backend.Backend.Dao.cicloDao;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Ciclo;
import com.proyecto2.app.backend.Backend.Model.Grado;
import com.proyecto2.app.backend.Backend.Model.Empleado;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import com.proyecto2.app.backend.Backend.seguridad.SeguridadContrasena;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wilian
 */
public class AdministracionServicio {

    private  UsuarioDao usuarioDAO;
    private  EmpleadoDao empleadoDAO;
    private  cicloDao cicloDAO;
    private  GradoDao gradoDAO;
    private  SeguridadContrasena seguridadContrasena;

    public AdministracionServicio() {
        usuarioDAO = new UsuarioDao();
        empleadoDAO = new EmpleadoDao();
        cicloDAO = new cicloDao();
        gradoDAO = new GradoDao();
        seguridadContrasena = new SeguridadContrasena();
    }

    public boolean crearSuperAdmin(Usuario usuario) {
        return crearAdministrativo(usuario, null, "SUPER_ADMIN");
    }

    public boolean modificarSuperAdmin(Usuario usuario) {
        return modificarAdministrativo(usuario, null, "SUPER_ADMIN");
    }

    public boolean desactivarSuperAdmin(int idUsuario) {

        if (idUsuario <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "El id del Super Admin no es válido");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Collection<Usuario> usuarios = usuarioDAO.consultar(conexion);
            int superAdminActivos = 0;
            boolean encontrado = false;

            for (Usuario usuario : usuarios) {

                if ("SUPER_ADMIN".equalsIgnoreCase(usuario.getRol()) && "ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
                    superAdminActivos++;
                }

                if (usuario.getIdUsuario() == idUsuario && "SUPER_ADMIN".equalsIgnoreCase(usuario.getRol())) {
                    encontrado = true;
                }
            }

            if (!encontrado) {
                throw new ServicioException("NO_ENCONTRADO", "El Super Admin no existe");
            }

            if (superAdminActivos <= 1) {
                throw new ServicioException("CONFLICTO", "Debe permanecer al menos un Super Admin activo en el sistema");
            }

            boolean desactivado = usuarioDAO.desactivar(idUsuario, conexion);

            if (!desactivado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible desactivar el Super Admin");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al desactivar el Super Admin", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al desactivar el Super Admin", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean crearAdmin(Usuario usuario) {
        return crearAdministrativo(usuario, null, "ADMIN");
    }

    public boolean modificarAdmin(Usuario usuario) {
        return modificarAdministrativo(usuario, null, "ADMIN");
    }

    public boolean desactivarAdmin(int idUsuario) {

        if (idUsuario <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "El id del Admin no es válido");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Collection<Usuario> usuarios = usuarioDAO.consultar(conexion);
            boolean encontrado = false;

            for (Usuario usuario : usuarios) {
                if (usuario.getIdUsuario() == idUsuario && "ADMIN".equalsIgnoreCase(usuario.getRol())) {
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                throw new ServicioException("NO_ENCONTRADO", "El Admin no existe");
            }

            boolean desactivado = usuarioDAO.desactivar(idUsuario, conexion);

            if (!desactivado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible desactivar el Admin");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al desactivar el Admin", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al desactivar el Admin", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    // Reactiva únicamente la cuenta del rol indicado, sin cambiar EMPLEADO.estado.
    public boolean activarAdministrativo(int idUsuario, String rol) {
        validarRol(rol);
        if (idUsuario <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Selecciona un usuario administrativo válido");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Usuario usuario = usuarioDAO.consultarPorId(idUsuario, conexion);
            if (usuario == null || !rol.equals(usuario.getRol())) {
                throw new ServicioException("NO_ENCONTRADO", "El usuario administrativo no existe para el rol seleccionado");
            }
            if ("ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
                throw new ServicioException("CONFLICTO", "La cuenta ya se encuentra activa");
            }
            if (!"INACTIVO".equalsIgnoreCase(usuario.getEstado())) {
                throw new ServicioException("CONFLICTO", "El estado de la cuenta no permite su reactivación");
            }
            if (!usuarioDAO.activar(idUsuario, rol, conexion)) {
                throw new ServicioException("CONFLICTO", "La cuenta cambió de estado; actualiza la lista e intenta nuevamente");
            }

            conexion.commit();
            return true;
        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;
        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "No fue posible reactivar la cuenta administrativa", e);
        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al reactivar la cuenta", e);
        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean crearCiclo(Ciclo ciclo) {

        if (ciclo == null || ciclo.getAnioInicio() <= 0 || ciclo.getAnioFin() <= 0 || ciclo.getAnioFin() < ciclo.getAnioInicio()) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del ciclo escolar no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Collection<Ciclo> ciclos = cicloDAO.consultarParaActualizacion(conexion);

            for (Ciclo cicloActual : ciclos) {
                if ("ACTIVO".equalsIgnoreCase(cicloActual.getEstado())) {
                    throw new ServicioException("CONFLICTO", "Ya existe un ciclo escolar activo");
                }
                if (cicloActual.getAnioInicio() == ciclo.getAnioInicio()
                        && cicloActual.getAnioFin() == ciclo.getAnioFin()) {
                    throw new ServicioException("CONFLICTO", "Ya existe un año lectivo con esas fechas");
                }
            }

            ciclo.setEstado("ACTIVO");

            boolean creado = cicloDAO.insertar(ciclo, conexion);

            if (!creado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible crear el ciclo escolar");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al crear el ciclo escolar", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al crear el ciclo escolar", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    // Permite activar un ciclo inactivo solo cuando no existe otro ciclo activo.
    public boolean activarCiclo(int idCiclo) {

        if (idCiclo <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Selecciona un año lectivo válido");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Collection<Ciclo> ciclos = cicloDAO.consultarParaActualizacion(conexion);
            Ciclo seleccionado = null;
            Ciclo activo = null;

            for (Ciclo ciclo : ciclos) {
                if (ciclo.getIdCiclo() == idCiclo) {
                    seleccionado = ciclo;
                }
                if ("ACTIVO".equalsIgnoreCase(ciclo.getEstado())) {
                    activo = ciclo;
                }
            }

            if (seleccionado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El año lectivo seleccionado no existe");
            }
            if ("ACTIVO".equalsIgnoreCase(seleccionado.getEstado())) {
                throw new ServicioException("CONFLICTO", "El año lectivo seleccionado ya está activo");
            }
            if (!"INACTIVO".equalsIgnoreCase(seleccionado.getEstado())) {
                throw new ServicioException("CONFLICTO", "El estado del año lectivo no permite activarlo");
            }
            if (activo != null) {
                throw new ServicioException("CONFLICTO", "Primero desactiva el año lectivo "
                        + activo.getAnioInicio() + " - " + activo.getAnioFin()
                        + " antes de activar otro");
            }

            if (!cicloDAO.activar(idCiclo, conexion)) {
                throw new ServicioException("CONFLICTO", "El año lectivo cambió de estado; actualiza la lista e intenta de nuevo");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "No se pudo acceder a los años lectivos en la base de datos", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "No se pudo completar la activación del año lectivo", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean desactivarCiclo(int idCiclo) {

        if (idCiclo <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "El id del ciclo no es válido");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Collection<Ciclo> ciclos = cicloDAO.consultarParaActualizacion(conexion);
            Ciclo seleccionado = null;
            for (Ciclo ciclo : ciclos) {
                if (ciclo.getIdCiclo() == idCiclo) {
                    seleccionado = ciclo;
                    break;
                }
            }
            if (seleccionado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El año lectivo seleccionado no existe");
            }
            if (!"ACTIVO".equalsIgnoreCase(seleccionado.getEstado())) {
                throw new ServicioException("CONFLICTO", "El año lectivo ya está inactivo o no permite desactivarse");
            }

            if (!cicloDAO.desactivar(idCiclo, conexion)) {
                throw new ServicioException("CONFLICTO", "El año lectivo cambió de estado; actualiza la lista e intenta de nuevo");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al desactivar el ciclo escolar", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al desactivar el ciclo escolar", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean crearGrado(Grado grado) {

        if (grado == null || grado.getNombre() == null || grado.getNombre().trim().isEmpty() || grado.getIdNivel() <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del grado no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            boolean creado = gradoDAO.insertar(grado, conexion);

            if (!creado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible crear el grado");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al crear el grado", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al crear el grado", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean modificarGrado(Grado grado) {

        if (grado == null || grado.getIdGrado() <= 0 || grado.getNombre() == null || grado.getNombre().trim().isEmpty() || grado.getIdNivel() <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del grado no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            boolean actualizado = gradoDAO.actualizar(grado, conexion);

            if (!actualizado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible modificar el grado");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al modificar el grado", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al modificar el grado", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    // Reutiliza la misma transacción que las operaciones existentes de creación.
    public boolean crearAdministrativoCompleto(Usuario usuario, Empleado empleado, String rol) {
        if (empleado == null) {
            throw new ServicioException("DATOS_INVALIDOS", "Debes proporcionar los datos personales del administrador");
        }
        return crearAdministrativo(usuario, empleado, rol);
    }

    // Conserva el ID de empleado y la contraseña actual al modificar.
    public boolean modificarAdministrativoCompleto(int idUsuario, Usuario usuario, Empleado empleado, String rol) {
        if (usuario == null || idUsuario <= 0 || empleado == null) {
            throw new ServicioException("DATOS_INVALIDOS", "Debes proporcionar el usuario y sus datos personales");
        }
        usuario.setIdUsuario(idUsuario);
        return modificarAdministrativo(usuario, empleado, rol);
    }

    private boolean crearAdministrativo(Usuario usuario, Empleado empleado, String rol) {
        validarRol(rol);
        validarUsuario(usuario, true);
        if (empleado != null) {
            validarEmpleado(empleado);
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Usuario duplicado = usuarioDAO.consultarPorNombreUsuario(usuario.getNombreUsuario().trim(), conexion);
            if (duplicado != null) {
                throw new ServicioException("CONFLICTO", "El nombre de usuario ya está registrado");
            }

            if (empleado != null) {
                empleado.setPuesto(rol);
                empleado.setEstado("ACTIVO");

                if (!empleadoDAO.insertar(empleado, conexion) || empleado.getIdEmpleado() <= 0) {
                    throw new ServicioException("OPERACION_ERROR", "No fue posible registrar los datos del empleado");
                }
                usuario.setIdEmpleado(empleado.getIdEmpleado());
            }

            usuario.setNombreUsuario(usuario.getNombreUsuario().trim());
            usuario.setRol(rol);
            usuario.setEstado("ACTIVO");
            usuario.setContrasena(seguridadContrasena.hashear(usuario.getContrasena()));

            if (!usuarioDAO.insertar(usuario, conexion)) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible crear el usuario administrativo");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;
        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al registrar el administrador", e);
        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "No se pudo completar el registro del administrador", e);
        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    private boolean modificarAdministrativo(Usuario usuario, Empleado empleado, String rol) {
        validarRol(rol);
        validarUsuario(usuario, false);
        if (usuario.getIdUsuario() <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Selecciona un administrador válido");
        }
        if (empleado != null) {
            validarEmpleado(empleado);
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Usuario actual = usuarioDAO.consultarPorId(usuario.getIdUsuario(), conexion);
            if (actual == null || !rol.equals(actual.getRol())) {
                throw new ServicioException("NO_ENCONTRADO", "El administrador seleccionado no existe");
            }

            Usuario duplicado = usuarioDAO.consultarPorNombreUsuario(usuario.getNombreUsuario().trim(), conexion);
            if (duplicado != null && duplicado.getIdUsuario() != usuario.getIdUsuario()) {
                throw new ServicioException("CONFLICTO", "El nombre de usuario ya está registrado");
            }

            if (empleado != null) {
                if (actual.getIdEmpleado() == null || actual.getIdEmpleado() <= 0) {
                    throw new ServicioException("CONFLICTO", "El administrador no tiene un empleado asociado");
                }
                empleado.setIdEmpleado(actual.getIdEmpleado());
                empleado.setPuesto(rol);
                if (!empleadoDAO.actualizar(empleado, conexion)) {
                    throw new ServicioException("OPERACION_ERROR", "No fue posible actualizar los datos personales");
                }
            }

            usuario.setNombreUsuario(usuario.getNombreUsuario().trim());
            usuario.setRol(rol);
            usuario.setIdEmpleado(actual.getIdEmpleado() == null ? 0 : actual.getIdEmpleado());

            if (!usuarioDAO.actualizar(usuario, conexion)) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible modificar el administrador");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;
        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al modificar el administrador", e);
        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "No se pudo completar la modificación del administrador", e);
        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    private void validarRol(String rol) {
        if (!"SUPER_ADMIN".equals(rol) && !"ADMIN".equals(rol)) {
            throw new ServicioException("DATOS_INVALIDOS", "El rol administrativo no es válido");
        }
    }

    private void validarUsuario(Usuario usuario, boolean creacion) {
        if (usuario == null || usuario.getNombreUsuario() == null || usuario.getNombreUsuario().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "El nombre de usuario es obligatorio");
        }
        if (creacion && (usuario.getContrasena() == null || usuario.getContrasena().length() < 8)) {
            throw new ServicioException("DATOS_INVALIDOS", "La contraseña debe contener al menos 8 caracteres");
        }
    }

    private void validarEmpleado(Empleado empleado) {
        if (empleado.getNombre() == null || empleado.getNombre().isBlank()
                || empleado.getApellido() == null || empleado.getApellido().isBlank()
                || empleado.getFechaContratacion() == null
                || !Double.isFinite(empleado.getSalario()) || empleado.getSalario() < 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Completa nombre, apellido, fecha de contratación y salario válido");
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
            Logger.getLogger(AdministracionServicio.class.getName()).log(Level.SEVERE, "No fue posible restaurar el autoCommit", e);
        }
    }
}

private void cerrarConexion(Connection conexion) {
    if (conexion != null) {
        try {
            conexion.close();
        } catch (SQLException e) {
            Logger.getLogger(AdministracionServicio.class.getName()).log(Level.SEVERE, "No fue posible cerrar la conexión", e);
        }
    }
}
}
