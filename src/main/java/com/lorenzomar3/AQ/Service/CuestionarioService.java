package com.lorenzomar3.AQ.Service;

import com.lorenzomar3.AQ.Repository.CuestionarioRepository;
import com.lorenzomar3.AQ.Repository.CuestionarioRepositoryFull;
import com.lorenzomar3.AQ.Repository.TemaRepository;
import com.lorenzomar3.AQ.dto.dto.CuestionarioSimpleDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CuestionarioService {

    @Autowired
    CuestionarioRepository cuestionarioRepository;

    @Autowired
    CuestionarioRepositoryFull cuestionarioRepositoryFull;


    @Autowired
    TemaRepository temaRepository;

    @Transactional(readOnly = true)
    public List<Cuestionario> allCuestionario(){
        return cuestionarioRepository.findAll();
    };

    @Transactional
    public Cuestionario saveCuestionario(Cuestionario cuestionario){
        return cuestionarioRepository.save(cuestionario);
    }

    @Transactional(readOnly = true)
    public Cuestionario obtenerTodo(Long id){

        Cuestionario c = cuestionarioRepository.findById(id).orElseThrow(() -> new BussinesException("No existe ese cuestionario"));
        return c;
    }



    @Transactional(readOnly = true)
    public Cuestionario getById(Long id) {
        return cuestionarioRepository.findById(id).orElseThrow(() -> new BussinesException("No existe ese cuestionario"));
    }

    @Transactional
    public Cuestionario crearCuestionario(CuestionarioSimpleDTO cuestionario) {
        Cuestionario cuestionarioBD = new Cuestionario();
        cuestionarioBD.setNombreCuestionario(cuestionario.getName());

        cuestionarioRepository.save(cuestionarioBD);
        cuestionarioBD = cuestionarioRepository.
                findById(cuestionarioBD.getId())
                .orElseThrow(() -> new BussinesException("El cuestionario no fue guardado correctamente"));
        return cuestionarioBD;
    }

    @Transactional
    public void eliminarCuestionario(Long id) {
        Cuestionario cuestionario = cuestionarioRepository.findById(id).orElseThrow(() -> new BussinesException("El elemento no existe"));

        cuestionarioRepository.deleteById(cuestionario.getId());

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Cuestionario actualizarCuestionario(Cuestionario unCuestionario){

        Cuestionario cuestionario = cuestionarioRepository.findById(unCuestionario.getId()).orElseThrow(() -> new BussinesException("El elemento no existe"));

        cuestionario.setNombreCuestionario(unCuestionario.getNombreCuestionario());

        return cuestionarioRepository.save(cuestionario);
    }











}
