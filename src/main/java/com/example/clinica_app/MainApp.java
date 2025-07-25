package com.example.clinica_app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Carregar pacientes e médicos
        List<Paciente> pacientes = ArquivoUtils.carregarPacientes();
        List<Medico> medicos = ArquivoUtils.carregarMedicos();

        for (Paciente p : pacientes) AppContext.sistema.registrarPaciente(p);
        for (Medico m : medicos) AppContext.sistema.registrarMedico(m);

        // Carregar consultas e associar
        Map<String, Paciente> mapaPacientes = pacientes.stream()
                .collect(Collectors.toMap(Paciente::getIdPaciente, p -> p));
        Map<String, Medico> mapaMedicos = medicos.stream()
                .collect(Collectors.toMap(Medico::getIdMedico, m -> m));
        List<Consulta> consultas = ArquivoUtils.carregarConsultas(mapaPacientes, mapaMedicos);

        // Adiciona as consultas nas agendas dos médicos
        for (Consulta c : consultas) {
            String idMedico = c.getMedico().getIdMedico();
            if (AppContext.sistema.isMedico(idMedico)) {
                AppContext.sistema.getAgendaMedico(idMedico)
                        .put(c.getDataHoraInicio(), c);
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/view/menu-inicial.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 280, 287);
        stage.setTitle("Agendamento de Consultas Médicas");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}