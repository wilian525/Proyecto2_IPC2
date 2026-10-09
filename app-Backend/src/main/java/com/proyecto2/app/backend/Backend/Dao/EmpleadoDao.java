/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Empleado;
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
public class EmpleadoDao {
    
    private ConexionDB conexionDB;

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS EMPLEADO (
                id_empleado INT PRIMARY KEY AUTO_INCREMENT,
                nombre VARCHAR(100) NOT NULL,
                apellido VARCHAR(100) NOT NULL,
                puesto VARCHAR(50) NOT NULL,
                fecha_contratacion DATE NOT NULL,
                salario DECIMAL(10,2) NOT NULL,
                estado VARCHAR(20) NOT NULL
            )
            """;

    private static final String INSERTAR ="INSERT INTO EMPLEADO (nombre, apellido, puesto, fecha_contratacion, salario, estado) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String CONSULTAR = "SELECT id_empleado, nombre, apellido, puesto, fecha_contratacion, salario, estado FROM EMPLEADO ORDER BY apellido, nombre";
    private static final String ACTUALIZAR ="UPDATE EMPLEADO SET nombre = ?, apellido = ?, puesto = ?, fecha_contratacion = ?, salario = ? WHERE id_empleado = ?";
    private static final String DESACTIVAR = "UPDATE EMPLEADO SET estado = 'INACTIVO' WHERE id_empleado = ?";

    public EmpleadoDao() {
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

    public boolean insertar(Empleado empleado) {

        if (empleado == null) {
            return false;
        }

        if (empleado.getNombre() == null || empleado.getNombre().trim().isEmpty()) {
            return false;
        }

        if (empleado.getApellido() == null || empleado.getApellido().trim().isEmpty()) {
            return false;
        }

        if (empleado.getPuesto() == null || empleado.getPuesto().trim().isEmpty()) {
            return false;
        }

        if (empleado.getFechaContratacion() == null) {
            return false;
        }

        if (empleado.getSalario() < 0) {
            return false;
        }

        if (empleado.getEstado() == null || empleado.getEstado().trim().isEmpty()) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(INSERTAR);
            ps.setString(1, empleado.getNombre().trim());
            ps.setString(2, empleado.getApellido().trim());
            ps.setString(3, empleado.getPuesto().trim());
            ps.setDate(4,Date.valueOf(empleado.getFechaContratacion()));
            ps.setDouble(5, empleado.getSalario());
            ps.setString(6, empleado.getEstado().trim());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Empleado> consultar() {
        Collection<Empleado> empleados = new ArrayList<>();

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                empleados.add(construirEmpleado(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return empleados;
    }

    public boolean actualizar(Empleado empleado) {

        if (empleado == null) {
            return false;
        }

        if (empleado.getIdEmpleado() <= 0) {
            return false;
        }

        if (empleado.getNombre() == null|| empleado.getNombre().trim().isEmpty()) {
            return false;
        }
        if (empleado.getApellido() == null || empleado.getApellido().trim().isEmpty()) {
            return false;
        }
        if (empleado.getPuesto() == null|| empleado.getPuesto().trim().isEmpty()) {
            return false;
        }
        if (empleado.getFechaContratacion() == null) {
            return false;
        }
        if (empleado.getSalario() < 0) {
            return false;
        }
        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ACTUALIZAR);

            ps.setString(1, empleado.getNombre().trim());
            ps.setString(2, empleado.getApellido().trim());
            ps.setString(3, empleado.getPuesto().trim());
            ps.setDate(4, Date.valueOf(empleado.getFechaContratacion()) );
            ps.setDouble(5, empleado.getSalario());
            ps.setInt(6, empleado.getIdEmpleado());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public boolean desactivar(int idEmpleado) {

        if (idEmpleado <= 0) {
            return false;
        }
        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(DESACTIVAR);
            ps.setInt(1, idEmpleado);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private Empleado construirEmpleado(ResultSet rs) throws SQLException {

        return new Empleado(rs.getInt("id_empleado"),rs.getString("nombre"),rs.getString("apellido"),rs.getString("puesto"),rs.getDate("fecha_contratacion").toLocalDate(),
                rs.getDouble("salario"),
                rs.getString("estado")
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
}
