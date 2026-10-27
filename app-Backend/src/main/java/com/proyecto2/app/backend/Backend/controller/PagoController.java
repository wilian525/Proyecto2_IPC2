/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.controller;

import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import com.proyecto2.app.backend.Backend.Model.Factura;
import com.proyecto2.app.backend.Backend.Model.Pago;
import com.proyecto2.app.backend.Backend.Model.CuotaMensual;
import com.proyecto2.app.backend.Backend.Reporte.GeneradorReportePdf;
import java.util.List;
import java.time.LocalDate;
import com.proyecto2.app.backend.Backend.procesos.EstudianteProcesos;
import com.proyecto2.app.backend.Backend.procesos.PagoProcesos;
import com.proyecto2.app.backend.Backend.servicio.PagoServicio;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.Collection;
import java.util.Map;

@Path("pagos")
@Produces(MediaType.APPLICATION_JSON)
public class PagoController {

    @GET
    @RolesAllowed("SECRETARIA")
    public Response consultarPagos(
            @DefaultValue("1") @QueryParam("pagina") int pagina,
            @DefaultValue("20") @QueryParam("limite") int limite) {

        PagoProcesos procesos = new PagoProcesos();

        try {
            Collection<Pago> pagos = procesos.consultarPagos(pagina, limite);
            int total = procesos.contarPagos();

            return Response.ok(
                    Map.of(
                            "datos", pagos,
                            "pagina", pagina,
                            "limite", limite,
                            "total", total
                    )
            ).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("SECRETARIA")
    public Response registrarPago(
            Pago pago,
            @Context HttpServletRequest request) {

        Object valorIdEmpleado = request.getAttribute("idEmpleado");

        if (!(valorIdEmpleado instanceof Integer)|| ((Integer) valorIdEmpleado) <= 0) {

            return Response.status(Response.Status.FORBIDDEN).entity(Map.of( "codigo", "NO_AUTORIZADO","mensaje", "No se pudo identificar a la Secretaria autenticada")).build();
        }

        if(pago==null) return responderError("DATOS_INVALIDOS","Los datos del pago son obligatorios");
        pago.setIdEmpleado((Integer) valorIdEmpleado);

        PagoServicio servicio = new PagoServicio();

        try {
            servicio.registrarPago(pago);
            return Response.status(Response.Status.CREATED) .entity(Map.of("mensaje", "Pago registrado correctamente" )).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("estudiantes/{idEstudiante}/historial")
    @RolesAllowed("SECRETARIA")
    public Response consultarHistorial(
            @PathParam("idEstudiante") int idEstudiante,
            @DefaultValue("1") @QueryParam("pagina") int pagina,
            @DefaultValue("20") @QueryParam("limite") int limite) {

        PagoProcesos procesos = new PagoProcesos();

        try {
            Collection<Pago> pagos= procesos.consultarHistorialPagos(idEstudiante, pagina, limite);
            int total = procesos.contarHistorialPagos(idEstudiante);

            return Response.ok(
                    Map.of( "datos", pagos, "pagina", pagina, "limite", limite,"total", total ) ).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("estudiantes/{idEstudiante}/estado-cuenta")
    @RolesAllowed("SECRETARIA")
    public Response consultarEstadoCuenta(
            @PathParam("idEstudiante") int idEstudiante,
            @DefaultValue("0") @QueryParam("mensualidades") int mensualidades) {

        PagoProcesos procesos = new PagoProcesos();

        try {
            double saldoPendiente= procesos.consultarEstadoCuenta(idEstudiante, mensualidades);

            return Response.ok(
                    Map.of("idEstudiante", idEstudiante, "saldoPendiente", saldoPendiente )).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("estudiantes/{idEstudiante}/solvencia")
    @RolesAllowed("SECRETARIA")
    public Response consultarSolvencia(
            @PathParam("idEstudiante") int idEstudiante,
            @DefaultValue("0") @QueryParam("mensualidades") int mensualidades) {

        PagoProcesos procesos = new PagoProcesos();

        try {
            boolean solvente
                    = procesos.consultarSolvencia(idEstudiante, mensualidades);

            return Response.ok(
                    Map.of(
                            "idEstudiante", idEstudiante,
                            "solvente", solvente
                    )
            ).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("mi-historial")
    @RolesAllowed("ESTUDIANTE")
    public Response consultarMiHistorial(
            @Context SecurityContext securityContext,
            @DefaultValue("1") @QueryParam("pagina") int pagina,
            @DefaultValue("20") @QueryParam("limite") int limite) {

        int idUsuario = obtenerIdUsuario(securityContext);

        if (idUsuario <= 0) {
            return respuestaUsuarioNoIdentificado();
        }

        try {
            EstudianteProcesos estudianteProcesos= new EstudianteProcesos();

            Estudiante estudiante= estudianteProcesos.consultarPerfil(idUsuario);

            PagoProcesos procesos = new PagoProcesos();

            Collection<Pago> pagos= procesos.consultarHistorialPagos(estudiante.getIdEstudiante(),  pagina,limite);

            int total= procesos.contarHistorialPagos(estudiante.getIdEstudiante());

            return Response.ok(
                    Map.of("datos", pagos,"pagina", pagina,"limite", limite, "total", total ) ).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("mi-estado-cuenta")
    @RolesAllowed("ESTUDIANTE")
    public Response consultarMiEstadoCuenta(
            @Context SecurityContext securityContext,
            @DefaultValue("0") @QueryParam("mensualidades") int mensualidades) {

        int idUsuario = obtenerIdUsuario(securityContext);

        if (idUsuario <= 0) {
            return respuestaUsuarioNoIdentificado();
        }

        try {
            EstudianteProcesos estudianteProcesos = new EstudianteProcesos();

            Estudiante estudiante = estudianteProcesos.consultarPerfil(idUsuario);

            PagoProcesos procesos = new PagoProcesos();

            double saldoPendiente= procesos.consultarEstadoCuenta( estudiante.getIdEstudiante(),mensualidades
 );

            Collection<CuotaMensual> cuotas = procesos.consultarCuotasPendientes(estudiante.getIdEstudiante());
            double totalCuotas = 0;
            for (CuotaMensual cuota : cuotas) totalCuotas += cuota.getMonto();
            double inscripcionPendiente = Math.max(0, saldoPendiente - totalCuotas);

            return Response.ok(Map.of(
                    "idEstudiante", estudiante.getIdEstudiante(),
                    "saldoPendiente", saldoPendiente,
                    "inscripcionPendiente", inscripcionPendiente,
                    "cuotasPendientes", cuotas
            )).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("facturas")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("SECRETARIA")
    public Response emitirFactura(Factura factura) {

        PagoServicio servicio = new PagoServicio();

        try {
            servicio.emitirFactura(factura);
            return Response.status(Response.Status.CREATED) .entity(Map.of("mensaje", "Factura emitida correctamente")).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("facturas")
    @RolesAllowed("SECRETARIA")
    public Response consultarFacturas(
            @DefaultValue("1") @QueryParam("pagina") int pagina,
            @DefaultValue("20") @QueryParam("limite") int limite) {

        PagoProcesos procesos = new PagoProcesos();

        try {
            Collection<Factura> facturas = procesos.consultarFacturas(pagina, limite);
            int total = procesos.contarFacturas();

            return Response.ok(
                    Map.of( "datos", facturas, "pagina", pagina,"limite", limite,"total", total) ).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("cuotas/{idEstudiante}")
    @RolesAllowed("SECRETARIA")
    public Response cuotas(@PathParam("idEstudiante") int idEstudiante) {
        try { return Response.ok(new PagoProcesos().consultarCuotas(idEstudiante)).build(); }
        catch(ProcesosException e) {return responderError(e.getCodigo(),e.getMessage());}
    }

    @POST
    @Path("cuotas")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("SECRETARIA")
    public Response registrarCuota(CuotaMensual cuota) {
        try {
            new PagoServicio().registrarCuota(cuota);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje","Mensualidad registrada para el período indicado")).build();
        } catch(ServicioException e) {return responderError(e.getCodigo(),e.getMessage());}
    }

    @POST
    @Path("conciliaciones")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("SECRETARIA")
    public Response conciliar(ConciliacionRequest datos) {
        if(datos==null) return responderError("DATOS_INVALIDOS","Selecciona un pago y su inscripción");
        try {
            new PagoServicio().conciliarPago(datos.idPago,datos.idInscripcion,datos.idCuota);
            return Response.ok(Map.of("mensaje","Pago histórico conciliado correctamente")).build();
        } catch(ServicioException e) {return responderError(e.getCodigo(),e.getMessage());}
    }

    public static class ConciliacionRequest {
        public int idPago;
        public int idInscripcion;
        public Integer idCuota;
    }

    @GET
    @Path("facturas/{idFactura}/pdf")
    @Produces({"application/pdf", MediaType.APPLICATION_JSON})
    @RolesAllowed("SECRETARIA")
    public Response facturaPdf(@PathParam("idFactura") int idFactura) {
        try {
            PagoProcesos procesos = new PagoProcesos();
            Factura factura=procesos.consultarFactura(idFactura);
            Pago pago=procesos.consultarPago(factura.getIdPago());
            String nombre=procesos.consultarNombreEstudiante(pago.getIdEstudiante());
            byte[] pdf=new GeneradorReportePdf().generarPdf(GeneradorReportePdf.FACTURA,List.of("factura"),
                    new java.util.HashMap<>(Map.of("NUMERO",factura.getNumeroFactura(),"ESTUDIANTE",nombre,"CONCEPTO",pago.getConcepto(),
                            "MONTO",String.format(java.util.Locale.US,"Q %,.2f",factura.getMontoTotal()),
                            "FECHA",factura.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            "ID_PAGO",String.valueOf(pago.getIdPago()))));
            return Response.ok(pdf,"application/pdf").header("Content-Disposition","attachment; filename=factura_"+idFactura+".pdf").build();
        } catch(ProcesosException e) {return responderError(e.getCodigo(),e.getMessage());}
    }

    @GET
    @Path("estudiantes/{idEstudiante}/solvencia/pdf")
    @Produces({"application/pdf", MediaType.APPLICATION_JSON})
    @RolesAllowed("SECRETARIA")
    public Response solvenciaPdf(@PathParam("idEstudiante") int idEstudiante) {
        try {
            PagoProcesos procesos=new PagoProcesos();
            if(!procesos.consultarSolvencia(idEstudiante,0))
                return responderError("CONFLICTO","El estudiante tiene obligaciones pendientes; no se puede emitir solvencia");
            String nombre=procesos.consultarNombreEstudiante(idEstudiante);
            byte[] pdf=new GeneradorReportePdf().generarPdf(GeneradorReportePdf.SOLVENCIA,List.of("solvencia"),
                    new java.util.HashMap<>(Map.of("ESTUDIANTE",nombre,"ID_ESTUDIANTE",String.valueOf(idEstudiante),
                           "FECHA",LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))));
            return Response.ok(pdf,"application/pdf").header("Content-Disposition","attachment; filename=solvencia_"+idEstudiante+".pdf").build();
        } catch(ProcesosException e) {return responderError(e.getCodigo(),e.getMessage());}
    }

    private int obtenerIdUsuario(SecurityContext securityContext) {
        if (securityContext == null|| securityContext.getUserPrincipal() == null) {
            return 0;
        }

        try {
            return Integer.parseInt(securityContext.getUserPrincipal().getName());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Response respuestaUsuarioNoIdentificado() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("codigo", "NO_AUTORIZADO","mensaje", "No se pudo identificar al usuario autenticado" )).build();
    }

    private Response responderError(String codigo, String mensaje) {

        if ("DATOS_INVALIDOS".equals(codigo)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of( "codigo", codigo, "mensaje", mensaje )) .build();
        }

        if ("NO_ENCONTRADO".equals(codigo)) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("codigo", codigo,"mensaje", mensaje)) .build();
        }

        if ("CONFLICTO".equals(codigo)) {
            return Response.status(Response.Status.CONFLICT).entity(Map.of("codigo", codigo,"mensaje", mensaje)) .build();
        }

        if ("NO_AUTORIZADO".equals(codigo)) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of( "codigo", codigo, "mensaje", mensaje)) .build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of( "codigo", codigo, "mensaje", mensaje )) .build();
    }
}
