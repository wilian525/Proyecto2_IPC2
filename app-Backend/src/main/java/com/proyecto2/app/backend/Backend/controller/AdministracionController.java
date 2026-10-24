/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.controller;

import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Model.Ciclo;
import com.proyecto2.app.backend.Backend.Model.Empleado;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto2.app.backend.Backend.Model.Grado;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import com.proyecto2.app.backend.Backend.procesos.AdministracionProcesos;
import com.proyecto2.app.backend.Backend.servicio.AdministracionServicio;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 *
 * @author wilian
 */
@Path("administracion")
@Produces(MediaType.APPLICATION_JSON)
public class AdministracionController {

    @POST
    @Path("super-admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("SUPER_ADMIN")
    public Response crearSuperAdmin(Usuario usuario) {
        AdministracionServicio servicio = new AdministracionServicio();

        try {
            servicio.crearSuperAdmin(usuario);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("mensaje", "Super Admin creado correctamente"))
                    .build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("super-admin/{idUsuario}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("SUPER_ADMIN")
    public Response modificarSuperAdmin(@PathParam("idUsuario") int idUsuario, Usuario usuario) {
        usuario.setIdUsuario(idUsuario);
        AdministracionServicio servicio = new AdministracionServicio();

        try {
            servicio.modificarSuperAdmin(usuario);

            return Response.ok(
                    Map.of("mensaje", "Super Admin modificado correctamente")
            ).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @DELETE
    @Path("super-admin/{idUsuario}")
    @RolesAllowed("SUPER_ADMIN")
    public Response desactivarSuperAdmin(@PathParam("idUsuario") int idUsuario) {
        AdministracionServicio servicio = new AdministracionServicio();

        try {
            servicio.desactivarSuperAdmin(idUsuario);

            return Response.ok(
                    Map.of("mensaje", "Super Admin desactivado correctamente")
            ).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("SUPER_ADMIN")
    public Response crearAdmin(Usuario usuario) {
        AdministracionServicio servicio = new AdministracionServicio();

        try {
            servicio.crearAdmin(usuario);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("mensaje", "Admin creado correctamente"))
                    .build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("admin/{idUsuario}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("SUPER_ADMIN")
    public Response modificarAdmin(@PathParam("idUsuario") int idUsuario, Usuario usuario) {
        usuario.setIdUsuario(idUsuario);
        AdministracionServicio servicio = new AdministracionServicio();

        try {
            servicio.modificarAdmin(usuario);

            return Response.ok(
                    Map.of("mensaje", "Admin modificado correctamente")
            ).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @DELETE
    @Path("admin/{idUsuario}")
    @RolesAllowed("SUPER_ADMIN")
    public Response desactivarAdmin(@PathParam("idUsuario") int idUsuario) {
        AdministracionServicio servicio = new AdministracionServicio();

        try {
            servicio.desactivarAdmin(idUsuario);

            return Response.ok(
                    Map.of("mensaje", "Admin desactivado correctamente")
            ).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("{tipo:admin|super-admin}/{idUsuario}/activar")
    @RolesAllowed("SUPER_ADMIN")
    public Response activarAdministrativo(@PathParam("tipo") String tipo,
            @PathParam("idUsuario") int idUsuario) {
        try {
            new AdministracionServicio().activarAdministrativo(idUsuario, obtenerRol(tipo));
            return Response.ok(Map.of("mensaje", "Cuenta administrativa activada correctamente")).build();
        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("ciclos")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN", "SECRETARIA"})
    public Response consultarCiclos() {
        AdministracionProcesos procesos = new AdministracionProcesos();

        try {
            return Response.ok(procesos.consultarCiclo()).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("ciclos")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("SUPER_ADMIN")
    public Response crearCiclo(Ciclo ciclo) {
        AdministracionServicio servicio = new AdministracionServicio();

        try {
            servicio.crearCiclo(ciclo);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("mensaje", "Ciclo creado correctamente"))
                    .build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("ciclos/{idCiclo}/activar")
    @RolesAllowed("SUPER_ADMIN")
    public Response activarCiclo(@PathParam("idCiclo") int idCiclo) {
        AdministracionServicio servicio = new AdministracionServicio();

        try {
            servicio.activarCiclo(idCiclo);
            return Response.ok(Map.of("mensaje", "Año lectivo activado correctamente")).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @DELETE
    @Path("ciclos/{idCiclo}")
    @RolesAllowed("SUPER_ADMIN")
    public Response desactivarCiclo(@PathParam("idCiclo") int idCiclo) {
        AdministracionServicio servicio = new AdministracionServicio();

        try {
            servicio.desactivarCiclo(idCiclo);

            return Response.ok(
                    Map.of("mensaje", "Ciclo desactivado correctamente")
            ).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("grados")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN", "SECRETARIA"})
    public Response consultarGrados() {
        AdministracionProcesos procesos = new AdministracionProcesos();

        try {
            return Response.ok(procesos.consultarGrados()).build();

        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("grados")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response crearGrado(Grado grado) {
        AdministracionServicio servicio = new AdministracionServicio();

        try {
            servicio.crearGrado(grado);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("mensaje", "Grado creado correctamente"))
                    .build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("grados/{idGrado}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response modificarGrado(@PathParam("idGrado") int idGrado, Grado grado) {
        grado.setIdGrado(idGrado);
        AdministracionServicio servicio = new AdministracionServicio();

        try {
            servicio.modificarGrado(grado);

            return Response.ok(
                    Map.of("mensaje", "Grado modificado correctamente")
            ).build();

        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    // Listado del panel Super Admin, con consulta en Procesos y DAO.
    @GET
    @Path("usuarios-administrativos")
    @RolesAllowed("SUPER_ADMIN")
    public Response consultarAdministrativos(@DefaultValue("1") @QueryParam("pagina") int pagina,
            @DefaultValue("10") @QueryParam("limite") int limite,
            @QueryParam("rol") String rol) {
        try {
            return Response.ok(new AdministracionProcesos().consultarAdministrativos(pagina, limite, rol)).build();
        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("niveles")
    @RolesAllowed({"SUPER_ADMIN", "ADMIN", "SECRETARIA"})
    public Response consultarNiveles() {
        try {
            return Response.ok(new AdministracionProcesos().consultarNiveles()).build();
        } catch (ProcesosException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    // Acepta el mismo formato que los formularios Angular: {usuario, empleado}.
    @POST
    @Path("{tipo:admin|super-admin}/completo")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("SUPER_ADMIN")
    public Response crearAdministrativoCompleto(@PathParam("tipo") String tipo, Map<String, Object> datos) {
        try {
            Usuario usuario = convertirUsuario(datos);
            Empleado empleado = convertirEmpleado(datos);
            new AdministracionServicio().crearAdministrativoCompleto(usuario, empleado, obtenerRol(tipo));
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("mensaje", "Usuario administrativo creado correctamente")).build();
        } catch (IllegalArgumentException e) {
            return responderError("DATOS_INVALIDOS", "Los datos de usuario o empleado no tienen el formato correcto");
        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("{tipo:admin|super-admin}/completo/{idUsuario}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("SUPER_ADMIN")
    public Response modificarAdministrativoCompleto(@PathParam("tipo") String tipo,
            @PathParam("idUsuario") int idUsuario, Map<String, Object> datos) {
        try {
            Usuario usuario = convertirUsuario(datos);
            Empleado empleado = convertirEmpleado(datos);
            new AdministracionServicio().modificarAdministrativoCompleto(idUsuario, usuario, empleado, obtenerRol(tipo));
            return Response.ok(Map.of("mensaje", "Usuario administrativo modificado correctamente")).build();
        } catch (IllegalArgumentException e) {
            return responderError("DATOS_INVALIDOS", "Los datos de usuario o empleado no tienen el formato correcto");
        } catch (ServicioException e) {
            return responderError(e.getCodigo(), e.getMessage());
        }
    }

    private String obtenerRol(String tipo) {
        return "super-admin".equals(tipo) ? "SUPER_ADMIN" : "ADMIN";
    }

    private Usuario convertirUsuario(Map<String, Object> datos) {
        if (datos == null || datos.get("usuario") == null) {
            throw new IllegalArgumentException("Faltan datos de usuario");
        }
        return new ObjectMapper().findAndRegisterModules().convertValue(datos.get("usuario"), Usuario.class);
    }

    private Empleado convertirEmpleado(Map<String, Object> datos) {
        if (datos == null || datos.get("empleado") == null) {
            throw new IllegalArgumentException("Faltan datos de empleado");
        }
        return new ObjectMapper().findAndRegisterModules().convertValue(datos.get("empleado"), Empleado.class);
    }

    private Response responderError(String codigo, String mensaje) {
        if ("DATOS_INVALIDOS".equals(codigo)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
        }

        if ("NO_ENCONTRADO".equals(codigo)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
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
