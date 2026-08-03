package com.lorenzomar3.AQ.content.infrastructure.controller;


import com.lorenzomar3.AQ.content.application.port.in.CrearCuestionarioUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearIssueInversoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearIssueUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarIssueUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarIssueUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerCuestionariosUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerIdsDePreguntasUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerItemsDeIssueUseCase;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.*;
import com.lorenzomar3.AQ.content.infrastructure.controller.projection.QuestionnaireItem;
import jdk.jfr.Description;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class TemarioController {

    private static final Logger logger = LoggerFactory.getLogger(TemarioController.class);


    @Autowired
    ObtenerCuestionariosUseCase obtenerCuestionariosUseCase;

    @Autowired
    CrearCuestionarioUseCase crearCuestionarioUseCase;

    @Autowired
    CrearIssueUseCase crearIssueUseCase;

    @Autowired
    EditarIssueUseCase editarIssueUseCase;

    @Autowired
    EliminarIssueUseCase eliminarIssueUseCase;

    @Autowired
    ObtenerIdsDePreguntasUseCase obtenerIdsDePreguntasUseCase;

    @Autowired
    CrearIssueInversoUseCase crearIssueInversoUseCase;

    @Autowired
    ObtenerItemsDeIssueUseCase obtenerItemsDeIssueUseCase;

    @GetMapping("/questionnaires")
    public ResponseEntity<List<TemarioBasicDTO>> todosLosCuestionarios() {
        logger.info("[GET /questionnaires]");

        List<TemarioBasicDTO> temarioBasicDTO = obtenerCuestionariosUseCase.obtenerCuestionarios()
                .stream()
                .map(temario -> new TemarioBasicDTO(temario.getId(), temario.getTitulo(), temario.getFechaDeCreacion(), null))
                .toList();


        return new ResponseEntity<>(temarioBasicDTO, HttpStatus.OK);
    }


    @GetMapping("/issues/{id}/items")
    public ResponseEntity<IssueWhitItemsDTO> getTopicContent(@PathVariable Long id) {
        logger.info("[GET /issues/{}/items]", id);

        return new ResponseEntity<>(obtenerItemsDeIssueUseCase.obtenerItems(id), HttpStatus.OK);
    }


    @PostMapping("/questionnaires")
    public ResponseEntity<TemarioBasicDTO> crearCuestionario(@RequestBody TemarioBasicDTO temarioBasicDTO) {

        logger.info("[POST /questionnaires] nombre={}", temarioBasicDTO.name());

        TemarioBasicDTO temarioCuestionarioGuardado = crearCuestionarioUseCase.crear(temarioBasicDTO);

        return new ResponseEntity<>(temarioCuestionarioGuardado, HttpStatus.CREATED);
    }


    @DeleteMapping("/issues/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        logger.info("[DELETE /issues/{}]", id);

        eliminarIssueUseCase.eliminar(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PutMapping("/issues")
    public ResponseEntity<TemarioBasicDTO> editarCuestionario(@RequestBody TemarioBasicDTO temarioBasicDTO) {
        logger.info("[PUT /issues] id={}", temarioBasicDTO.id());

        TemarioBasicDTO c = editarIssueUseCase.editar(temarioBasicDTO);
        return new ResponseEntity<>(c, HttpStatus.OK);
    }

    @PostMapping("/issues")
    public ResponseEntity<AResponderItemListDTO> crearTema(@RequestBody TemarioBasicDTO temarioBasicDTO) {
        logger.info("[POST /issues] Creacion de issue, fatherId={}", temarioBasicDTO.fatherid());

        AResponderItemListDTO itemDTO = crearIssueUseCase.crear(temarioBasicDTO);
        return new ResponseEntity<>(itemDTO, HttpStatus.CREATED);
    }






    @GetMapping("/issues/{id}/question-ids")
    @Description("Descripcion pendiente")
    public ResponseEntity<List<Long>> obtenerIdsPreguntas(@PathVariable Long id) {
        logger.info("[GET /issues/{}/question-ids]", id);

        List<Long> aRetornar;
        aRetornar = obtenerIdsDePreguntasUseCase.obtenerIdsDePreguntas(id);
        return new ResponseEntity<>(aRetornar, HttpStatus.OK);


    }


    @PostMapping("/issues/inverse")
    public ResponseEntity<AResponderItemListDTO> crearTemaConPreguntasInversas(@RequestBody InverseIssueCreateDTO inverseIssueCreateDTO) {
        logger.info("[POST /issues/inverse] issueId={}", inverseIssueCreateDTO.idIssue());

        AResponderItemListDTO itemDTO = crearIssueInversoUseCase.crear(inverseIssueCreateDTO);

        return new ResponseEntity<>(itemDTO, HttpStatus.CREATED);

    }


    }
