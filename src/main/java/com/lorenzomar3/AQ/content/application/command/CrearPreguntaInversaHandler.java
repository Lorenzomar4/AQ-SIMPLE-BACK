package com.lorenzomar3.AQ.content.application.command;

import com.lorenzomar3.AQ.content.application.port.in.CrearPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.content.domain.PreguntaSimple;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Service
public class CrearPreguntaInversaHandler {

    private final PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;
    private final CrearPreguntaUseCase crearPreguntaUseCase;

    public CrearPreguntaInversaHandler(PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort,
                                        CrearPreguntaUseCase crearPreguntaUseCase) {
        this.preguntaSimpleRepositoryPort = preguntaSimpleRepositoryPort;
        this.crearPreguntaUseCase = crearPreguntaUseCase;
    }

    public void ejecutar(CrearPreguntaInversaCommand command) {
        if (command.tipo() != TipoAResponder.PREGUNTA_SIMPLE) {
            return;
        }

        PreguntaSimple preguntaSimple = preguntaSimpleRepositoryPort.findById(command.idPregunta())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        String respuestaLimpia = Jsoup.parse(preguntaSimple.getRespuestaEstablecida()).text();

        PostPreguntaDTO postPreguntaDTO = new PostPreguntaDTO(
                null,
                respuestaLimpia,
                preguntaSimple.getDescripcion(),
                command.tipo(),
                preguntaSimple.getIdDuenio(),
                null,
                preguntaSimple.getTitulo(),
                null,
                null,
                null,
                null
        );

        crearPreguntaUseCase.crear(postPreguntaDTO);
    }
}
