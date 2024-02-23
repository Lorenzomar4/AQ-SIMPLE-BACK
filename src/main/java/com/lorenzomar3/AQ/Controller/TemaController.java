package com.lorenzomar3.AQ.Controller;


import com.lorenzomar3.AQ.Service.TemaService;
import com.lorenzomar3.AQ.dto.conversor.TemaConversorDTO;
import com.lorenzomar3.AQ.dto.dto.TemaPostDTO;
import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.dto.newDto.TemaDTO;
import com.lorenzomar3.AQ.model.AResponder.Tema;
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


}
