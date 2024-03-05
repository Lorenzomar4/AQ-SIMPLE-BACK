package com.lorenzomar3.AQ.model.AResponder;

import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.DesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.DesplegableCompartido;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.OpcionMultiple;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.PreguntaSimple;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.SeleccionUnica;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.VerdaderoOFalso;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

@Component

public class FabricaDePreguntas {


    Map<TipoAResponder, Pregunta> mapTipoKeyPreguntaValue =
            Map.of(
                    TipoAResponder.PREGUNTA_SIMPLE, obtenerClaseTipo(PreguntaSimple::new),
                    TipoAResponder.VERDADERO_FALSO, obtenerClaseTipo(VerdaderoOFalso::new),
                    TipoAResponder.SELECCION_UNICA, obtenerClaseTipo(SeleccionUnica::new),
                    TipoAResponder.OPCION_MULTIPLE, obtenerClaseTipo(OpcionMultiple::new),
                    TipoAResponder.DESPLEGABLE_COMPARTIDO, obtenerClaseTipo(DesplegableCompartido::new),
                    TipoAResponder.DESPLEGABLE_INDEPENDIENTE, obtenerClaseTipo(DesplegableIndependiente::new)
            );


    public FabricaDePreguntas() {

    }

    private static Pregunta obtenerClaseTipo(Supplier<? extends Pregunta> supplier) {
        return supplier.get();

    }

    public AResponder fromJSON(PostPreguntaDTO preguntaDTO) {
        AResponder pregunta = mapTipoKeyPreguntaValue.get(preguntaDTO.getTipo());
        BeanUtils.copyProperties(preguntaDTO, pregunta);

        if(pregunta.getTipo().equals(TipoAResponder.OPCION_MULTIPLE)){
            OpcionMultiple opcionMultiple = (OpcionMultiple) pregunta;

        }

        return pregunta;
    }


}