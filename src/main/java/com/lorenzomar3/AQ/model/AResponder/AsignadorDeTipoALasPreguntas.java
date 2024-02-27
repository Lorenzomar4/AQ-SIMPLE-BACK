package com.lorenzomar3.AQ.model.AResponder;

import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.*;
import com.lorenzomar3.AQ.model.TipoAResponder;

import java.util.HashMap;

public class AsignadorDeTipoALasPreguntas {

    private static AsignadorDeTipoALasPreguntas instance;

    public static AsignadorDeTipoALasPreguntas getInstance() {


        if (instance == null) {
            instance = new AsignadorDeTipoALasPreguntas();

        }
        return instance;

    }

    public TipoAResponder asignarTipo(Pregunta<?> tipo) {

        HashMap<Class<? extends Pregunta<?>>, TipoAResponder> mapAsignadorDeTipo = new HashMap<>();

        mapAsignadorDeTipo.put(PreguntaSimple.class, TipoAResponder.PREGUNTA_SIMPLE);
        mapAsignadorDeTipo.put(VerdaderoOFalso.class, TipoAResponder.VERDADERO_FALSO);
        mapAsignadorDeTipo.put(SeleccionUnica.class, TipoAResponder.SELECCION_UNICA);
        mapAsignadorDeTipo.put(OpcionMultiple.class, TipoAResponder.OPCION_MULTIPLE);
        mapAsignadorDeTipo.put(DesplegableCompartido.class,TipoAResponder.DESPLEGABLE_COMPARTIDO);
        mapAsignadorDeTipo.put(DesplegableIndependiente.class,TipoAResponder.DESPLEGABLE_COMPARTIDO);

        return mapAsignadorDeTipo.get(tipo.getClass());

    }


}
