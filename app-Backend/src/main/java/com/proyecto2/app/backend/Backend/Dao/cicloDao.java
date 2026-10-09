/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Ciclo;
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
public class cicloDao {

    private ConexionDB conexionDB;

    public static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS CICLO (
                id_ciclo INT PRIMARY KEY AUTO_INCREMENT,
                año_inicio INT NOT NULL,
                año_fin INT NOT NULL,
                estado VARCHAR(20) NOT NULL
            )
            """;

    private static final String INSERTAR = "INSERT INTO CICLO (año_inicio, año_fin, estado) VALUES (?, ?, ?)";
    private static final String CONSULTAR  = "SELECT id_ciclo, año_inicio, año_fin, estado " + "FROM CICLO ORDER BY año_inicio DESC";
    private static final String DESACTIVAR = "UPDATE CICLO SET estado = 'INACTIVO' " + "WHERE id_ciclo = ? AND estado <> 'INACTIVO'";
    private static final String EXISTE_CICLO = "SELECT COUNT(*) FROM CICLO " + "WHERE año_inicio = ? AND año_fin = ?";

    public cicloDao() {
        this.conexionDB = ConexionDB.getInstance();
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

    public boolean insertar(Ciclo ciclo) {

        if (ciclo == null) {
            return false;
        }

        if (ciclo.getAnioInicio() <= 0 || ciclo.getAnioFin() <= 0) {
            return false;
        }

        if (ciclo.getAnioFin() < ciclo.getAnioInicio()) {
            return false;
        }

        if (ciclo.getEstado() == null
                || ciclo.getEstado().trim().isEmpty()) {
            return false;
        }

        if (existeCiclo(ciclo.getAnioInicio(), ciclo.getAnioFin())) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {

            ps = conexion.prepareStatement(INSERTAR);

            ps.setInt(1, ciclo.getAnioInicio());
            ps.setInt(2, ciclo.getAnioFin());
            ps.setString(3, ciclo.getEstado().trim());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Ciclo> consultar() {

        Collection<Ciclo> ciclos = new ArrayList<>();

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                ciclos.add(construirCiclo(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return ciclos;
    }

    public boolean desactivar(int idCiclo) {

        if (idCiclo <= 0) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(DESACTIVAR);
            ps.setInt(1, idCiclo);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private boolean existeCiclo(int anioInicio, int anioFin) {

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = conexion.prepareStatement(EXISTE_CICLO);

            ps.setInt(1, anioInicio);
            ps.setInt(2, anioFin);

            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }
        return false;
    }

    private Ciclo construirCiclo(ResultSet rs) throws SQLException {
        int idCiclo = rs.getInt("id_ciclo");
        int anioInicio = rs.getInt("año_inicio");
        int anioFin = rs.getInt("año_fin");
        String estado = rs.getString("estado");

        return new Ciclo( idCiclo, anioInicio,  anioFin, estado );
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
