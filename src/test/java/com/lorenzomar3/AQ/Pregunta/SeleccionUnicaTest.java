package com.lorenzomar3.AQ.Pregunta;

import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SeleccionUnicaTest {


    SeleccionUnica seleccionUnica;


    Opcion SeleccionUnicaOpcion1;
    Opcion SeleccionUnicaOpcion2;
    Opcion SeleccionUnicaOpcion3;


    Opcion SeleccionUnicaOpcion1FromFrontend;
    Opcion SeleccionUnicaOpcion2FromFrontend;
    Opcion SeleccionUnicaOpcion3FromFrontend;


    RespuestaDePreguntaDTO seleccionUnicaRespuesta;


    @BeforeEach
    void init() {

        seleccionUnicaData();

    }


    void seleccionUnicaData() {
        SeleccionUnicaOpcion1 = new Opcion("River Plate", false);
        SeleccionUnicaOpcion1.setId(1L);
        SeleccionUnicaOpcion2 = new Opcion("Boca Juniors", true);
        SeleccionUnicaOpcion2.setId(2L);
        SeleccionUnicaOpcion3 = new Opcion("Independiente", false);
        SeleccionUnicaOpcion3.setId(3L);


        SeleccionUnicaOpcion1FromFrontend = new Opcion("River Plate", false);
        SeleccionUnicaOpcion1FromFrontend.setId(1L);


        SeleccionUnicaOpcion2FromFrontend = new Opcion("Boca Juniors", false);
        SeleccionUnicaOpcion2FromFrontend.setId(2L);

        SeleccionUnicaOpcion3FromFrontend = new Opcion("Independiente", true);
        SeleccionUnicaOpcion3FromFrontend.setId(3L);


        List<Opcion> listaDeOpciones = List.of(SeleccionUnicaOpcion1, SeleccionUnicaOpcion2, SeleccionUnicaOpcion3);

        List<Opcion> listaDeRespuestaDelUsuario = List.of(SeleccionUnicaOpcion1FromFrontend,
                SeleccionUnicaOpcion2FromFrontend,
                SeleccionUnicaOpcion3FromFrontend);
        seleccionUnica = new SeleccionUnica("¿En que equipo Jugo Palermo?", listaDeOpciones);

        seleccionUnicaRespuesta = RespuestaDePreguntaDTO.builder().listaDeOpciones(listaDeRespuestaDelUsuario).build();

    }

    @Test
    void laRespuestaEnviadaPorElUsuarioEsInvalida() {
        Assertions.assertFalse(seleccionUnica.laRespuestaEsCorrecta(seleccionUnicaRespuesta));
    }

    @Test
    void laRespuestaEnviadaPorElUsuarioEsValida() {

        SeleccionUnicaOpcion2FromFrontend.setLaRespuestaEs(true);
        SeleccionUnicaOpcion3FromFrontend.setLaRespuestaEs(false);

        Assertions.assertTrue(seleccionUnica.laRespuestaEsCorrecta(seleccionUnicaRespuesta));

    }


}
