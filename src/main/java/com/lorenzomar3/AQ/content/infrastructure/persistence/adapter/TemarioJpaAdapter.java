package com.lorenzomar3.AQ.content.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.content.application.port.out.AResponderChildRef;
import com.lorenzomar3.AQ.content.application.port.out.AResponderItemDetail;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.content.infrastructure.persistence.mapper.TemarioMapper;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.AResponderJpaRepository;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.DesplegableCompartidoJpaRepository;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.DesplegableIndependienteJpaRepository;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.OpcionMultipleJpaRepository;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.PreguntaSimpleJpaRepository;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.SeleccionUnicaJpaRepository;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.TemarioJpaRepository;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.VerdaderoOFalsoJpaRepository;
import com.lorenzomar3.AQ.content.api.TipoAResponder;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class TemarioJpaAdapter implements TemarioRepositoryPort {

    private final TemarioJpaRepository temarioJpaRepository;
    private final TemarioMapper temarioMapper;
    private final AResponderJpaRepository aResponderJpaRepository;
    private final PreguntaSimpleJpaRepository preguntaSimpleJpaRepository;
    private final VerdaderoOFalsoJpaRepository verdaderoOFalsoJpaRepository;
    private final SeleccionUnicaJpaRepository seleccionUnicaJpaRepository;
    private final OpcionMultipleJpaRepository opcionMultipleJpaRepository;
    private final DesplegableCompartidoJpaRepository desplegableCompartidoJpaRepository;
    private final DesplegableIndependienteJpaRepository desplegableIndependienteJpaRepository;

    private final Map<TipoAResponder, Consumer<Long>> mapDeBorrado = new HashMap<>();

    public TemarioJpaAdapter(TemarioJpaRepository temarioJpaRepository,
                              TemarioMapper temarioMapper,
                              AResponderJpaRepository aResponderJpaRepository,
                              PreguntaSimpleJpaRepository preguntaSimpleJpaRepository,
                              VerdaderoOFalsoJpaRepository verdaderoOFalsoJpaRepository,
                              SeleccionUnicaJpaRepository seleccionUnicaJpaRepository,
                              OpcionMultipleJpaRepository opcionMultipleJpaRepository,
                              DesplegableCompartidoJpaRepository desplegableCompartidoJpaRepository,
                              DesplegableIndependienteJpaRepository desplegableIndependienteJpaRepository) {
        this.temarioJpaRepository = temarioJpaRepository;
        this.temarioMapper = temarioMapper;
        this.aResponderJpaRepository = aResponderJpaRepository;
        this.preguntaSimpleJpaRepository = preguntaSimpleJpaRepository;
        this.verdaderoOFalsoJpaRepository = verdaderoOFalsoJpaRepository;
        this.seleccionUnicaJpaRepository = seleccionUnicaJpaRepository;
        this.opcionMultipleJpaRepository = opcionMultipleJpaRepository;
        this.desplegableCompartidoJpaRepository = desplegableCompartidoJpaRepository;
        this.desplegableIndependienteJpaRepository = desplegableIndependienteJpaRepository;
    }

    @PostConstruct
    private void init() {
        mapDeBorrado.put(TipoAResponder.TEMA, temarioJpaRepository::deleteById);
        mapDeBorrado.put(TipoAResponder.SUBTEMA, temarioJpaRepository::deleteById);
        mapDeBorrado.put(TipoAResponder.PREGUNTA_SIMPLE, preguntaSimpleJpaRepository::deleteById);
        mapDeBorrado.put(TipoAResponder.VERDADERO_FALSO, verdaderoOFalsoJpaRepository::deleteById);
        mapDeBorrado.put(TipoAResponder.SELECCION_UNICA, seleccionUnicaJpaRepository::deleteById);
        mapDeBorrado.put(TipoAResponder.OPCION_MULTIPLE, opcionMultipleJpaRepository::deleteById);
        mapDeBorrado.put(TipoAResponder.DESPLEGABLE_COMPARTIDO, desplegableCompartidoJpaRepository::deleteById);
        mapDeBorrado.put(TipoAResponder.DESPLEGABLE_INDEPENDIENTE, desplegableIndependienteJpaRepository::deleteById);
    }

    @Override
    public List<Temario> findAllCuestionarios() {

        List<Temario> cuestionarioARetornar = temarioJpaRepository.findByTipo(TipoAResponder.CUESTIONARIO)
                .stream()
                .map(temarioMapper::toDomain)
                .toList();

        return cuestionarioARetornar;
    }

    @Override
    public Optional<Temario> findById(Long id) {
        return temarioJpaRepository.findById(id).map(temarioMapper::toDomain);
    }

    @Override
    public Temario save(Temario temario) {
        return temarioMapper.toDomain(temarioJpaRepository.save(temarioMapper.toEntity(temario)));
    }

    @Override
    public void deleteById(Long id) {
        aResponderJpaRepository.findDescendantIds(id)
                .forEach(descendiente -> mapDeBorrado.get(descendiente.getType()).accept(descendiente.getId()));
        temarioJpaRepository.deleteById(id);
    }

    @Override
    public List<AResponderChildRef> findDirectChildren(Long id) {
        return aResponderJpaRepository.getIssueItems(id).stream()
                .filter(item -> !item.getId().equals(id))
                .map(item -> new AResponderChildRef(item.getId(), item.getType()))
                .toList();
    }

    @Override
    public List<AResponderItemDetail> findIssueItems(Long id) {
        return aResponderJpaRepository.getIssueItems(id).stream()
                .map(item -> new AResponderItemDetail(item.getId(), item.getType(), item.getName(),
                        item.getCreationDate(), item.getIsCritic(), item.getNumberOfQuestions()))
                .toList();
    }

    @Override
    public List<Long> findCriticalQuestionIds(Long id) {
        return aResponderJpaRepository.getCriticsIdsForQuestion(id);
    }
}
