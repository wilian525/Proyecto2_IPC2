/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.procesos;

import com.proyecto2.app.backend.Backend.Dao.EstudianteDao;
import com.proyecto2.app.backend.Backend.Dao.PagoDao;
import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
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
    private EstudianteDao estudianteDAO;

    public PagoProcesos() {
        pagoDAO = new PagoDao();
        estudianteDAO = new EstudianteDao();
    }

    public double consultarEstadoCuenta(int idEstudiante, int mensualidadesRequeridas) {
        if (idEstudiante <= 0 || mensualidadesRequeridas < 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "Los datos para consultar el estado de cuenta no son válidos");
        }

        try {
            if (!existeEstudiante(idEstudiante)) {
                throw new ProcesosException("NO_ENCONTRADO", "No se encontró el estudiante");
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
}
