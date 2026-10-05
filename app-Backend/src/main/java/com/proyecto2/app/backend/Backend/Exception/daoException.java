/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Exception;

import java.sql.SQLException;

/**
 *
 * @author wilian
 */
public class daoException extends RuntimeException {

    public daoException(String mensaje) {
        super(mensaje);
    }

    public daoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static void manejarError(SQLException e) {

        String mensaje = "Error en la base de datos"
                + "\nMensaje: " + e.getMessage()
                + "\nSQLState: " + e.getSQLState()
                + "\nCódigo SQL: " + e.getErrorCode();

        if (e.getSQLState() != null) {

            if (e.getSQLState().startsWith("23")) {
                mensaje += "\nTipo: Error de integridad o relación entre tablas.";

            } else if (e.getSQLState().startsWith("42")) {
                mensaje += "\nTipo: Error en la consulta SQL o estructura de la tabla.";

            } else if (e.getSQLState().startsWith("08")) {
                mensaje += "\nTipo: Error de conexión con la base de datos.";

            } else {
                mensaje += "\nTipo: Error SQL no identificado.";
            }
        }

        System.err.println(mensaje);

        throw new daoException(mensaje, e);
    }
}
