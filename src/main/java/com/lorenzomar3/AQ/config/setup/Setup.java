package com.lorenzomar3.AQ.config.setup;

import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.AResponder.TeoriaDeLaPregunta;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.*;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.DesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.DesplegableCompartido;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
public class Setup implements ApplicationRunner {

    @Autowired
    TemarioRepository temarioRepository;


    public Temario eym;

    public Temario caso;



    public Temario campoElectrico;
    public Temario capacitores;

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

    TeoriaDeLaPregunta seleccionUnicaTeoria;

    TeoriaDeLaPregunta opcionMultipleTeoria;

    Temario subtemario;
    TeoriaDeLaPregunta paraSimple;


    public void datos() {
        Supplier<TeoriaDeLaPregunta> teoriaDeLaPreguntaSupplier = () -> new TeoriaDeLaPregunta("Prueba");
        eym = new Temario("Electricidad y magnetismo", TipoAResponder.CUESTIONARIO);
        capacitores = new Temario("Capacitores");
        campoElectrico = new Temario("Campo electrico");
        preguntaSimple = new PreguntaSimple("¿Como estuvo tu dia hoy?", "Bien dentro de todo");
        paraSimple = new TeoriaDeLaPregunta("No tiene sentido agregar teoria aqui ,ya que solo se esta realizando pruebas");
        preguntaSimple.agregarTeoria(paraSimple);
        preguntaVF = new VerdaderoOFalso("¿El planeta es redondo?", true);
        SeleccionUnicaOpcion1 = new Opcion("River Plate", false);
        SeleccionUnicaOpcion2 = new Opcion("Boca Juniors", true);
        SeleccionUnicaOpcion3 = new Opcion("Independiente", false);
        List<Opcion> listaDeOpciones = List.of(SeleccionUnicaOpcion1, SeleccionUnicaOpcion2, SeleccionUnicaOpcion3);
        seleccionUnica = new SeleccionUnica("¿En que equipo Jugo Palermo?", listaDeOpciones);

        seleccionUnicaTeoria = new TeoriaDeLaPregunta("Martin Palermo jugo  en el club atletico boca junior" +
                " convirtiendose en uno de los maximos idolos de la historia del club conviertiendo un total de 236 goles");

        seleccionUnica.agregarTeoria(seleccionUnicaTeoria);

        Opcion opcion1 = new Opcion("es un proseador de decima generacio", true);
        Opcion opcion2 = new Opcion("es comparable a un ryzen 3600x", false);
        Opcion opcion3 = new Opcion("tiene 5 nucleos", false);
        Opcion opcion4 = new Opcion("tiene 8 nucleos", true);

        opcionMultiple = new OpcionMultiple("Intel i7 10700", List.of(opcion1, opcion2, opcion3, opcion4));


        opcionMultipleTeoria = new TeoriaDeLaPregunta("El procesador intel i7 10700 es de decima generacion" +
                " tiene 8 nucles y es comparable con un procesador" +
                "5600x y 5800x de amd ryzen ");


        opcionMultiple.agregarTeoria(opcionMultipleTeoria);


        opcionDeDesplegableCompartido1 = new OpcionDeDesplegableCompartido("Sao Pablo", "Brasil");
        opcionDeDesplegableCompartido2 = new OpcionDeDesplegableCompartido("Buenos Aires", "Argentina");
        opcionDeDesplegableCompartido3 = new OpcionDeDesplegableCompartido("Montevideo", "Uruguay");
        opcionDeDesplegableCompartido4 = new OpcionDeDesplegableCompartido("Lima", "Peru");


        desplegableCompartido = new DesplegableCompartido("A que pais pertenecen cada una de estas provincias",
                List.of(opcionDeDesplegableCompartido1, opcionDeDesplegableCompartido2,
                        opcionDeDesplegableCompartido3, opcionDeDesplegableCompartido4));

        //        desplegableCompartido.agregarTeoria(teoriaDeLaPreguntaSupplier.get());

        opA = new Opcion("1900", false);
        opB = new Opcion("1816", true);
        opC = new Opcion("1810", false);


        opD = new Opcion("2007", true);
        opE = new Opcion("2001", false);
        opF = new Opcion("2003", false);

        //        subtemario = new Temario("Sub temario");

        seleccionUnicaParaDesplegableInd1 = new SeleccionUnicaParaDesplegableIndependiente("¿En que año se declaro la independencia?", List.of(opA, opB, opC));
        seleccionUnicaParaDesplegableInd2 = new SeleccionUnicaParaDesplegableIndependiente("En que año boca gano su sexta libertadores?", List.of(opD, opE, opF));


        desplegableIndependiente =
                new DesplegableIndependiente("Para cada pregunta seleccione una opcion que considere correcta",
                        List.of(seleccionUnicaParaDesplegableInd1, seleccionUnicaParaDesplegableInd2)
                );
        /*


        desplegableIndependiente.agregarTeoria(teoriaDeLaPreguntaSupplier.get());
        eym.init();


        eym.agregarALaLista(preguntaSimple);
        eym.agregarALaLista(preguntaVF);
        eym.agregarALaLista(seleccionUnica);
        eym.agregarALaLista(opcionMultiple);
        eym.agregarALaLista(desplegableCompartido);
        eym.agregarALaLista(desplegableIndependiente);
        campoElectrico.init();
        campoElectrico.agregarALaLista(subtemario);
        subtemario.init();
        subtemario.agregarALaLista( new VerdaderoOFalso("¿Riquelme se retiro en boca?", false));
        */
        eym.init();

        eym.agregarALaLista(campoElectrico);
        eym.agregarALaLista(capacitores);
        eym.agregarALaLista(preguntaSimple);
        eym.agregarALaLista(preguntaVF);
        eym.agregarALaLista(seleccionUnica);
        eym.agregarALaLista(opcionMultiple);
        eym.agregarALaLista(desplegableCompartido);


        //FIX PENDIENTE
        //eym.agregarALaLista(desplegableIndependiente);

    }


    @Override
    public void run(ApplicationArguments args) throws Exception {


        datos();
        guardarCuestionario();


    }

    public void guardarCuestionario() {
        temarioRepository.save(eym);
        //temarioRepository.save(caso);


    }


}
