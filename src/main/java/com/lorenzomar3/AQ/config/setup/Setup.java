package com.lorenzomar3.AQ.config.setup;

import com.lorenzomar3.AQ.Repository.CuestionarioRepository;
import com.lorenzomar3.AQ.Service.CuestionarioService;
import com.lorenzomar3.AQ.model.AResponder.Opcion;
import com.lorenzomar3.AQ.model.AResponder.Tema;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.OpcionMultiple;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.PreguntaSimple;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.SeleccionUnica;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.VerdaderoOFalso;
import com.lorenzomar3.AQ.model.Cuestionario;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Setup implements ApplicationRunner {

    @Autowired
    CuestionarioService cuestionarioService;


    public Cuestionario eym;

    public Cuestionario matematicaDiscreta;
    public Tema fuerzaElectrica;
    public Tema capacitores;
    //public Pregunta pregunta;
    //public Pregunta pregunta2;

    public PreguntaSimple preguntaSimple;

    public VerdaderoOFalso  preguntaVF;

    SeleccionUnica seleccionUnica;

    OpcionMultiple opcionMultiple;


    Opcion SeleccionUnicaOpcion1;
    Opcion SeleccionUnicaOpcion2;
    Opcion SeleccionUnicaOpcion3;



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

        opcionMultiple = new OpcionMultiple("Intel i7 10700", List.of(opcion1,opcion2,opcion3,opcion4));


        eym.agregarNuevoPreguntaOTema(fuerzaElectrica);
        eym.agregarNuevoPreguntaOTema(capacitores);
        eym.agregarNuevoPreguntaOTema(preguntaSimple);
        eym.agregarNuevoPreguntaOTema(preguntaVF);
        eym.agregarNuevoPreguntaOTema(seleccionUnica);
        eym.agregarNuevoPreguntaOTema(opcionMultiple);

    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        datos();
        guardarCuestionario();
    }

    public void guardarCuestionario(){

        cuestionarioService.saveCuestionario(eym);

    }


}
