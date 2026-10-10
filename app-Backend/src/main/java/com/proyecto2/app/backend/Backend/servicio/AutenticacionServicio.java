/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.servicio;

import com.proyecto2.app.backend.Backend.Dao.UsuarioDao;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import com.proyecto2.app.backend.Backend.seguridad.JwtUtil;
import com.proyecto2.app.backend.Backend.seguridad.SeguridadContrasena;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class AutenticacionServicio {
    
     private final UsuarioDao usuarioDAO;
    private final SeguridadContrasena seguridadContrasena;
    private final JwtUtil jwtUtil;

    public AutenticacionServicio() {

        this.usuarioDAO = new UsuarioDao();
        this.seguridadContrasena = new SeguridadContrasena();
        this.jwtUtil = new JwtUtil();
    }

    public boolean cerrarSesion(String token) {

        if (token == null || token.trim().isEmpty()) {
            throw new ServicioException( "El token de sesión no puede estar vacío");
        }

        /*
         * JWT es stateless.
         *
         * Aquí comprobamos que el token recibido sea válido.
         * El Servlet posteriormente indicará al frontend que
         * elimine el token almacenado.
         *
         * No eliminamos nada de la base de datos.
         */
        if (!jwtUtil.validarToken(token)) {
            throw new ServicioException(
                    "La sesión no es válida o ya expiró"
            );
        }

        return true;
    }

    public boolean recuperarContraseña(
            String nombreUsuario,
            String nuevaContrasena) {

        validarNombreUsuario(nombreUsuario);
        validarContrasena(nuevaContrasena);

        try {

            Usuario usuario = buscarUsuario(nombreUsuario.trim());

            if (usuario == null) {
                throw new ServicioException("No existe un usuario con ese nombre" );
            }

            if (usuario.getEstado() == null || !usuario.getEstado().equalsIgnoreCase("ACTIVO")) {
                throw new ServicioException( "El usuario no se encuentra activo");
            }

            String contrasenaHasheada = seguridadContrasena.hashear(nuevaContrasena);

            boolean actualizado = usuarioDAO.cambiarContraseña(usuario.getIdUsuario(),contrasenaHasheada);

            if (!actualizado) {
                throw new ServicioException( "No fue posible actualizar la contraseña" );
            }

            return true;

        } catch (ServicioException e) {
            throw e;

        } catch (Exception e) {

            throw new ServicioException(
                    "Error al recuperar la contraseña",
                    e
            );
        }
    }

    private Usuario buscarUsuario(String nombreUsuario) {
        Collection<Usuario> usuarios = usuarioDAO.consultar();

        for (Usuario usuario : usuarios) {

            if (usuario.getNombreUsuario() != null  && usuario.getNombreUsuario() .equalsIgnoreCase(nombreUsuario)) {
                return usuario;
            }
        }
        return null;
    }

    private void validarNombreUsuario(String nombreUsuario) {

        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new ServicioException( "El nombre de usuario no puede estar vacío" );
        }
    }

    private void validarContrasena(String contrasena) {

        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new ServicioException( "La nueva contraseña no puede estar vacía");
        }

        if (contrasena.length() < 8) {
            throw new ServicioException("La contraseña debe contener al menos 8 caracteres" );
        }
    }
}
