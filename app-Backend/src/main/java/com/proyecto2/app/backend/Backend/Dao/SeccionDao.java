/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Nivel;
import com.proyecto2.app.backend.Backend.Model.Seccion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class SeccionDao {

    private ConexionDB conexionDB;

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS SECCION (
                id_seccion INT PRIMARY KEY AUTO_INCREMENT,
                nombre VARCHAR(100) NOT NULL,
                id_grado INT NOT NULL,

                CONSTRAINT fk_seccion_grado
                    FOREIGN KEY (id_grado)
                    REFERENCES GRADO(id_grado)
            )
            """;

    private static final String INSERTAR = "INSERT INTO SECCION (nombre, id_grado) VALUES (?, ?)";
    private static final String CONSULTAR = "SELECT id_seccion, nombre, id_grado FROM SECCION ORDER BY nombre";
    private static final String ACTUALIZAR = "UPDATE SECCION SET nombre = ?, id_grado = ? WHERE id_seccion = ?";

    public SeccionDao() {
        this.conexionDB = ConexionDB.getInstance();
    }

    public void crearTabla() {
        try (Connection conexion = conexionDB.getConnection(); Statement statement = conexion.createStatement()) {
            statement.execute(CREAR_TABLA);
        } catch (SQLException e) {
            daoException.manejarError(e);
        }
    }

    public boolean insertar(Seccion seccion) {
        try (Connection conexion = conexionDB.getConnection()) {
            return insertar(seccion, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean insertar(Seccion seccion, Connection conexion) {
        if (seccion == null) {
            return false;
        }

        if (seccion.getNombre() == null || seccion.getNombre().trim().isEmpty()) {
            return false;
        }

        if (seccion.getIdGrado() <= 0) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(INSERTAR);
            ps.setString(1, seccion.getNombre().trim());
            ps.setInt(2, seccion.getIdGrado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Seccion> consultar() {
        try (Connection conexion = conexionDB.getConnection()) {
            return consultar(conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return new ArrayList<>();
        }
    }

    public Collection<Seccion> consultar(Connection conexion) {
        Collection<Seccion> secciones = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                secciones.add(construirSeccion(rs));
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return secciones;
    }

    public boolean actualizar(Seccion seccion) {
        try (Connection conexion = conexionDB.getConnection()) {
            return actualizar(seccion, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean actualizar(Seccion seccion, Connection conexion) {
        if (seccion == null) {
            return false;
        }

        if (seccion.getIdSeccion() <= 0) {
            return false;
        }

        if (seccion.getNombre() == null || seccion.getNombre().trim().isEmpty()) {
            return false;
        }

        if (seccion.getIdGrado() <= 0) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ACTUALIZAR);
            ps.setString(1, seccion.getNombre().trim());
            ps.setInt(2, seccion.getIdGrado());
            ps.setInt(3, seccion.getIdSeccion());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private Seccion construirSeccion(ResultSet rs) throws SQLException {
        int idSeccion = rs.getInt("id_seccion");
        String nombre = rs.getString("nombre");
        int idGrado = rs.getInt("id_grado");

        return new Seccion(idSeccion, nombre, idGrado);
    }

    private void cerrar(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                daoException.manejarError(e);
            }
        }
    }

    private void cerrar(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                daoException.manejarError(e);
            }
        }
    }
}
