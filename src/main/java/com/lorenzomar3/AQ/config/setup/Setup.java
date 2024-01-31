package com.lorenzomar3.AQ.config.setup;

import com.lorenzomar3.AQ.Repository.CuestionarioRepository;
import com.lorenzomar3.AQ.Service.CuestionarioService;
import com.lorenzomar3.AQ.model.Cuestionario;
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
    public Tema fuerzaElectrica;


    @Autowired
    CuestionarioRepository cuestionarioRepository;

    public void datos() {
        eym = new Cuestionario("Electricidad y Magnetismo");
        fuerzaElectrica = new Tema("Campo electrico");

        eym.agregarTema(fuerzaElectrica);

    }

    public void guardarCuestionario() {
        cuestionarioService.crearCuestionario(eym);

    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        datos();
        guardarCuestionario();
    }
}
