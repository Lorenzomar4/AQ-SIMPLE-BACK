package com.lorenzomar3.AQ.Controller;

import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Service.CuestionarioService;
import com.lorenzomar3.AQ.dto.conversor.CuestionarioDTOConversor;
import com.lorenzomar3.AQ.dto.dto.CuestionarioConTemasDTO;
import com.lorenzomar3.AQ.dto.dto.CuestionarioPostDTO;
import com.lorenzomar3.AQ.dto.dto.CuestionarioSimpleDTO;
import com.lorenzomar3.AQ.dto.newDto.CuestionarioDTO;
import com.lorenzomar3.AQ.dto.newDto.CuestionarioWithListDTO;
import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class CuestionarioController {

    @Autowired
    CuestionarioService cuestionarioService;

    @GetMapping("/allCuestionario")
    public ResponseEntity<List<CuestionarioDTO>> todosLosCuestionarios() {
        List<CuestionarioDTO> cuestionarioDTOList = cuestionarioService.allCuestionario()
                .stream().map(Cuestionario::toDTO).toList();

        return new ResponseEntity<>(cuestionarioDTOList, HttpStatus.OK);
    }


    @PostMapping("/questionnaireCreate")
    public ResponseEntity<CuestionarioDTO> crearCuestionario(@RequestBody CuestionarioDTO cuestionarioDTO) {
        Cuestionario cuestionario = CuestionarioDTOConversor.fromJSON(cuestionarioDTO);

        CuestionarioDTO cuestionarioGuardado = cuestionarioService.saveCuestionario(cuestionario).toDTO();

        return new ResponseEntity<>(cuestionarioGuardado, HttpStatus.CREATED);
    }


    @Transactional(readOnly = true)
    @GetMapping("/fullQuestionarioById/{id}")
    public ResponseEntity<CuestionarioWithListDTO> cuestionarioById(@PathVariable Long id) {
        System.out.println("fullQuestionarioById");

        CuestionarioWithListDTO cuestionarioWithListDTO = cuestionarioService.obtenerTodo(id).toDTOwhitItemList();

        JsonVisualizador.verJson(cuestionarioWithListDTO);
        return new ResponseEntity<>(cuestionarioWithListDTO, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cuestionarioService.eliminarCuestionario(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PutMapping("/editarCuestionario")
    public ResponseEntity<CuestionarioDTO> editarCuestionario(@RequestBody CuestionarioDTO cuestionarioDTO) {
        Cuestionario cuestionario = CuestionarioDTOConversor.fromJSON(cuestionarioDTO);
        CuestionarioDTO c = cuestionarioService.actualizarCuestionario(cuestionario).toDTO();
        return new ResponseEntity<>(c, HttpStatus.OK);
    }


}
