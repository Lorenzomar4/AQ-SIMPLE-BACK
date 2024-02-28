package com.lorenzomar3.AQ.Pregunta;

import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.DesplegableCompartido;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DesplegableCompartidoTest {


    OpcionDeDesplegableCompartido opcion1;
    OpcionDeDesplegableCompartido opcion2;

    OpcionDeDesplegableCompartido opcion3;

    OpcionDeDesplegableCompartido opcion4;


    OpcionDeDesplegableCompartido opcion1FromUser;
    OpcionDeDesplegableCompartido opcion2FromUser;

    OpcionDeDesplegableCompartido opcion3FromUser;

    OpcionDeDesplegableCompartido opcion4FromUser;

    DesplegableCompartido desplegableCompartido;

    RespuestaDePreguntaDTO desplegableCompartidoeRespuestaDelUsuario;


    @BeforeEach
    void init() {

        opcion1 = new OpcionDeDesplegableCompartido("Sao Pablo", "Brasil");
        opcion1.setId(1L);
        opcion2 = new OpcionDeDesplegableCompartido("Buenos Aires", "Argentina");
        opcion2.setId(2L);
        opcion3 = new OpcionDeDesplegableCompartido("Montevideo", "Uruguay");
        opcion3.setId(3L);
        opcion4 = new OpcionDeDesplegableCompartido("Lima", "Peru");
        opcion4.setId(4L);


        opcion1FromUser = new OpcionDeDesplegableCompartido("Sao Pablo", "Argentina");
        opcion1FromUser.setId(1L);
        opcion2FromUser = new OpcionDeDesplegableCompartido("Buenos Aires", "Brasil");
        opcion2FromUser.setId(2L);
        opcion3FromUser = new OpcionDeDesplegableCompartido("Montevideo", "Peru");
        opcion3FromUser.setId(3L);
        opcion4FromUser = new OpcionDeDesplegableCompartido("Lima", "Uruguay");
        opcion4FromUser.setId(4L);


        desplegableCompartido =
                new DesplegableCompartido("A que pais pertenecen cada una de estas provincias",
                        List.of(opcion1, opcion2,
                        opcion3, opcion4));

        List<OpcionDeDesplegableCompartido>
                listaDeOpcionesIngresadaPorElUsuario =
                List.of(opcion1FromUser, opcion2FromUser, opcion3FromUser, opcion4FromUser);


        desplegableCompartidoeRespuestaDelUsuario =
                RespuestaDePreguntaDTO
                        .builder()
                        .listaDeOpcionesParaDesplegableCompartidos(listaDeOpcionesIngresadaPorElUsuario)
                        .build();


    }


    @Test
    void laRespuestaEnviadaPorElUsuarioEsInvalida() {
        Assertions.assertFalse(desplegableCompartido.laRespuestaEsCorrecta(desplegableCompartidoeRespuestaDelUsuario));
    }

    @Test
    void laRespuestaEnviadaPorElUsuarioEsValida() {

        opcion1FromUser.setRespuesta("Brasil");
        opcion2FromUser.setRespuesta("Argentina");
        opcion3FromUser.setRespuesta("Uruguay");
        opcion4FromUser.setRespuesta("Peru");

        Assertions.assertTrue(desplegableCompartido.laRespuestaEsCorrecta(desplegableCompartidoeRespuestaDelUsuario));

    }



}
