/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Zona;
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
public class ZonaDao {

    private ConexionDB conexionDB;

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS ZONA (
                id_zona INT PRIMARY KEY AUTO_INCREMENT,
                nombre VARCHAR(100) NOT NULL,
                porcentaje DECIMAL(5,2) NOT NULL,
                id_asignacion INT NOT NULL,

                CONSTRAINT fk_zona_asignacion
                    FOREIGN KEY (id_asignacion)
                    REFERENCES ASIGNACION(id_asignacion)
            )
            """;

    private static final String INSERTAR = "INSERT INTO ZONA (nombre, porcentaje, id_asignacion) VALUES (?, ?, ?)";
    private static final String CONSULTAR = "SELECT id_zona, nombre, porcentaje, id_asignacion FROM ZONA ORDER BY id_zona";
    private static final String ACTUALIZAR = "UPDATE ZONA SET nombre = ?, porcentaje = ?, id_asignacion = ? WHERE id_zona = ?";

    public ZonaDao() {
        this.conexionDB = ConexionDB.getInstance();
    }

    public void crearTabla() {
        try (Connection conexion = conexionDB.getConnection(); Statement statement = conexion.createStatement()) {
            statement.execute(CREAR_TABLA);
        } catch (SQLException e) {
            daoException.manejarError(e);
        }
    }

    public boolean insertar(Zona zona) {
        try (Connection conexion = conexionDB.getConnection()) {
            return insertar(zona, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean insertar(Zona zona, Connection conexion) {
        if (zona == null) {
            return false;
        }
        if (zona.getNombre() == null || zona.getNombre().trim().isEmpty()) {
            return false;
        }
        if (zona.getPorcentaje() <= 0 || zona.getPorcentaje() > 100) {
            return false;
        }
        if (zona.getIdAsignacion() <= 0) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(INSERTAR);
            ps.setString(1, zona.getNombre().trim());
            ps.setDouble(2, zona.getPorcentaje());
            ps.setInt(3, zona.getIdAsignacion());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Zona> consultar() {
        try (Connection conexion = conexionDB.getConnection()) {
            return consultar(conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return new ArrayList<>();
        }
    }

    public Collection<Zona> consultar(Connection conexion) {
        Collection<Zona> zonas = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                zonas.add(construirZona(rs));
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return zonas;
    }

    public boolean actualizar(Zona zona) {
        try (Connection conexion = conexionDB.getConnection()) {
            return actualizar(zona, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean actualizar(Zona zona, Connection conexion) {
        if (zona == null) {
            return false;
        }
        if (zona.getIdZona() <= 0) {
            return false;
        }
        if (zona.getNombre() == null || zona.getNombre().trim().isEmpty()) {
            return false;
        }
        if (zona.getPorcentaje() <= 0 || zona.getPorcentaje() > 100) {
            return false;
        }
        if (zona.getIdAsignacion() <= 0) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ACTUALIZAR);
            ps.setString(1, zona.getNombre().trim());
            ps.setDouble(2, zona.getPorcentaje());
            ps.setInt(3, zona.getIdAsignacion());
            ps.setInt(4, zona.getIdZona());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private Zona construirZona(ResultSet rs) throws SQLException {
        return new Zona(rs.getInt("id_zona"), rs.getString("nombre"), rs.getDouble("porcentaje"), rs.getInt("id_asignacion"));
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
