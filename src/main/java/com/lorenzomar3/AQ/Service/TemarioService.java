package com.lorenzomar3.AQ.Service;

import com.lorenzomar3.AQ.Controller.TemarioController;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.dto.conversor.TemarioDTOConversor;
import com.lorenzomar3.AQ.dto.newDto.InverseIssueCreateDTO;
import com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioBasicDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.PreguntaSimple;
import com.lorenzomar3.AQ.model.TipoAResponder;
import com.lorenzomar3.AQ.projections.QuestionnaireItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TemarioService {
    private static final Logger logger = LoggerFactory.getLogger(TemarioService.class);


    TemarioRepository temarioRepository;

    @Autowired
    public TemarioService(TemarioRepository temarioRepository) {
        this.temarioRepository = temarioRepository;
    }

    @Autowired
    public PreguntaService preguntaService;


    @Transactional
    public Temario saveTemarioCuestionario(Temario temario) {
        logger.info("Guardando cuestionario titulo={}", temario.getTitulo());
        temario.setTipo(TipoAResponder.CUESTIONARIO);
        return temarioRepository.save(temario);
    }

    @Transactional
    public void eliminarCuestionario(Long id) {
        logger.info("Eliminando cuestionario id={}", id);
        temarioRepository.deleteById(id);
    }

    public Temario actualizarCuestionario(TemarioBasicDTO temarioDto) {
        logger.info("Actualizando cuestionario id={}, nuevoNombre={}", temarioDto.id(), temarioDto.name());

        final Temario temaBd = temarioRepository
                .findByIdEssential(temarioDto.id()).orElseThrow(() -> new BussinesException("Error , no existe este cuestionario"));

        temaBd.setTitulo(temarioDto.name());
        temaBd.setUltimaActualizacion(LocalDateTime.now());

        return temarioRepository.save(temaBd);

    }

    @Transactional
    public Temario crearNuevoTemarioHijo(TemarioBasicDTO temarioBasicDTO) {
        logger.info("Creando temario hijo en padre={}", temarioBasicDTO.fatherid());

        Temario temarioPadre = temarioRepository
                .findByIdEssential(temarioBasicDTO.fatherid())
                .orElseThrow(() -> new BussinesException("Error , no existe este cuestionario"));

        Temario temarioHijo = TemarioDTOConversor.fromJSON(temarioBasicDTO);

        temarioPadre.agregarALaLista(temarioHijo);

        return temarioRepository.save(temarioPadre);

    }


    //Buscar en un futuro otra forma de hacerlo! Pensar en queys de JPQL ,HQL O nativas. Incluso en metodos provistos por spring data
    //Ver por supuesto si vale la pena realizar el cambio.
    @Transactional(readOnly = true)
    public List<Long> obtenerTodosLosIdsDePreguntas(Long id) {
        logger.debug("obtenerTodosLosIdsDePreguntas temarioId={}", id);

        Temario tema = temarioRepository.findById(id).orElseThrow(() ->
                new BussinesException("Error no existe un temario con ese id"));

        return tema.obtenerListaDeIdentificadoresDePreguntas();

    }

    @Transactional
    public Temario crearTemarioPreguntasInversa(InverseIssueCreateDTO inverseIssueCreateDTO) {

        Long id = inverseIssueCreateDTO.idIssue();

        Temario tema = temarioRepository.findById(id).orElseThrow(() ->
                new BussinesException("Error no existe un temario con ese id"));


        List<AResponder> listaDePreguntasAResponder = tema.getListaAResponder()
                .stream().filter(a -> a.getTipo().equals(TipoAResponder.PREGUNTA_SIMPLE)).toList();

        List<Long> ids = listaDePreguntasAResponder.stream().map(AResponder::getId).toList();

        List<PreguntaSimple> listaDePregSimples = preguntaService.getListOfPreguntaSimples(ids);

        List<AResponder> listaDePregSimplesInversa = listaDePregSimples.stream().map(p -> (AResponder) p.inversar()).toList();

        Temario nuevoTemario = new Temario();
        nuevoTemario.setIdDuenio(tema.getIdDuenio());
        nuevoTemario.setTitulo(inverseIssueCreateDTO.name());
        nuevoTemario.setListaAResponder(listaDePregSimplesInversa);
        nuevoTemario.setTipo(tema.getTipo());


        return temarioRepository.save(nuevoTemario);

    }


}
