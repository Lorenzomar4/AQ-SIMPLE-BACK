package com.lorenzomar3.AQ.Pregunta;

import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.DesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DesplegableIndependienteTest {

    DesplegableIndependiente desplegableIndependiente;


    SeleccionUnicaParaDesplegableIndependiente seleccionUnicaParaDesplegableInd1;

    SeleccionUnicaParaDesplegableIndependiente seleccionUnicaParaDesplegableInd1FromFrontend1;

    Opcion opA;
    Opcion opB;
    Opcion opC;
    SeleccionUnicaParaDesplegableIndependiente seleccionUnicaParaDesplegableInd2;
    SeleccionUnicaParaDesplegableIndependiente seleccionUnicaParaDesplegableInd2FromFrontend2;

    Opcion opD;
    Opcion opE;
    Opcion opF;

    RespuestaDePreguntaDTO respuestaDePreguntaDTO;


    Opcion opA1;
    Opcion opB2;
    Opcion opC3;


    Opcion opD1;
    Opcion opE2;
    Opcion opF3;


    @BeforeEach
    void init() {

        opA = new Opcion("1900", false);
        opA.setId(1L);
        opB = new Opcion("1816", true);
        opB.setId(2L);
        opC = new Opcion("1810", false);
        opC.setId(3L);

        seleccionUnicaParaDesplegableInd1 =
                new SeleccionUnicaParaDesplegableIndependiente("¿En que año se declaro la independencia?",
                        List.of(opA, opB, opC));
        seleccionUnicaParaDesplegableInd1.setId(1L);

        opA1 = new Opcion("1900", true);
        opA1.setId(1L);
        opB2 = new Opcion("1816", false);
        opB2.setId(2L);
        opC3 = new Opcion("1810", false);
        opC3.setId(3L);


        seleccionUnicaParaDesplegableInd1FromFrontend1 =
                new SeleccionUnicaParaDesplegableIndependiente("¿En que año se declaro la independencia?",
                        List.of(opA1, opB2, opC3));

        seleccionUnicaParaDesplegableInd1FromFrontend1.setId(1L);

        // ---------------------------------------------------------------------------------------------------------------------
        opD = new Opcion("2007", true);
        opD.setId(4L);
        opE = new Opcion("2001", false);
        opE.setId(5L);
        opF = new Opcion("2003", false);
        opF.setId(6L);

        seleccionUnicaParaDesplegableInd2 =
                new SeleccionUnicaParaDesplegableIndependiente("En que año boca gano su sexta libertadores?",
                        List.of(opD, opE, opF));
        seleccionUnicaParaDesplegableInd2.setId(2L);

        opD1 = new Opcion("2007", false);
        opD1.setId(4L);
        opE2 = new Opcion("2001", false);
        opE2.setId(5L);
        opF3 = new Opcion("2003", true);
        opF3.setId(6L);


        seleccionUnicaParaDesplegableInd2FromFrontend2 =
                new SeleccionUnicaParaDesplegableIndependiente("En que año boca gano su sexta libertadores?",
                        List.of(opD1, opE2, opF3));

        seleccionUnicaParaDesplegableInd2FromFrontend2.setId(2L);


        desplegableIndependiente =
                new DesplegableIndependiente("Para cada pregunta seleccione una opcion que considere correcta",
                        List.of(seleccionUnicaParaDesplegableInd1, seleccionUnicaParaDesplegableInd2)
                );


        respuestaDePreguntaDTO = RespuestaDePreguntaDTO
                .builder()
                .listaDeSeleccionesUnicasParaDesplegableIndependiente(
                        List.of(seleccionUnicaParaDesplegableInd1FromFrontend1, seleccionUnicaParaDesplegableInd2FromFrontend2)
                )
                .build();

    }


    @Test
    void laRespuestaEnviadaPorElUsuarioEsInvalida() {
        Assertions.assertFalse(desplegableIndependiente.laRespuestaEsCorrecta(respuestaDePreguntaDTO));
    }


    @Test
    void laRespuestaEnviadaPorElUsuarioEsValida() {

        opA1.setEsLaOpcionVerdadera(false ); //new Opcion("1900", false);
        opB2.setEsLaOpcionVerdadera(true );
        opC3.setEsLaOpcionVerdadera(false );

        opD1.setEsLaOpcionVerdadera(true );
        opE2.setEsLaOpcionVerdadera(false );
        opF3.setEsLaOpcionVerdadera(false );

        Assertions.assertTrue(desplegableIndependiente.laRespuestaEsCorrecta(respuestaDePreguntaDTO));
    }


}

