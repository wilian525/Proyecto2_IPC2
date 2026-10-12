/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.controller;

import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Model.Factura;
import com.proyecto2.app.backend.Backend.Model.Pago;
import com.proyecto2.app.backend.Backend.procesos.PagoProcesos;
import com.proyecto2.app.backend.Backend.servicio.PagoServicio;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;

@Path("pagos")
@Produces(MediaType.APPLICATION_JSON)
public class PagoController {
    
    @GET
    public Response consultarPagos(
            @DefaultValue("1") @QueryParam("pagina") int pagina,
            @DefaultValue("20") @QueryParam("limite") int limite) {

        PagoProcesos procesos = new PagoProcesos();

        try {
            Collection<Pago> pagos = procesos.consultarPagos(pagina, limite);
            int total = procesos.contarPagos();

            return Response.ok(Map.of("datos", pagos,"pagina", pagina,"limite", limite,"total", total)).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarPago(Pago pago) {

        PagoServicio servicio = new PagoServicio();

        try {
            servicio.registrarPago(pago);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("mensaje", "Pago registrado correctamente")).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("estudiantes/{idEstudiante}/historial")
    public Response consultarHistorial(
            @PathParam("idEstudiante") int idEstudiante,
            @DefaultValue("1") @QueryParam("pagina") int pagina,
            @DefaultValue("20") @QueryParam("limite") int limite) {

        PagoProcesos procesos = new PagoProcesos();

        try {
            Collection<Pago> pagos = procesos.consultarHistorialPagos(idEstudiante, pagina, limite);
            int total = procesos.contarHistorialPagos(idEstudiante);

            return Response.ok(Map.of("datos", pagos, "pagina", pagina,"limite", limite,"total", total)).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("estudiantes/{idEstudiante}/estado-cuenta")
    public Response consultarEstadoCuenta(
            @PathParam("idEstudiante") int idEstudiante,
            @DefaultValue("-1") @QueryParam("mensualidades") int mensualidades) {

        PagoProcesos procesos = new PagoProcesos();

        try {
            double saldoPendiente = procesos.consultarEstadoCuenta(idEstudiante, mensualidades);

            return Response.ok(Map.of("idEstudiante", idEstudiante,"saldoPendiente", saldoPendiente)).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("estudiantes/{idEstudiante}/solvencia")
    public Response consultarSolvencia(
            @PathParam("idEstudiante") int idEstudiante,
            @DefaultValue("-1") @QueryParam("mensualidades") int mensualidades) {

        PagoProcesos procesos = new PagoProcesos();

        try {
            boolean solvente = procesos.consultarSolvencia(idEstudiante, mensualidades);

            return Response.ok(Map.of( "idEstudiante", idEstudiante,"solvente", solvente )).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("facturas")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response emitirFactura(Factura factura) {

        PagoServicio servicio = new PagoServicio();

        try {
            servicio.emitirFactura(factura);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("mensaje", "Factura emitida correctamente")).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("facturas")
    public Response consultarFacturas(
            @DefaultValue("1") @QueryParam("pagina") int pagina,
            @DefaultValue("20") @QueryParam("limite") int limite) {

        PagoProcesos procesos = new PagoProcesos();

        try {
            Collection<Factura> facturas = procesos.consultarFacturas(pagina, limite);
            int total = procesos.contarFacturas();

            return Response.ok(Map.of("datos", facturas,"pagina", pagina,"limite", limite, "total", total)).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    private Response responderError(String codigo, String mensaje) {

        if ("DATOS_INVALIDOS".equals(codigo)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("codigo", codigo, "mensaje", mensaje)) .build();
        }

        if ("NO_ENCONTRADO".equals(codigo)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("codigo", codigo, "mensaje", mensaje)) .build();
        }

        if ("CONFLICTO".equals(codigo)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
        }

        if ("NO_AUTORIZADO".equals(codigo)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
    }
}
