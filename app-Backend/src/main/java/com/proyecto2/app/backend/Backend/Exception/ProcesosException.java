/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Exception;

/**
 *
 * @author wilian
 */
public class ProcesosException extends RuntimeException {

    private final String codigo;

    public ProcesosException(String mensaje) {
        super(mensaje);
        this.codigo = "PROCESOS_ERROR";
    }

    public ProcesosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = "PROCESOS_ERROR";
    }

    public ProcesosException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public ProcesosException(String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
