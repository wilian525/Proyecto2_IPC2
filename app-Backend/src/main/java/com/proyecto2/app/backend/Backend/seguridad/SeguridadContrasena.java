/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.seguridad;
import org.mindrot.jbcrypt.BCrypt;
/**
 *
 * @author wilian
 */
public class SeguridadContrasena {
    
     private final int rondas;

    public SeguridadContrasena() {
        this.rondas = 12;
    }

    public String hashear(String contrasena) {

        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La contraseña no puede estar vacía"
            );
        }

        String salt = BCrypt.gensalt(rondas);

        return BCrypt.hashpw(contrasena, salt);
    }

    public boolean verificar(
            String contrasenaIngresada,
            String contrasenaHasheada) {

        if (contrasenaIngresada == null
                || contrasenaIngresada.isEmpty()) {
            return false;
        }

        if (contrasenaHasheada == null
                || contrasenaHasheada.isEmpty()) {
            return false;
        }

        try {

            return BCrypt.checkpw(
                    contrasenaIngresada,
                    contrasenaHasheada
            );

        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
