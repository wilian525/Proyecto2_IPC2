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
            @DefaultValue("-1") @QueryParam("mensualidades") int mensualidades) {

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
            @DefaultValue("-1") @QueryParam("mensualidades") int mensualidades) {

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
            @DefaultValue("-1") @QueryParam("mensualidades") int mensualidades) {

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

            return Response.ok(
                    Map.of( "idEstudiante", estudiante.getIdEstudiante(), "saldoPendiente", saldoPendiente) ).build();

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
