package com.lorenzomar3.AQ.Pregunta;

import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.OpcionMultiple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class OpcionMultipleTest {
    Opcion opcion1;
    Opcion opcion2;

    Opcion opcion3;

    Opcion opcion4;
    OpcionMultiple opcionMultiple;


    Opcion opcion1FromFrontend;
    Opcion opcion2FromFrontend;
    Opcion opcion3FromFrontend;
    Opcion opcion4FromFrontend;

    RespuestaDePreguntaDTO opcionMultipleRespuestaDelUsuario;

    @BeforeEach
    void init() {

        List<Long> listaDeLongs = List.of(1L, 2L, 3L, 4L);


        opcion1 = new Opcion("es un proseador de decima generacio", true);
        opcion1.setId(1L);

        opcion2 = new Opcion("es comparable a un ryzen 3600x", false);
        opcion2.setId(2L);

        opcion3 = new Opcion("tiene 5 nucleos", false);
        opcion3.setId(3L);

        opcion4 = new Opcion("tiene 8 nucleos", true);
        opcion4.setId(4L);

        opcionMultiple = new OpcionMultiple("Intel i7 10700", List.of(opcion1, opcion2, opcion3, opcion4));


        opcion1FromFrontend = new Opcion("es un proseador de decima generacio", false);
        opcion1FromFrontend.setId(1L);

        opcion2FromFrontend = new Opcion("es comparable a un ryzen 3600x", true);
        opcion2FromFrontend.setId(2L);

        opcion3FromFrontend = new Opcion("tiene 5 nucleos", true);
        opcion3FromFrontend.setId(3L);


        opcion4FromFrontend = new Opcion("tiene 8 nucleos", true);
        opcion4FromFrontend.setId(4L);


        List<Opcion> listaDeRespuestaDelUsuario = List.of(opcion1FromFrontend, opcion2FromFrontend,
                opcion3FromFrontend, opcion4FromFrontend);


        opcionMultipleRespuestaDelUsuario = RespuestaDePreguntaDTO.builder().listaDeOpciones(listaDeRespuestaDelUsuario).build();


    }


    @Test
    void laRespuestaEnviadaPorElUsuarioEsInvalida() {
        Assertions.assertFalse(opcionMultiple.laRespuestaEsCorrecta(opcionMultipleRespuestaDelUsuario));
    }


    @Test
    void laRespuestaEnviadaPorElUsuarioEsValida() {

        opcion1FromFrontend.setEsLaOpcionVerdadera(true);
        opcion2FromFrontend.setEsLaOpcionVerdadera(false);
        opcion3FromFrontend.setEsLaOpcionVerdadera(false);


        Assertions.assertTrue(opcionMultiple.laRespuestaEsCorrecta(opcionMultipleRespuestaDelUsuario));

    }


}
