/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Reporte;

import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class GeneradorReportePdf {
    
    public static final String SOLVENTES_MOROSOS = "solventes_morosos";
    public static final String RENDIMIENTO = "rendimiento";
    public static final String HISTORIAL_PAGOS = "historial_pagos";
    public static final String INGRESOS = "ingresos";
    public static final String PRESTAMOS_MORA = "prestamos_mora";

    public byte[] generarPdf(String nombreReporte, Collection<?> datos, Map<String, Object> parametros) {
        if (datos == null) throw new ProcesosException("REPORTE_ERROR", "Los datos del reporte no pueden ser nulos");

        String ruta = obtenerRuta(nombreReporte);
        Map<String, Object> parametrosReporte = new HashMap<>();
        if (parametros != null) parametrosReporte.putAll(parametros);

        try (InputStream archivo = getClass().getClassLoader().getResourceAsStream(ruta)) {
            if (archivo == null) throw new ProcesosException("REPORTE_ERROR", "No se encontró la plantilla " + ruta);

            JasperReport reporte = JasperCompileManager.compileReport(archivo);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(datos);
            JasperPrint impresion = JasperFillManager.fillReport(reporte, parametrosReporte, dataSource);
            return JasperExportManager.exportReportToPdf(impresion);

        } catch (JRException e) {
            throw new ProcesosException("REPORTE_ERROR", "No se pudo generar el reporte PDF", e);
        } catch (IOException e) {
            throw new ProcesosException("REPORTE_ERROR", "No se pudo cerrar la plantilla del reporte", e);
        }
    }

    public byte[] generarPdf(String nombreReporte, Object dato, Map<String, Object> parametros) {
        if (dato == null) throw new ProcesosException("REPORTE_ERROR", "Los datos del reporte no pueden ser nulos");
        return generarPdf(nombreReporte, List.of(dato), parametros);
    }

    private String obtenerRuta(String nombreReporte) {
        if (SOLVENTES_MOROSOS.equals(nombreReporte)) return "reportes/solventes_morosos.jrxml";
        if (RENDIMIENTO.equals(nombreReporte)) return "reportes/rendimiento.jrxml";
        if (HISTORIAL_PAGOS.equals(nombreReporte)) return "reportes/historial_pagos.jrxml";
        if (INGRESOS.equals(nombreReporte)) return "reportes/ingresos.jrxml";
        if (PRESTAMOS_MORA.equals(nombreReporte)) return "reportes/prestamos_mora.jrxml";
        throw new ProcesosException("REPORTE_ERROR", "El reporte solicitado no existe");
    }
}
