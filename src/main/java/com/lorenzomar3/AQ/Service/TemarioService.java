package com.lorenzomar3.AQ.Service;

import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.dto.newDto.TemarioCuestionarioCardDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.Tema;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.Cuestionario;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TemarioService {


    TemarioRepository temarioRepository;

    @Autowired
    public TemarioService(TemarioRepository temarioRepository) {
        this.temarioRepository = temarioRepository;
    }


    @Transactional(readOnly = true)
    public List<Temario> obtenerTodosLosTemariosDeTipoCuestionario() {
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

    public Temario actualizarCuestionario(TemarioCuestionarioCardDTO temarioDto) {

        final Temario temaBd = temarioRepository
                .findByIdEscencial(temarioDto.getId()).orElseThrow(() -> new BussinesException("Error , no existe este cuestionario"));

        temaBd.setTitulo(temarioDto.getName());
        temaBd.setUltimaActualizacion(LocalDateTime.now());

        return temarioRepository.save(temaBd);

    }

}
