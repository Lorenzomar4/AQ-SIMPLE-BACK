package com.lorenzomar3.AQ.content;

import com.lorenzomar3.AQ.Repository.PreguntaRepository.DesplegableCompartidoRepositorio;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaRepository;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableCompartidoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarDesplegableCompartidoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarDesplegableCompartidoUseCase;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableCompartidoRepositoryPort;
import com.lorenzomar3.AQ.dto.newDto.CreateQuestionResponseDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
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
class DesplegableCompartidoParidadTest {

    @Autowired
    private TemarioRepository temarioRepository;

    @Autowired
    private PreguntaRepository preguntaRepositoryViejo;

    @Autowired
    private DesplegableCompartidoRepositorio desplegableCompartidoRepositoryViejo;

    @Autowired
    private PreguntaService preguntaServiceViejo;

    @Autowired
    private CrearDesplegableCompartidoUseCase crearDesplegableCompartidoUseCase;

    @Autowired
    private EditarDesplegableCompartidoUseCase editarDesplegableCompartidoUseCase;

    @Autowired
    private EliminarDesplegableCompartidoUseCase eliminarDesplegableCompartidoUseCase;

    @Autowired
    private DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort;

    private Long temarioId;
    private final List<Long> preguntasCreadas = new ArrayList<>();

    @BeforeEach
    void crearTemarioDePrueba() {
        Temario temario = new Temario("Temario de prueba paridad DesplegableCompartido", TipoAResponder.CUESTIONARIO);
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

    private List<OpcionDeDesplegableCompartido> opcionesDeCreacion() {
        List<OpcionDeDesplegableCompartido> lista = new ArrayList<>();
        lista.add(new OpcionDeDesplegableCompartido("River Plate es un club de", "futbol"));
        lista.add(new OpcionDeDesplegableCompartido("Boca Juniors es un club de", "futbol"));
        lista.add(new OpcionDeDesplegableCompartido("Independiente es un club de", "futbol"));
        return lista;
    }

    private PostPreguntaDTO dtoCreacion(String titulo, String descripcion, List<OpcionDeDesplegableCompartido> opciones) {
        return new PostPreguntaDTO(null, titulo, descripcion, TipoAResponder.DESPLEGABLE_COMPARTIDO, temarioId,
                null, null, null, null, opciones, null);
    }

    @Test
    void crearDesplegableCompartidoProduceElMismoResultadoQueElFlujoViejo() {
        PostPreguntaDTO dtoViejo = dtoCreacion("Pregunta de prueba", "Descripcion de prueba", opcionesDeCreacion());
        PostPreguntaDTO dtoNuevo = dtoCreacion("Pregunta de prueba", "Descripcion de prueba", opcionesDeCreacion());

        CreateQuestionResponseDTO respuestaVieja = preguntaServiceViejo.createaQuestion(dtoViejo);
        Long idPreguntaVieja = obtenerUltimoHijo();
        preguntasCreadas.add(idPreguntaVieja);

        CreateQuestionResponseDTO respuestaNueva = crearDesplegableCompartidoUseCase.crear(dtoNuevo);
        Long idPreguntaNueva = obtenerUltimoHijo();
        preguntasCreadas.add(idPreguntaNueva);

        assertEquals(respuestaVieja, respuestaNueva);

        var entidadVieja = desplegableCompartidoRepositoryViejo.findById(idPreguntaVieja).orElseThrow();
        var entidadNueva = desplegableCompartidoRepositoryPort.findById(idPreguntaNueva).orElseThrow();

        assertEquals(entidadVieja.getTitulo(), entidadNueva.getTitulo());
        assertEquals(entidadVieja.getDescripcion(), entidadNueva.getDescripcion());
        assertEquals(entidadVieja.getTipo(), entidadNueva.getTipo());
        assertEquals(entidadVieja.getIdDuenio(), entidadNueva.getIdDuenio());
        assertEquals(entidadVieja.getIntentosParaQueDejeDeSerCriticoDisponible(), entidadNueva.getIntentosParaQueDejeDeSerCriticoDisponible());

        var opcionesViejasOrdenadas = entidadVieja.getListaDeOpcionDesplegableCompartido().stream()
                .sorted(Comparator.comparing(OpcionDeDesplegableCompartido::getPregunta)).toList();
        var opcionesNuevasOrdenadas = entidadNueva.getListaDeOpciones().stream()
                .sorted(Comparator.comparing(com.lorenzomar3.AQ.content.domain.OpcionDeDesplegableCompartido::getPregunta)).toList();

        assertEquals(3, opcionesViejasOrdenadas.size());
        assertEquals(opcionesViejasOrdenadas.size(), opcionesNuevasOrdenadas.size());
        for (int i = 0; i < opcionesViejasOrdenadas.size(); i++) {
            assertEquals(opcionesViejasOrdenadas.get(i).getPregunta(), opcionesNuevasOrdenadas.get(i).getPregunta());
            assertEquals(opcionesViejasOrdenadas.get(i).getRespuestaCorrecta(), opcionesNuevasOrdenadas.get(i).getRespuesta());
        }
    }

    @Test
    void editarDesplegableCompartidoReemplazaLasOpcionesIgualQueElFlujoViejo() {
        Long idViejo;
        Long idNuevo;

        preguntaServiceViejo.createaQuestion(dtoCreacion("Original", "Desc original", opcionesDeCreacion()));
        idViejo = obtenerUltimoHijo();
        preguntasCreadas.add(idViejo);

        crearDesplegableCompartidoUseCase.crear(dtoCreacion("Original", "Desc original", opcionesDeCreacion()));
        idNuevo = obtenerUltimoHijo();
        preguntasCreadas.add(idNuevo);

        var entidadPreviaVieja = preguntaRepositoryViejo.findById(idViejo).orElseThrow();
        entidadPreviaVieja.setIntentosParaQueDejeDeSerCriticoDisponible(2);
        preguntaRepositoryViejo.save(entidadPreviaVieja);

        var entidadPreviaNueva = preguntaRepositoryViejo.findById(idNuevo).orElseThrow();
        entidadPreviaNueva.setIntentosParaQueDejeDeSerCriticoDisponible(2);
        preguntaRepositoryViejo.save(entidadPreviaNueva);

        List<OpcionDeDesplegableCompartido> opcionesEdicionVieja = new ArrayList<>();
        opcionesEdicionVieja.add(new OpcionDeDesplegableCompartido("Newells es un club de", "futbol"));
        opcionesEdicionVieja.add(new OpcionDeDesplegableCompartido("River Plate fue fundado en", "1901"));

        List<OpcionDeDesplegableCompartido> opcionesEdicionNueva = new ArrayList<>();
        opcionesEdicionNueva.add(new OpcionDeDesplegableCompartido("Newells es un club de", "futbol"));
        opcionesEdicionNueva.add(new OpcionDeDesplegableCompartido("River Plate fue fundado en", "1901"));

        PostPreguntaDTO dtoEdicionViejo = new PostPreguntaDTO(idViejo, "Editado", "Desc editada",
                TipoAResponder.DESPLEGABLE_COMPARTIDO, temarioId, null, null, null, null, opcionesEdicionVieja, null);
        PostPreguntaDTO dtoEdicionNuevo = new PostPreguntaDTO(idNuevo, "Editado", "Desc editada",
                TipoAResponder.DESPLEGABLE_COMPARTIDO, temarioId, null, null, null, null, opcionesEdicionNueva, null);

        preguntaServiceViejo.updateQuestion(dtoEdicionViejo);
        var entidadViejaEditada = desplegableCompartidoRepositoryViejo.findById(idViejo).orElseThrow();

        var entidadNuevaEditada = editarDesplegableCompartidoUseCase.editar(dtoEdicionNuevo);

        assertEquals(entidadViejaEditada.getTitulo(), entidadNuevaEditada.getTitulo());
        assertEquals(entidadViejaEditada.getDescripcion(), entidadNuevaEditada.getDescripcion());
        assertEquals(entidadViejaEditada.getIntentosParaQueDejeDeSerCriticoDisponible(), entidadNuevaEditada.getIntentosParaQueDejeDeSerCriticoDisponible());
        assertEquals(2, entidadNuevaEditada.getIntentosParaQueDejeDeSerCriticoDisponible());

        var opcionesViejasOrdenadas = entidadViejaEditada.getListaDeOpcionDesplegableCompartido().stream()
                .sorted(Comparator.comparing(OpcionDeDesplegableCompartido::getPregunta)).toList();
        var opcionesNuevasOrdenadas = entidadNuevaEditada.getListaDeOpciones().stream()
                .sorted(Comparator.comparing(com.lorenzomar3.AQ.content.domain.OpcionDeDesplegableCompartido::getPregunta)).toList();

        assertEquals(2, opcionesViejasOrdenadas.size());
        assertEquals(opcionesViejasOrdenadas.size(), opcionesNuevasOrdenadas.size());
        for (int i = 0; i < opcionesViejasOrdenadas.size(); i++) {
            assertEquals(opcionesViejasOrdenadas.get(i).getPregunta(), opcionesNuevasOrdenadas.get(i).getPregunta());
            assertEquals(opcionesViejasOrdenadas.get(i).getRespuestaCorrecta(), opcionesNuevasOrdenadas.get(i).getRespuesta());
        }
    }

    @Test
    void eliminarDesplegableCompartidoUseCaseBorraLaPreguntaCreada() {
        crearDesplegableCompartidoUseCase.crear(dtoCreacion("A borrar", "Desc", opcionesDeCreacion()));
        Long id = obtenerUltimoHijo();

        eliminarDesplegableCompartidoUseCase.eliminar(id);

        assertTrue(desplegableCompartidoRepositoryPort.findById(id).isEmpty());
    }
}
