/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.servicio;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Dao.EmpleadoDao;
import com.proyecto2.app.backend.Backend.Dao.UsuarioDao;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Empleado;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class EmpleadoServicio {
    
     private final EmpleadoDao empleadoDAO;
    private final UsuarioDao usuarioDAO;

    public EmpleadoServicio() {
        empleadoDAO = new EmpleadoDao();
        usuarioDAO = new UsuarioDao();
    }

    public boolean crearEmpleado(Empleado empleado) {

        if (empleado == null || empleado.getNombre() == null || empleado.getNombre().trim().isEmpty() || empleado.getApellido() == null || empleado.getApellido().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del empleado no son válidos");
        }

        if (empleado.getPuesto() == null || empleado.getPuesto().trim().isEmpty() || empleado.getFechaContratacion() == null || empleado.getSalario() < 0) {
            throw new ServicioException("DATOS_INVALIDOS", "El puesto, fecha de contratación o salario no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            empleado.setEstado("ACTIVO");

            boolean creado = empleadoDAO.insertar(empleado, conexion);

            if (!creado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible crear el empleado");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al crear el empleado", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al crear el empleado", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean modificarEmpleado(Empleado empleado) {

        if (empleado == null || empleado.getIdEmpleado() <= 0 || empleado.getNombre() == null || empleado.getNombre().trim().isEmpty() || empleado.getApellido() == null || empleado.getApellido().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del empleado no son válidos");
        }

        if (empleado.getPuesto() == null || empleado.getPuesto().trim().isEmpty() || empleado.getFechaContratacion() == null || empleado.getSalario() < 0) {
            throw new ServicioException("DATOS_INVALIDOS", "El puesto, fecha de contratación o salario no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Collection<Empleado> empleados = empleadoDAO.consultar(conexion);
            Empleado empleadoActual = null;

            for (Empleado actual : empleados) {
                if (actual.getIdEmpleado() == empleado.getIdEmpleado()) {
                    empleadoActual = actual;
                    break;
                }
            }

            if (empleadoActual == null) {
                throw new ServicioException("NO_ENCONTRADO", "El empleado no existe");
            }

            empleado.setEstado(empleadoActual.getEstado());

            boolean actualizado = empleadoDAO.actualizar(empleado, conexion);

            if (!actualizado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible modificar el empleado");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al modificar el empleado", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al modificar el empleado", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean desactivarEmpleado(int idEmpleado) {

        if (idEmpleado <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "El id del empleado no es válido");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Collection<Empleado> empleados = empleadoDAO.consultar(conexion);
            Empleado empleadoEncontrado = null;

            for (Empleado empleado : empleados) {
                if (empleado.getIdEmpleado() == idEmpleado) {
                    empleadoEncontrado = empleado;
                    break;
                }
            }

            if (empleadoEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El empleado no existe");
            }

            if ("INACTIVO".equalsIgnoreCase(empleadoEncontrado.getEstado())) {
                throw new ServicioException("CONFLICTO", "El empleado ya se encuentra inactivo");
            }

            boolean desactivado = empleadoDAO.desactivar(idEmpleado, conexion);

            if (!desactivado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible desactivar el empleado");
            }

            Collection<Usuario> usuarios = usuarioDAO.consultar(conexion);

            for (Usuario usuario : usuarios) {
                if (Integer.valueOf(idEmpleado).equals(usuario.getIdEmpleado()) && "ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
                    boolean usuarioDesactivado = usuarioDAO.desactivar(usuario.getIdUsuario(), conexion);

                    if (!usuarioDesactivado) {
                        throw new ServicioException("OPERACION_ERROR", "No fue posible desactivar el usuario relacionado al empleado");
                    }

                    break;
                }
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al desactivar el empleado", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al desactivar el empleado", e);

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
