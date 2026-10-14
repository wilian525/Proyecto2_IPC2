/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Model.Empleado;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import com.proyecto2.app.backend.Backend.procesos.EmpleadoProcesos;
import com.proyecto2.app.backend.Backend.servicio.EmpleadoServicio;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author wilian
 */
@Path("empleados")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "SUPER_ADMIN"})
public class EmpleadoController {
    
      private final ObjectMapper objectMapper;

    public EmpleadoController() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @GET
    public Response consultarEmpleados(
            @DefaultValue("1") @QueryParam("pagina") int pagina,
            @DefaultValue("20") @QueryParam("limite") int limite) {

        EmpleadoProcesos procesos = new EmpleadoProcesos();

        try {
            Collection<Empleado> empleados = procesos.consultarEmpleado(pagina, limite);
            int total = procesos.contarEmpleados();

            return Response.ok(
                    Map.of(
                            "datos", empleados,
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
    public Response crearEmpleado(Map<String, Object> datos) {

        try {
            Empleado empleado = objectMapper.convertValue(datos.get("empleado"), Empleado.class);
            Usuario usuario = objectMapper.convertValue(datos.get("usuario"), Usuario.class);

            EmpleadoServicio servicio = new EmpleadoServicio();
            servicio.crearEmpleado(empleado, usuario);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of(
                            "mensaje", "Empleado creado correctamente",
                            "idEmpleado", empleado.getIdEmpleado()
                    ))
                    .build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "codigo", "DATOS_INVALIDOS",
                            "mensaje", "El formato de los datos enviados no es válido"
                    ))
                    .build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("{idEmpleado}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modificarEmpleado(
            @PathParam("idEmpleado") int idEmpleado,
            Empleado empleado) {

        empleado.setIdEmpleado(idEmpleado);
        EmpleadoServicio servicio = new EmpleadoServicio();

        try {
            servicio.modificarEmpleado(empleado);

            return Response.ok(
                    Map.of("mensaje", "Empleado modificado correctamente")
            ).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @DELETE
    @Path("{idEmpleado}")
    public Response desactivarEmpleado(
            @PathParam("idEmpleado") int idEmpleado) {

        EmpleadoServicio servicio = new EmpleadoServicio();

        try {
            servicio.desactivarEmpleado(idEmpleado);

            return Response.ok(
                    Map.of("mensaje", "Empleado desactivado correctamente")
            ).build();

        } catch (ServicioException e) {
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
