/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.controller;

import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Model.ReporteHistorialPago;
import com.proyecto2.app.backend.Backend.Model.ReporteIngresos;
import com.proyecto2.app.backend.Backend.Model.ReportePrestamoMora;
import com.proyecto2.app.backend.Backend.Model.ReporteRendimiento;
import com.proyecto2.app.backend.Backend.Model.ReporteSolventeMoroso;
import com.proyecto2.app.backend.Backend.Reporte.GeneradorReportePdf;
import com.proyecto2.app.backend.Backend.procesos.ReporteProcesos;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Map;
import com.proyecto2.app.backend.Backend.Dao.cicloDao;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import com.proyecto2.app.backend.Backend.procesos.AcademicoProcesos;

@Path("reportes")
@Produces(MediaType.APPLICATION_JSON)
public class ReporteController {
    
   @GET
    @Path("solventes-morosos")
    @RolesAllowed({"ADMIN", "SECRETARIA"})
    public Response solventesMorosos(@Context SecurityContext seguridad, @QueryParam("grado") int idGrado, @QueryParam("seccion") int idSeccion, @QueryParam("ciclo") int idCiclo, @DefaultValue("0") @QueryParam("mensualidades") int mensualidades) {
        try {
            verificarCicloSecretaria(seguridad,idCiclo);
            return Response.ok(new ReporteProcesos().generarReporteSolventesMorosos(idGrado, idSeccion, idCiclo, mensualidades)).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("solventes-morosos/pdf")
    @Produces({"application/pdf", MediaType.APPLICATION_JSON})
    @RolesAllowed({"ADMIN", "SECRETARIA"})
    public Response solventesMorososPdf(@Context SecurityContext seguridad, @QueryParam("grado") int idGrado, @QueryParam("seccion") int idSeccion, @QueryParam("ciclo") int idCiclo, @DefaultValue("0") @QueryParam("mensualidades") int mensualidades) {
        try {
            verificarCicloSecretaria(seguridad,idCiclo);
            Collection<ReporteSolventeMoroso> datos = new ReporteProcesos().generarReporteSolventesMorosos(idGrado, idSeccion, idCiclo, mensualidades);
            byte[] pdf = new GeneradorReportePdf().generarPdf(GeneradorReportePdf.SOLVENTES_MOROSOS, datos, new java.util.HashMap<>(Map.of()));
            return responderPdf(pdf, "solventes_morosos.pdf");
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("rendimiento")
    @RolesAllowed("ADMIN")
    public Response rendimiento(@QueryParam("grado") int idGrado, @QueryParam("seccion") int idSeccion, @QueryParam("ciclo") int idCiclo) {
        try {
            return Response.ok(new ReporteProcesos().generarReporteRendimiento(idGrado, idSeccion, idCiclo)).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("rendimiento/pdf")
    @Produces({"application/pdf", MediaType.APPLICATION_JSON})
    @RolesAllowed("ADMIN")
    public Response rendimientoPdf(@QueryParam("grado") int idGrado, @QueryParam("seccion") int idSeccion, @QueryParam("ciclo") int idCiclo) {
        try {
            ReporteRendimiento datos = new ReporteProcesos().generarReporteRendimiento(idGrado, idSeccion, idCiclo);
            byte[] pdf = new GeneradorReportePdf().generarPdf(GeneradorReportePdf.RENDIMIENTO, datos, new java.util.HashMap<>(Map.of()));
            return responderPdf(pdf, "rendimiento_academico.pdf");
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("mi-rendimiento/{idAsignacion}")
    @RolesAllowed("MAESTRO")
    public Response miRendimiento(@PathParam("idAsignacion") int idAsignacion, @Context HttpServletRequest request) {
        try {
            int idEmpleado = obtenerIdEmpleado(request);
            return Response.ok(new AcademicoProcesos().consultarRendimientoMaestro(idAsignacion, idEmpleado)).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("mi-rendimiento/{idAsignacion}/pdf")
    @Produces({"application/pdf", MediaType.APPLICATION_JSON})
    @RolesAllowed("MAESTRO")
    public Response miRendimientoPdf(@PathParam("idAsignacion") int idAsignacion, @Context HttpServletRequest request) {
        try {
            int idEmpleado = obtenerIdEmpleado(request);
            ReporteRendimiento datos = new AcademicoProcesos().consultarRendimientoMaestro(idAsignacion, idEmpleado);
            byte[] pdf = new GeneradorReportePdf().generarPdf(GeneradorReportePdf.RENDIMIENTO, datos, new java.util.HashMap<>(Map.of()));
            return responderPdf(pdf, "mi_rendimiento.pdf");
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    private int obtenerIdEmpleado(HttpServletRequest request) {
        Object id = request.getAttribute("idEmpleado");
        if (!(id instanceof Integer) || (Integer) id <= 0) {
            throw new ProcesosException("NO_AUTORIZADO", "No se identificó al maestro autenticado");
        }
        return (Integer) id;
    }

    @GET
    @Path("historial-pagos/{idEstudiante}")
    @RolesAllowed({"ADMIN", "SECRETARIA"})
    public Response historialPagos(@PathParam("idEstudiante") int idEstudiante, @QueryParam("fechaInicio") String fechaInicio, @QueryParam("fechaFin") String fechaFin, @DefaultValue("0") @QueryParam("mensualidades") int mensualidades) {
        try {
            return Response.ok(new ReporteProcesos().generarReporteHistorialPagos(idEstudiante, convertirFecha(fechaInicio), convertirFecha(fechaFin), mensualidades)).build();
        } catch (DateTimeParseException e) {
            return error("DATOS_INVALIDOS", "Las fechas deben tener formato yyyy-MM-dd");
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("historial-pagos/{idEstudiante}/pdf")
    @Produces({"application/pdf", MediaType.APPLICATION_JSON})
    @RolesAllowed({"ADMIN", "SECRETARIA"})
    public Response historialPagosPdf(@PathParam("idEstudiante") int idEstudiante, @QueryParam("fechaInicio") String fechaInicio, @QueryParam("fechaFin") String fechaFin, @DefaultValue("0") @QueryParam("mensualidades") int mensualidades) {
        try {
            LocalDate inicio = convertirFecha(fechaInicio);
            LocalDate fin = convertirFecha(fechaFin);
            ReporteHistorialPago datos = new ReporteProcesos().generarReporteHistorialPagos(idEstudiante, inicio, fin, mensualidades);
            byte[] pdf = new GeneradorReportePdf().generarPdf(GeneradorReportePdf.HISTORIAL_PAGOS, datos, new java.util.HashMap<>(Map.of()));
            return responderPdf(pdf, "historial_pagos_" + idEstudiante + ".pdf");
        } catch (DateTimeParseException e) {
            return error("DATOS_INVALIDOS", "Las fechas deben tener formato yyyy-MM-dd");
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("ingresos")
    @RolesAllowed("ADMIN")
    public Response ingresos(@QueryParam("fechaInicio") String fechaInicio, @QueryParam("fechaFin") String fechaFin) {
        try {
            return Response.ok(new ReporteProcesos().generarReporteIngresos(convertirFecha(fechaInicio), convertirFecha(fechaFin))).build();
        } catch (DateTimeParseException e) {
            return error("DATOS_INVALIDOS", "Las fechas deben tener formato yyyy-MM-dd");
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("ingresos/pdf")
    @Produces({"application/pdf", MediaType.APPLICATION_JSON})
    @RolesAllowed("ADMIN")
    public Response ingresosPdf(@QueryParam("fechaInicio") String fechaInicio, @QueryParam("fechaFin") String fechaFin) {
        try {
            LocalDate inicio = convertirFecha(fechaInicio);
            LocalDate fin = convertirFecha(fechaFin);
            ReporteIngresos datos = new ReporteProcesos().generarReporteIngresos(inicio, fin);
            byte[] pdf = new GeneradorReportePdf().generarPdf(GeneradorReportePdf.INGRESOS, datos, new java.util.HashMap<>(Map.of()));
            return responderPdf(pdf, "reporte_ingresos.pdf");
        } catch (DateTimeParseException e) {
            return error("DATOS_INVALIDOS", "Las fechas deben tener formato yyyy-MM-dd");
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("prestamos-mora")
    @RolesAllowed({"ADMIN", "BIBLIOTECARIO"})
    public Response prestamosMora() {
        try {
            return Response.ok(new ReporteProcesos().generarReportePrestamosMora()).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("prestamos-mora/pdf")
   @Produces({"application/pdf", MediaType.APPLICATION_JSON})
    @RolesAllowed({"ADMIN", "BIBLIOTECARIO"})
    public Response prestamosMoraPdf() {
        try {
            Collection<ReportePrestamoMora> datos = new ReporteProcesos().generarReportePrestamosMora();
            byte[] pdf = new GeneradorReportePdf().generarPdf(GeneradorReportePdf.PRESTAMOS_MORA, datos, new java.util.HashMap<>(Map.of()));
            return responderPdf(pdf, "prestamos_mora.pdf");
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    // Consultas históricas permitidas; activar un ciclo solo es necesario para operaciones de registro.
    private void verificarCicloSecretaria(SecurityContext seguridad, int idCiclo) {
        if (seguridad==null || !seguridad.isUserInRole("SECRETARIA")) return;
        for(var ciclo:new cicloDao().consultar()) {
            if(ciclo.getIdCiclo()==idCiclo) return;
        }
        throw new ProcesosException("CONFLICTO","No se encontró el ciclo escolar del reporte");
    }

    private LocalDate convertirFecha(String fecha) {
        return fecha == null || fecha.isBlank() ? null : LocalDate.parse(fecha);
    }

    private Response responderPdf(byte[] pdf, String nombreArchivo) {
        return Response.ok(pdf, "application/pdf").header("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"").build();
    }

    private Response error(String codigo, String mensaje) {
    Response.Status estado = "DATOS_INVALIDOS".equals(codigo) ? Response.Status.BAD_REQUEST
            : "NO_ENCONTRADO".equals(codigo) ? Response.Status.NOT_FOUND
            : "CONFLICTO".equals(codigo) ? Response.Status.CONFLICT
            : "NO_AUTORIZADO".equals(codigo) ? Response.Status.FORBIDDEN
            : Response.Status.INTERNAL_SERVER_ERROR;

    return Response.status(estado)
            .type(MediaType.APPLICATION_JSON)
            .entity(Map.of("codigo", codigo, "mensaje", mensaje))
            .build();
}
}
