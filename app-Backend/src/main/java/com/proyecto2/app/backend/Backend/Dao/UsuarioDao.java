/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import java.sql.Connection;
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
public class UsuarioDao {

    private ConexionDB conexionDB;

    public static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS USUARIO (
                id_usuario INT PRIMARY KEY AUTO_INCREMENT,
                nombre_usuario VARCHAR(100) NOT NULL UNIQUE,
                contraseña VARCHAR(255) NOT NULL,
                rol VARCHAR(30) NOT NULL,
                estado VARCHAR(20) NOT NULL,
                id_empleado INT,
                CONSTRAINT fk_usuario_empleado FOREIGN KEY (id_empleado) REFERENCES EMPLEADO(id_empleado)
            )
            """;

    private static final String INSERTAR = "INSERT INTO USUARIO (nombre_usuario, contraseña, rol, estado, id_empleado) VALUES (?, ?, ?, ?, ?)";
    private static final String CONSULTAR = "SELECT id_usuario, nombre_usuario, contraseña, rol, estado, id_empleado FROM USUARIO ORDER BY nombre_usuario";
    private static final String ACTUALIZAR = "UPDATE USUARIO SET nombre_usuario = ?, rol = ?, id_empleado = ? WHERE id_usuario = ?";
    private static final String DESACTIVAR = "UPDATE USUARIO SET estado = 'INACTIVO' WHERE id_usuario = ?";
    private static final String CAMBIAR_CONTRASEÑA = "UPDATE USUARIO SET contraseña = ? WHERE id_usuario = ?";
    private static final String CONSULTAR_PAGINADO = CONSULTAR + " LIMIT ? OFFSET ?";
    private static final String CONTAR = "SELECT COUNT(*) AS total FROM USUARIO";
    private static final String VALIDAR_SECRETARIA = "SELECT 1 FROM USUARIO u JOIN EMPLEADO e ON e.id_empleado=u.id_empleado WHERE e.id_empleado=? AND e.estado='ACTIVO' AND u.estado='ACTIVO' AND u.rol='SECRETARIA' LIMIT 1";
    private static final String CONSULTAR_POR_NOMBRE = "SELECT id_usuario, nombre_usuario, contraseña, rol, estado, id_empleado FROM USUARIO WHERE nombre_usuario = ? LIMIT 1";
    private static final String CONSULTAR_POR_ID = "SELECT id_usuario, nombre_usuario, contraseña, rol, estado, id_empleado FROM USUARIO WHERE id_usuario = ?";
    private static final String ACTIVAR_PERSONAL = "UPDATE USUARIO SET estado = 'ACTIVO' WHERE id_usuario = ? AND estado = 'INACTIVO' AND rol IN ('SECRETARIA','MAESTRO','BIBLIOTECARIO','MANTENIMIENTO','LIMPIEZA')";
    private static final String ACTIVAR = "UPDATE USUARIO SET estado = 'ACTIVO' WHERE id_usuario = ? AND rol = ? AND estado = 'INACTIVO'";
    private static final String ADMINISTRATIVOS_DESDE = " FROM USUARIO u LEFT JOIN EMPLEADO e ON u.id_empleado = e.id_empleado"
            + " WHERE u.rol IN ('ADMIN', 'SUPER_ADMIN')";
    private static final String ADMINISTRATIVOS_COLUMNAS = "SELECT u.id_usuario, u.nombre_usuario, u.rol, u.estado, u.id_empleado, "
            + "e.nombre, e.apellido, e.puesto, e.fecha_contratacion, e.salario";
    private static final String CONSULTAR_ADMINISTRATIVOS = ADMINISTRATIVOS_COLUMNAS + ADMINISTRATIVOS_DESDE
            + " ORDER BY u.id_usuario DESC LIMIT ? OFFSET ?";
    private static final String CONSULTAR_ADMINISTRATIVOS_ROL = ADMINISTRATIVOS_COLUMNAS + ADMINISTRATIVOS_DESDE
            + " AND u.rol = ? ORDER BY u.id_usuario DESC LIMIT ? OFFSET ?";
    private static final String CONTAR_ADMINISTRATIVOS = "SELECT COUNT(*) AS total" + ADMINISTRATIVOS_DESDE;
    private static final String CONTAR_ADMINISTRATIVOS_ROL = CONTAR_ADMINISTRATIVOS + " AND u.rol = ?";

    public UsuarioDao() {
        this.conexionDB = ConexionDB.getInstance();
    }

    public boolean esSecretariaActiva(int idEmpleado, Connection conexion) {
        try (PreparedStatement ps = conexion.prepareStatement(VALIDAR_SECRETARIA)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public void crearTabla() {
        try (Connection conexion = conexionDB.getConnection()) {
            crearTabla(conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
        }
    }

    public void crearTabla(Connection conexion) {
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

    public boolean insertar(Usuario usuario) {
        try (Connection conexion = conexionDB.getConnection()) {
            return insertar(usuario, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean insertar(Usuario usuario, Connection conexion) {

        if (usuario == null) {
            return false;
        }

        if (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().trim().isEmpty()) {
            return false;
        }

        if (usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty()) {
            return false;
        }

        if (usuario.getRol() == null || usuario.getRol().trim().isEmpty()) {
            return false;
        }

        if (usuario.getEstado() == null || usuario.getEstado().trim().isEmpty()) {
            return false;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(INSERTAR, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, usuario.getNombreUsuario().trim());
            ps.setString(2, usuario.getContrasena());
            ps.setString(3, usuario.getRol().trim());
            ps.setString(4, usuario.getEstado().trim());

            if (usuario.getIdEmpleado() > 0) {
                ps.setInt(5, usuario.getIdEmpleado());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            if (ps.executeUpdate() <= 0) {
                return false;
            }

            rs = ps.getGeneratedKeys();

            if (rs.next()) {
                usuario.setIdUsuario(rs.getInt(1));
                return true;
            }

            return false;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(rs);
            cerrar(ps);
        }
    }

    public Collection<Usuario> consultar() {
        try (Connection conexion = conexionDB.getConnection()) {
            return consultar(conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return new ArrayList<>();
        }
    }

    public Collection<Usuario> consultar(Connection conexion) {

        Collection<Usuario> usuarios = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                usuarios.add(construirUsuario(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return usuarios;
    }

    public boolean actualizar(Usuario usuario) {
        try (Connection conexion = conexionDB.getConnection()) {
            return actualizar(usuario, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean actualizar(Usuario usuario, Connection conexion) {

        if (usuario == null) {
            return false;
        }

        if (usuario.getIdUsuario() <= 0) {
            return false;
        }

        if (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().trim().isEmpty()) {
            return false;
        }

        if (usuario.getRol() == null || usuario.getRol().trim().isEmpty()) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ACTUALIZAR);
            ps.setString(1, usuario.getNombreUsuario().trim());
            ps.setString(2, usuario.getRol().trim());

            if (usuario.getIdEmpleado() > 0) {
                ps.setInt(3, usuario.getIdEmpleado());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setInt(4, usuario.getIdUsuario());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public boolean desactivar(int idUsuario) {
        try (Connection conexion = conexionDB.getConnection()) {
            return desactivar(idUsuario, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean desactivar(int idUsuario, Connection conexion) {

        if (idUsuario <= 0) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(DESACTIVAR);
            ps.setInt(1, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public boolean activarPersonal(int idUsuario, Connection conexion) {
        try (PreparedStatement ps = conexion.prepareStatement(ACTIVAR_PERSONAL)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    // Cambia únicamente el acceso del usuario; el estado del empleado es independiente.
    public boolean activar(int idUsuario, String rol, Connection conexion) {
        if (idUsuario <= 0 || (!"ADMIN".equals(rol) && !"SUPER_ADMIN".equals(rol))) {
            return false;
        }

        try (PreparedStatement ps = conexion.prepareStatement(ACTIVAR)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, rol);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean cambiarContraseña(int idUsuario, String nuevaContraseña) {
        try (Connection conexion = conexionDB.getConnection()) {
            return cambiarContraseña(idUsuario, nuevaContraseña, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean cambiarContraseña(int idUsuario, String nuevaContraseña, Connection conexion) {

        if (idUsuario <= 0) {
            return false;
        }

        if (nuevaContraseña == null || nuevaContraseña.trim().isEmpty()) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(CAMBIAR_CONTRASEÑA);
            ps.setString(1, nuevaContraseña);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private Usuario construirUsuario(ResultSet rs) throws SQLException {

        int idUsuario = rs.getInt("id_usuario");
        String nombreUsuario = rs.getString("nombre_usuario");
        String contrasena = rs.getString("contraseña");
        String rol = rs.getString("rol");
        String estado = rs.getString("estado");
        int idEmpleado = rs.getInt("id_empleado");

        if (rs.wasNull()) {
            idEmpleado = 0;
        }

        return new Usuario(idUsuario, nombreUsuario, contrasena, rol, estado, idEmpleado);
    }

    public Collection<Usuario> consultar(int pagina, int limite) {

        if (pagina <= 0 || limite <= 0) {
            return new ArrayList<>();
        }

        Collection<Usuario> usuarios = new ArrayList<>();
        int offset = (pagina - 1) * limite;

        try (Connection conexion = conexionDB.getConnection(); PreparedStatement ps = conexion.prepareStatement(CONSULTAR_PAGINADO)) {

            ps.setInt(1, limite);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(construirUsuario(rs));
                }
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        }

        return usuarios;
    }

    public int contar() {

        try (Connection conexion = conexionDB.getConnection(); PreparedStatement ps = conexion.prepareStatement(CONTAR); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        }

        return 0;
    }

    public Usuario consultarPorNombreUsuario(String nombreUsuario) {
        try (Connection conexion = conexionDB.getConnection()) {
            return consultarPorNombreUsuario(nombreUsuario, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return null;
        }
    }

    public Usuario consultarPorNombreUsuario(String nombreUsuario, Connection conexion) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            return null;
        }

        try (PreparedStatement ps = conexion.prepareStatement(CONSULTAR_POR_NOMBRE)) {
            ps.setString(1, nombreUsuario.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirUsuario(rs);
                }
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        }

        return null;
    }

    public Usuario consultarPorId(int idUsuario) {
        if (idUsuario <= 0) {
            return null;
        }

        try (Connection conexion = conexionDB.getConnection(); PreparedStatement ps = conexion.prepareStatement(CONSULTAR_POR_ID)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirUsuario(rs);
                }
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        }

        return null;
    }

    // Busca por ID en la transacción recibida; reutiliza la consulta existente.
    public Usuario consultarPorId(int idUsuario, Connection conexion) {
        if (idUsuario <= 0) {
            return null;
        }
        try (PreparedStatement ps = conexion.prepareStatement(CONSULTAR_POR_ID)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? construirUsuario(rs) : null;
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
            return null;
        }
    }

    // Consulta solo datos administrativos; nunca selecciona ni devuelve contraseñas.
    public java.util.Map<String, Object> consultarAdministrativos(int pagina, int limite, String rol) {
        boolean filtrar = rol != null;
        String consulta = filtrar ? CONSULTAR_ADMINISTRATIVOS_ROL : CONSULTAR_ADMINISTRATIVOS;
        String conteo = filtrar ? CONTAR_ADMINISTRATIVOS_ROL : CONTAR_ADMINISTRATIVOS;
        java.util.List<java.util.Map<String, Object>> datos = new java.util.ArrayList<>();

        try (Connection conexion = conexionDB.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                PreparedStatement psConteo = conexion.prepareStatement(conteo)) {

            int posicion = 1;
            if (filtrar) {
                ps.setString(posicion++, rol);
                psConteo.setString(1, rol);
            }
            ps.setInt(posicion++, limite);
            ps.setInt(posicion, (pagina - 1) * limite);

            int total = 0;
            try (ResultSet rs = psConteo.executeQuery()) {
                if (rs.next()) {
                    total = rs.getInt("total");
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
                    item.put("idUsuario", rs.getInt("id_usuario"));
                    item.put("nombreUsuario", rs.getString("nombre_usuario"));
                    item.put("rol", rs.getString("rol"));
                    item.put("estado", rs.getString("estado"));
                    int idEmpleado = rs.getInt("id_empleado");
                    item.put("idEmpleado", rs.wasNull() ? null : idEmpleado);
                    item.put("nombre", rs.getString("nombre"));
                    item.put("apellido", rs.getString("apellido"));
                    item.put("puesto", rs.getString("puesto"));
                    java.sql.Date fecha = rs.getDate("fecha_contratacion");
                    item.put("fechaContratacion", fecha == null ? null : fecha.toLocalDate().toString());
                    item.put("salario", rs.getBigDecimal("salario"));
                    datos.add(item);
                }
            }
            return java.util.Map.of("datos", datos, "total", total, "pagina", pagina, "limite", limite);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return java.util.Map.of();
        }
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
