package com.lorenzomar3.AQ.Controller;

import com.lorenzomar3.AQ.Service.CuestionarioService;
import com.lorenzomar3.AQ.dto.conversor.CuestionarioConversorDTO;
import com.lorenzomar3.AQ.dto.dto.CuestionarioConTemasDTO;
import com.lorenzomar3.AQ.dto.dto.CuestionarioSimpleDTO;
import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CuestionarioController {

    @Autowired
    CuestionarioService cuestionarioService;

    @GetMapping("/cuestionarios")
    public ResponseEntity<List<CuestionarioSimpleDTO>> todosLosCuestionarios() {
        List<Cuestionario> lista = cuestionarioService.getAllCuestionario();
        List<CuestionarioSimpleDTO> dtoList = lista
                .stream()
                .map(cuest -> CuestionarioConversorDTO.toCuestionarioSimpleDTO(cuest)).collect(Collectors.toList());

        System.out.println("xxxx");

        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    @GetMapping("/cuestionario/{id}")
    public ResponseEntity<CuestionarioConTemasDTO> obtenerCuestionario(@PathVariable Long id) {
        Cuestionario cuestionario = cuestionarioService.getById(id);
        CuestionarioConTemasDTO cuestionarioConTemas = CuestionarioConversorDTO.toCuestionarioConTemas(cuestionario);

        return new ResponseEntity<>(cuestionarioConTemas, HttpStatus.OK);

    }

    @PostMapping("/cuestionario")
    public ResponseEntity<Cuestionario> crearCuestionario(@RequestBody Cuestionario cuestionario) {
        Cuestionario cuestionarioCreado = cuestionarioService.crearCuestionario(cuestionario);

        return new ResponseEntity<>(cuestionarioCreado, HttpStatus.CREATED);
    }

    @DeleteMapping("/cuestionario/{id}")
    public ResponseEntity<String> eliminarCuestionario(@RequestParam Long id) {
        cuestionarioService.eliminarCuestionario(id);
        return new ResponseEntity<>("Se ha eliminado el cuestionario", HttpStatus.NO_CONTENT);
    }

}
