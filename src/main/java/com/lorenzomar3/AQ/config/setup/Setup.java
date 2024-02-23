package com.lorenzomar3.AQ.config.setup;

import com.lorenzomar3.AQ.Repository.CuestionarioRepository;
import com.lorenzomar3.AQ.Service.CuestionarioService;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.Tema;
import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class Setup implements ApplicationRunner {

    @Autowired
    CuestionarioService cuestionarioService;


    public Cuestionario eym;

    public Cuestionario matematicaDiscreta;
    public Tema fuerzaElectrica;
    public Tema capacitores;
    public Pregunta pregunta;
    public Pregunta pregunta2;


    @Autowired
    CuestionarioRepository cuestionarioRepository;

    public void datos() {

        eym = new Cuestionario("Electricidad y Magnetismo");
        fuerzaElectrica = new Tema("Campo electrico");
        pregunta = new Pregunta("¿Como estas hoy?","Horriblemente mal");
        pregunta2 = new Pregunta("¿Como te fue en EYM?","¡Bien por suerte!");

        eym.agregarNuevoPreguntaOTema(pregunta);
        eym.agregarNuevoPreguntaOTema(pregunta2);
        eym.agregarNuevoPreguntaOTema(fuerzaElectrica);

        //eym.agregarNuevoPreguntaOTema(fuerzaElectrica);

        /*
        eym = new Cuestionario("Electricidad y Magnetismo");

        matematicaDiscreta = new Cuestionario("Matematica discreta");

        capacitores = new Tema("Capacitores");

        System.out.println("LLego");
        fuerzaElectrica.agregarPregunta(pregunta);
        System.out.println("LLego2");

        eym.agregarTema(fuerzaElectrica);
        eym.agregarTema(capacitores);
        */
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
