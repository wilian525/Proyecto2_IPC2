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
    @Produces(MediaType.APPLICATION_JSON)
    public Response iniciarSesion(Usuario usuario) {

        if (usuario == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("codigo", "DATOS_INVALIDOS","mensaje", "Los datos de inicio de sesión son obligatorios" )).build();
        }

        AutenticacionProcesos autenticacionProcesos = new AutenticacionProcesos();

        try {
            String token = autenticacionProcesos.iniciarSesion(
                    usuario.getNombreUsuario(),
                    usuario.getContrasena()
            );

            return Response.ok(
                    Map.of("token", token) ).build();

        } catch (ProcesosException e) {

            if ("DATOS_INVALIDOS".equals(e.getCodigo())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of( "codigo", e.getCodigo(),"mensaje", e.getMessage() )).build();
            }

            if ("NO_AUTORIZADO".equals(e.getCodigo())) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("codigo", e.getCodigo(), "mensaje", e.getMessage() )).build();
            }

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("codigo", e.getCodigo(),"mensaje", e.getMessage() )).build();
        }
    }

    @POST
    @Path("logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cerrarSesion(@HeaderParam("Authorization") String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of( "codigo", "NO_AUTORIZADO","mensaje", "El token de autorización no es válido")) .build();
        }

        String token = authorization.substring(7).trim();

        AutenticacionServicio autenticacionServicio = new AutenticacionServicio();

        try {
            autenticacionServicio.cerrarSesion(token);

            return Response.ok(
                    Map.of( "codigo", "OK", "mensaje", "Sesión cerrada correctamente" )).build();

        } catch (ServicioException e) {
            return Response.status(Response.Status.UNAUTHORIZED) 
                    .entity(Map.of("codigo", e.getCodigo(),"mensaje", e.getMessage())).build();
        }
    }
}
