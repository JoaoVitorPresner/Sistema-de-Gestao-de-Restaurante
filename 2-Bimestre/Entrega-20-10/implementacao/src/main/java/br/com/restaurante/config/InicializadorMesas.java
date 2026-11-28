package br.com.restaurante.config;

import br.com.restaurante.model.Mesa;
import br.com.restaurante.model.StatusMesa;
import br.com.restaurante.repository.MesaRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InicializadorMesas implements CommandLineRunner {

    private final MesaRepository mesas;

    public InicializadorMesas(MesaRepository mesas) {
        this.mesas = mesas;
    }

    @Override
    public void run(String... args) {

        if (mesas.count() > 0) {
            return;
        }

        for (int numero = 1; numero <= 12; numero++) {

            Mesa mesa = new Mesa();

            mesa.numero = numero;
            mesa.status = StatusMesa.LIVRE;

            if (numero <= 4) {

                mesa.capacidade = 2;

            } else if (numero <= 10) {

                mesa.capacidade = 4;

            } else {

                mesa.capacidade = 6;
            }

            mesas.save(mesa);
        }

        System.out.println(
                "12 mesas iniciais cadastradas."
        );
    }
}