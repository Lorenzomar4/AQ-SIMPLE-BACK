package com.lorenzomar3.AQ.Service;

import com.lorenzomar3.AQ.Controller.TemarioController;
import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Repository.AResponderRepository;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.*;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.dto.newDto.IssueWhitItemsDTO;
import com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.FabricaDePreguntas;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.TipoAResponder;
import com.lorenzomar3.AQ.projections.IssueOrQuestionnaireProjection;
import com.lorenzomar3.AQ.projections.QuestionnaireItem;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class PreguntaService {

    public PreguntaService() {
    }

    private static final Logger logger = LoggerFactory.getLogger(PreguntaService.class);

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
        logger.info("obtenerPregunta");
        return (Pregunta) mapDeRepositorios.get(tipo).findById(idPregunta).orElseThrow(
                () -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));
    }


    public Pregunta obtenerPreguntaFull(ObtenerPreguntaDTO getQuestionDTO) {
        logger.info("obtenerPreguntaFull");

        TipoAResponder tipo = getQuestionDTO.getTipoAResponder();
        Long id = getQuestionDTO.getId();

        return (Pregunta) mapDeRepositorios.get(tipo)
                .findByIdWithTeoriaDeLaPregunta(id)
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));
    }


    @Transactional
    public AResponder createaQuestion(PostPreguntaDTO preguntaDTO) {
        logger.info("createaQuestion");


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
        logger.info("delete");

        preguntaRepository.deleteById(id);
    }


    @Transactional
    public Pregunta updateQuestion(PostPreguntaDTO preguntaDTO) {
        logger.info("updateQuestion");
        Pregunta pregunta = obtenerPregunta(preguntaDTO.getId(), preguntaDTO.getTipo());
        BeanUtils.copyProperties(preguntaDTO, pregunta);
        return preguntaRepository.save(pregunta);
    }

    @Transactional(readOnly = true)
    public Boolean verifyResponse(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        logger.info("verifyResponse");

        JsonVisualizador.verJson(respuestaDePreguntaDTO);
        Pregunta pregunta = obtenerPregunta(respuestaDePreguntaDTO.getIdPregunta(), respuestaDePreguntaDTO.getTipoDePregunta());

        return pregunta.verificarSiLaRespuestaEsCorrectaYAsignarCriticos(respuestaDePreguntaDTO);

    }


    @Transactional(readOnly = true)
    public IssueWhitItemsDTO getIssueItems(Long id) {
        logger.info("Se trae al cuestionario/tema padre");
        IssueOrQuestionnaireProjection temario = temarioRepository.findByIdBasic(id)
                .orElseThrow(() -> new BussinesException("No existe ese cuestionario"));

        Long idd = temario.getId();
        List<QuestionnaireItem> itemList;
        try {
            logger.info("Se trae todo el contenido perteneciente al cuestionario/tema con id" + id);
            itemList = aResponderRepository.getIssueItems(id);
        } catch (Exception e) {
            logger.error(e.getMessage());
            itemList = Collections.emptyList();
        }

        IssueWhitItemsDTO issueWhitItemsDTO = new IssueWhitItemsDTO(id, temario.getName(), temario.getCreationDate(), temario.getFatherId(), itemList);


        return issueWhitItemsDTO;
    }


}


