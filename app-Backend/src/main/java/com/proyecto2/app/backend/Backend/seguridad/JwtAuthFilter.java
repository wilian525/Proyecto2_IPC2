/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.seguridad;

import com.proyecto2.app.backend.Backend.Dao.UsuarioDao;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioDao usuarioDAO;

    public JwtAuthFilter() {
        jwtUtil = new JwtUtil();
        usuarioDAO = new UsuarioDao();
    }

    @Override
    public void filter(ContainerRequestContext request) throws IOException {
        String ruta = request.getUriInfo().getPath();

       if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || "autenticacion/login".equals(ruta) || "autenticacion/recuperar".equals(ruta)) return;

        String authorization = request.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            rechazar(request, "Se requiere un token de autorización");
            return;
        }

        String token = authorization.substring(7).trim();

        if (!jwtUtil.validarToken(token)) {
            rechazar(request, "El token no es válido o ha expirado");
            return;
        }

        try {
            int idUsuario = jwtUtil.obtenerIdUsuario(token);
            String rolToken = jwtUtil.obtenerRol(token);

            Usuario usuario = usuarioDAO.consultarPorId(idUsuario);

            if (usuario == null) {
                rechazar(request, "El usuario del token no existe");
                return;
            }

            if (usuario.getEstado() == null || !usuario.getEstado().equalsIgnoreCase("ACTIVO")) {
                rechazar(request, "El usuario no se encuentra activo");
                return;
            }

            if (usuario.getRol() == null || !usuario.getRol().equalsIgnoreCase(rolToken)) {
                rechazar(request, "El rol del usuario ha cambiado");
                return;
            }

            request.setProperty("idUsuario", usuario.getIdUsuario());
            request.setProperty("idEmpleado", usuario.getIdEmpleado());

            establecerSeguridad(request, usuario);

        } catch (daoException e) {
            request.abortWith( Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .type(MediaType.APPLICATION_JSON).entity(Map.of( "codigo", "BD_ERROR", "mensaje", "No se pudo validar la sesión"))
                            .build()
            );
        }
    }

    private void establecerSeguridad(
            ContainerRequestContext request,
            Usuario usuario) {

        SecurityContext anterior = request.getSecurityContext();

        request.setSecurityContext(new SecurityContext() {

            @Override
            public Principal getUserPrincipal() {
                return new Principal() {
                    @Override
                    public String getName() {
                        return String.valueOf(usuario.getIdUsuario());
                    }
                };
            }

            @Override
            public boolean isUserInRole(String rol) {
                if (usuario.getRol() == null) {
                    return false;
                }

                if (usuario.getRol().equalsIgnoreCase("SUPER_ADMIN")) {
                    return true;
                }

                return usuario.getRol().equalsIgnoreCase(rol);
            }

            @Override
            public boolean isSecure() {
                return anterior != null && anterior.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        });
    }

    private void rechazar(ContainerRequestContext request, String mensaje) {
        request.abortWith(Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON)
                        .entity(Map.of("codigo", "NO_AUTORIZADO","mensaje", mensaje))
                        .build());
    }
}
