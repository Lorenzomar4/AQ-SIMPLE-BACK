package com.lorenzomar3.AQ.config.setup;

import com.lorenzomar3.AQ.Repository.CuestionarioRepository;
import com.lorenzomar3.AQ.Service.CuestionarioService;
import com.lorenzomar3.AQ.dto.conversor.CuestionarioConversorDTO;
import com.lorenzomar3.AQ.dto.dto.CuestionarioPostDTO;
import com.lorenzomar3.AQ.model.Cuestionario;
import com.lorenzomar3.AQ.model.Pregunta;
import com.lorenzomar3.AQ.model.Tema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
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


    @Autowired
    CuestionarioRepository cuestionarioRepository;

    public void datos() {
        eym = new Cuestionario("Electricidad y Magnetismo");

        matematicaDiscreta = new Cuestionario("Matematica discreta");
        fuerzaElectrica = new Tema("Campo electrico");
        capacitores = new Tema("Capacitores");
        pregunta = new Pregunta("¿Como estas hoy?","Horriblemente mal");
        System.out.println("LLego");
        fuerzaElectrica.agregarPregunta(pregunta);
        System.out.println("LLego2");

        eym.agregarTema(fuerzaElectrica);
        eym.agregarTema(capacitores);

    }

    public void guardarCuestionario() {

        CuestionarioPostDTO c =  CuestionarioConversorDTO.toCuestionarioPostDTO(eym);
        cuestionarioService.crearCuestionario(c);
        cuestionarioService.crearCuestionario(CuestionarioConversorDTO.toCuestionarioPostDTO(matematicaDiscreta));

    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        datos();
        guardarCuestionario();
    }
}
