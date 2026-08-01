package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.CrearIssueInversoUseCase;
import com.lorenzomar3.AQ.content.application.port.out.AResponderChildRef;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.PreguntaSimple;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.dto.newDto.InverseIssueCreateDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CrearIssueInversoService implements CrearIssueInversoUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;
    private final PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;

    public CrearIssueInversoService(TemarioRepositoryPort temarioRepositoryPort,
                                     PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
        this.preguntaSimpleRepositoryPort = preguntaSimpleRepositoryPort;
    }

    @Override
    @Transactional
    public AResponderItemListDTO crear(InverseIssueCreateDTO inverseIssueCreateDTO) {
        Temario temaOriginal = temarioRepositoryPort.findById(inverseIssueCreateDTO.idIssue())
                .orElseThrow(() -> new BussinesException("Error no existe un temario con ese id"));

        Temario temarioInverso = new Temario();
        temarioInverso.setTitulo(inverseIssueCreateDTO.name());
        temarioInverso.setTipo(temaOriginal.getTipo());
        temarioInverso.setIdDuenio(temaOriginal.getIdDuenio());

        LocalDateTime ahora = LocalDateTime.now();
        temarioInverso.setFechaDeCreacion(ahora);
        temarioInverso.setUltimaActualizacion(ahora);

        Temario temarioInversoGuardado = temarioRepositoryPort.save(temarioInverso);

        temarioRepositoryPort.findDirectChildren(temaOriginal.getId()).stream()
                .filter(hijo -> hijo.tipo() == TipoAResponder.PREGUNTA_SIMPLE)
                .map(AResponderChildRef::id)
                .map(preguntaSimpleRepositoryPort::findById)
                .flatMap(Optional::stream)
                .forEach(preguntaOriginal -> {
                    PreguntaSimple preguntaInversa = invertir(preguntaOriginal);
                    preguntaInversa.setIdDuenio(temarioInversoGuardado.getId());
                    preguntaSimpleRepositoryPort.save(preguntaInversa);
                });

        return new AResponderItemListDTO(temarioInversoGuardado.getId(), temarioInversoGuardado.getTitulo(),
                temarioInversoGuardado.getTipo(), null, false);
    }

    private PreguntaSimple invertir(PreguntaSimple original) {
        PreguntaSimple inversa = new PreguntaSimple();
        inversa.setTitulo(Jsoup.parse(original.getRespuestaEstablecida()).text());
        inversa.setRespuestaEstablecida(original.getTitulo());
        inversa.setTipo(original.getTipo());
        inversa.setRespuestaPrecisa(false);
        inversa.setIntentosParaQueDejeDeSerCriticoDisponible(0);

        LocalDateTime ahora = LocalDateTime.now();
        inversa.setFechaDeCreacion(ahora);
        inversa.setUltimaActualizacion(ahora);

        return inversa;
    }
}
