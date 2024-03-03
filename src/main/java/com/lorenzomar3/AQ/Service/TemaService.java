package com.lorenzomar3.AQ.Service;


import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Repository.TemaRepository;
import com.lorenzomar3.AQ.dto.conversor.TemaConversorDTO;
import com.lorenzomar3.AQ.dto.newDto.TemaPostDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.Tema;
import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TemaService {

    @Autowired
    TemaRepository temaRepository;

    @Autowired
    CuestionarioService cuestionarioService;


    @Transactional
    public Tema crearTema(TemaPostDTO temaPostDTO) {
        JsonVisualizador.verJson(temaPostDTO);

        Cuestionario c = cuestionarioService.getById(temaPostDTO.getFatherid());

        Tema tema = TemaConversorDTO.fromJSON(temaPostDTO);
        tema.setFechaDeCreacion(LocalDateTime.now());
        tema.setUltimaActualizacion(LocalDateTime.now());

        c.agregarNuevoPreguntaOTema(tema);
        cuestionarioService.saveCuestionario(c);
        return tema;

    }

    @Transactional
    public void eliminarTema(Long id) {
        temaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Tema obtenerTemaPorId(Long id) {
        Tema tema = getById(id);
        return tema;
    }


    public Tema getById(Long id) {
        return temaRepository.findById(id).orElseThrow(() -> new BussinesException("El tema no existe"));
    }

    @Transactional
    public Tema actualizarTema(TemaPostDTO temaDTO) {
        final Tema temaBd = getById(temaDTO.getId());

        temaBd.setTitulo(temaDTO.getName());
        temaBd.setUltimaActualizacion(LocalDateTime.now());

        return temaRepository.save(temaBd);

    }

    @Transactional
    public void guardarSubTema(TemaPostDTO temaPostDTO) {
        Tema temaPadre = getById(temaPostDTO.getFatherid());
        Tema subTema = new Tema();
        subTema.setTitulo(temaPostDTO.getName());

        temaPadre.agregarNuevoPreguntaOTema(subTema);

        temaRepository.save(temaPadre);
        JsonVisualizador.verJson(subTema);

    }


}
