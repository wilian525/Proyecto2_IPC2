/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.procesos;

import com.proyecto2.app.backend.Backend.Dao.EstudianteDao;
import com.proyecto2.app.backend.Backend.Dao.FacturaDao;
import com.proyecto2.app.backend.Backend.Dao.PagoDao;
import com.proyecto2.app.backend.Backend.Dao.InscripcionDao;
import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import com.proyecto2.app.backend.Backend.Model.Factura;
import com.proyecto2.app.backend.Backend.Model.Pago;
import com.proyecto2.app.backend.Backend.servicio.PagoServicio;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class PagoProcesos {

    private PagoDao pagoDAO;
    private InscripcionDao inscripcionDAO;
    private EstudianteDao estudianteDAO;
    private FacturaDao facturaDAO;

    public PagoProcesos() {
        pagoDAO = new PagoDao();
        inscripcionDAO = new InscripcionDao();
        estudianteDAO = new EstudianteDao();
        facturaDAO = new FacturaDao();
    }

    public double consultarEstadoCuenta(int idEstudiante, int mensualidadesRequeridas) {
        if (idEstudiante <= 0 || mensualidadesRequeridas < 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "Los datos para consultar el estado de cuenta no son válidos");
        }

        try {
            if (mensualidadesRequeridas > 12) {
                throw new ProcesosException("DATOS_INVALIDOS", "No puedes evaluar más de 12 mensualidades en un ciclo escolar");
            }
            if (!existeEstudiante(idEstudiante)) {
                throw new ProcesosException("NO_ENCONTRADO", "No se encontró el estudiante");
            }

            boolean inscrito = false;
            for (var inscripcion : inscripcionDAO.consultar()) {
                if (inscripcion.getIdEstudiante() == idEstudiante) {
                    inscrito = true;
                    break;
                }
            }
            if (!inscrito) {
                return 0;
            }

            double totalInscripcion = 0;
            double totalMensualidades = 0;

            for (Pago pago : pagoDAO.consultar()) {
                if (pago.getIdEstudiante() == idEstudiante) {
                    if ("INSCRIPCION".equalsIgnoreCase(pago.getConcepto())) {
                        totalInscripcion += pago.getMonto();
                    }

                    if ("MENSUALIDAD".equalsIgnoreCase(pago.getConcepto())) {
                        totalMensualidades += pago.getMonto();
                    }
                }
            }

            double totalRequerido = PagoServicio.PRECIO_INSCRIPCION + (mensualidadesRequeridas * PagoServicio.PRECIO_MENSUALIDAD);
            double totalPagado = totalInscripcion + totalMensualidades;
            double saldoPendiente = totalRequerido - totalPagado;

            return Math.max(saldoPendiente, 0);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el estado de cuenta", e);
        }
    }

    public Collection<Pago> consultarHistorialPagos(int idEstudiante) {
        if (idEstudiante <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El id del estudiante no es válido");
        }

        try {
            if (!existeEstudiante(idEstudiante)) {
                throw new ProcesosException("NO_ENCONTRADO", "No se encontró el estudiante");
            }

            Collection<Pago> historial = new ArrayList<>();

            for (Pago pago : pagoDAO.consultar()) {
                if (pago.getIdEstudiante() == idEstudiante) {
                    historial.add(pago);
                }
            }

            return historial;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el historial de pagos", e);
        }
    }

    private boolean existeEstudiante(int idEstudiante) {
        for (Estudiante estudiante : estudianteDAO.consultar()) {
            if (estudiante.getIdEstudiante() == idEstudiante) {
                return true;
            }
        }
        return false;
    }

    public Collection<Pago> consultarPagos(int pagina, int limite) {
        if (pagina <= 0 || limite <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "La página y el límite deben ser mayores que cero");
        }

        try {
            return pagoDAO.consultar(pagina, limite);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar los pagos", e);
        }
    }

    public int contarPagos() {
        try {
            return pagoDAO.contar();
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo obtener el total de pagos", e);
        }
    }

    public Collection<Pago> consultarHistorialPagos(int idEstudiante, int pagina, int limite) {
        if (idEstudiante <= 0 || pagina <= 0 || limite <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "Los datos para consultar el historial no son válidos");
        }

        try {
            if (!existeEstudiante(idEstudiante)) {
                throw new ProcesosException("NO_ENCONTRADO", "No se encontró el estudiante");
            }

            return pagoDAO.consultarPorEstudiante(idEstudiante, pagina, limite);

        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el historial de pagos", e);
        }
    }

    public int contarHistorialPagos(int idEstudiante) {
        if (idEstudiante <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El id del estudiante no es válido");
        }

        try {
            if (!existeEstudiante(idEstudiante)) {
                throw new ProcesosException("NO_ENCONTRADO", "No se encontró el estudiante");
            }

            return pagoDAO.contarPorEstudiante(idEstudiante);

        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo obtener el total de pagos del estudiante", e);
        }
    }

    public boolean consultarSolvencia(int idEstudiante, int mensualidadesRequeridas) {
        if (idEstudiante <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El estudiante indicado no es válido");
        }
        try {
            boolean inscrito = false;
            for (var inscripcion : inscripcionDAO.consultar()) {
                if (inscripcion.getIdEstudiante() == idEstudiante) {
                    inscrito = true;
                    break;
                }
            }
            if (!inscrito) {
                throw new ProcesosException("CONFLICTO", "No se puede emitir solvencia: el estudiante no tiene inscripción registrada");
            }
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron verificar las inscripciones", e);
        }
        return consultarEstadoCuenta(idEstudiante, mensualidadesRequeridas) <= 0.01;
    }

    public Collection<Factura> consultarFacturas(int pagina, int limite) {
        if (pagina <= 0 || limite <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "La página y el límite deben ser mayores que cero");
        }

        try {
            return facturaDAO.consultar(pagina, limite);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar las facturas", e);
        }
    }

    public int contarFacturas() {
        try {
            return facturaDAO.contar();
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo obtener el total de facturas", e);
        }
    }
}
