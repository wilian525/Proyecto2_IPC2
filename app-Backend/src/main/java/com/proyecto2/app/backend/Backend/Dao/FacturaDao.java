/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Factura;
import java.sql.Connection;
import java.sql.Date;
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
public class FacturaDao {

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS FACTURA (
                id_factura INT PRIMARY KEY AUTO_INCREMENT,
                numero_factura VARCHAR(100) NOT NULL UNIQUE,
                fecha DATE NOT NULL,
                monto_total DECIMAL(10,2) NOT NULL,
                id_pago INT NOT NULL UNIQUE,

                CONSTRAINT fk_factura_pago
                    FOREIGN KEY (id_pago)
                    REFERENCES PAGO(id_pago)
            )
            """;

    private static final String INSERTAR = "INSERT INTO FACTURA (numero_factura, fecha, monto_total, id_pago) VALUES (?, ?, ?, ?)";
    private static final String CONSULTAR = "SELECT id_factura, numero_factura, fecha, monto_total, id_pago FROM FACTURA ORDER BY fecha DESC";

    public void crearTabla() {

        Connection conexion = null;
        Statement statement = null;

        try {

            conexion = ConexionDB.getInstance().getConnection();

            statement = conexion.createStatement();
            statement.execute(CREAR_TABLA);

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(statement);
            cerrar(conexion);
        }
    }

    public boolean insertar(Factura factura) {
        try (Connection conexion = ConexionDB.getInstance().getConnection()) {
            return insertar(factura, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean insertar(Factura factura, Connection conexion) {

        if (factura == null) {
            return false;
        }

        if (factura.getNumeroFactura() == null || factura.getNumeroFactura().trim().isEmpty()) {
            return false;
        }

        if (factura.getFecha() == null) {
            return false;
        }

        if (factura.getMontoTotal() <= 0) {
            return false;
        }

        if (factura.getIdPago() <= 0) {
            return false;
        }

        PreparedStatement ps = null;

        try {

            ps = conexion.prepareStatement(INSERTAR);
            ps.setString(1, factura.getNumeroFactura().trim());
            ps.setDate(2, Date.valueOf(factura.getFecha()));
            ps.setDouble(3, factura.getMontoTotal());
            ps.setInt(4, factura.getIdPago());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Factura> consultar() {
        try (Connection conexion = ConexionDB.getInstance().getConnection()) {
            return consultar(conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return new ArrayList<>();
        }
    }

    public Collection<Factura> consultar(Connection conexion) {

        Collection<Factura> facturas = new ArrayList<>();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = conexion.prepareStatement(CONSULTAR);

            rs = ps.executeQuery();

            while (rs.next()) {
                facturas.add(construirFactura(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return facturas;
    }

    private Factura construirFactura(ResultSet rs) throws SQLException {

        return new Factura(
                rs.getInt("id_factura"),
                rs.getString("numero_factura"),
                rs.getDate("fecha").toLocalDate(),
                rs.getDouble("monto_total"),
                rs.getInt("id_pago")
        );
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

    private void cerrar(Connection conexion) {

        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                daoException.manejarError(e);
            }
        }
    }
}
