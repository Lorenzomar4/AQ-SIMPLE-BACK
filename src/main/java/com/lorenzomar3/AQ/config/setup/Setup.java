package com.lorenzomar3.AQ.config.setup;

import com.lorenzomar3.AQ.Repository.CuestionarioRepository;
import com.lorenzomar3.AQ.Service.CuestionarioService;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.AResponder.Tema;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.*;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.DesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.DesplegableCompartido;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Setup implements ApplicationRunner {

    @Autowired
    CuestionarioService cuestionarioService;


    public Cuestionario eym;

    public Cuestionario matematicaDiscreta;
    public Tema fuerzaElectrica;
    public Tema capacitores;

    public PreguntaSimple preguntaSimple;

    public VerdaderoOFalso preguntaVF;

    SeleccionUnica seleccionUnica;

    OpcionMultiple opcionMultiple;


    Opcion SeleccionUnicaOpcion1;
    Opcion SeleccionUnicaOpcion2;
    Opcion SeleccionUnicaOpcion3;


    OpcionDeDesplegableCompartido opcionDeDesplegableCompartido1;
    OpcionDeDesplegableCompartido opcionDeDesplegableCompartido2;

    OpcionDeDesplegableCompartido opcionDeDesplegableCompartido3;

    OpcionDeDesplegableCompartido opcionDeDesplegableCompartido4;

    DesplegableCompartido desplegableCompartido;

    DesplegableIndependiente desplegableIndependiente;


    SeleccionUnicaParaDesplegableIndependiente seleccionUnicaParaDesplegableInd1;

    Opcion opA;
    Opcion opB;
    Opcion opC;

    SeleccionUnicaParaDesplegableIndependiente seleccionUnicaParaDesplegableInd2;

    Opcion opD;
    Opcion opE;
    Opcion opF;


    @Autowired
    CuestionarioRepository cuestionarioRepository;

    public void datos() {

        eym = new Cuestionario("Electricidad y Magnetismo");
        capacitores = new Tema("Capacitores");
        fuerzaElectrica = new Tema("Campo electrico");
        preguntaSimple = new PreguntaSimple("¿Como estuvo tu dia hoy?", "Bien dentro de todo");
        preguntaVF = new VerdaderoOFalso("¿El planeta es redondo?", true);

        SeleccionUnicaOpcion1 = new Opcion("River Plate", false);
        SeleccionUnicaOpcion2 = new Opcion("Boca Juniors", true);
        SeleccionUnicaOpcion3 = new Opcion("Independiente", false);
        List<Opcion> listaDeOpciones = List.of(SeleccionUnicaOpcion1, SeleccionUnicaOpcion2, SeleccionUnicaOpcion3);

        seleccionUnica = new SeleccionUnica("¿En que equipo Jugo Palermo?", listaDeOpciones);

        Opcion opcion1 = new Opcion("es un proseador de decima generacio", true);
        Opcion opcion2 = new Opcion("es comparable a un ryzen 3600x", false);
        Opcion opcion3 = new Opcion("tiene 5 nucleos", false);
        Opcion opcion4 = new Opcion("tiene 8 nucleos", true);

        opcionMultiple = new OpcionMultiple("Intel i7 10700", List.of(opcion1, opcion2, opcion3, opcion4));

        opcionDeDesplegableCompartido1 = new OpcionDeDesplegableCompartido("Sao Pablo", "Brasil");
        opcionDeDesplegableCompartido2 = new OpcionDeDesplegableCompartido("Buenos Aires", "Argentina");
        opcionDeDesplegableCompartido3 = new OpcionDeDesplegableCompartido("Montevideo", "Uruguay");
        opcionDeDesplegableCompartido4 = new OpcionDeDesplegableCompartido("Lima", "Peru");


        desplegableCompartido = new DesplegableCompartido("A que pais pertenecen cada una de estas provincias",
                List.of(opcionDeDesplegableCompartido1, opcionDeDesplegableCompartido2,
                        opcionDeDesplegableCompartido3, opcionDeDesplegableCompartido4));


        opA = new Opcion("1900", false);
        opB = new Opcion("1816", true);
        opC = new Opcion("1810", false);


        opD = new Opcion("2007", true);
        opE = new Opcion("2001", false);
        opF = new Opcion("2003", false);


        seleccionUnicaParaDesplegableInd1 = new SeleccionUnicaParaDesplegableIndependiente("¿En que año se declaro la independencia?", List.of(opA, opB, opC));
        seleccionUnicaParaDesplegableInd2 = new SeleccionUnicaParaDesplegableIndependiente("En que año boca gano su sexta libertadores?", List.of(opD, opE, opF));

        desplegableIndependiente =
                new DesplegableIndependiente("Para cada pregunta seleccione una opcion que considere correcta",
                        List.of(seleccionUnicaParaDesplegableInd1, seleccionUnicaParaDesplegableInd2)
                );


        eym.agregarNuevoPreguntaOTema(fuerzaElectrica);
        eym.agregarNuevoPreguntaOTema(capacitores);
        eym.agregarNuevoPreguntaOTema(preguntaSimple);
        eym.agregarNuevoPreguntaOTema(preguntaVF);
        eym.agregarNuevoPreguntaOTema(seleccionUnica);
        eym.agregarNuevoPreguntaOTema(opcionMultiple);
        eym.agregarNuevoPreguntaOTema(desplegableCompartido);
        eym.agregarNuevoPreguntaOTema(desplegableIndependiente);

    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        datos();
        guardarCuestionario();
    }

    public void guardarCuestionario() {

        cuestionarioService.saveCuestionario(eym);

    }


}
