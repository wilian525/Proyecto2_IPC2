/*
 * Servicio para registrar cuotas, cobros, conciliaciones y facturas.
 */
package com.proyecto2.app.backend.Backend.servicio;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Dao.CuotaMensualDao;
import com.proyecto2.app.backend.Backend.Dao.FacturaDao;
import com.proyecto2.app.backend.Backend.Dao.InscripcionDao;
import com.proyecto2.app.backend.Backend.Dao.PagoDao;
import com.proyecto2.app.backend.Backend.Dao.UsuarioDao;
import com.proyecto2.app.backend.Backend.Dao.cicloDao;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Ciclo;
import com.proyecto2.app.backend.Backend.Model.CuotaMensual;
import com.proyecto2.app.backend.Backend.Model.Factura;
import com.proyecto2.app.backend.Backend.Model.Inscripcion;
import com.proyecto2.app.backend.Backend.Model.Pago;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PagoServicio {

    public static final double PRECIO_INSCRIPCION = 500;
    public static final double PRECIO_MENSUALIDAD = 300;

    private final PagoDao pagoDAO = new PagoDao();
    private final CuotaMensualDao cuotaDAO = new CuotaMensualDao();
    private final cicloDao cicloDAO = new cicloDao();
    private final FacturaDao facturaDAO = new FacturaDao();
    private final InscripcionDao inscripcionDAO = new InscripcionDao();
    private final UsuarioDao usuarioDAO = new UsuarioDao();

    public boolean registrarPago(Pago pago) {
        validarPago(pago);
        String concepto = pago.getConcepto().trim().toUpperCase();
        Connection conexion = null;
        boolean anterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            anterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            if (!usuarioDAO.esSecretariaActiva(pago.getIdEmpleado(), conexion)) {
                throw new ServicioException("NO_AUTORIZADO", "Solo una Secretaria activa puede registrar pagos");
            }

            CuotaMensual cuota = null;
            Inscripcion inscripcion;
            if ("MENSUALIDAD".equals(concepto)) {
                if (pago.getIdCuota() == null || pago.getIdCuota() <= 0) {
                    throw new ServicioException("DATOS_INVALIDOS", "Selecciona una mensualidad con mes y año");
                }
                cuota = cuotaDAO.consultarPorId(pago.getIdCuota(), conexion);
                if (cuota == null) throw new ServicioException("NO_ENCONTRADO", "No existe la mensualidad seleccionada");
                if (cuota.getIdPago() != null) throw new ServicioException("CONFLICTO", "Esa mensualidad ya fue pagada");
                if (Math.abs(cuota.getMonto() - pago.getMonto()) > 0.01) {
                    throw new ServicioException("DATOS_INVALIDOS", "El monto debe coincidir con el de la mensualidad");
                }
                inscripcion = inscripcionDAO.consultarPorId(cuota.getIdInscripcion(), conexion);
            } else {
                if (pago.getIdInscripcion() == null || pago.getIdInscripcion() <= 0) {
                    throw new ServicioException("DATOS_INVALIDOS", "Selecciona la inscripción a la que corresponde el pago");
                }
                if (!cuotaDAO.bloquearInscripcion(pago.getIdInscripcion(), conexion)) {
                    throw new ServicioException("NO_ENCONTRADO", "No existe la inscripción");
                }
                inscripcion = inscripcionDAO.consultarPorId(pago.getIdInscripcion(), conexion);
            }

            if (inscripcion == null || inscripcion.getIdEstudiante() != pago.getIdEstudiante()) {
                throw new ServicioException("CONFLICTO", "El pago no corresponde a la inscripción del estudiante");
            }
            validarCicloActivo(inscripcion.getIdCiclo(), conexion);
            validarPagoAnterior(pago, concepto, inscripcion.getIdInscripcion(), conexion);

            pago.setConcepto(concepto);
            int idPago = pagoDAO.insertarConId(pago, conexion);
            if (idPago <= 0) throw new ServicioException("OPERACION_ERROR", "No se pudo guardar el pago");

            boolean asociado = cuota != null
                    ? cuotaDAO.marcarPagada(cuota.getIdCuota(), idPago, conexion)
                    : cuotaDAO.vincularPago(idPago, inscripcion.getIdInscripcion(), conexion);
            if (!asociado) throw new ServicioException("CONFLICTO", "No se pudo vincular el pago a su obligación");

            conexion.commit();
            return true;
        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;
        } catch (daoException e) {
            rollback(conexion, e);
            throw errorDao(e, "No se pudo registrar el pago");
        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "No se pudo guardar el pago", e);
        } finally {
            restaurarAutoCommit(conexion, anterior);
            cerrarConexion(conexion);
        }
    }

    private void validarPago(Pago pago) {
        if (pago == null || pago.getIdEstudiante() <= 0 || pago.getIdEmpleado() <= 0
                || pago.getConcepto() == null || pago.getFecha() == null || pago.getMonto() <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del pago no son válidos");
        }
        String concepto = pago.getConcepto().trim().toUpperCase();
        if (!"INSCRIPCION".equals(concepto) && !"MENSUALIDAD".equals(concepto) && !"ACTIVIDAD".equals(concepto)) {
            throw new ServicioException("DATOS_INVALIDOS", "El concepto indicado no es válido");
        }
        if (("INSCRIPCION".equals(concepto) && Math.abs(pago.getMonto() - PRECIO_INSCRIPCION) > 0.01)
                || ("MENSUALIDAD".equals(concepto) && Math.abs(pago.getMonto() - PRECIO_MENSUALIDAD) > 0.01)) {
            throw new ServicioException("DATOS_INVALIDOS", "El monto no coincide con la tarifa del colegio");
        }
    }

    private void validarPagoAnterior(Pago pago, String concepto, int idInscripcion, Connection conexion) {
        if ("ACTIVIDAD".equals(concepto)) return;
        if (cuotaDAO.tienePagoAntiguo(pago.getIdEstudiante(), concepto, conexion)) {
            throw new ServicioException("CONFLICTO", "Existen pagos anteriores sin período asociado. Concílialos antes de cobrar otra vez");
        }
        if ("INSCRIPCION".equals(concepto) && cuotaDAO.inscripcionPagada(idInscripcion, conexion)) {
            throw new ServicioException("CONFLICTO", "La inscripción de este ciclo ya está pagada");
        }
    }

    private Ciclo validarCicloActivo(int idCiclo, Connection conexion) {
        for (Ciclo ciclo : cicloDAO.consultar(conexion)) {
            if (ciclo.getIdCiclo() == idCiclo) {
                if ("ACTIVO".equalsIgnoreCase(ciclo.getEstado())) return ciclo;
                throw new ServicioException("CONFLICTO", "Solo se pueden registrar cobros del ciclo activo");
            }
        }
        throw new ServicioException("NO_ENCONTRADO", "No se encontró el ciclo escolar");
    }

    public boolean registrarCuota(CuotaMensual cuota) {
        if (cuota == null || cuota.getIdInscripcion() <= 0 || cuota.getMes() < 1 || cuota.getMes() > 12
                || cuota.getAnio() < 2000 || Math.abs(cuota.getMonto() - PRECIO_MENSUALIDAD) > 0.01) {
            throw new ServicioException("DATOS_INVALIDOS", "Selecciona un mes, año y cuota de Q 300.00");
        }
        Connection conexion = null;
        boolean anterior = true;
        try {
            conexion = ConexionDB.getInstance().getConnection();
            anterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);
            if (!cuotaDAO.bloquearInscripcion(cuota.getIdInscripcion(), conexion)) {
                throw new ServicioException("NO_ENCONTRADO", "No se encontró la inscripción");
            }
            Inscripcion inscripcion = inscripcionDAO.consultarPorId(cuota.getIdInscripcion(), conexion);
            if (inscripcion == null) throw new ServicioException("NO_ENCONTRADO", "No se encontró la inscripción");
            Ciclo ciclo = validarCicloActivo(inscripcion.getIdCiclo(), conexion);
            if (cuota.getAnio() < ciclo.getAnioInicio() || cuota.getAnio() > ciclo.getAnioFin()) {
                throw new ServicioException("DATOS_INVALIDOS", "El año de la mensualidad no corresponde al ciclo");
            }
            // El índice UNIQUE(id_inscripcion, anio, mes) es la protección definitiva contra duplicados.
            if (!cuotaDAO.insertar(cuota, conexion)) throw new ServicioException("OPERACION_ERROR", "No se pudo crear la cuota");
            conexion.commit();
            return true;
        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;
        } catch (daoException e) {
            rollback(conexion, e);
            throw errorDao(e, "No se pudo registrar la mensualidad");
        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "No se pudo registrar la mensualidad", e);
        } finally {
            restaurarAutoCommit(conexion, anterior);
            cerrarConexion(conexion);
        }
    }

    public boolean conciliarPago(int idPago, int idInscripcion, Integer idCuota) {
        if (idPago <= 0 || idInscripcion <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Selecciona el pago y la inscripción");
        }
        Connection conexion = null;
        boolean anterior = true;
        try {
            conexion = ConexionDB.getInstance().getConnection();
            anterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);
            Pago pago = pagoDAO.consultarPorId(idPago, conexion);
            Inscripcion inscripcion = inscripcionDAO.consultarPorId(idInscripcion, conexion);
            if (pago == null || inscripcion == null || pago.getIdEstudiante() != inscripcion.getIdEstudiante()) {
                throw new ServicioException("DATOS_INVALIDOS", "El pago no corresponde a la inscripción seleccionada");
            }
            if (cuotaDAO.estaVinculado(idPago, conexion)) {
                throw new ServicioException("CONFLICTO", "Este pago ya está vinculado a una obligación");
            }
            boolean asociado;
            if ("MENSUALIDAD".equalsIgnoreCase(pago.getConcepto())) {
                if (idCuota == null || idCuota <= 0) {
                    throw new ServicioException("DATOS_INVALIDOS", "Selecciona el mes y año de la cuota histórica");
                }
                CuotaMensual cuota = cuotaDAO.consultarPorId(idCuota, conexion);
                if (cuota == null || cuota.getIdInscripcion() != idInscripcion || cuota.getIdPago() != null
                        || Math.abs(cuota.getMonto() - pago.getMonto()) > 0.01) {
                    throw new ServicioException("CONFLICTO", "La cuota no corresponde al pago o ya está pagada");
                }
                asociado = cuotaDAO.marcarPagada(idCuota, idPago, conexion);
            } else {
                if ("INSCRIPCION".equalsIgnoreCase(pago.getConcepto())
                        && cuotaDAO.inscripcionPagada(idInscripcion, conexion)) {
                    throw new ServicioException("CONFLICTO", "Esta inscripción ya tiene un pago aplicado");
                }
                asociado = cuotaDAO.vincularPago(idPago, idInscripcion, conexion);
            }
            if (!asociado) throw new ServicioException("CONFLICTO", "No fue posible conciliar el pago");
            conexion.commit();
            return true;
        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;
        } catch (daoException e) {
            rollback(conexion, e);
            throw errorDao(e, "No se pudo conciliar el pago");
        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "No se pudo conciliar el pago", e);
        } finally {
            restaurarAutoCommit(conexion, anterior);
            cerrarConexion(conexion);
        }
    }

    private ServicioException errorDao(daoException e, String mensaje) {
        if (e.getCause() instanceof SQLException sql && sql.getSQLState() != null
                && sql.getSQLState().startsWith("23")) {
            return new ServicioException("CONFLICTO", "El pago o la mensualidad ya existe para ese período", e);
        }
        return new ServicioException("BD_ERROR", mensaje, e);
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

            Pago pagoEncontrado = pagoDAO.consultarPorId(factura.getIdPago(), conexion);

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
            throw errorDao(e, "No se pudo emitir la factura");

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al emitir la factura", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    // Compatibilidad con los endpoints anteriores: ya no se aceptan cuotas arbitrarias.
    @Deprecated
    public boolean generarSolvencia(int idEstudiante, int mensualidadesIgnoradas) {
        try {
            return new com.proyecto2.app.backend.Backend.procesos.PagoProcesos()
                    .consultarSolvencia(idEstudiante, 0);
        } catch (com.proyecto2.app.backend.Backend.Exception.ProcesosException e) {
            throw new ServicioException(e.getCodigo(), e.getMessage(), e);
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

    private void restaurarAutoCommit(Connection conexion, boolean anterior) {
        if (conexion == null) return;
        try {
            conexion.setAutoCommit(anterior);
        } catch (SQLException e) {
            Logger.getLogger(PagoServicio.class.getName()).log(Level.SEVERE, "No se pudo restaurar la transacción", e);
        }
    }

    private void cerrarConexion(Connection conexion) {
        if (conexion == null) return;
        try {
            conexion.close();
        } catch (SQLException e) {
            Logger.getLogger(PagoServicio.class.getName()).log(Level.SEVERE, "No se pudo cerrar la conexión", e);
        }
    }
}
