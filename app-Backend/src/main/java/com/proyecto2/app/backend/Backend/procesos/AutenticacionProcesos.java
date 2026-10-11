/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.procesos;

import com.proyecto2.app.backend.Backend.Dao.UsuarioDao;
import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import com.proyecto2.app.backend.Backend.seguridad.JwtUtil;
import com.proyecto2.app.backend.Backend.seguridad.SeguridadContrasena;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class AutenticacionProcesos {
    
     private UsuarioDao usuarioDAO;
    private SeguridadContrasena seguridadContrasena;
    private JwtUtil jwtUtil;

    public AutenticacionProcesos() {
        this.usuarioDAO = new UsuarioDao();
        this.seguridadContrasena = new SeguridadContrasena();
        this.jwtUtil = new JwtUtil();
    }

    public String iniciarSesion(String nombreUsuario, String contrasena) {

        if (nombreUsuario == null || nombreUsuario.trim().isEmpty() || contrasena == null || contrasena.isEmpty()) {
            throw new ProcesosException("DATOS_INVALIDOS","El usuario y la contraseña son obligatorios");
        }
        Usuario usuario = buscarUsuario(nombreUsuario.trim());
        if (usuario == null) {
            throw new ProcesosException( "NO_AUTORIZADO", "Usuario o contraseña incorrectos" );
        }

        if (usuario.getEstado() == null || !usuario.getEstado().equalsIgnoreCase("ACTIVO")) {

            throw new ProcesosException( "NO_AUTORIZADO", "El usuario no se encuentra activo" );
        }

        if (!seguridadContrasena.verificar( contrasena,usuario.getContrasena())) {

            throw new ProcesosException("NO_AUTORIZADO","Usuario o contraseña incorrectos");
        }

        return jwtUtil.generarToken( usuario.getIdUsuario(),  usuario.getRol()
        );
    }

    private Usuario buscarUsuario(String nombreUsuario) {

        try {
            Collection<Usuario> usuarios = usuarioDAO.consultar();

            for (Usuario usuario : usuarios) {
                if (usuario.getNombreUsuario() != null&& usuario.getNombreUsuario() .equalsIgnoreCase(nombreUsuario)) {
                    return usuario;
                }
            }
            return null;

        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR",  "No se pudo consultar el usuario", e);
        }
    }
}
