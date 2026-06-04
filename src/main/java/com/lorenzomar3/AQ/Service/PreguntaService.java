package com.lorenzomar3.AQ.Service;

import com.lorenzomar3.AQ.Controller.TemarioController;
import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Repository.AResponderRepository;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.*;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.dto.newDto.*;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.FabricaDePreguntas;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.PreguntaSimple;
import com.lorenzomar3.AQ.model.TipoAResponder;
import com.lorenzomar3.AQ.projections.IssueOrQuestionnaireProjection;
import com.lorenzomar3.AQ.projections.QuestionnaireItem;
import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
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
    PreguntaSimpleRepository preguntaSimpleRepository;

    @Autowired
    TemarioRepository temarioRepository;

    HashMap<TipoAResponder, BasePreguntaRepositorio<?>> mapDeRepositorios = new HashMap<>();

    @PostConstruct
    private void init() {
        mapDeRepositorios.put(TipoAResponder.PREGUNTA_SIMPLE, preguntaSimpleRepository);
        mapDeRepositorios.put(TipoAResponder.VERDADERO_FALSO, preguntaRepository);
        mapDeRepositorios.put(TipoAResponder.SELECCION_UNICA, seleccionUnicaRepository);
        mapDeRepositorios.put(TipoAResponder.OPCION_MULTIPLE, opcionMultipleRepository);
        mapDeRepositorios.put(TipoAResponder.DESPLEGABLE_COMPARTIDO, desplegableCompartidoRepositorio);
        mapDeRepositorios.put(TipoAResponder.DESPLEGABLE_INDEPENDIENTE, desplegableIndependienteRepository);
    }


    public Pregunta obtenerPregunta(Long idPregunta, TipoAResponder tipo) {
        logger.debug("obtenerPregunta id={}, tipo={}", idPregunta, tipo);
        return (Pregunta) mapDeRepositorios.get(tipo).findById(idPregunta).orElseThrow(
                () -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));
    }


    public List<Pregunta> obtenerPreguntaS(List<Long> ids, TipoAResponder tipo) {
        logger.debug("obtenerPreguntas ids={}, tipo={}", ids.size(), tipo);
        return (List<Pregunta>) mapDeRepositorios.get(tipo).findAllById(ids);
    }


    public Pregunta obtenerPreguntaFull(ObtenerPreguntaDTO getQuestionDTO) {
        TipoAResponder tipo = getQuestionDTO.tipoAResponder();
        Long id = getQuestionDTO.id();
        logger.debug("obtenerPreguntaFull id={}, tipo={}", id, tipo);

        return (Pregunta) mapDeRepositorios.get(tipo)
                .findByIdWithTeoriaDeLaPregunta(id)
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));
    }


    @Transactional
    public CreateQuestionResponseDTO createaQuestion(PostPreguntaDTO preguntaDTO) {
        logger.info("Creando pregunta tipo={} en temario={}", preguntaDTO.tipo(), preguntaDTO.idTemarioPerteneciente());

        Temario temario = temarioRepository.findById(preguntaDTO.idTemarioPerteneciente())
                .orElseThrow(() -> new BussinesException("No existe ese cuestionario"));

        FabricaDePreguntas fabricaDePreguntas = new FabricaDePreguntas();
        AResponder pregunta = fabricaDePreguntas.fromJSON(preguntaDTO);

        temario.agregarALaLista(pregunta);

        temarioRepository.save(temario);

        return new CreateQuestionResponseDTO(temario.getId(), temario.getTipo());
    }

    @Transactional
    public void delete(Long id) {
        logger.info("Eliminando pregunta id={}", id);
        preguntaRepository.deleteById(id);
    }


    @Transactional
    public Pregunta updateQuestion(PostPreguntaDTO preguntaDTO) {
        logger.info("Actualizando pregunta id={}, tipo={}", preguntaDTO.id(), preguntaDTO.tipo());
        Pregunta pregunta = obtenerPregunta(preguntaDTO.id(), preguntaDTO.tipo());
        BeanUtils.copyProperties(preguntaDTO, pregunta);
        return preguntaRepository.save(pregunta);
    }


    @Transactional(readOnly = true)
    public IssueWhitItemsDTO getIssueItems(Long id) {
        logger.debug("getIssueItems id={}", id);
        IssueOrQuestionnaireProjection temario = temarioRepository.findByIdBasic(id)
                .orElseThrow(() -> new BussinesException("No existe ese cuestionario"));


        List<QuestionnaireItem> itemList;
        Boolean isCriticTheActualIssue = false;
        try {
            logger.debug("Obteniendo items del temario id={}", id);
            itemList = aResponderRepository.getIssueItems(id);
            isCriticTheActualIssue = itemList.stream().filter(ele -> ele.getId().equals(id)).findFirst().get().getIsCritic();
            itemList.removeIf(ele -> ele.getId().equals(id));
        } catch (Exception e) {
            logger.error("Error al obtener items del temario id={}", id, e);
            itemList = Collections.emptyList();
        }


        IssueWhitItemsDTO issueWhitItemsDTO = new IssueWhitItemsDTO(id, temario.getName(),
                temario.getCreationDate(),
                temario.getFatherId(), itemList, temario.getType(), isCriticTheActualIssue);


        return issueWhitItemsDTO;
    }

    @Transactional
    List<Long> obtenerTodosLosIdsDePreguntas(ObtenerPreguntaDTO obtenerPreguntaDTO) {
        Pregunta pregunta = obtenerPregunta(obtenerPreguntaDTO.id(), obtenerPreguntaDTO.tipoAResponder());
        return pregunta.obtenerListaDeIdentificadoresDePreguntas();
    }

    @Transactional
    public Boolean verifyResponse(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {

        Pregunta preguntaAResponder = obtenerPregunta(respuestaDePreguntaDTO.idPregunta(), respuestaDePreguntaDTO.tipoDePregunta());

        Boolean esCorrecta = preguntaAResponder.verificarSiLaRespuestaEsCorrectaYAsignarCriticos(respuestaDePreguntaDTO);

        preguntaRepository.save(preguntaAResponder);

        return esCorrecta;

    }

    @Transactional
    public void createInverseQuestion(InverseQuestionCreateDTO inverseQuestionCreateDTO) {

        Long idPregunta = inverseQuestionCreateDTO.idQuestion();
        TipoAResponder tipo = inverseQuestionCreateDTO.tipo();

        Pregunta pregunta = obtenerPregunta(idPregunta, tipo);


        if (pregunta.getTipo().equals(TipoAResponder.PREGUNTA_SIMPLE)) {

            PreguntaSimple preguntaSimple = (PreguntaSimple) pregunta;

            String respuesta = Jsoup.parse(preguntaSimple.getRespuestaEstablecida()).text();
            logger.info("Respuesta invertida: {}", respuesta);

            PostPreguntaDTO postPreguntaDTO = new PostPreguntaDTO(
                null,
                respuesta,
                pregunta.getDescripcion(),
                tipo,
                preguntaSimple.getIdDuenio(),
                null,
                preguntaSimple.titulo,
                null,
                null,
                null,
                null
            );


            createaQuestion(postPreguntaDTO);
        }

    }

    public List<PreguntaSimple>  getListOfPreguntaSimples(List<Long> ids){

        return (List<PreguntaSimple>) mapDeRepositorios.get(TipoAResponder.PREGUNTA_SIMPLE).findAllById(ids);

    }





}


