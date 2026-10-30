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
import java.time.LocalDate;
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

    private static final String INSERTAR = "INSERT INTO PAGO (id_estudiante, id_empleado, concepto, monto, fecha) VALUES (?, ?, ?, ?, ?)";
    private static final String CONSULTAR = "SELECT id_pago, id_estudiante, id_empleado, concepto, monto, fecha FROM PAGO ORDER BY fecha DESC";
    private static final String CONSULTAR_PAGINADO = CONSULTAR + " LIMIT ? OFFSET ?";
    private static final String CONTAR = "SELECT COUNT(*) AS total FROM PAGO";
    private static final String CONSULTAR_POR_ID = "SELECT id_pago, id_estudiante, id_empleado, concepto, monto, fecha FROM PAGO WHERE id_pago = ? FOR UPDATE";
    private static final String CONSULTAR_ESTUDIANTE_PAGINADO = "SELECT id_pago, id_estudiante, id_empleado, concepto, monto, fecha FROM PAGO WHERE id_estudiante = ? ORDER BY fecha DESC LIMIT ? OFFSET ?";
    private static final String CONSULTAR_REPORTE = "SELECT id_pago, id_estudiante, id_empleado, concepto, monto, fecha "
            + "FROM PAGO WHERE id_estudiante = ? AND fecha >= ? AND fecha <= ? "
            + "ORDER BY fecha DESC, id_pago DESC LIMIT ? OFFSET ?";
    private static final String CONTAR_REPORTE = "SELECT COUNT(*) AS total FROM PAGO "
            + "WHERE id_estudiante = ? AND fecha >= ? AND fecha <= ?";
    private static final String CONTAR_ESTUDIANTE = "SELECT COUNT(*) AS total FROM PAGO WHERE id_estudiante = ?";

    public void crearTabla() {
        try (Connection conexion = ConexionDB.getInstance().getConnection(); 
                Statement statement = conexion.createStatement()) {
            statement.execute(CREAR_TABLA);
        } catch (SQLException e) {
            daoException.manejarError(e);
        }
    }

    public boolean insertar(Pago pago) {
        try (Connection conexion = ConexionDB.getInstance().getConnection()) {
            return insertar(pago, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean insertar(Pago pago, Connection conexion) {
        if (pago == null) {
            return false;
        }

        if (pago.getIdEstudiante() <= 0 || pago.getIdEmpleado() <= 0) {
            return false;
        }

        if (pago.getConcepto() == null || pago.getConcepto().trim().isEmpty()) {
            return false;
        }

        if (pago.getMonto() <= 0) {
            return false;
        }

        if (pago.getFecha() == null) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(INSERTAR);
            ps.setInt(1, pago.getIdEstudiante());
            ps.setInt(2, pago.getIdEmpleado());
            ps.setString(3, pago.getConcepto().trim());
            ps.setDouble(4, pago.getMonto());
            ps.setDate(5, Date.valueOf(pago.getFecha()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public int insertarConId(Pago pago, Connection conexion) {
        try (PreparedStatement ps = conexion.prepareStatement(INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pago.getIdEstudiante()); ps.setInt(2, pago.getIdEmpleado());
            ps.setString(3, pago.getConcepto()); ps.setDouble(4, pago.getMonto());
            ps.setDate(5, Date.valueOf(pago.getFecha()));
            if (ps.executeUpdate()!=1) return 0;
            try (ResultSet rs=ps.getGeneratedKeys()) {return rs.next()?rs.getInt(1):0;}
        } catch(SQLException e) {daoException.manejarError(e);return 0;}
    }

    public Collection<Pago> consultar() {
        try (Connection conexion = ConexionDB.getInstance().getConnection()) {
            return consultar(conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return new ArrayList<>();
        }
    }

    public Collection<Pago> consultar(Connection conexion) {
        Collection<Pago> pagos = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
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
        }

        return pagos;
    }

    public Pago consultarPorId(int idPago, Connection conexion) {
        try (PreparedStatement ps = conexion.prepareStatement(CONSULTAR_POR_ID)) {
            ps.setInt(1, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? construirPago(rs) : null;
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
            return null;
        }
    }

    private Pago construirPago(ResultSet rs) throws SQLException {
        return new Pago(rs.getInt("id_pago"), rs.getInt("id_estudiante"), rs.getInt("id_empleado"), rs.getString("concepto"), rs.getDouble("monto"),
                rs.getDate("fecha").toLocalDate());
    }

    public Collection<Pago> consultar(int pagina, int limite) {
        if (pagina <= 0 || limite <= 0) {
            return new ArrayList<>();
        }

        Collection<Pago> pagos = new ArrayList<>();
        int offset = (pagina - 1) * limite;

        try (Connection conexion = ConexionDB.getInstance().getConnection(); 
                PreparedStatement ps = conexion.prepareStatement(CONSULTAR_PAGINADO)) {

            ps.setInt(1, limite);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagos.add(construirPago(rs));
                }
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        }

        return pagos;
    }

    public int contar() {
        try (Connection conexion = ConexionDB.getInstance().getConnection(); 
                PreparedStatement ps = conexion.prepareStatement(CONTAR); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        }

        return 0;
    }

    // El rango es opcional. Sin fecha se consulta todo el historial.
    public Collection<Pago> consultarReporte(int idEstudiante, LocalDate inicio, LocalDate fin, int pagina, int limite) {
        Collection<Pago> resultado = new ArrayList<>();
        try (Connection conexion = ConexionDB.getInstance().getConnection();
                PreparedStatement ps = conexion.prepareStatement(CONSULTAR_REPORTE)) {
            parametrosReporte(ps, idEstudiante, inicio, fin);
            ps.setInt(4, limite);
            ps.setInt(5, (pagina - 1) * limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.add(construirPago(rs));
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        }
        return resultado;
    }

    public int contarReporte(int idEstudiante, LocalDate inicio, LocalDate fin) {
        try (Connection conexion = ConexionDB.getInstance().getConnection();
                PreparedStatement ps = conexion.prepareStatement(CONTAR_REPORTE)) {
            parametrosReporte(ps, idEstudiante, inicio, fin);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
            return 0;
        }
    }

    private void parametrosReporte(PreparedStatement ps, int idEstudiante, LocalDate inicio, LocalDate fin) throws SQLException {
        ps.setInt(1, idEstudiante);
        ps.setDate(2, Date.valueOf(inicio == null ? LocalDate.of(1000, 1, 1) : inicio));
        ps.setDate(3, Date.valueOf(fin == null ? LocalDate.of(9999, 12, 31) : fin));
    }

    public Collection<Pago> consultarPorEstudiante(int idEstudiante, int pagina, int limite) {
        Collection<Pago> pagos = new ArrayList<>();

        if (idEstudiante <= 0 || pagina <= 0 || limite <= 0) {
            return pagos;
        }
        int offset = (pagina - 1) * limite;

        try (Connection conexion = ConexionDB.getInstance().getConnection(); 
                PreparedStatement ps = conexion.prepareStatement(CONSULTAR_ESTUDIANTE_PAGINADO)) {

            ps.setInt(1, idEstudiante);
            ps.setInt(2, limite);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagos.add(construirPago(rs));
                }
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        }
        return pagos;
    }

    public int contarPorEstudiante(int idEstudiante) {
        if (idEstudiante <= 0) {
            return 0;
        }

        try (Connection conexion = ConexionDB.getInstance().getConnection(); 
                PreparedStatement ps = conexion.prepareStatement(CONTAR_ESTUDIANTE)) {

            ps.setInt(1, idEstudiante);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        }

        return 0;
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
