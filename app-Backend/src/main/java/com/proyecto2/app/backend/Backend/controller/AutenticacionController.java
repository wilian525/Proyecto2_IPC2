/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.controller;

import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import com.proyecto2.app.backend.Backend.procesos.AutenticacionProcesos;
import com.proyecto2.app.backend.Backend.servicio.AutenticacionServicio;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 *
 * @author wilian
 */
@Path("autenticacion")
public class AutenticacionController {
    
      @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response iniciarSesion(Usuario usuario) {
        if (usuario == null) return error("DATOS_INVALIDOS", "Los datos de inicio de sesión son obligatorios");

        try {
            String token = new AutenticacionProcesos().iniciarSesion(usuario.getNombreUsuario(), usuario.getContrasena());
            return Response.ok(Map.of("token", token)).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("recuperar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response recuperarContrasena(Map<String, String> datos) {
        if (datos == null) return error("DATOS_INVALIDOS", "Los datos de recuperación son obligatorios");

        try {
            String fechaTexto = datos.get("fechaVerificacion");
            LocalDate fecha = fechaTexto == null || fechaTexto.isBlank() ? null : LocalDate.parse(fechaTexto);

            new AutenticacionServicio().recuperarContraseña(datos.get("nombreUsuario"), datos.get("nombre"), datos.get("apellido"), fecha, datos.get("nuevaContrasena"));

            return Response.ok(Map.of("mensaje", "Contraseña actualizada correctamente")).build();
        } catch (DateTimeParseException e) {
            return error("DATOS_INVALIDOS", "La fecha debe tener formato yyyy-MM-dd");
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("logout")
    public Response cerrarSesion(@HeaderParam("Authorization") String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return error("NO_AUTORIZADO", "El token de autorización no es válido");
        }

        try {
            new AutenticacionServicio().cerrarSesion(authorization.substring(7).trim());
            return Response.ok(Map.of("codigo", "OK", "mensaje", "Sesión cerrada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    private Response error(String codigo, String mensaje) {
        Response.Status estado = "DATOS_INVALIDOS".equals(codigo) ? Response.Status.BAD_REQUEST
                : "NO_AUTORIZADO".equals(codigo) ? Response.Status.UNAUTHORIZED
                : "CONFLICTO".equals(codigo) ? Response.Status.CONFLICT
                : Response.Status.INTERNAL_SERVER_ERROR;

        return Response.status(estado).entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
    }
}
