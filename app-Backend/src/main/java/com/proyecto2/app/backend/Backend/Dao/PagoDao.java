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

    private Pago construirPago(ResultSet rs) throws SQLException {
        return new Pago(rs.getInt("id_pago"), rs.getInt("id_estudiante"), rs.getInt("id_empleado"), rs.getString("concepto"), rs.getDouble("monto"), rs.getDate("fecha").toLocalDate());
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
         PreparedStatement ps = conexion.prepareStatement(CONTAR);
         ResultSet rs = ps.executeQuery()) {

        if (rs.next()) {
            return rs.getInt("total");
        }

    } catch (SQLException e) {
        daoException.manejarError(e);
    }

    return 0;
}

public Collection<Pago> consultarPorEstudiante(int idEstudiante, LocalDate fechaInicio, LocalDate fechaFin, int pagina, int limite) {
    Collection<Pago> pagos = new ArrayList<>();

    if (idEstudiante <= 0 || pagina <= 0 || limite <= 0) {
        return pagos;
    }

    StringBuilder sql = new StringBuilder("SELECT id_pago, id_estudiante, id_empleado, concepto, monto, fecha FROM PAGO WHERE id_estudiante = ?");

    if (fechaInicio != null) {
        sql.append(" AND fecha >= ?");
    }

    if (fechaFin != null) {
        sql.append(" AND fecha <= ?");
    }

    sql.append(" ORDER BY fecha DESC LIMIT ? OFFSET ?");

    int offset = (pagina - 1) * limite;

    try (Connection conexion = ConexionDB.getInstance().getConnection();
         PreparedStatement ps = conexion.prepareStatement(sql.toString())) {

        int indice = 1;

        ps.setInt(indice++, idEstudiante);

        if (fechaInicio != null) {
            ps.setDate(indice++, Date.valueOf(fechaInicio));
        }

        if (fechaFin != null) {
            ps.setDate(indice++, Date.valueOf(fechaFin));
        }

        ps.setInt(indice++, limite);
        ps.setInt(indice, offset);

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

public int contarPorEstudiante(int idEstudiante, LocalDate fechaInicio, LocalDate fechaFin) {
    if (idEstudiante <= 0) {
        return 0;
    }

    StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM PAGO WHERE id_estudiante = ?");

    if (fechaInicio != null) {
        sql.append(" AND fecha >= ?");
    }

    if (fechaFin != null) {
        sql.append(" AND fecha <= ?");
    }

    try (Connection conexion = ConexionDB.getInstance().getConnection();
         PreparedStatement ps = conexion.prepareStatement(sql.toString())) {

        int indice = 1;

        ps.setInt(indice++, idEstudiante);

        if (fechaInicio != null) {
            ps.setDate(indice++, Date.valueOf(fechaInicio));
        }

        if (fechaFin != null) {
            ps.setDate(indice, Date.valueOf(fechaFin));
        }

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
