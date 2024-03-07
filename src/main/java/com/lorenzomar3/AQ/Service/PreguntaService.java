package com.lorenzomar3.AQ.Service;

import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Repository.AResponderRepository;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.*;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.FabricaDePreguntas;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.TipoAResponder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
public class PreguntaService {

    public PreguntaService() {
    }

    @Autowired
    PreguntaRepository preguntaRepository;

    @Autowired
    SeleccionUnicaRepository seleccionUnicaRepository;

    @Autowired
    OpcionMultipleRepository opcionMultipleRepository;

    @Autowired
    DesplegableCompartidoRepositorio desplegableCompartidoRepositorio;

    @Autowired
    DesplegableIndependienteRepository desplegableIndependienteRepository;

    @Autowired
    AResponderRepository aResponderRepository;

    @Autowired
    TemarioRepository temarioRepository;

    HashMap<TipoAResponder, BasePreguntaRepositorio<?>> mapDeRepositorios = new HashMap<>();

    @PostConstruct
    private void init() {
        mapDeRepositorios.put(TipoAResponder.PREGUNTA_SIMPLE, preguntaRepository);
        mapDeRepositorios.put(TipoAResponder.VERDADERO_FALSO, preguntaRepository);
        mapDeRepositorios.put(TipoAResponder.SELECCION_UNICA, seleccionUnicaRepository);
        mapDeRepositorios.put(TipoAResponder.OPCION_MULTIPLE, opcionMultipleRepository);
        mapDeRepositorios.put(TipoAResponder.DESPLEGABLE_COMPARTIDO, desplegableCompartidoRepositorio);
        mapDeRepositorios.put(TipoAResponder.DESPLEGABLE_INDEPENDIENTE, desplegableIndependienteRepository);
    }


    public Pregunta obtenerPregunta(Long idPregunta, TipoAResponder tipo) {

        return (Pregunta) mapDeRepositorios.get(tipo).findById(idPregunta).orElseThrow(
                () -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));
    }


    public Pregunta obtenerPreguntaFull(ObtenerPreguntaDTO getQuestionDTO) {
        TipoAResponder tipo = getQuestionDTO.getTipoAResponder();
        Long id = getQuestionDTO.getId();

        return (Pregunta) mapDeRepositorios.get(tipo)
                .findByIdWithTeoriaDeLaPregunta(id)
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));
    }


    @Transactional
    public AResponder createaQuestion(PostPreguntaDTO preguntaDTO) {

        Temario temario = temarioRepository.findById(preguntaDTO.getIdTemarioPerteneciente())
                .orElseThrow(() -> new BussinesException("No existe ese cuestionario"));

        FabricaDePreguntas fabricaDePreguntas = new FabricaDePreguntas();
        AResponder pregunta = fabricaDePreguntas.fromJSON(preguntaDTO);

        temario.agregarALaLista(pregunta);

        temarioRepository.save(temario);

        return pregunta;
    }

    @Transactional
    public void delete(Long id) {
        preguntaRepository.deleteById(id);
    }


    @Transactional
    public Pregunta updateQuestion(PostPreguntaDTO preguntaDTO) {
        Pregunta pregunta = obtenerPregunta(preguntaDTO.getId(), preguntaDTO.getTipo());
        BeanUtils.copyProperties(preguntaDTO, pregunta);
        return preguntaRepository.save(pregunta);
    }

    @Transactional(readOnly = true)
    public Boolean verifyResponse(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        JsonVisualizador.verJson(respuestaDePreguntaDTO);
        Pregunta pregunta = obtenerPregunta(respuestaDePreguntaDTO.getIdPregunta(), respuestaDePreguntaDTO.getTipoDePregunta());

        return pregunta.verificarSiLaRespuestaEsCorrectaYAsignarCriticos(respuestaDePreguntaDTO);

    }


}


