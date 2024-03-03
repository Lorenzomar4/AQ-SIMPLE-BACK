package com.lorenzomar3.AQ.Controller;


import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Service.TemarioService;
import com.lorenzomar3.AQ.dto.conversor.CuestionarioDTOConversor;
import com.lorenzomar3.AQ.dto.conversor.TemarioDTOConversor;
import com.lorenzomar3.AQ.dto.newDto.CuestionarioDTO;
import com.lorenzomar3.AQ.dto.newDto.CuestionarioWithListDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioCuestionarioCardDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioCuestionarioWhitItemListDTO;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class TemarioTipoCuestionarioController {

    @Autowired
    TemarioService temarioService;

    @GetMapping("/allCuestionario")
    public ResponseEntity<List<TemarioCuestionarioCardDTO>> todosLosCuestionarios() {

        List<TemarioCuestionarioCardDTO> temarioCuestionarioCardDTO = temarioService.obtenerTodosLosTemariosDeTipoCuestionario()
                .stream().map(Temario::toTemarioCuestionarioCardDTO).toList();

        return new ResponseEntity<>(temarioCuestionarioCardDTO, HttpStatus.OK);
    }


    @Transactional(readOnly = true)
    @GetMapping("/fullQuestionarioById/{id}")
    public ResponseEntity<TemarioCuestionarioWhitItemListDTO> cuestionarioById(@PathVariable Long id) {
        TemarioCuestionarioWhitItemListDTO cuestionarioWithListDTO = temarioService.obtenerTodo(id).toTemarioCuestionarioWhitItemList();
        return new ResponseEntity<>(cuestionarioWithListDTO, HttpStatus.OK);
    }


    @PostMapping("/questionnaireCreate")
    public ResponseEntity<TemarioCuestionarioCardDTO> crearCuestionario(@RequestBody TemarioCuestionarioCardDTO temarioCuestionarioCardDTO) {
        Temario temario = TemarioDTOConversor.fromJSON(temarioCuestionarioCardDTO);

        TemarioCuestionarioCardDTO TemarioCuestionarioGuardado =
                temarioService.saveTemarioCuestionario(temario).toTemarioCuestionarioCardDTO();

        return new ResponseEntity<>(TemarioCuestionarioGuardado, HttpStatus.CREATED);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        temarioService.eliminarCuestionario(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PutMapping("/editarCuestionario")
    public ResponseEntity<TemarioCuestionarioCardDTO> editarCuestionario(@RequestBody TemarioCuestionarioCardDTO temarioCuestionarioCardDTO) {
       JsonVisualizador.verJson(temarioCuestionarioCardDTO);

        TemarioCuestionarioCardDTO c = temarioService.actualizarCuestionario(temarioCuestionarioCardDTO).toTemarioCuestionarioCardDTO();
        return new ResponseEntity<>(c, HttpStatus.OK);
    }

}
