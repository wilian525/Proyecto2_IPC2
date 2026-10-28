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

    public Collection<Empleado> consultarEmpleado(int pagina, int limite) {
        if (pagina <= 0 || limite <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "La página y el límite deben ser mayores que cero");
        }

        try {
            return empleadoDAO.consultar(pagina, limite);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar los empleados", e);
        }
    }

    public Collection<Empleado> consultarPersonal(int pagina, int limite) {
        if (pagina <= 0 || limite <= 0) throw new ProcesosException("DATOS_INVALIDOS", "La página y el límite deben ser positivos");
        try {
            return empleadoDAO.consultarPersonal(pagina, limite);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el personal", e);
        }
    }

    public int contarPersonal() {
        try {
            return empleadoDAO.contarPersonal();
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo contar el personal", e);
        }
    }

    public int contarEmpleados() {
        try {
            return empleadoDAO.contar();
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo obtener el total de empleados", e);
        }
    }
}
