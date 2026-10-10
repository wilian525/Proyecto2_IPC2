/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Exception;

/**
 *
 * @author wilian
 */
public class ServicioException extends RuntimeException  {
    
       private final String codigo;

    public ServicioException(String mensaje) {
        super(mensaje);
        this.codigo = "SERVICIO_ERROR";
    }

    public ServicioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = "SERVICIO_ERROR";
    }

    public ServicioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public ServicioException(
            String codigo,
            String mensaje,
            Throwable causa) {

        super(mensaje, causa);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
