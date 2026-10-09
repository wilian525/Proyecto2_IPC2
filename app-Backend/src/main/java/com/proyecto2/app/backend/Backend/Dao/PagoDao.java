/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Pago;
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
public class PagoDao {
    
    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS PAGO (
                id_pago INT PRIMARY KEY AUTO_INCREMENT,
                id_estudiante INT NOT NULL,
                id_empleado INT NOT NULL,
                concepto VARCHAR(100) NOT NULL,
                monto DECIMAL(10,2) NOT NULL,
                fecha DATE NOT NULL,

                CONSTRAINT fk_pago_estudiante
                    FOREIGN KEY (id_estudiante)
                    REFERENCES ESTUDIANTE(id_estudiante),

                CONSTRAINT fk_pago_empleado
                    FOREIGN KEY (id_empleado)
                    REFERENCES EMPLEADO(id_empleado)
            )
            """;

    private static final String INSERTAR ="INSERT INTO PAGO (id_estudiante, id_empleado, concepto, monto, fecha) VALUES (?, ?, ?, ?, ?)";
    private static final String CONSULTAR ="SELECT id_pago, id_estudiante, id_empleado, concepto, monto, fecha FROM PAGO ORDER BY fecha DESC";

    public void crearTabla() {
        Connection conexion = null;
        Statement statement = null;

        try {

            conexion = ConexionDB.obtenerConexion();

            statement = conexion.createStatement();
            statement.execute(CREAR_TABLA);

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(statement);
            cerrar(conexion);
        }
    }

    public boolean insertar(Pago pago) {

        if (pago == null) {
            return false;
        }

        if (pago.getIdEstudiante() <= 0 || pago.getIdEmpleado() <= 0) {
            return false;
        }

        if (pago.getConcepto() == null|| pago.getConcepto().trim().isEmpty()) {
            return false;
        }

        if (pago.getMonto() <= 0) {
            return false;
        }

        if (pago.getFecha() == null) {
            return false;
        }

        Connection conexion = null;
        PreparedStatement ps = null;

        try {

            conexion = ConexionDB.obtenerConexion();

            ps = conexion.prepareStatement(INSERTAR);

            ps.setInt(1, pago.getIdEstudiante());
            ps.setInt(2, pago.getIdEmpleado());
            ps.setString(3, pago.getConcepto().trim());
            ps.setDouble(4, pago.getMonto());
            ps.setDate(5,Date.valueOf(pago.getFecha()));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
            cerrar(conexion);
        }
    }

    public Collection<Pago> consultar() {

        Collection<Pago> pagos = new ArrayList<>();

        Connection conexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            conexion = ConexionDB.obtenerConexion();

            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                pagos.add(construirPago(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
            cerrar(conexion);
        }

        return pagos;
    }

    private Pago construirPago(ResultSet rs)
            throws SQLException {

        return new Pago(
                rs.getInt("id_pago"),
                rs.getInt("id_estudiante"),
                rs.getInt("id_empleado"),
                rs.getString("concepto"),
                rs.getDouble("monto"),
                rs.getDate("fecha").toLocalDate()
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
