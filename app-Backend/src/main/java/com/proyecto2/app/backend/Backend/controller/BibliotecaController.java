/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.controller;

import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Model.Libro;
import com.proyecto2.app.backend.Backend.Model.Prestamo;
import com.proyecto2.app.backend.Backend.procesos.BibliotecaProcesos;
import com.proyecto2.app.backend.Backend.servicio.BibliotecaServicio;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;

@Path("biblioteca")
@Produces(MediaType.APPLICATION_JSON)
public class BibliotecaController {
    
    @GET
    @Path("libros")
    public Response consultarLibros(
            @DefaultValue("1") @QueryParam("pagina") int pagina,
            @DefaultValue("20") @QueryParam("limite") int limite) {

        BibliotecaProcesos procesos = new BibliotecaProcesos();

        try {
            Collection<Libro> libros = procesos.consultarLibros(pagina, limite);
            int total = procesos.contarLibros();

            return Response.ok(Map.of("datos", libros, "pagina", pagina, "limite", limite,"total", total)).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("libros/{idLibro}")
    public Response consultarLibro(@PathParam("idLibro") int idLibro) {
        BibliotecaProcesos procesos = new BibliotecaProcesos();

        try {
            return Response.ok(procesos.consultarLibro(idLibro)).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("libros")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crearLibro(Libro libro) {
        BibliotecaServicio servicio = new BibliotecaServicio();

        try {
            servicio.crearLibro(libro);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("mensaje", "Libro creado correctamente")).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("libros/{idLibro}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modificarLibro(@PathParam("idLibro") int idLibro,Libro libro) {

        libro.setIdLibro(idLibro);
        BibliotecaServicio servicio = new BibliotecaServicio();

        try {
            servicio.modificarLibro(libro);

            return Response.ok( Map.of("mensaje", "Libro modificado correctamente") ).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @DELETE
    @Path("libros/{idLibro}")
    public Response eliminarLibro(@PathParam("idLibro") int idLibro) {

        BibliotecaServicio servicio = new BibliotecaServicio();

        try {
            servicio.eliminarLibro(idLibro);

            return Response.ok( Map.of("mensaje", "Libro eliminado correctamente")).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("catalogo")
    public Response consultarCatalogo() {

        BibliotecaProcesos procesos = new BibliotecaProcesos();

        try {
            return Response.ok(procesos.consultarCatalogo()).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("prestamos")
    public Response consultarPrestamos(
            @DefaultValue("1") @QueryParam("pagina") int pagina,
            @DefaultValue("20") @QueryParam("limite") int limite) {

        BibliotecaProcesos procesos = new BibliotecaProcesos();

        try {
            Collection<Prestamo> prestamos = procesos.consultarPrestamos(pagina, limite);
            int total = procesos.contarPrestamos();

            return Response.ok(Map.of("datos", prestamos, "pagina", pagina,"limite", limite,"total", total)).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("prestamos")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarPrestamo(Prestamo prestamo) {

        BibliotecaServicio servicio = new BibliotecaServicio();

        try {
            servicio.registrarPrestamo(prestamo);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("mensaje", "Préstamo registrado correctamente")) .build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("prestamos/{idPrestamo}/devolucion")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarDevolucion(@PathParam("idPrestamo") int idPrestamo, Prestamo prestamo) {

        BibliotecaServicio servicio = new BibliotecaServicio();

        try {
            servicio.registrarDevolucion(idPrestamo, prestamo.getFechaDevolucion());

            return Response.ok(Map.of("mensaje", "Devolución registrada correctamente")).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("atrasos")
    public Response consultarAtrasos(
            @DefaultValue("1") @QueryParam("pagina") int pagina,
            @DefaultValue("20") @QueryParam("limite") int limite) {

        BibliotecaProcesos procesos = new BibliotecaProcesos();

        try {
            Collection<Prestamo> atrasos = procesos.consultarAtrasos(pagina, limite);
            int total = procesos.contarAtrasos();

            return Response.ok(Map.of("datos", atrasos,"pagina", pagina, "limite", limite,"total", total)).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("usuarios/{idUsuario}/pendientes")
    public Response consultarPendientes(@PathParam("idUsuario") int idUsuario) {

        BibliotecaProcesos procesos = new BibliotecaProcesos();

        try {
            return Response.ok(
                    procesos.consultarLibrosPendientes(idUsuario)
            ).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("estudiantes/{idEstudiante}/multas")
    public Response consultarMultas(@PathParam("idEstudiante") int idEstudiante) {

        BibliotecaProcesos procesos = new BibliotecaProcesos();

        try {
            return Response.ok( procesos.consultarMultasEstudiante(idEstudiante)).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    private Response responderError(String codigo, String mensaje) {

        if ("DATOS_INVALIDOS".equals(codigo)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("codigo", codigo, "mensaje", mensaje))
                    .build();
        }

        if ("NO_ENCONTRADO".equals(codigo)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("codigo", codigo, "mensaje", mensaje))
                    .build();
        }

        if ("CONFLICTO".equals(codigo)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("codigo", codigo, "mensaje", mensaje))
                    .build();
        }

        if ("NO_AUTORIZADO".equals(codigo)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("codigo", codigo, "mensaje", mensaje))
                    .build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("codigo", codigo, "mensaje", mensaje))
                .build();
    }
}
