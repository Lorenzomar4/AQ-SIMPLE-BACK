package com.lorenzomar3.AQ.content.infrastructure.persistence.repository;

import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.PreguntaSimpleEntity;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.TemarioEntity;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.VerdaderoOFalsoEntity;
import com.lorenzomar3.AQ.model.TipoAResponder;
import com.lorenzomar3.AQ.projections.AResponderIdTipoProjection;
import com.lorenzomar3.AQ.projections.QuestionnaireItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class AResponderJpaRepositoryIntegrationTest {

    @Autowired
    private AResponderJpaRepository aResponderJpaRepository;

    @Autowired
    private TemarioJpaRepository temarioJpaRepository;

    @Autowired
    private PreguntaSimpleJpaRepository preguntaSimpleJpaRepository;

    @Autowired
    private VerdaderoOFalsoJpaRepository verdaderoOFalsoJpaRepository;

    private TemarioEntity cuestionario;
    private TemarioEntity tema;
    private TemarioEntity subtema;
    private PreguntaSimpleEntity preguntaSimple;
    private VerdaderoOFalsoEntity verdaderoOFalso;

    @BeforeEach
    void setUp() {
        cuestionario = new TemarioEntity();
        cuestionario.setTitulo("Cuestionario de prueba");
        cuestionario.setTipo(TipoAResponder.CUESTIONARIO);
        cuestionario.setFechaDeCreacion(LocalDateTime.now());
        cuestionario = temarioJpaRepository.saveAndFlush(cuestionario);

        tema = new TemarioEntity();
        tema.setTitulo("Tema de prueba");
        tema.setTipo(TipoAResponder.TEMA);
        tema.setIdDuenio(cuestionario.getId());
        tema.setFechaDeCreacion(LocalDateTime.now());
        tema = temarioJpaRepository.saveAndFlush(tema);

        subtema = new TemarioEntity();
        subtema.setTitulo("Subtema de prueba");
        subtema.setTipo(TipoAResponder.SUBTEMA);
        subtema.setIdDuenio(tema.getId());
        subtema.setFechaDeCreacion(LocalDateTime.now());
        subtema = temarioJpaRepository.saveAndFlush(subtema);

        preguntaSimple = new PreguntaSimpleEntity();
        preguntaSimple.setTitulo("Pregunta simple de prueba");
        preguntaSimple.setTipo(TipoAResponder.PREGUNTA_SIMPLE);
        preguntaSimple.setIdDuenio(subtema.getId());
        preguntaSimple.setFechaDeCreacion(LocalDateTime.now());
        preguntaSimple.setIntentosParaQueDejeDeSerCriticoDisponible(0);
        preguntaSimple = preguntaSimpleJpaRepository.saveAndFlush(preguntaSimple);

        verdaderoOFalso = new VerdaderoOFalsoEntity();
        verdaderoOFalso.setTitulo("Verdadero o falso de prueba");
        verdaderoOFalso.setTipo(TipoAResponder.VERDADERO_FALSO);
        verdaderoOFalso.setIdDuenio(subtema.getId());
        verdaderoOFalso.setFechaDeCreacion(LocalDateTime.now());
        verdaderoOFalso.setIntentosParaQueDejeDeSerCriticoDisponible(3);
        verdaderoOFalso = verdaderoOFalsoJpaRepository.saveAndFlush(verdaderoOFalso);
    }

    @Test
    void getIssueItems_devuelveHijosDirectosConCriticoAgregadoYConteoDePreguntas() {
        List<QuestionnaireItem> items = aResponderJpaRepository.getIssueItems(tema.getId());

        assertThat(items)
                .extracting(QuestionnaireItem::getId, QuestionnaireItem::getIsCritic, QuestionnaireItem::getNumberOfQuestions)
                .containsExactlyInAnyOrder(
                        tuple(tema.getId(), false, 0),
                        tuple(subtema.getId(), true, 2)
                );
    }

    @Test
    void getCriticsIdsForQuestion_devuelveSoloLosDescendientesCriticos() {
        List<Long> criticos = aResponderJpaRepository.getCriticsIdsForQuestion(cuestionario.getId());

        assertThat(criticos).containsExactly(verdaderoOFalso.getId());
    }

    @Test
    void findDescendantIds_devuelveTodosLosDescendientesSinIncluirseASiMismo() {
        List<AResponderIdTipoProjection> descendientes = aResponderJpaRepository.findDescendantIds(cuestionario.getId());

        assertThat(descendientes)
                .extracting(AResponderIdTipoProjection::getId, AResponderIdTipoProjection::getType)
                .containsExactlyInAnyOrder(
                        tuple(tema.getId(), TipoAResponder.TEMA),
                        tuple(subtema.getId(), TipoAResponder.SUBTEMA),
                        tuple(preguntaSimple.getId(), TipoAResponder.PREGUNTA_SIMPLE),
                        tuple(verdaderoOFalso.getId(), TipoAResponder.VERDADERO_FALSO)
                );
    }
}
