package com.lorenzomar3.AQ.content.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.PreguntaSimpleEntity;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.TemarioEntity;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.VerdaderoOFalsoEntity;
import com.lorenzomar3.AQ.content.infrastructure.persistence.mapper.TemarioMapper;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.PreguntaSimpleJpaRepository;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.TemarioJpaRepository;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.VerdaderoOFalsoJpaRepository;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({TemarioJpaAdapter.class, TemarioMapper.class})
class TemarioJpaAdapterDeleteByIdIntegrationTest {

    @Autowired
    private TemarioJpaAdapter temarioJpaAdapter;

    @Autowired
    private TemarioJpaRepository temarioJpaRepository;

    @Autowired
    private PreguntaSimpleJpaRepository preguntaSimpleJpaRepository;

    @Autowired
    private VerdaderoOFalsoJpaRepository verdaderoOFalsoJpaRepository;

    @Test
    void deleteById_borraElNodoYTodosSusDescendientesDeVariosTiposSinDejarFilasHuerfanas() {
        TemarioEntity cuestionario = new TemarioEntity();
        cuestionario.setTitulo("Cuestionario a borrar");
        cuestionario.setTipo(TipoAResponder.CUESTIONARIO);
        cuestionario.setFechaDeCreacion(LocalDateTime.now());
        cuestionario = temarioJpaRepository.saveAndFlush(cuestionario);

        TemarioEntity tema = new TemarioEntity();
        tema.setTitulo("Tema a borrar");
        tema.setTipo(TipoAResponder.TEMA);
        tema.setIdDuenio(cuestionario.getId());
        tema.setFechaDeCreacion(LocalDateTime.now());
        tema = temarioJpaRepository.saveAndFlush(tema);

        TemarioEntity subtema = new TemarioEntity();
        subtema.setTitulo("Subtema a borrar");
        subtema.setTipo(TipoAResponder.SUBTEMA);
        subtema.setIdDuenio(tema.getId());
        subtema.setFechaDeCreacion(LocalDateTime.now());
        subtema = temarioJpaRepository.saveAndFlush(subtema);

        PreguntaSimpleEntity preguntaSimple = new PreguntaSimpleEntity();
        preguntaSimple.setTitulo("Pregunta simple a borrar");
        preguntaSimple.setTipo(TipoAResponder.PREGUNTA_SIMPLE);
        preguntaSimple.setIdDuenio(subtema.getId());
        preguntaSimple.setFechaDeCreacion(LocalDateTime.now());
        preguntaSimple.setIntentosParaQueDejeDeSerCriticoDisponible(0);
        preguntaSimple = preguntaSimpleJpaRepository.saveAndFlush(preguntaSimple);

        VerdaderoOFalsoEntity verdaderoOFalso = new VerdaderoOFalsoEntity();
        verdaderoOFalso.setTitulo("Verdadero o falso a borrar");
        verdaderoOFalso.setTipo(TipoAResponder.VERDADERO_FALSO);
        verdaderoOFalso.setIdDuenio(subtema.getId());
        verdaderoOFalso.setFechaDeCreacion(LocalDateTime.now());
        verdaderoOFalso.setIntentosParaQueDejeDeSerCriticoDisponible(3);
        verdaderoOFalso = verdaderoOFalsoJpaRepository.saveAndFlush(verdaderoOFalso);

        Long cuestionarioId = cuestionario.getId();
        Long temaId = tema.getId();
        Long subtemaId = subtema.getId();
        Long preguntaSimpleId = preguntaSimple.getId();
        Long verdaderoOFalsoId = verdaderoOFalso.getId();

        temarioJpaAdapter.deleteById(temaId);

        assertThat(temarioJpaRepository.findById(temaId)).isEmpty();
        assertThat(temarioJpaRepository.findById(subtemaId)).isEmpty();
        assertThat(preguntaSimpleJpaRepository.findById(preguntaSimpleId)).isEmpty();
        assertThat(verdaderoOFalsoJpaRepository.findById(verdaderoOFalsoId)).isEmpty();
        assertThat(temarioJpaRepository.findById(cuestionarioId)).isPresent();
    }
}
