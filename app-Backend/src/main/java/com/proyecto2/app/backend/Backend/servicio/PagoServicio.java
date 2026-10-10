/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.servicio;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Dao.EmpleadoDao;
import com.proyecto2.app.backend.Backend.Dao.EstudianteDao;
import com.proyecto2.app.backend.Backend.Dao.FacturaDao;
import com.proyecto2.app.backend.Backend.Dao.PagoDao;
import com.proyecto2.app.backend.Backend.Dao.UsuarioDao;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Empleado;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import com.proyecto2.app.backend.Backend.Model.Factura;
import com.proyecto2.app.backend.Backend.Model.Pago;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class PagoServicio {
    
      public static final double PRECIO_INSCRIPCION = 500;
    public static final double PRECIO_MENSUALIDAD = 300;
    public static final double PRECIO_ACTIVIDAD = 100;

    private PagoDao pagoDAO;
    private FacturaDao facturaDAO;
    private EstudianteDao estudianteDAO;
    private EmpleadoDao empleadoDAO;
    private UsuarioDao usuarioDAO;

    public PagoServicio() {
        pagoDAO = new PagoDao();
        facturaDAO = new FacturaDao();
        estudianteDAO = new EstudianteDao();
        empleadoDAO = new EmpleadoDao();
        usuarioDAO = new UsuarioDao();
    }

    public boolean registrarPago(Pago pago) {

        if (pago == null || pago.getIdEstudiante() <= 0 || pago.getIdEmpleado() <= 0 || pago.getConcepto() == null || pago.getConcepto().trim().isEmpty() || pago.getFecha() == null) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del pago no son válidos");
        }

        String concepto = pago.getConcepto().trim().toUpperCase();
        double montoEsperado;

        if ("INSCRIPCION".equals(concepto)) {
            montoEsperado = PRECIO_INSCRIPCION;
        } else if ("MENSUALIDAD".equals(concepto)) {
            montoEsperado = PRECIO_MENSUALIDAD;
        } else if ("ACTIVIDAD".equals(concepto)) {
            montoEsperado = PRECIO_ACTIVIDAD;
        } else {
            throw new ServicioException("DATOS_INVALIDOS", "El concepto debe ser INSCRIPCION, MENSUALIDAD o ACTIVIDAD");
        }

        if (Math.abs(pago.getMonto() - montoEsperado) > 0.01) {
            throw new ServicioException("DATOS_INVALIDOS", "El monto no corresponde al precio establecido para el concepto");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Estudiante estudianteEncontrado = null;
            Collection<Estudiante> estudiantes = estudianteDAO.consultar(conexion);

            for (Estudiante estudiante : estudiantes) {
                if (estudiante.getIdEstudiante() == pago.getIdEstudiante()) {
                    estudianteEncontrado = estudiante;
                    break;
                }
            }

            if (estudianteEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El estudiante no existe");
            }

            Empleado empleadoEncontrado = null;
            Collection<Empleado> empleados = empleadoDAO.consultar(conexion);

            for (Empleado empleado : empleados) {
                if (empleado.getIdEmpleado() == pago.getIdEmpleado()) {
                    empleadoEncontrado = empleado;
                    break;
                }
            }

            if (empleadoEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El empleado que registra el pago no existe");
            }

            if (!"ACTIVO".equalsIgnoreCase(empleadoEncontrado.getEstado())) {
                throw new ServicioException("NO_AUTORIZADO", "El empleado que registra el pago no se encuentra activo");
            }

            Usuario usuarioSecretaria = null;
            Collection<Usuario> usuarios = usuarioDAO.consultar(conexion);

            for (Usuario usuario : usuarios) {
                if (usuario.getIdEmpleado() != null && usuario.getIdEmpleado() == pago.getIdEmpleado()) {
                    usuarioSecretaria = usuario;
                    break;
                }
            }

            if (usuarioSecretaria == null || !"SECRETARIA".equalsIgnoreCase(usuarioSecretaria.getRol()) || !"ACTIVO".equalsIgnoreCase(usuarioSecretaria.getEstado())) {
                throw new ServicioException("NO_AUTORIZADO", "El pago únicamente puede ser registrado por una Secretaria activa");
            }

            pago.setConcepto(concepto);

            boolean registrado = pagoDAO.insertar(pago, conexion);

            if (!registrado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible registrar el pago");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al registrar el pago", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al registrar el pago", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean emitirFactura(Factura factura) {

        if (factura == null || factura.getNumeroFactura() == null || factura.getNumeroFactura().trim().isEmpty() || factura.getFecha() == null || factura.getIdPago() <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos de la factura no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Pago pagoEncontrado = null;
            Collection<Pago> pagos = pagoDAO.consultar(conexion);

            for (Pago pago : pagos) {
                if (pago.getIdPago() == factura.getIdPago()) {
                    pagoEncontrado = pago;
                    break;
                }
            }

            if (pagoEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El pago relacionado con la factura no existe");
            }

            if (factura.getFecha().isBefore(pagoEncontrado.getFecha())) {
                throw new ServicioException("DATOS_INVALIDOS", "La fecha de la factura no puede ser anterior a la fecha del pago");
            }

            Collection<Factura> facturas = facturaDAO.consultar(conexion);

            for (Factura actual : facturas) {

                if (actual.getIdPago() == factura.getIdPago()) {
                    throw new ServicioException("CONFLICTO", "El pago ya posee una factura");
                }

                if (actual.getNumeroFactura().equalsIgnoreCase(factura.getNumeroFactura())) {
                    throw new ServicioException("CONFLICTO", "El número de factura ya existe");
                }
            }

            factura.setMontoTotal(pagoEncontrado.getMonto());

            boolean registrada = facturaDAO.insertar(factura, conexion);

            if (!registrada) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible emitir la factura");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al emitir la factura", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al emitir la factura", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean generarSolvencia(int idEstudiante, int mensualidadesRequeridas) {

        if (idEstudiante <= 0 || mensualidadesRequeridas < 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos para generar la solvencia no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            boolean estudianteExiste = false;
            Collection<Estudiante> estudiantes = estudianteDAO.consultar(conexion);

            for (Estudiante estudiante : estudiantes) {
                if (estudiante.getIdEstudiante() == idEstudiante) {
                    estudianteExiste = true;
                    break;
                }
            }

            if (!estudianteExiste) {
                throw new ServicioException("NO_ENCONTRADO", "El estudiante no existe");
            }

            double totalInscripcion = 0;
            double totalMensualidades = 0;

            Collection<Pago> pagos = pagoDAO.consultar(conexion);

            for (Pago pago : pagos) {

                if (pago.getIdEstudiante() == idEstudiante) {

                    if ("INSCRIPCION".equalsIgnoreCase(pago.getConcepto())) {
                        totalInscripcion += pago.getMonto();
                    }

                    if ("MENSUALIDAD".equalsIgnoreCase(pago.getConcepto())) {
                        totalMensualidades += pago.getMonto();
                    }
                }
            }

            double montoInscripcionRequerido = PRECIO_INSCRIPCION;
            double montoMensualidadesRequerido = mensualidadesRequeridas * PRECIO_MENSUALIDAD;

            boolean solvente = totalInscripcion >= montoInscripcionRequerido && totalMensualidades >= montoMensualidadesRequerido;

            conexion.commit();
            return solvente;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al generar la solvencia", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al generar la solvencia", e);

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
