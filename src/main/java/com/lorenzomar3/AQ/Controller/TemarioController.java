package com.lorenzomar3.AQ.Controller;


import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.Service.TemarioService;
import com.lorenzomar3.AQ.dto.conversor.TemarioDTOConversor;
import com.lorenzomar3.AQ.dto.newDto.*;
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

    @GetMapping("/questionnaires")
    public ResponseEntity<List<TemarioBasicDTO>> todosLosCuestionarios() {
        logger.info("[GET /questionnaires]");

        List<TemarioBasicDTO> temarioBasicDTO = temarioService.obtenerTodosLosTemariosDeTipoCuestionario()
                .stream().map(Temario::toTemarioCuestionarioCardDTO).toList();


        return new ResponseEntity<>(temarioBasicDTO, HttpStatus.OK);
    }


    @Transactional
    @GetMapping("/issues/{id}/items")
    public ResponseEntity<IssueWhitItemsDTO> getTopicContent(@PathVariable Long id) {
        logger.info("[GET /issues/{}/items]", id);

        return new ResponseEntity<>(preguntaService.getIssueItems(id), HttpStatus.OK);
    }


    @PostMapping("/questionnaires")
    public ResponseEntity<TemarioBasicDTO> crearCuestionario(@RequestBody TemarioBasicDTO temarioBasicDTO) {

        logger.info("[POST /questionnaires] nombre={}", temarioBasicDTO.name());

        Temario temario = TemarioDTOConversor.fromJSON(temarioBasicDTO);

        TemarioBasicDTO TemarioCuestionarioGuardado =
                temarioService.saveTemarioCuestionario(temario).toTemarioCuestionarioCardDTO();

        return new ResponseEntity<>(TemarioCuestionarioGuardado, HttpStatus.CREATED);
    }


    @DeleteMapping("/issues/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        logger.info("[DELETE /issues/{}]", id);

        temarioService.eliminarCuestionario(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PutMapping("/issues")
    public ResponseEntity<TemarioBasicDTO> editarCuestionario(@RequestBody TemarioBasicDTO temarioBasicDTO) {
        logger.info("[PUT /issues] id={}", temarioBasicDTO.id());

        TemarioBasicDTO c = temarioService.actualizarCuestionario(temarioBasicDTO).toTemarioCuestionarioCardDTO();
        return new ResponseEntity<>(c, HttpStatus.OK);
    }

    @PostMapping("/issues")
    public ResponseEntity<AResponderItemListDTO> crearTema(@RequestBody TemarioBasicDTO temarioBasicDTO) {
        logger.info("[POST /issues] Creacion de issue, fatherId={}", temarioBasicDTO.fatherid());

        AResponderItemListDTO itemDTO = temarioService.crearNuevoTemarioHijo(temarioBasicDTO).toResponderItemListDTO();
        return new ResponseEntity<>(itemDTO, HttpStatus.CREATED);
    }






    @GetMapping("/issues/{id}/question-ids")
    @Description("Descripcion pendiente")
    @Transactional
    public ResponseEntity<List<Long>> obtenerIdsPreguntas(@PathVariable Long id) {
        logger.info("[GET /issues/{}/question-ids]", id);

        List<Long> aRetornar;
        aRetornar = temarioService.obtenerTodosLosIdsDePreguntas(id);
        return new ResponseEntity<>(aRetornar, HttpStatus.OK);


    }


    @PostMapping("/issues/inverse")
    public ResponseEntity<AResponderItemListDTO> crearTemaConPreguntasInversas(@RequestBody InverseIssueCreateDTO inverseIssueCreateDTO) {
        logger.info("[POST /issues/inverse] issueId={}", inverseIssueCreateDTO.idIssue());

        AResponderItemListDTO itemDTO = temarioService.crearTemarioPreguntasInversa(inverseIssueCreateDTO).toResponderItemListDTO();

        return new ResponseEntity<>(itemDTO, HttpStatus.CREATED);

    }


    }
