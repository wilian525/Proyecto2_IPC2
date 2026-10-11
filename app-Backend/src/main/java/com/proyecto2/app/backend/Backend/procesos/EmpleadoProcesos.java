/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.procesos;

import com.proyecto2.app.backend.Backend.Dao.EmpleadoDao;
import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Empleado;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class EmpleadoProcesos {
    
    private EmpleadoDao empleadoDAO;

    public EmpleadoProcesos() {
        this.empleadoDAO = new EmpleadoDao();
    }

    public Collection<Empleado> consultarEmpleado() {
        try {
            return empleadoDAO.consultar();
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar los empleados", e);
        }
    }
}
