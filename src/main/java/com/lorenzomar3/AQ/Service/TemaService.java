package com.lorenzomar3.AQ.Service;


import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Repository.TemaRepository;
import com.lorenzomar3.AQ.dto.conversor.TemaConversorDTO;
import com.lorenzomar3.AQ.dto.dto.TemaPostDTO;
import com.lorenzomar3.AQ.model.AResponder.Tema;
import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TemaService {

    @Autowired
    TemaRepository temaRepository;

    @Autowired
    CuestionarioService cuestionarioService;


    @Transactional
    public Tema crearTema(TemaPostDTO temaPostDTO) {
        JsonVisualizador.verJson(temaPostDTO);

        Cuestionario c = cuestionarioService.getById(temaPostDTO.getIdquestionnaire());

        Tema tema = TemaConversorDTO.fromJSON(temaPostDTO);

        c.agregarNuevoPreguntaOTema(tema);

        cuestionarioService.saveCuestionario(c);

        return tema;

    }


}
