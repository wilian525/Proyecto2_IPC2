/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.controller;

import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import com.proyecto2.app.backend.Backend.Model.Libro;
import com.proyecto2.app.backend.Backend.Model.Prestamo;
import com.proyecto2.app.backend.Backend.procesos.BibliotecaProcesos;
import com.proyecto2.app.backend.Backend.procesos.EstudianteProcesos;
import com.proyecto2.app.backend.Backend.servicio.BibliotecaServicio;
import jakarta.annotation.security.RolesAllowed;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.Collection;
import java.util.Map;

@Path("biblioteca")
@Produces(MediaType.APPLICATION_JSON)
public class BibliotecaController {
    
    @GET
    @Path("libros")
    @RolesAllowed("BIBLIOTECARIO")
    public Response consultarLibros(@DefaultValue("1") @QueryParam("pagina") int pagina, @DefaultValue("20") @QueryParam("limite") int limite) {
        BibliotecaProcesos procesos = new BibliotecaProcesos();
        try {
            Collection<Libro> libros = procesos.consultarLibros(pagina, limite);
            return Response.ok(Map.of("datos", libros, "pagina", pagina, "limite", limite, "total", procesos.contarLibros())).build();
        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("libros/{idLibro}")
    @RolesAllowed("BIBLIOTECARIO")
    public Response consultarLibro(@PathParam("idLibro") int idLibro) {
        try {
            return Response.ok(new BibliotecaProcesos().consultarLibro(idLibro)).build();
        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("libros")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("BIBLIOTECARIO")
    public Response crearLibro(Libro libro) {
        try {
            new BibliotecaServicio().crearLibro(libro);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Libro creado correctamente")).build();
        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("libros/{idLibro}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("BIBLIOTECARIO")
    public Response modificarLibro(@PathParam("idLibro") int idLibro, Libro libro) {
        libro.setIdLibro(idLibro);
        try {
            new BibliotecaServicio().modificarLibro(libro);
            return Response.ok(Map.of("mensaje", "Libro modificado correctamente")).build();
        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @DELETE
    @Path("libros/{idLibro}")
    @RolesAllowed("BIBLIOTECARIO")
    public Response eliminarLibro(@PathParam("idLibro") int idLibro) {
        try {
            new BibliotecaServicio().eliminarLibro(idLibro);
            return Response.ok(Map.of("mensaje", "Libro eliminado correctamente")).build();
        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("catalogo")
    @RolesAllowed({"BIBLIOTECARIO", "ESTUDIANTE"})
    public Response consultarCatalogo() {
        try {
            return Response.ok(new BibliotecaProcesos().consultarCatalogo()).build();
        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("prestamos")
    @RolesAllowed("BIBLIOTECARIO")
    public Response consultarPrestamos(@DefaultValue("1") @QueryParam("pagina") int pagina, @DefaultValue("20") @QueryParam("limite") int limite) {
        BibliotecaProcesos procesos = new BibliotecaProcesos();
        try {
            Collection<Prestamo> prestamos = procesos.consultarPrestamos(pagina, limite);
            return Response.ok(Map.of("datos", prestamos, "pagina", pagina, "limite", limite, "total", procesos.contarPrestamos())).build();
        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("prestamos")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("BIBLIOTECARIO")
    public Response registrarPrestamo(Prestamo prestamo) {
        try {
            new BibliotecaServicio().registrarPrestamo(prestamo);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Préstamo registrado correctamente")).build();
        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("prestamos/{idPrestamo}/devolucion")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("BIBLIOTECARIO")
    public Response registrarDevolucion(@PathParam("idPrestamo") int idPrestamo, Prestamo prestamo) {
        try {
            new BibliotecaServicio().registrarDevolucion(idPrestamo, prestamo.getFechaDevolucion());
            return Response.ok(Map.of("mensaje", "Devolución registrada correctamente")).build();
        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("atrasos")
    @RolesAllowed("BIBLIOTECARIO")
    public Response consultarAtrasos(@DefaultValue("1") @QueryParam("pagina") int pagina, @DefaultValue("20") @QueryParam("limite") int limite) {
        BibliotecaProcesos procesos = new BibliotecaProcesos();
        try {
            Collection<Prestamo> atrasos = procesos.consultarAtrasos(pagina, limite);
            return Response.ok(Map.of("datos", atrasos, "pagina", pagina, "limite", limite, "total", procesos.contarAtrasos())).build();
        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("usuarios/{idUsuario}/pendientes")
    @RolesAllowed("BIBLIOTECARIO")
    public Response consultarPendientes(@PathParam("idUsuario") int idUsuario) {
        try {
            return Response.ok(new BibliotecaProcesos().consultarLibrosPendientes(idUsuario)).build();
        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("estudiantes/{idEstudiante}/multas")
    @RolesAllowed("BIBLIOTECARIO")
    public Response consultarMultas(@PathParam("idEstudiante") int idEstudiante) {
        try {
            return Response.ok(new BibliotecaProcesos().consultarMultasEstudiante(idEstudiante)).build();
        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("mis-pendientes")
    @RolesAllowed("ESTUDIANTE")
    public Response consultarMisPendientes(@Context SecurityContext securityContext) {
        try {
            int idUsuario = obtenerIdUsuario(securityContext);
            return Response.ok(new BibliotecaProcesos().consultarLibrosPendientes(idUsuario)).build();
        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("mis-multas")
    @RolesAllowed("ESTUDIANTE")
    public Response consultarMisMultas(@Context SecurityContext securityContext) {
        try {
            int idUsuario = obtenerIdUsuario(securityContext);
            Estudiante estudiante = new EstudianteProcesos().consultarPerfil(idUsuario);
            return Response.ok(new BibliotecaProcesos().consultarMultasEstudiante(estudiante.getIdEstudiante())).build();
        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    private int obtenerIdUsuario(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) throw new ProcesosException("NO_AUTORIZADO", "No se pudo identificar al usuario autenticado");
        try {
            int idUsuario = Integer.parseInt(securityContext.getUserPrincipal().getName());
            if (idUsuario <= 0) throw new ProcesosException("NO_AUTORIZADO", "No se pudo identificar al usuario autenticado");
            return idUsuario;
        } catch (NumberFormatException e) {
            throw new ProcesosException("NO_AUTORIZADO", "No se pudo identificar al usuario autenticado");
        }
    }

    private Response responderError(String codigo, String mensaje) {
        if ("DATOS_INVALIDOS".equals(codigo)){
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
        }
        if ("NO_ENCONTRADO".equals(codigo)) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
        }
        if ("CONFLICTO".equals(codigo)) {
            return Response.status(Response.Status.CONFLICT).entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
        }
        if ("NO_AUTORIZADO".equals(codigo)) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
    }
}
