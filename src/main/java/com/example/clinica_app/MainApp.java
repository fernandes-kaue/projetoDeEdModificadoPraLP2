package com.example.clinica_app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainApp extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Carregar pacientes e médicos
        List<Paciente> pacientes = ArquivoUtils.carregarPacientes();
        List<Medico> medicos = ArquivoUtils.carregarMedicos();

        if (medicos.isEmpty()) {
            medicos.add(new Medico("CRM123", "Dr. Carlos", "Cardiologia"));
            medicos.add(new Medico("CRM456", "Dra. Ana", "Dermatologia"));
            medicos.add(new Medico("CRM789", "Dr. James", "Odontologia"));
            medicos.add(new Medico("CRM101", "Dra. Maria", "Anestesiologia"));
            medicos.add(new Medico("CRM102", "Dra. Stephanie", "Ortopedia"));

            ArquivoUtils.salvarMedicos(medicos);
        }

        for (Paciente p : pacientes) AppContext.sistema.registrarPaciente(p);
        for (Medico m : medicos) AppContext.sistema.registrarMedico(m);



        // Carregar consultas e associar
        Map<String, Paciente> mapaPacientes = pacientes.stream().collect(Collectors.toMap(Paciente::getIdPaciente, p -> p));
        Map<String, Medico> mapaMedicos = medicos.stream().collect(Collectors.toMap(Medico::getIdMedico, m -> m));
        List<Consulta> consultas = ArquivoUtils.carregarConsultas(mapaPacientes, mapaMedicos);

        // Adiciona as consultas nas agendas dos médicos
        for (Consulta c : consultas) {
            String idMedico = c.getMedico().getIdMedico();
            if (AppContext.sistema.isMedico(idMedico)) {
                AppContext.sistema.getAgendaMedico(idMedico).put(c.getDataHoraInicio(), c);
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/view/menu-inicial.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 280, 287);
        stage.setTitle("Agendamento de Consultas Médicas");
        stage.setScene(scene);
        stage.show();
    }
}