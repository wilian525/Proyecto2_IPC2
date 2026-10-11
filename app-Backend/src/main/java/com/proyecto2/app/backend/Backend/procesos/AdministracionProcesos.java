/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.procesos;

import com.proyecto2.app.backend.Backend.Dao.GradoDao;
import com.proyecto2.app.backend.Backend.Dao.cicloDao;
import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Ciclo;
import com.proyecto2.app.backend.Backend.Model.Grado;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class AdministracionProcesos {
    
    
    private cicloDao cicloDAO;
    private GradoDao gradoDAO;

    public AdministracionProcesos() {
        this.cicloDAO = new cicloDao();
        this.gradoDAO = new GradoDao();
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
}
