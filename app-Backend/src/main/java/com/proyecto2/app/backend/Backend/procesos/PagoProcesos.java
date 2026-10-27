/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.procesos;

import com.proyecto2.app.backend.Backend.Dao.EstudianteDao;
import com.proyecto2.app.backend.Backend.Dao.CuotaMensualDao;
import com.proyecto2.app.backend.Backend.Dao.cicloDao;
import com.proyecto2.app.backend.Backend.Model.CuotaMensual;
import com.proyecto2.app.backend.Backend.Model.Ciclo;
import com.proyecto2.app.backend.Backend.Model.Inscripcion;
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
    private CuotaMensualDao cuotaDAO;
    private cicloDao cicloDAO;
    private InscripcionDao inscripcionDAO;
    private EstudianteDao estudianteDAO;
    private FacturaDao facturaDAO;

    public PagoProcesos() {
        pagoDAO = new PagoDao();
        cuotaDAO = new CuotaMensualDao();
        cicloDAO = new cicloDao();
        inscripcionDAO = new InscripcionDao();
        estudianteDAO = new EstudianteDao();
        facturaDAO = new FacturaDao();
    }

    // Se conserva la firma para los endpoints existentes, pero el cliente no determina cuotas.
    public double consultarEstadoCuenta(int idEstudiante, int mensualidadesIgnoradas) {
        if (idEstudiante<=0) throw new ProcesosException("DATOS_INVALIDOS","El estudiante no es válido");
        if(!existeEstudiante(idEstudiante)) throw new ProcesosException("NO_ENCONTRADO","No se encontró al estudiante");
        try {
            for(Ciclo ciclo:cicloDAO.consultar()) {
                if("ACTIVO".equalsIgnoreCase(ciclo.getEstado()))
                    return consultarEstadoCuentaPorCiclo(idEstudiante,ciclo.getIdCiclo());
            }
            return 0;
        } catch(daoException e) {throw new ProcesosException("BD_ERROR","No se pudo consultar el estado de cuenta",e);}
    }

    public double consultarEstadoCuentaPorCiclo(int idEstudiante, int idCiclo) {
        if (idEstudiante <= 0 || idCiclo <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "Estudiante o ciclo no válidos");
        }
        try {
            return cuotaDAO.saldoPendiente(idEstudiante, idCiclo, PagoServicio.PRECIO_INSCRIPCION);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo calcular el saldo", e);
        }
    }

    // Solo cuotas cuyo mes ya terminó; conserva el saldo total para el estado de cuenta.
    public double consultarSaldoVencido(int idEstudiante, int idCiclo) {
        if (idEstudiante <= 0 || idCiclo <= 0)
            throw new ProcesosException("DATOS_INVALIDOS", "Estudiante o ciclo no válidos");
        try {
            return cuotaDAO.saldoVencido(idEstudiante, idCiclo, PagoServicio.PRECIO_INSCRIPCION);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el saldo vencido", e);
        }
    }

    public double consultarSaldoVencido(int idEstudiante) {
        for (Ciclo ciclo : cicloDAO.consultar()) {
            if ("ACTIVO".equalsIgnoreCase(ciclo.getEstado()))
                return consultarSaldoVencido(idEstudiante, ciclo.getIdCiclo());
        }
        return 0;
    }

    public Collection<CuotaMensual> consultarCuotasPendientes(int idEstudiante) {
        Collection<CuotaMensual> pendientes = new ArrayList<>();
        int cicloActivo = 0;
        for (Ciclo ciclo : cicloDAO.consultar()) {
            if ("ACTIVO".equalsIgnoreCase(ciclo.getEstado())) {
                cicloActivo = ciclo.getIdCiclo();
                break;
            }
        }
        if (cicloActivo == 0) return pendientes;

        int idInscripcion = 0;
        for (Inscripcion inscripcion : inscripcionDAO.consultar()) {
            if (inscripcion.getIdEstudiante() == idEstudiante && inscripcion.getIdCiclo() == cicloActivo) {
                idInscripcion = inscripcion.getIdInscripcion();
                break;
            }
        }
        if (idInscripcion == 0) return pendientes;

        for (CuotaMensual cuota : cuotaDAO.consultar(idEstudiante)) {
            if (cuota.getIdInscripcion() == idInscripcion && cuota.getIdPago() == null) {
                pendientes.add(cuota);
            }
        }
        return pendientes;
    }

    public Collection<CuotaMensual> consultarCuotas(int idEstudiante) {
        if(idEstudiante<=0) throw new ProcesosException("DATOS_INVALIDOS","Selecciona un estudiante");
        return cuotaDAO.consultar(idEstudiante);
    }

    public Factura consultarFactura(int idFactura) {
        if(idFactura<=0) throw new ProcesosException("DATOS_INVALIDOS","La factura indicada no es válida");
        for(Factura factura:facturaDAO.consultar()) if(factura.getIdFactura()==idFactura) return factura;
        throw new ProcesosException("NO_ENCONTRADO","No se encontró la factura");
    }

    public Pago consultarPago(int idPago) {
        for(Pago p:pagoDAO.consultar()) if(p.getIdPago()==idPago) return p;
        throw new ProcesosException("NO_ENCONTRADO","No se encontró el pago");
    }

    public String consultarNombreEstudiante(int idEstudiante) {
        for(Estudiante e:estudianteDAO.consultar())
            if(e.getIdEstudiante()==idEstudiante) return e.getNombre()+" "+e.getApellido();
        throw new ProcesosException("NO_ENCONTRADO","No se encontró el estudiante");
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

    public boolean consultarSolvencia(int idEstudiante, int mensualidadesIgnoradas) {
        if (idEstudiante <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El estudiante no es válido");
        }
        try {
            for (Ciclo ciclo : cicloDAO.consultar()) {
                if (!"ACTIVO".equalsIgnoreCase(ciclo.getEstado())) continue;
                for (Inscripcion inscripcion : inscripcionDAO.consultar()) {
                    if (inscripcion.getIdEstudiante() == idEstudiante
                            && inscripcion.getIdCiclo() == ciclo.getIdCiclo()) {
                        return consultarSaldoVencido(idEstudiante, ciclo.getIdCiclo()) <= 0.01;
                    }
                }
            }
            throw new ProcesosException("CONFLICTO", "No se puede emitir solvencia sin inscripción en el ciclo activo");
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar la solvencia", e);
        }
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
