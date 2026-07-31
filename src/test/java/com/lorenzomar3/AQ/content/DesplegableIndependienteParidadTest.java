package com.lorenzomar3.AQ.content;

import com.lorenzomar3.AQ.Repository.PreguntaRepository.DesplegableIndependienteRepository;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaRepository;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import com.lorenzomar3.AQ.dto.newDto.CreateQuestionResponseDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
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
class DesplegableIndependienteParidadTest {

    @Autowired
    private TemarioRepository temarioRepository;

    @Autowired
    private PreguntaRepository preguntaRepositoryViejo;

    @Autowired
    private DesplegableIndependienteRepository desplegableIndependienteRepositoryViejo;

    @Autowired
    private PreguntaService preguntaServiceViejo;

    @Autowired
    private CrearDesplegableIndependienteUseCase crearDesplegableIndependienteUseCase;

    @Autowired
    private EditarDesplegableIndependienteUseCase editarDesplegableIndependienteUseCase;

    @Autowired
    private EliminarDesplegableIndependienteUseCase eliminarDesplegableIndependienteUseCase;

    @Autowired
    private DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort;

    private Long temarioId;
    private final List<Long> preguntasCreadas = new ArrayList<>();

    @BeforeEach
    void crearTemarioDePrueba() {
        Temario temario = new Temario("Temario de prueba paridad DesplegableIndependiente", TipoAResponder.CUESTIONARIO);
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

    private List<SeleccionUnicaParaDesplegableIndependiente> subPreguntasDeCreacion() {
        List<SeleccionUnicaParaDesplegableIndependiente> lista = new ArrayList<>();
        lista.add(new SeleccionUnicaParaDesplegableIndependiente("Los mamiferos son", List.of(
                new Opcion("Vertebrados", true),
                new Opcion("Invertebrados", false)
        )));
        lista.add(new SeleccionUnicaParaDesplegableIndependiente("Los peces son", List.of(
                new Opcion("Acuaticos", true),
                new Opcion("Terrestres", false)
        )));
        return lista;
    }

    private PostPreguntaDTO dtoCreacion(String titulo, String descripcion, List<SeleccionUnicaParaDesplegableIndependiente> subPreguntas) {
        return new PostPreguntaDTO(null, titulo, descripcion, TipoAResponder.DESPLEGABLE_INDEPENDIENTE, temarioId,
                null, null, null, null, null, subPreguntas);
    }

    @Test
    void crearDesplegableIndependienteProduceElMismoResultadoQueElFlujoViejo() {
        PostPreguntaDTO dtoViejo = dtoCreacion("Pregunta de prueba", "Descripcion de prueba", subPreguntasDeCreacion());
        PostPreguntaDTO dtoNuevo = dtoCreacion("Pregunta de prueba", "Descripcion de prueba", subPreguntasDeCreacion());

        CreateQuestionResponseDTO respuestaVieja = preguntaServiceViejo.createaQuestion(dtoViejo);
        Long idPreguntaVieja = obtenerUltimoHijo();
        preguntasCreadas.add(idPreguntaVieja);

        CreateQuestionResponseDTO respuestaNueva = crearDesplegableIndependienteUseCase.crear(dtoNuevo);
        Long idPreguntaNueva = obtenerUltimoHijo();
        preguntasCreadas.add(idPreguntaNueva);

        assertEquals(respuestaVieja, respuestaNueva);

        var entidadVieja = desplegableIndependienteRepositoryViejo.findById(idPreguntaVieja).orElseThrow();
        var entidadNueva = desplegableIndependienteRepositoryPort.findById(idPreguntaNueva).orElseThrow();

        assertEquals(entidadVieja.getTitulo(), entidadNueva.getTitulo());
        assertEquals(entidadVieja.getDescripcion(), entidadNueva.getDescripcion());
        assertEquals(entidadVieja.getTipo(), entidadNueva.getTipo());
        assertEquals(entidadVieja.getIdDuenio(), entidadNueva.getIdDuenio());
        assertEquals(entidadVieja.getIntentosParaQueDejeDeSerCriticoDisponible(), entidadNueva.getIntentosParaQueDejeDeSerCriticoDisponible());

        var subPreguntasViejasOrdenadas = entidadVieja.getListaDeOpcionDesplegableIndependiente().stream()
                .sorted(Comparator.comparing(SeleccionUnicaParaDesplegableIndependiente::getTitulo)).toList();
        var subPreguntasNuevasOrdenadas = entidadNueva.getListaDeOpciones().stream()
                .sorted(Comparator.comparing(com.lorenzomar3.AQ.content.domain.SeleccionUnicaParaDesplegableIndependiente::getTitulo)).toList();

        assertEquals(2, subPreguntasViejasOrdenadas.size());
        assertEquals(subPreguntasViejasOrdenadas.size(), subPreguntasNuevasOrdenadas.size());
        for (int i = 0; i < subPreguntasViejasOrdenadas.size(); i++) {
            var subViejo = subPreguntasViejasOrdenadas.get(i);
            var subNuevo = subPreguntasNuevasOrdenadas.get(i);
            assertEquals(subViejo.getTitulo(), subNuevo.getTitulo());

            var opcionesViejasOrdenadas = subViejo.getListaDeOpcionesDisponible().stream()
                    .sorted(Comparator.comparing(Opcion::getOpcion)).toList();
            var opcionesNuevasOrdenadas = subNuevo.getListaDeOpciones().stream()
                    .sorted(Comparator.comparing(com.lorenzomar3.AQ.content.domain.Opcion::getOpcion)).toList();

            assertEquals(2, opcionesViejasOrdenadas.size());
            assertEquals(opcionesViejasOrdenadas.size(), opcionesNuevasOrdenadas.size());
            for (int j = 0; j < opcionesViejasOrdenadas.size(); j++) {
                assertEquals(opcionesViejasOrdenadas.get(j).getOpcion(), opcionesNuevasOrdenadas.get(j).getOpcion());
                assertEquals(opcionesViejasOrdenadas.get(j).getRespuestaCorrecta(), opcionesNuevasOrdenadas.get(j).getLaRespuestaEs());
            }
        }
    }

    @Test
    void editarDesplegableIndependienteReemplazaLaEstructuraAnidadaIgualQueElFlujoViejo() {
        Long idViejo;
        Long idNuevo;

        preguntaServiceViejo.createaQuestion(dtoCreacion("Original", "Desc original", subPreguntasDeCreacion()));
        idViejo = obtenerUltimoHijo();
        preguntasCreadas.add(idViejo);

        crearDesplegableIndependienteUseCase.crear(dtoCreacion("Original", "Desc original", subPreguntasDeCreacion()));
        idNuevo = obtenerUltimoHijo();
        preguntasCreadas.add(idNuevo);

        var entidadPreviaVieja = preguntaRepositoryViejo.findById(idViejo).orElseThrow();
        entidadPreviaVieja.setIntentosParaQueDejeDeSerCriticoDisponible(2);
        preguntaRepositoryViejo.save(entidadPreviaVieja);

        var entidadPreviaNueva = preguntaRepositoryViejo.findById(idNuevo).orElseThrow();
        entidadPreviaNueva.setIntentosParaQueDejeDeSerCriticoDisponible(2);
        preguntaRepositoryViejo.save(entidadPreviaNueva);

        List<SeleccionUnicaParaDesplegableIndependiente> subPreguntasEdicionVieja = new ArrayList<>();
        subPreguntasEdicionVieja.add(new SeleccionUnicaParaDesplegableIndependiente("Los reptiles son", List.of(
                new Opcion("De sangre fria", true),
                new Opcion("De sangre caliente", false)
        )));

        List<SeleccionUnicaParaDesplegableIndependiente> subPreguntasEdicionNueva = new ArrayList<>();
        subPreguntasEdicionNueva.add(new SeleccionUnicaParaDesplegableIndependiente("Los reptiles son", List.of(
                new Opcion("De sangre fria", true),
                new Opcion("De sangre caliente", false)
        )));

        PostPreguntaDTO dtoEdicionViejo = new PostPreguntaDTO(idViejo, "Editado", "Desc editada",
                TipoAResponder.DESPLEGABLE_INDEPENDIENTE, temarioId, null, null, null, null, null, subPreguntasEdicionVieja);
        PostPreguntaDTO dtoEdicionNuevo = new PostPreguntaDTO(idNuevo, "Editado", "Desc editada",
                TipoAResponder.DESPLEGABLE_INDEPENDIENTE, temarioId, null, null, null, null, null, subPreguntasEdicionNueva);

        preguntaServiceViejo.updateQuestion(dtoEdicionViejo);
        var entidadViejaEditada = desplegableIndependienteRepositoryViejo.findById(idViejo).orElseThrow();

        var entidadNuevaEditada = editarDesplegableIndependienteUseCase.editar(dtoEdicionNuevo);

        assertEquals(entidadViejaEditada.getTitulo(), entidadNuevaEditada.getTitulo());
        assertEquals(entidadViejaEditada.getDescripcion(), entidadNuevaEditada.getDescripcion());
        assertEquals(entidadViejaEditada.getIntentosParaQueDejeDeSerCriticoDisponible(), entidadNuevaEditada.getIntentosParaQueDejeDeSerCriticoDisponible());
        assertEquals(2, entidadNuevaEditada.getIntentosParaQueDejeDeSerCriticoDisponible());

        var subPreguntasViejasOrdenadas = entidadViejaEditada.getListaDeOpcionDesplegableIndependiente().stream()
                .sorted(Comparator.comparing(SeleccionUnicaParaDesplegableIndependiente::getTitulo)).toList();
        var subPreguntasNuevasOrdenadas = entidadNuevaEditada.getListaDeOpciones().stream()
                .sorted(Comparator.comparing(com.lorenzomar3.AQ.content.domain.SeleccionUnicaParaDesplegableIndependiente::getTitulo)).toList();

        assertEquals(1, subPreguntasViejasOrdenadas.size());
        assertEquals(subPreguntasViejasOrdenadas.size(), subPreguntasNuevasOrdenadas.size());
        for (int i = 0; i < subPreguntasViejasOrdenadas.size(); i++) {
            var subViejo = subPreguntasViejasOrdenadas.get(i);
            var subNuevo = subPreguntasNuevasOrdenadas.get(i);
            assertEquals(subViejo.getTitulo(), subNuevo.getTitulo());

            var opcionesViejasOrdenadas = subViejo.getListaDeOpcionesDisponible().stream()
                    .sorted(Comparator.comparing(Opcion::getOpcion)).toList();
            var opcionesNuevasOrdenadas = subNuevo.getListaDeOpciones().stream()
                    .sorted(Comparator.comparing(com.lorenzomar3.AQ.content.domain.Opcion::getOpcion)).toList();

            assertEquals(2, opcionesViejasOrdenadas.size());
            assertEquals(opcionesViejasOrdenadas.size(), opcionesNuevasOrdenadas.size());
            for (int j = 0; j < opcionesViejasOrdenadas.size(); j++) {
                assertEquals(opcionesViejasOrdenadas.get(j).getOpcion(), opcionesNuevasOrdenadas.get(j).getOpcion());
                assertEquals(opcionesViejasOrdenadas.get(j).getRespuestaCorrecta(), opcionesNuevasOrdenadas.get(j).getLaRespuestaEs());
            }
        }
    }

    @Test
    void eliminarDesplegableIndependienteUseCaseBorraLaPreguntaCreada() {
        crearDesplegableIndependienteUseCase.crear(dtoCreacion("A borrar", "Desc", subPreguntasDeCreacion()));
        Long id = obtenerUltimoHijo();

        eliminarDesplegableIndependienteUseCase.eliminar(id);

        assertTrue(desplegableIndependienteRepositoryPort.findById(id).isEmpty());
    }
}
