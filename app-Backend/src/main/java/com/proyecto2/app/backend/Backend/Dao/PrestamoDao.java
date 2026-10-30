/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Prestamo;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class PrestamoDao {

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS PRESTAMO (
                id_prestamo INT PRIMARY KEY AUTO_INCREMENT,
                id_libro INT NOT NULL,
                id_usuario INT NOT NULL,
                fecha_prestamo DATE NOT NULL,
                fecha_devolucion_esperada DATE NOT NULL,
                fecha_devolucion DATE,
                dias_atraso INT NOT NULL DEFAULT 0,
                monto_multa DECIMAL(10,2) NOT NULL DEFAULT 0,
                fecha_aviso DATE NULL,

                CONSTRAINT fk_prestamo_libro
                    FOREIGN KEY (id_libro)
                    REFERENCES LIBRO(id_libro),

                CONSTRAINT fk_prestamo_usuario
                    FOREIGN KEY (id_usuario)
                    REFERENCES USUARIO(id_usuario)
            )
            """;

    private static final String INSERTAR = "INSERT INTO PRESTAMO (id_libro, id_usuario, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion, dias_atraso, monto_multa) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String CONSULTAR = "SELECT id_prestamo, id_libro, id_usuario, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion, dias_atraso, monto_multa, fecha_aviso "
            + "FROM PRESTAMO ORDER BY fecha_prestamo DESC";
    private static final String CONSULTAR_ATRASOS = "SELECT id_prestamo, id_libro, id_usuario, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion, dias_atraso, monto_multa, fecha_aviso "
            + "FROM PRESTAMO WHERE fecha_devolucion IS NULL AND fecha_devolucion_esperada < CURDATE() ORDER BY fecha_devolucion_esperada";
    private static final String ACTUALIZAR = "UPDATE PRESTAMO SET fecha_devolucion = ?, dias_atraso = ?, monto_multa = ? WHERE id_prestamo = ?";
    private static final String CONSULTAR_PAGINADO = CONSULTAR + " LIMIT ? OFFSET ?";
    private static final String CONSULTAR_ID = "SELECT id_prestamo, id_libro, id_usuario, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion, dias_atraso, monto_multa, fecha_aviso FROM PRESTAMO WHERE id_prestamo=? FOR UPDATE";
    private static final String AVISO = "UPDATE PRESTAMO SET fecha_aviso=CURDATE() WHERE id_prestamo=? AND fecha_devolucion IS NULL AND fecha_devolucion_esperada<CURDATE()";
    private static final String CONTAR = "SELECT COUNT(*) AS total FROM PRESTAMO";
    private static final String CONSULTAR_ATRASOS_PAGINADO = CONSULTAR_ATRASOS + " LIMIT ? OFFSET ?";
    private static final String CONSULTAR_MORA_ESTUDIANTES = "SELECT p.id_prestamo, p.id_libro, p.id_usuario, "
            + "p.fecha_prestamo, p.fecha_devolucion_esperada, p.fecha_devolucion, p.dias_atraso, p.monto_multa, p.fecha_aviso "
            + "FROM PRESTAMO p JOIN ESTUDIANTE e ON e.id_usuario = p.id_usuario "
            + "WHERE p.fecha_devolucion IS NULL AND p.fecha_devolucion_esperada < CURDATE() "
            + "ORDER BY p.fecha_devolucion_esperada, p.id_prestamo LIMIT ? OFFSET ?";
    private static final String CONTAR_MORA_ESTUDIANTES = "SELECT COUNT(*) AS total FROM PRESTAMO p "
            + "JOIN ESTUDIANTE e ON e.id_usuario = p.id_usuario "
            + "WHERE p.fecha_devolucion IS NULL AND p.fecha_devolucion_esperada < CURDATE()";
    private static final String CONTAR_ATRASOS = "SELECT COUNT(*) AS total FROM PRESTAMO WHERE fecha_devolucion IS NULL AND fecha_devolucion_esperada < CURDATE()";

    public void crearTabla() {
        try (Connection conexion = ConexionDB.getInstance().getConnection(); Statement statement = conexion.createStatement()) {
            statement.execute(CREAR_TABLA);
        } catch (SQLException e) {
            daoException.manejarError(e);
        }
    }

    public boolean insertar(Prestamo prestamo) {
        try (Connection conexion = ConexionDB.getInstance().getConnection()) {
            return insertar(prestamo, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean insertar(Prestamo prestamo, Connection conexion) {
        if (prestamo == null) {
            return false;
        }

        if (prestamo.getIdLibro() <= 0 || prestamo.getIdUsuario() <= 0) {
            return false;
        }

        if (prestamo.getFechaPrestamo() == null || prestamo.getFechaDevolucionEsperada() == null) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(INSERTAR);
            ps.setInt(1, prestamo.getIdLibro());
            ps.setInt(2, prestamo.getIdUsuario());
            ps.setDate(3, Date.valueOf(prestamo.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(prestamo.getFechaDevolucionEsperada()));

            if (prestamo.getFechaDevolucion() != null) {
                ps.setDate(5, Date.valueOf(prestamo.getFechaDevolucion()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            ps.setInt(6, prestamo.getDiasAtraso());
            ps.setDouble(7, prestamo.getMontoMulta());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Prestamo> consultar() {
        try (Connection conexion = ConexionDB.getInstance().getConnection()) {
            return consultar(conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return new ArrayList<>();
        }
    }

    public Collection<Prestamo> consultar(Connection conexion) {
        Collection<Prestamo> prestamos = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                prestamos.add(construirPrestamo(rs));
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return prestamos;
    }

    public Collection<Prestamo> consultarAtrasos() {
        try (Connection conexion = ConexionDB.getInstance().getConnection()) {
            return consultarAtrasos(conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return new ArrayList<>();
        }
    }

    public Collection<Prestamo> consultarAtrasos(Connection conexion) {
        Collection<Prestamo> prestamos = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR_ATRASOS);
            rs = ps.executeQuery();

            while (rs.next()) {
                prestamos.add(construirPrestamo(rs));
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return prestamos;
    }

    public Prestamo consultarPorId(int idPrestamo, Connection conexion) {
        try (PreparedStatement ps = conexion.prepareStatement(CONSULTAR_ID)) {
            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? construirPrestamo(rs) : null;
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
            return null;
        }
    }

    public boolean registrarAviso(int idPrestamo) {
        try (Connection conexion = ConexionDB.getInstance().getConnection();
                PreparedStatement ps = conexion.prepareStatement(AVISO)) {
            ps.setInt(1, idPrestamo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean actualizar(Prestamo prestamo) {
        try (Connection conexion = ConexionDB.getInstance().getConnection()) {
            return actualizar(prestamo, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean actualizar(Prestamo prestamo, Connection conexion) {
        if (prestamo == null) {
            return false;
        }

        if (prestamo.getIdPrestamo() <= 0) {
            return false;
        }

        if (prestamo.getDiasAtraso() < 0 || prestamo.getMontoMulta() < 0) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ACTUALIZAR);

            if (prestamo.getFechaDevolucion() != null) {
                ps.setDate(1, Date.valueOf(prestamo.getFechaDevolucion()));
            } else {
                ps.setNull(1, Types.DATE);
            }

            ps.setInt(2, prestamo.getDiasAtraso());
            ps.setDouble(3, prestamo.getMontoMulta());
            ps.setInt(4, prestamo.getIdPrestamo());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private Prestamo construirPrestamo(ResultSet rs) throws SQLException {
        Date fechaDevolucion = rs.getDate("fecha_devolucion");

        Prestamo prestamo = new Prestamo(
                rs.getInt("id_prestamo"),
                rs.getInt("id_libro"),
                rs.getInt("id_usuario"),
                rs.getDate("fecha_prestamo").toLocalDate(),
                rs.getDate("fecha_devolucion_esperada").toLocalDate(),
                fechaDevolucion != null ? fechaDevolucion.toLocalDate() : null,
                rs.getInt("dias_atraso"),
                rs.getDouble("monto_multa")
        );
        Date aviso = rs.getDate("fecha_aviso");
        prestamo.setFechaAviso(aviso == null ? null : aviso.toLocalDate());
        return prestamo;
    }
    
    public Collection<Prestamo> consultar(int pagina, int limite) {
    if (pagina <= 0 || limite <= 0) {
        return new ArrayList<>();
    }

    Collection<Prestamo> prestamos = new ArrayList<>();
    int offset = (pagina - 1) * limite;

    try (Connection conexion = ConexionDB.getInstance().getConnection();
         PreparedStatement ps = conexion.prepareStatement(CONSULTAR_PAGINADO)) {

        ps.setInt(1, limite);
        ps.setInt(2, offset);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                prestamos.add(construirPrestamo(rs));
            }
        }

    } catch (SQLException e) {
        daoException.manejarError(e);
    }

    return prestamos;
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
    
    // El reporte de mora muestra solo prestamos de estudiantes.
    public Collection<Prestamo> consultarMoraEstudiantes(int pagina, int limite) {
        Collection<Prestamo> resultado = new ArrayList<>();
        try (Connection conexion = ConexionDB.getInstance().getConnection();
                PreparedStatement ps = conexion.prepareStatement(CONSULTAR_MORA_ESTUDIANTES)) {
            ps.setInt(1, limite);
            ps.setInt(2, (pagina - 1) * limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.add(construirPrestamo(rs));
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        }
        return resultado;
    }

    public int contarMoraEstudiantes() {
        try (Connection conexion = ConexionDB.getInstance().getConnection();
                PreparedStatement ps = conexion.prepareStatement(CONTAR_MORA_ESTUDIANTES);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return 0;
        }
    }

    public Collection<Prestamo> consultarAtrasos(int pagina, int limite) {
    if (pagina <= 0 || limite <= 0) {
        return new ArrayList<>();
    }

    Collection<Prestamo> prestamos = new ArrayList<>();
    int offset = (pagina - 1) * limite;

    try (Connection conexion = ConexionDB.getInstance().getConnection();
         PreparedStatement ps = conexion.prepareStatement(CONSULTAR_ATRASOS_PAGINADO)) {

        ps.setInt(1, limite);
        ps.setInt(2, offset);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                prestamos.add(construirPrestamo(rs));
            }
        }

    } catch (SQLException e) {
        daoException.manejarError(e);
    }

    return prestamos;
}
    
    public int contarAtrasos() {
    try (Connection conexion = ConexionDB.getInstance().getConnection();
         PreparedStatement ps = conexion.prepareStatement(CONTAR_ATRASOS);
         ResultSet rs = ps.executeQuery()) {

        if (rs.next()) {
            return rs.getInt("total");
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
