package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.CuotaMensual;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class CuotaMensualDao {

    private static final String BLOQUEAR_INSCRIPCION = "SELECT id_inscripcion FROM INSCRIPCION WHERE id_inscripcion=? FOR UPDATE";
    private static final String INSERTAR = "INSERT INTO CUOTA_MENSUAL (id_inscripcion, anio, mes, monto) VALUES (?, ?, ?, ?)";
    private static final String CONSULTAR = "SELECT c.id_cuota, c.id_inscripcion, c.anio, c.mes, c.monto, c.id_pago FROM CUOTA_MENSUAL c JOIN INSCRIPCION i ON i.id_inscripcion=c.id_inscripcion WHERE i.id_estudiante=? ORDER BY c.anio, c.mes";
    private static final String CONSULTAR_ID = "SELECT id_cuota, id_inscripcion, anio, mes, monto, id_pago FROM CUOTA_MENSUAL WHERE id_cuota=? FOR UPDATE";
    private static final String MARCAR_PAGADA = "UPDATE CUOTA_MENSUAL SET id_pago=? WHERE id_cuota=? AND id_pago IS NULL";
    private static final String VINCULAR_PAGO = "INSERT INTO PAGO_INSCRIPCION (id_pago, id_inscripcion) VALUES (?, ?)";
    private static final String PAGO_VINCULADO = "SELECT id_pago FROM CUOTA_MENSUAL WHERE id_pago=? UNION SELECT id_pago FROM PAGO_INSCRIPCION WHERE id_pago=?";
    private static final String INSCRIPCION_PAGADA = "SELECT 1 FROM PAGO_INSCRIPCION v JOIN PAGO p ON p.id_pago=v.id_pago WHERE v.id_inscripcion=? AND p.concepto='INSCRIPCION' LIMIT 1";
    private static final String PAGO_ANTIGUO = "SELECT 1 FROM PAGO p LEFT JOIN PAGO_INSCRIPCION v ON v.id_pago=p.id_pago LEFT JOIN CUOTA_MENSUAL c ON c.id_pago=p.id_pago WHERE p.id_estudiante=? AND p.concepto=? AND v.id_pago IS NULL AND c.id_pago IS NULL LIMIT 1";
    private static final String SALDO_INSCRIPCION = "SELECT COALESCE(SUM(GREATEST(0, ?-COALESCE((SELECT SUM(p.monto) FROM PAGO_INSCRIPCION v JOIN PAGO p ON p.id_pago=v.id_pago WHERE v.id_inscripcion=i.id_inscripcion AND p.concepto='INSCRIPCION'),0))),0) FROM INSCRIPCION i WHERE i.id_estudiante=? AND i.id_ciclo=?";
    // Se considera vencida al finalizar el mes indicado; las futuras no generan mora.
    private static final String SALDO_CUOTAS_VENCIDAS = "SELECT COALESCE(SUM(c.monto),0) FROM CUOTA_MENSUAL c "
            + "JOIN INSCRIPCION i ON i.id_inscripcion=c.id_inscripcion "
            + "WHERE i.id_estudiante=? AND i.id_ciclo=? AND c.id_pago IS NULL "
            + "AND LAST_DAY(STR_TO_DATE(CONCAT(c.anio,'-',c.mes,'-01'),'%Y-%c-%d')) < CURDATE()";
    private static final String SALDO_CUOTAS = "SELECT COALESCE(SUM(c.monto),0) FROM CUOTA_MENSUAL c JOIN INSCRIPCION i ON i.id_inscripcion=c.id_inscripcion WHERE i.id_estudiante=? AND i.id_ciclo=? AND c.id_pago IS NULL";

    public Collection<CuotaMensual> consultar(int idEstudiante) {
        try (Connection conexion = ConexionDB.getInstance().getConnection()) {
            return consultar(idEstudiante, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return new ArrayList<>();
        }
    }

    public Collection<CuotaMensual> consultar(int idEstudiante, Connection conexion) {
        Collection<CuotaMensual> cuotas = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(CONSULTAR)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) cuotas.add(leer(rs));
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        }
        return cuotas;
    }

    public CuotaMensual consultarPorId(int idCuota, Connection conexion) {
        try (PreparedStatement ps = conexion.prepareStatement(CONSULTAR_ID)) {
            ps.setInt(1, idCuota);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leer(rs) : null;
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
            return null;
        }
    }

    public boolean bloquearInscripcion(int idInscripcion, Connection conexion) {
        try (PreparedStatement ps = conexion.prepareStatement(BLOQUEAR_INSCRIPCION)) {
            ps.setInt(1, idInscripcion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean insertar(CuotaMensual cuota, Connection conexion) {
        try (PreparedStatement ps = conexion.prepareStatement(INSERTAR)) {
            ps.setInt(1, cuota.getIdInscripcion());
            ps.setInt(2, cuota.getAnio());
            ps.setInt(3, cuota.getMes());
            ps.setDouble(4, cuota.getMonto());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean marcarPagada(int idCuota, int idPago, Connection conexion) {
        try (PreparedStatement ps = conexion.prepareStatement(MARCAR_PAGADA)) {
            ps.setInt(1, idPago);
            ps.setInt(2, idCuota);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean vincularPago(int idPago, int idInscripcion, Connection conexion) {
        try (PreparedStatement ps = conexion.prepareStatement(VINCULAR_PAGO)) {
            ps.setInt(1, idPago);
            ps.setInt(2, idInscripcion);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean estaVinculado(int idPago, Connection conexion) {
        try (PreparedStatement ps = conexion.prepareStatement(PAGO_VINCULADO)) {
            ps.setInt(1, idPago);
            ps.setInt(2, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean inscripcionPagada(int idInscripcion, Connection conexion) {
        try (PreparedStatement ps = conexion.prepareStatement(INSCRIPCION_PAGADA)) {
            ps.setInt(1, idInscripcion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean tienePagoAntiguo(int idEstudiante, String concepto, Connection conexion) {
        try (PreparedStatement ps = conexion.prepareStatement(PAGO_ANTIGUO)) {
            ps.setInt(1, idEstudiante);
            ps.setString(2, concepto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public double saldoPendiente(int idEstudiante, int idCiclo, double tarifaInscripcion) {
        try (Connection conexion = ConexionDB.getInstance().getConnection()) {
            return sumarSaldo(conexion, SALDO_INSCRIPCION, tarifaInscripcion, idEstudiante, idCiclo)
                    + sumarSaldo(conexion, SALDO_CUOTAS, null, idEstudiante, idCiclo);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return 0;
        }
    }

    public double saldoVencido(int idEstudiante, int idCiclo, double tarifaInscripcion) {
        try (Connection conexion = ConexionDB.getInstance().getConnection()) {
            return sumarSaldo(conexion, SALDO_INSCRIPCION, tarifaInscripcion, idEstudiante, idCiclo)
                    + sumarSaldo(conexion, SALDO_CUOTAS_VENCIDAS, null, idEstudiante, idCiclo);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return 0;
        }
    }

    private double sumarSaldo(Connection conexion, String consulta, Double tarifa, int idEstudiante, int idCiclo) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(consulta)) {
            int indice = 1;
            if (tarifa != null) ps.setDouble(indice++, tarifa);
            ps.setInt(indice++, idEstudiante);
            ps.setInt(indice, idCiclo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    private CuotaMensual leer(ResultSet rs) throws SQLException {
        return new CuotaMensual(rs.getInt("id_cuota"), rs.getInt("id_inscripcion"),
                rs.getInt("anio"), rs.getInt("mes"), rs.getDouble("monto"),
                (Integer) rs.getObject("id_pago"));
    }
}
