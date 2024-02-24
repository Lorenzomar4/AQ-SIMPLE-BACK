package com.lorenzomar3.AQ.Controller;


import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Service.TemaService;
import com.lorenzomar3.AQ.dto.newDto.TemaPostDTO;
import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.dto.newDto.TemaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class TemaController {

    @Autowired
    TemaService temaService;


    @PostMapping("/createIssue")
    public ResponseEntity<AResponderItemListDTO> crearTema(@RequestBody TemaPostDTO temaPost) {
        AResponderItemListDTO itemDTO = temaService.crearTema(temaPost).toResponderItemListDTO();
        JsonVisualizador.verJson(temaPost);


        return new ResponseEntity<>(itemDTO, HttpStatus.CREATED);
    }


    @DeleteMapping("/deleteIssue/{id}")
    public ResponseEntity<Void> eliminarTema(@PathVariable Long id) {
        temaService.eliminarTema(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    @GetMapping("/getIssueById/{id}")
    public ResponseEntity<TemaDTO> obtenerTema(@PathVariable Long id) {

        TemaDTO temaDTO = temaService.obtenerTemaPorId(id).toTemaDTO();

        return new ResponseEntity<>(temaDTO, HttpStatus.OK);
    }
    @Transactional
    @PutMapping("/updateIssue")
    public ResponseEntity<TemaDTO> actualizarTema(@RequestBody TemaPostDTO temaDTO) {

        JsonVisualizador.verJson(temaDTO);

        TemaDTO tema = temaService.actualizarTema(temaDTO).toTemaDTO();
        return new ResponseEntity<>(tema, HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/saveSubIssue")
    public ResponseEntity<Void> crearSubTema(@RequestBody TemaPostDTO temaPostDTO){
        temaService.guardarSubTema(temaPostDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


}
