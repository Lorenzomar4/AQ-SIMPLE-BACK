package com.lorenzomar3.AQ.Service;

import com.lorenzomar3.AQ.Repository.CuestionarioRepository;
import com.lorenzomar3.AQ.dto.conversor.CuestionarioConversorDTO;
import com.lorenzomar3.AQ.dto.dto.CuestionarioPostDTO;
import com.lorenzomar3.AQ.exception.ErrorDeNegocio;
import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CuestionarioService {

    @Autowired
    CuestionarioRepository cuestionarioRepository;

    @Transactional(readOnly = true)
    public List<Cuestionario> getAllCuestionario() {

        List<Cuestionario> listaCuestionario = cuestionarioRepository.findAll();


        return listaCuestionario;
    }

    @Transactional(readOnly = true)
    public Cuestionario getById(Long id) {
        return cuestionarioRepository.findById(id).orElseThrow(() -> new ErrorDeNegocio("No existe ese cuestionario"));
    }

    @Transactional
    public Cuestionario crearCuestionario(CuestionarioPostDTO cuestionario) {
        Cuestionario cuestionarioBD = CuestionarioConversorDTO.fromJsonPOST(cuestionario);

        cuestionarioRepository.save(cuestionarioBD);
        cuestionarioBD = cuestionarioRepository.
                findById(cuestionarioBD.getId())
                .orElseThrow(() -> new ErrorDeNegocio("El cuestionario no fue guardado correctamente"));
        return cuestionarioBD;
    }

    @Transactional
    public void eliminarCuestionario(Long id) {
        Cuestionario cuestionario = cuestionarioRepository.findById(id).orElseThrow(() -> new ErrorDeNegocio("El elemento no existe"));

        cuestionarioRepository.deleteById(cuestionario.getId());

    }


}
