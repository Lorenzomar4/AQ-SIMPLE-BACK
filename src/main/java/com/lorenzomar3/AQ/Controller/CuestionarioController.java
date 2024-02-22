package com.lorenzomar3.AQ.Controller;

import com.lorenzomar3.AQ.Service.CuestionarioService;
import com.lorenzomar3.AQ.dto.conversor.CuestionarioDTOConversor;
import com.lorenzomar3.AQ.dto.dto.CuestionarioConTemasDTO;
import com.lorenzomar3.AQ.dto.dto.CuestionarioPostDTO;
import com.lorenzomar3.AQ.dto.dto.CuestionarioSimpleDTO;
import com.lorenzomar3.AQ.dto.newDto.CuestionarioDTO;
import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

        return new ResponseEntity<>(cuestionarioGuardado, HttpStatus.OK);
    }


}
