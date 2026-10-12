/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.controller;

import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Model.*;
import com.proyecto2.app.backend.Backend.procesos.AcademicoProcesos;
import com.proyecto2.app.backend.Backend.servicio.AcademicoServicio;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;

@Path("academico")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AcademicoController {
    
    @GET
    @Path("cursos")
    public Response consultarCursos() {
        try {
            return Response.ok(new AcademicoProcesos().consultarCursos()).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("cursos")
    public Response crearCurso(Curso curso) {
        try {
            new AcademicoServicio().crearCurso(curso);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Curso creado correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("cursos/{idCurso}")
    public Response modificarCurso(@PathParam("idCurso") int idCurso, Curso curso) {
        curso.setIdCurso(idCurso);

        try {
            new AcademicoServicio().modificarCurso(curso);
            return Response.ok(Map.of("mensaje", "Curso modificado correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("secciones")
    public Response consultarSecciones() {
        try {
            return Response.ok(new AcademicoProcesos().consultarSecciones()).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("secciones")
    public Response crearSeccion(Seccion seccion) {
        try {
            new AcademicoServicio().crearSeccion(seccion);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Sección creada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("secciones/{idSeccion}")
    public Response modificarSeccion(@PathParam("idSeccion") int idSeccion, Seccion seccion) {
        seccion.setIdSeccion(idSeccion);

        try {
            new AcademicoServicio().modificarSeccion(seccion);
            return Response.ok(Map.of("mensaje", "Sección modificada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("asignaciones")
    public Response asignarMaestro(Asignacion asignacion) {
        try {
            new AcademicoServicio().asignarMaestroCurso(asignacion);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Maestro asignado correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("maestros/{idEmpleado}/asignaciones")
    public Response consultarAsignaciones(@PathParam("idEmpleado") int idEmpleado) {
        try {
            return Response.ok(new AcademicoProcesos().consultarAsignacionesMaestro(idEmpleado)).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("asignaciones/{idAsignacion}/nomina")
    public Response consultarNomina(@PathParam("idAsignacion") int idAsignacion) {
        try {
            return Response.ok(new AcademicoProcesos().consultarNomina(idAsignacion)).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("inscripciones")
    public Response consultarInscripciones(@DefaultValue("1") @QueryParam("pagina") int pagina, @DefaultValue("20") @QueryParam("limite") int limite) {
        try {
            AcademicoProcesos procesos = new AcademicoProcesos();
            Collection<Inscripcion> datos = procesos.consultarInscripciones(pagina, limite);
            return Response.ok(Map.of("datos", datos, "pagina", pagina, "limite", limite, "total", procesos.contarInscripciones())).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("inscripciones")
    public Response inscribirEstudiante(Inscripcion inscripcion) {
        try {
            new AcademicoServicio().inscribirEstudiante(inscripcion);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Estudiante inscrito correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("asignaciones/{idAsignacion}/asistencias")
    public Response consultarAsistencias(@PathParam("idAsignacion") int idAsignacion) {
        try {
            return Response.ok(new AcademicoProcesos().consultarAsistencia(idAsignacion)).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("asignaciones/{idAsignacion}/maestros/{idEmpleado}/asistencias")
    public Response registrarAsistencia(@PathParam("idAsignacion") int idAsignacion, @PathParam("idEmpleado") int idEmpleado, Asistencia asistencia) {
        asistencia.setIdAsignacion(idAsignacion);

        try {
            new AcademicoServicio().registrarAsistencia(asistencia, idEmpleado);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Asistencia registrada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("asignaciones/{idAsignacion}/zonas")
    public Response consultarZonas(@PathParam("idAsignacion") int idAsignacion) {
        try {
            return Response.ok(new AcademicoProcesos().consultarZonas(idAsignacion)).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("asignaciones/{idAsignacion}/maestros/{idEmpleado}/zonas")
    public Response definirZona(@PathParam("idAsignacion") int idAsignacion, @PathParam("idEmpleado") int idEmpleado, Zona zona) {
        zona.setIdAsignacion(idAsignacion);

        try {
            new AcademicoServicio().definirZona(zona, idEmpleado);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Zona definida correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("asignaciones/{idAsignacion}/notas")
    public Response consultarNotas(@PathParam("idAsignacion") int idAsignacion) {
        try {
            return Response.ok(new AcademicoProcesos().consultarNotas(idAsignacion)).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("maestros/{idEmpleado}/notas")
    public Response modificarNota(@PathParam("idEmpleado") int idEmpleado, Nota nota) {
        try {
            new AcademicoServicio().modificarNota(nota, idEmpleado);
            return Response.ok(Map.of("mensaje", "Nota registrada o modificada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("estudiantes/{idEstudiante}/notas")
    public Response consultarHistorialNotas(@PathParam("idEstudiante") int idEstudiante) {
        try {
            return Response.ok(new AcademicoProcesos().consultarHistorialNotas(idEstudiante)).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("carreras")
    public Response consultarCarreras() {
        try {
            return Response.ok(new AcademicoProcesos().consultarCarreras()).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("carreras")
    public Response crearCarrera(Carrera carrera) {
        try {
            new AcademicoServicio().crearCarrera(carrera);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Carrera creada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("carreras/{idCarrera}")
    public Response modificarCarrera(@PathParam("idCarrera") int idCarrera, Carrera carrera) {
        carrera.setIdCarrera(idCarrera);

        try {
            new AcademicoServicio().actualizarCarrera(carrera);
            return Response.ok(Map.of("mensaje", "Carrera modificada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @DELETE
    @Path("carreras/{idCarrera}")
    public Response desactivarCarrera(@PathParam("idCarrera") int idCarrera) {
        try {
            new AcademicoServicio().desactivarCarrera(idCarrera);
            return Response.ok(Map.of("mensaje", "Carrera desactivada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("curriculos")
    public Response crearCurriculo(Curriculo curriculo) {
        try {
            new AcademicoServicio().crearCurriculo(curriculo);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Currículo creado correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("curriculos/{idCurriculo}/cursos")
    public Response establecerCurriculo(@PathParam("idCurriculo") int idCurriculo, int[] idCursos) {
        try {
            new AcademicoServicio().establecerCurriculo(idCurriculo, idCursos);
            return Response.ok(Map.of("mensaje", "Cursos del currículo actualizados correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("curriculos")
    public Response consultarCurriculo(@QueryParam("grado") int idGrado, @QueryParam("ciclo") int idCiclo) {
        try {
            return Response.ok(new AcademicoProcesos().consultarCurriculo(idGrado, idCiclo)).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    private Response error(String codigo, String mensaje) {
        Response.Status estado = "DATOS_INVALIDOS".equals(codigo) ? Response.Status.BAD_REQUEST
                : "NO_ENCONTRADO".equals(codigo) ? Response.Status.NOT_FOUND
                : "CONFLICTO".equals(codigo) ? Response.Status.CONFLICT
                : "NO_AUTORIZADO".equals(codigo) ? Response.Status.FORBIDDEN
                : Response.Status.INTERNAL_SERVER_ERROR;

        return Response.status(estado).entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
    }
}
