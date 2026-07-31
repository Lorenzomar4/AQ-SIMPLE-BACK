package com.lorenzomar3.AQ.content;

import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaRepository;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.SeleccionUnicaRepository;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.content.application.port.in.CrearSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import com.lorenzomar3.AQ.dto.newDto.CreateQuestionResponseDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SeleccionUnicaParidadTest {

    @Autowired
    private TemarioRepository temarioRepository;

    @Autowired
    private PreguntaRepository preguntaRepositoryViejo;

    @Autowired
    private SeleccionUnicaRepository seleccionUnicaRepositoryViejo;

    @Autowired
    private PreguntaService preguntaServiceViejo;

    @Autowired
    private CrearSeleccionUnicaUseCase crearSeleccionUnicaUseCase;

    @Autowired
    private EditarSeleccionUnicaUseCase editarSeleccionUnicaUseCase;

    @Autowired
    private EliminarSeleccionUnicaUseCase eliminarSeleccionUnicaUseCase;

    @Autowired
    private SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort;

    private Long temarioId;
    private final List<Long> preguntasCreadas = new ArrayList<>();

    @BeforeEach
    void crearTemarioDePrueba() {
        Temario temario = new Temario("Temario de prueba paridad SeleccionUnica", TipoAResponder.CUESTIONARIO);
        temarioId = temarioRepository.save(temario).getId();
    }

    @AfterEach
    void limpiar() {
        preguntasCreadas.forEach(id -> {
            try {
                preguntaRepositoryViejo.deleteById(id);
            } catch (Exception ignored) {
            }
        });
        temarioRepository.deleteById(temarioId);
    }

    private Long obtenerUltimoHijo() {
        Temario temario = temarioRepository.findById(temarioId).orElseThrow();
        return temario.getListaAResponder().stream().mapToLong(AResponder::getId).max().orElseThrow();
    }

    private List<Opcion> opcionesDeCreacion() {
        List<Opcion> lista = new ArrayList<>();
        lista.add(new Opcion("River Plate", false));
        lista.add(new Opcion("Boca Juniors", true));
        lista.add(new Opcion("Independiente", false));
        return lista;
    }

    private PostPreguntaDTO dtoCreacion(String titulo, String descripcion, List<Opcion> opciones) {
        return new PostPreguntaDTO(null, titulo, descripcion, TipoAResponder.SELECCION_UNICA, temarioId,
                null, null, null, opciones, null, null);
    }

    @Test
    void crearSeleccionUnicaProduceElMismoResultadoQueElFlujoViejo() {
        PostPreguntaDTO dtoViejo = dtoCreacion("Pregunta de prueba", "Descripcion de prueba", opcionesDeCreacion());
        PostPreguntaDTO dtoNuevo = dtoCreacion("Pregunta de prueba", "Descripcion de prueba", opcionesDeCreacion());

        CreateQuestionResponseDTO respuestaVieja = preguntaServiceViejo.createaQuestion(dtoViejo);
        Long idPreguntaVieja = obtenerUltimoHijo();
        preguntasCreadas.add(idPreguntaVieja);

        CreateQuestionResponseDTO respuestaNueva = crearSeleccionUnicaUseCase.crear(dtoNuevo);
        Long idPreguntaNueva = obtenerUltimoHijo();
        preguntasCreadas.add(idPreguntaNueva);

        assertEquals(respuestaVieja, respuestaNueva);

        var entidadVieja = seleccionUnicaRepositoryViejo.findById(idPreguntaVieja).orElseThrow();
        var entidadNueva = seleccionUnicaRepositoryPort.findById(idPreguntaNueva).orElseThrow();

        assertEquals(entidadVieja.getTitulo(), entidadNueva.getTitulo());
        assertEquals(entidadVieja.getDescripcion(), entidadNueva.getDescripcion());
        assertEquals(entidadVieja.getTipo(), entidadNueva.getTipo());
        assertEquals(entidadVieja.getIdDuenio(), entidadNueva.getIdDuenio());
        assertEquals(entidadVieja.getIntentosParaQueDejeDeSerCriticoDisponible(), entidadNueva.getIntentosParaQueDejeDeSerCriticoDisponible());

        var opcionesViejasOrdenadas = entidadVieja.getListaDeOpcionesConSuRespuestaReal().stream()
                .sorted(Comparator.comparing(Opcion::getOpcion)).toList();
        var opcionesNuevasOrdenadas = entidadNueva.getListaDeOpciones().stream()
                .sorted(Comparator.comparing(com.lorenzomar3.AQ.content.domain.Opcion::getOpcion)).toList();

        assertEquals(3, opcionesViejasOrdenadas.size());
        assertEquals(opcionesViejasOrdenadas.size(), opcionesNuevasOrdenadas.size());
        for (int i = 0; i < opcionesViejasOrdenadas.size(); i++) {
            assertEquals(opcionesViejasOrdenadas.get(i).getOpcion(), opcionesNuevasOrdenadas.get(i).getOpcion());
            assertEquals(opcionesViejasOrdenadas.get(i).getRespuestaCorrecta(), opcionesNuevasOrdenadas.get(i).getLaRespuestaEs());
        }
    }

    @Test
    void editarSeleccionUnicaReemplazaLasOpcionesIgualQueElFlujoViejo() {
        Long idViejo;
        Long idNuevo;

        preguntaServiceViejo.createaQuestion(dtoCreacion("Original", "Desc original", opcionesDeCreacion()));
        idViejo = obtenerUltimoHijo();
        preguntasCreadas.add(idViejo);

        crearSeleccionUnicaUseCase.crear(dtoCreacion("Original", "Desc original", opcionesDeCreacion()));
        idNuevo = obtenerUltimoHijo();
        preguntasCreadas.add(idNuevo);

        var entidadPreviaVieja = preguntaRepositoryViejo.findById(idViejo).orElseThrow();
        entidadPreviaVieja.setIntentosParaQueDejeDeSerCriticoDisponible(2);
        preguntaRepositoryViejo.save(entidadPreviaVieja);

        var entidadPreviaNueva = preguntaRepositoryViejo.findById(idNuevo).orElseThrow();
        entidadPreviaNueva.setIntentosParaQueDejeDeSerCriticoDisponible(2);
        preguntaRepositoryViejo.save(entidadPreviaNueva);

        List<Opcion> opcionesEdicionVieja = new ArrayList<>();
        opcionesEdicionVieja.add(new Opcion("Newells", true));
        opcionesEdicionVieja.add(new Opcion("Central", false));

        List<Opcion> opcionesEdicionNueva = new ArrayList<>();
        opcionesEdicionNueva.add(new Opcion("Newells", true));
        opcionesEdicionNueva.add(new Opcion("Central", false));

        PostPreguntaDTO dtoEdicionViejo = new PostPreguntaDTO(idViejo, "Editado", "Desc editada",
                TipoAResponder.SELECCION_UNICA, temarioId, null, null, null, opcionesEdicionVieja, null, null);
        PostPreguntaDTO dtoEdicionNuevo = new PostPreguntaDTO(idNuevo, "Editado", "Desc editada",
                TipoAResponder.SELECCION_UNICA, temarioId, null, null, null, opcionesEdicionNueva, null, null);

        preguntaServiceViejo.updateQuestion(dtoEdicionViejo);
        var entidadViejaEditada = seleccionUnicaRepositoryViejo.findById(idViejo).orElseThrow();

        var entidadNuevaEditada = editarSeleccionUnicaUseCase.editar(dtoEdicionNuevo);

        assertEquals(entidadViejaEditada.getTitulo(), entidadNuevaEditada.getTitulo());
        assertEquals(entidadViejaEditada.getDescripcion(), entidadNuevaEditada.getDescripcion());
        assertEquals(entidadViejaEditada.getIntentosParaQueDejeDeSerCriticoDisponible(), entidadNuevaEditada.getIntentosParaQueDejeDeSerCriticoDisponible());
        assertEquals(2, entidadNuevaEditada.getIntentosParaQueDejeDeSerCriticoDisponible());

        var opcionesViejasOrdenadas = entidadViejaEditada.getListaDeOpcionesConSuRespuestaReal().stream()
                .sorted(Comparator.comparing(Opcion::getOpcion)).toList();
        var opcionesNuevasOrdenadas = entidadNuevaEditada.getListaDeOpciones().stream()
                .sorted(Comparator.comparing(com.lorenzomar3.AQ.content.domain.Opcion::getOpcion)).toList();

        assertEquals(2, opcionesViejasOrdenadas.size());
        assertEquals(opcionesViejasOrdenadas.size(), opcionesNuevasOrdenadas.size());
        for (int i = 0; i < opcionesViejasOrdenadas.size(); i++) {
            assertEquals(opcionesViejasOrdenadas.get(i).getOpcion(), opcionesNuevasOrdenadas.get(i).getOpcion());
            assertEquals(opcionesViejasOrdenadas.get(i).getRespuestaCorrecta(), opcionesNuevasOrdenadas.get(i).getLaRespuestaEs());
        }
    }

    @Test
    void eliminarSeleccionUnicaUseCaseBorraLaPreguntaCreada() {
        crearSeleccionUnicaUseCase.crear(dtoCreacion("A borrar", "Desc", opcionesDeCreacion()));
        Long id = obtenerUltimoHijo();

        eliminarSeleccionUnicaUseCase.eliminar(id);

        assertTrue(seleccionUnicaRepositoryPort.findById(id).isEmpty());
    }
}
