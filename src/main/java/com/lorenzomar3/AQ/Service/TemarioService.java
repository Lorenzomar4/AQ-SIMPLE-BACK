package com.lorenzomar3.AQ.Service;

import com.lorenzomar3.AQ.Controller.TemarioController;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.dto.conversor.TemarioDTOConversor;
import com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioBasicDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TemarioService {
    private static final Logger logger = LoggerFactory.getLogger(TemarioService.class);


    TemarioRepository temarioRepository;

    @Autowired
    public TemarioService(TemarioRepository temarioRepository) {
        this.temarioRepository = temarioRepository;
    }


    @Transactional(readOnly = true)
    public List<Temario> obtenerTodosLosTemariosDeTipoCuestionario() {

        logger.info("Servicio para obtencion de temarios de tipo:"+TipoAResponder.CUESTIONARIO);

        return temarioRepository.findTemarioByTipo(TipoAResponder.CUESTIONARIO);
    }


    @Transactional(readOnly = true)
    public Temario obtenerTodo(Long id) {
        return temarioRepository.findById(id).orElseThrow(() -> new BussinesException("Error ,no existe este cuestionario"));
    }


    @Transactional
    public Temario saveTemarioCuestionario(Temario temario) {
        temario.setTipo(TipoAResponder.CUESTIONARIO);
        return temarioRepository.save(temario);
    }

    @Transactional
    public void eliminarCuestionario(Long id) {
        temarioRepository.deleteById(id);
    }

    public Temario actualizarCuestionario(TemarioBasicDTO temarioDto) {

        final Temario temaBd = temarioRepository
                .findByIdEssential(temarioDto.getId()).orElseThrow(() -> new BussinesException("Error , no existe este cuestionario"));

        temaBd.setTitulo(temarioDto.getName());
        temaBd.setUltimaActualizacion(LocalDateTime.now());

        return temarioRepository.save(temaBd);

    }

    @Transactional
    public Temario crearNuevoTemarioHijo(TemarioBasicDTO temarioBasicDTO) {
        Temario temarioPadre = temarioRepository
                .findByIdEssential(temarioBasicDTO.getFatherid())
                .orElseThrow(() -> new BussinesException("Error , no existe este cuestionario"));

        Temario temarioHijo = TemarioDTOConversor.fromJSON(temarioBasicDTO);

        temarioPadre.agregarALaLista(temarioHijo);

        return temarioRepository.save(temarioPadre);

    }


    //Buscar en un futuro otra forma de hacerlo! Pensar en queys de JPQL ,HQL O nativas. Incluso en metodos provistos por spring data
    //Ver por supuesto si vale la pena realizar el cambio.
    @Transactional(readOnly = true)
    public List<Long> obtenerTodosLosIdsDePreguntas(Long id) {
        Temario tema = temarioRepository.findById(id).orElseThrow(() ->
                new BussinesException("Error no existe un temario con ese id"));
        return tema.obtenerListaDeIdentificadoresDePreguntas();
    }


}
