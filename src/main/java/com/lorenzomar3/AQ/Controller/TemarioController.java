package com.lorenzomar3.AQ.Controller;


import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.Service.TemarioService;
import com.lorenzomar3.AQ.dto.conversor.TemarioDTOConversor;
import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioBasicDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioCuestionarioWhitItemListDTO;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.projections.QuestionnaireItem;
import jdk.jfr.Description;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class TemarioController {

    private static final Logger logger = LoggerFactory.getLogger(TemarioController.class);


    @Autowired
    TemarioService temarioService;

    @Autowired
    PreguntaService preguntaService;

    @GetMapping("/allCuestionario")
    public ResponseEntity<List<TemarioBasicDTO>> todosLosCuestionarios() {

        List<TemarioBasicDTO> temarioBasicDTO = temarioService.obtenerTodosLosTemariosDeTipoCuestionario()
                .stream().map(Temario::toTemarioCuestionarioCardDTO).toList();

        logger.info("Endpoint: {}", "/allCuestionario");


        return new ResponseEntity<>(temarioBasicDTO, HttpStatus.OK);
    }


    @Transactional(readOnly = true)
    @GetMapping("/fullIssueById/{id}")
    public ResponseEntity<TemarioCuestionarioWhitItemListDTO> cuestionarioById(@PathVariable Long id) {
        logger.info("Contenido del temario de id : " + id + "  endpoint : /fullIssueById/" + id);

        TemarioCuestionarioWhitItemListDTO cuestionarioWithListDTO = temarioService.obtenerTodo(id).toTemarioCuestionarioWhitItemList();
        return new ResponseEntity<>(cuestionarioWithListDTO, HttpStatus.OK);
    }

    @Transactional
    @GetMapping("/issueItems/{id}")
    public ResponseEntity<List<QuestionnaireItem>> getTopicContent(@PathVariable Long id) {
        return new ResponseEntity<>(preguntaService.getIssueItems(id), HttpStatus.OK);
    }


    @PostMapping("/issuequestionnaireCreate")
    public ResponseEntity<TemarioBasicDTO> crearCuestionario(@RequestBody TemarioBasicDTO temarioBasicDTO) {
        Temario temario = TemarioDTOConversor.fromJSON(temarioBasicDTO);

        TemarioBasicDTO TemarioCuestionarioGuardado =
                temarioService.saveTemarioCuestionario(temario).toTemarioCuestionarioCardDTO();

        return new ResponseEntity<>(TemarioCuestionarioGuardado, HttpStatus.CREATED);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        temarioService.eliminarCuestionario(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PutMapping("/editIssue")
    public ResponseEntity<TemarioBasicDTO> editarCuestionario(@RequestBody TemarioBasicDTO temarioBasicDTO) {
        JsonVisualizador.verJson(temarioBasicDTO);

        TemarioBasicDTO c = temarioService.actualizarCuestionario(temarioBasicDTO).toTemarioCuestionarioCardDTO();
        return new ResponseEntity<>(c, HttpStatus.OK);
    }

    @PostMapping("/createIssue")

    public ResponseEntity<AResponderItemListDTO> crearTema(@RequestBody TemarioBasicDTO temarioBasicDTO) {
        AResponderItemListDTO itemDTO = temarioService.crearNuevoTemarioHijo(temarioBasicDTO).toResponderItemListDTO();
        return new ResponseEntity<>(itemDTO, HttpStatus.CREATED);
    }


    @GetMapping("/getAllQuestionIdsToAnswer/{id}")
    @Description("Descripcion pendiente")
    @Transactional
    public ResponseEntity<List<Long>> obtenerIdsPreguntas(@PathVariable Long id) {
        List<Long> aRetornar;
        aRetornar = temarioService.obtenerTodosLosIdsDePreguntas(id);
        return new ResponseEntity<>(aRetornar, HttpStatus.OK);


    }


}
