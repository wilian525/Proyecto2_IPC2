/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import com.proyecto2.app.backend.Backend.procesos.EstudianteProcesos;
import com.proyecto2.app.backend.Backend.servicio.EstudianteServicio;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
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
/**
 *
 * @author wilian
 */
@Path("estudiantes")
@Produces(MediaType.APPLICATION_JSON)
public class EstudianteController {

    private final ObjectMapper objectMapper;

    public EstudianteController() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @GET
    public Response consultarEstudiantes(
            @DefaultValue("1") @QueryParam("pagina") int pagina,
            @DefaultValue("20") @QueryParam("limite") int limite) {

        EstudianteProcesos procesos = new EstudianteProcesos();

        try {
            Collection<Estudiante> estudiantes = procesos.consultarEstudiantes(pagina, limite);
            int total = procesos.contarEstudiantes();

            return Response.ok(
                    Map.of("datos", estudiantes, "pagina", pagina,"limite", limite,"total", total )).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("{idEstudiante}")
    public Response consultarEstudiante(
            @PathParam("idEstudiante") int idEstudiante) {

        EstudianteProcesos procesos = new EstudianteProcesos();

        try {
            Estudiante estudiante = procesos.consultarEstudiante(idEstudiante);
            return Response.ok(estudiante).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crearEstudiante(Map<String, Object> datos) {

        try {
            Estudiante estudiante = objectMapper.convertValue(datos.get("estudiante"), Estudiante.class);
            Usuario usuario = objectMapper.convertValue(datos.get("usuario"), Usuario.class);

            EstudianteServicio servicio = new EstudianteServicio();

            servicio.crearEstudiante(estudiante, usuario);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("mensaje", "Estudiante creado correctamente" )).build();

        } catch (IllegalArgumentException e) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("codigo", "DATOS_INVALIDOS", "mensaje", "El formato de los datos enviados no es válido" )) .build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("{idEstudiante}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modificarEstudiante(@PathParam("idEstudiante") int idEstudiante, Estudiante estudiante) {

        estudiante.setIdEstudiante(idEstudiante);
        EstudianteServicio servicio = new EstudianteServicio();

        try {
            servicio.modificarEstudiante(estudiante);

            return Response.ok(
                    Map.of("mensaje", "Estudiante modificado correctamente" ) ).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("{idEstudiante}/estado")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cambiarEstado(@PathParam("idEstudiante") int idEstudiante, Estudiante estudiante) {

        EstudianteServicio servicio = new EstudianteServicio();

        try {
            servicio.cambiarEstadoEstudiante(idEstudiante, estudiante.getEstado());
            return Response.ok(Map.of("mensaje", "Estado del estudiante actualizado correctamente" )).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("{idEstudiante}/expediente")
    public Response consultarExpediente(@PathParam("idEstudiante") int idEstudiante) {
        EstudianteProcesos procesos = new EstudianteProcesos();

        try {
            return Response.ok(
                    procesos.consultarExpediente(idEstudiante)).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("perfil/{idUsuario}")
    public Response consultarPerfil(@PathParam("idUsuario") int idUsuario) {

        EstudianteProcesos procesos = new EstudianteProcesos();

        try {
            return Response.ok(procesos.consultarPerfil(idUsuario) ).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    private Response responderError(String codigo, String mensaje) {

        if ("DATOS_INVALIDOS".equals(codigo)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("codigo", codigo,"mensaje", mensaje)).build();
        }

        if ("NO_ENCONTRADO".equals(codigo)) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("codigo", codigo,"mensaje", mensaje)).build();
        }

        if ("CONFLICTO".equals(codigo)) {
            return Response.status(Response.Status.CONFLICT).entity(Map.of("codigo", codigo,"mensaje", mensaje)).build();
        }

        if ("NO_AUTORIZADO".equals(codigo)) {
            return Response.status(Response.Status.FORBIDDEN) .entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("codigo", codigo,"mensaje", mensaje)) .build();
    }
}
