/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.procesos;

import com.proyecto2.app.backend.Backend.Dao.GradoDao;
import com.proyecto2.app.backend.Backend.Dao.NivelDao;
import com.proyecto2.app.backend.Backend.Dao.UsuarioDao;
import com.proyecto2.app.backend.Backend.Dao.cicloDao;
import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Ciclo;
import com.proyecto2.app.backend.Backend.Model.Grado;
import com.proyecto2.app.backend.Backend.Model.Nivel;
import java.util.Locale;
import java.util.Map;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class AdministracionProcesos {
    
    
    private cicloDao cicloDAO;
    private GradoDao gradoDAO;
    private NivelDao nivelDAO;
    private UsuarioDao usuarioDAO;

    public AdministracionProcesos() {
        this.cicloDAO = new cicloDao();
        this.gradoDAO = new GradoDao();
        this.nivelDAO = new NivelDao();
        this.usuarioDAO = new UsuarioDao();
    }

    public Collection<Ciclo> consultarCiclo() {
        try {
            return cicloDAO.consultar();
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar los ciclos", e);
        }
    }

    public Collection<Grado> consultarGrados() {
        try {
            return gradoDAO.consultar();
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar los grados", e);
        }
    }
    public Collection<Nivel> consultarNiveles() {
        try {
            return nivelDAO.consultar();
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No fue posible consultar los niveles académicos", e);
        }
    }

    public Map<String, Object> consultarAdministrativos(int pagina, int limite, String rol) {
        if (pagina < 1 || limite < 1 || limite > 50 || (long) (pagina - 1) * limite > Integer.MAX_VALUE) {
            throw new ProcesosException("DATOS_INVALIDOS", "La página o el límite no son válidos (máximo 50)");
        }
        String filtro = rol == null || rol.isBlank() ? null : rol.trim().toUpperCase(Locale.ROOT);
        if (filtro != null && !"ADMIN".equals(filtro) && !"SUPER_ADMIN".equals(filtro)) {
            throw new ProcesosException("DATOS_INVALIDOS", "El filtro de rol no es válido");
        }
        try {
            return usuarioDAO.consultarAdministrativos(pagina, limite, filtro);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No fue posible consultar los usuarios administrativos", e);
        }
    }

}
