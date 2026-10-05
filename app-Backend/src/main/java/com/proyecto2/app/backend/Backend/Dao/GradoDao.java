/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Grado;
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
public class GradoDao {

    private DBConnectionSingleton conexionDB;

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS GRADO (
                id_grado INT PRIMARY KEY AUTO_INCREMENT,
                nombre VARCHAR(100) NOT NULL,
                id_nivel INT NOT NULL,

                CONSTRAINT fk_grado_nivel
                    FOREIGN KEY (id_nivel)
                    REFERENCES NIVEL(id_nivel)
            )
            """;

    private static final String INSERTAR= "INSERT INTO GRADO (nombre, id_nivel) VALUES (?, ?)";
    private static final String CONSULTAR = "SELECT id_grado, nombre, id_nivel FROM GRADO ORDER BY nombre";
    private static final String ACTUALIZAR= "UPDATE GRADO SET nombre = ?, id_nivel = ? WHERE id_grado = ?";

    public GradoDao() {
        this.conexionDB = DBConnectionSingleton.getInstance();
    }

    public void crearTabla() {
        Connection conexion = conexionDB.getConnection();
        Statement statement = null;

        try {
            statement = conexion.createStatement();
            statement.execute(CREAR_TABLA);

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(statement);
        }
    }

    public boolean insertar(Grado grado) {
        if (grado == null) {
            return false;
        }

        if (grado.getNombre() == null || grado.getNombre().trim().isEmpty()) {
            return false;
        }

        if (grado.getIdNivel() <= 0) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(INSERTAR);

            ps.setString(1, grado.getNombre().trim());
            ps.setInt(2, grado.getIdNivel());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Grado> consultar() {
        Collection<Grado> grados = new ArrayList<>();

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                grados.add(construirGrado(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return grados;
    }

    public boolean actualizar(Grado grado) {
        if (grado == null) {
            return false;
        }

        if (grado.getIdGrado() <= 0) {
            return false;
        }

        if (grado.getNombre() == null || grado.getNombre().trim().isEmpty()) {
            return false;
        }

        if (grado.getIdNivel() <= 0) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ACTUALIZAR);

            ps.setString(1, grado.getNombre().trim());
            ps.setInt(2, grado.getIdNivel());
            ps.setInt(3, grado.getIdGrado());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private Grado construirGrado(ResultSet rs) throws SQLException {
        int idGrado = rs.getInt("id_grado");
        String nombre = rs.getString("nombre");
        int idNivel = rs.getInt("id_nivel");
        return new Grado(idGrado, nombre, idNivel);
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
