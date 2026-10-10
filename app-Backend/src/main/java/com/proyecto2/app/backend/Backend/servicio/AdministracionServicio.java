/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.servicio;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Dao.GradoDao;
import com.proyecto2.app.backend.Backend.Dao.UsuarioDao;
import com.proyecto2.app.backend.Backend.Dao.cicloDao;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Ciclo;
import com.proyecto2.app.backend.Backend.Model.Grado;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import com.proyecto2.app.backend.Backend.seguridad.SeguridadContrasena;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class AdministracionServicio {

  private final UsuarioDao usuarioDAO;
    private final cicloDao cicloDAO;
    private final GradoDao gradoDAO;
    private final SeguridadContrasena seguridadContrasena;

    public AdministracionServicio() {
        usuarioDAO = new UsuarioDao();
        cicloDAO = new cicloDao();
        gradoDAO = new GradoDao();
        seguridadContrasena = new SeguridadContrasena();
    }

    public boolean crearSuperAdmin(Usuario usuario) {

        if (usuario == null || usuario.getNombreUsuario() == null || usuario.getNombreUsuario().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del Super Admin no son válidos");
        }

        if (usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "La contraseña no puede estar vacía");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            usuario.setRol("SUPER_ADMIN");
            usuario.setEstado("ACTIVO");

            String contrasenaHasheada = seguridadContrasena.hashear(usuario.getContrasena());
            usuario.setContrasena(contrasenaHasheada);

            boolean creado = usuarioDAO.insertar(usuario, conexion);

            if (!creado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible crear el Super Admin");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al crear el Super Admin", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al crear el Super Admin", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean modificarSuperAdmin(Usuario usuario) {

        if (usuario == null || usuario.getIdUsuario() <= 0 || usuario.getNombreUsuario() == null || usuario.getNombreUsuario().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del Super Admin no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Collection<Usuario> usuarios = usuarioDAO.consultar(conexion);
            boolean encontrado = false;

            for (Usuario actual : usuarios) {
                if (actual.getIdUsuario() == usuario.getIdUsuario() && "SUPER_ADMIN".equalsIgnoreCase(actual.getRol())) {
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                throw new ServicioException("NO_ENCONTRADO", "El Super Admin no existe");
            }

            usuario.setRol("SUPER_ADMIN");

            boolean actualizado = usuarioDAO.actualizar(usuario, conexion);

            if (!actualizado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible modificar el Super Admin");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al modificar el Super Admin", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al modificar el Super Admin", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
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

        if (usuario == null || usuario.getNombreUsuario() == null || usuario.getNombreUsuario().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del Admin no son válidos");
        }

        if (usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "La contraseña no puede estar vacía");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            usuario.setRol("ADMIN");
            usuario.setEstado("ACTIVO");

            String contrasenaHasheada = seguridadContrasena.hashear(usuario.getContrasena());
            usuario.setContrasena(contrasenaHasheada);

            boolean creado = usuarioDAO.insertar(usuario, conexion);

            if (!creado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible crear el Admin");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al crear el Admin", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al crear el Admin", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean modificarAdmin(Usuario usuario) {

        if (usuario == null || usuario.getIdUsuario() <= 0 || usuario.getNombreUsuario() == null || usuario.getNombreUsuario().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del Admin no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Collection<Usuario> usuarios = usuarioDAO.consultar(conexion);
            boolean encontrado = false;

            for (Usuario actual : usuarios) {
                if (actual.getIdUsuario() == usuario.getIdUsuario() && "ADMIN".equalsIgnoreCase(actual.getRol())) {
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                throw new ServicioException("NO_ENCONTRADO", "El Admin no existe");
            }

            usuario.setRol("ADMIN");

            boolean actualizado = usuarioDAO.actualizar(usuario, conexion);

            if (!actualizado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible modificar el Admin");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al modificar el Admin", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al modificar el Admin", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
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

            boolean desactivado = cicloDAO.desactivar(idCiclo, conexion);

            if (!desactivado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible desactivar el ciclo escolar");
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
