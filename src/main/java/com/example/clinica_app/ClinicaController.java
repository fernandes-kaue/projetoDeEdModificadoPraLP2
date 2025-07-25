package com.example.clinica_app;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Window;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.StringConverter;

import java.util.Optional;
import java.time.format.DateTimeFormatter;

public class ClinicaController {

    @FXML
    private TableColumn<Consulta, String> colMinhasConsultaMedico;
    @FXML
    private TableColumn<Consulta, String> colMinhasConsultaEspecialidade;

    // Campos do Menu Inicial
    @FXML
    private TextField campoNomeUsuario;

    // Campos de Cadastro de Paciente
    @FXML
    private TextField campoIdPaciente;
    @FXML
    private TextField campoNomePaciente;
    @FXML
    private TextField campoIdadePaciente;
    @FXML
    private TextField campoContatoPaciente;

    // Campos de Cadastro de Médico
    @FXML
    private TextField campoIdMedico;
    @FXML
    private TextField campoNomeMedico;
    @FXML
    private TextField campoEspecialidadeMedico;

    // Campos de Cadastro de Disponibilidade
    @FXML
    private DatePicker dataDisponibilidade;
    @FXML
    private TextField horaInicio;
    @FXML
    private TextField horaFim;
    @FXML
    private TableView<Consulta> tabelaDisponibilidades;
    @FXML
    private TableColumn<Consulta, LocalDateTime> colInicio;
    @FXML
    private TableColumn<Consulta, LocalDateTime> colFim;
    @FXML
    private TableColumn<Consulta, String> colStatus;

    // Campos para a tabela de disponibilidades (agenda)
    @FXML
    private TableView<Consulta> tabelaAgenda;
    @FXML
    private TableColumn<Consulta, String> colAgendaData;
    @FXML
    private TableColumn<Consulta, String> colAgendaHora;

    // Campos para a tabela de consultas agendadas
    @FXML
    private TableView<Consulta> tabelaConsultas;
    @FXML
    private TableColumn<Consulta, String> colConsultaData;
    @FXML
    private TableColumn<Consulta, String> colConsultaHora;
    @FXML
    private TableColumn<Consulta, String> colConsultaPaciente;
    @FXML
    private TableColumn<Consulta, String> colConsultaNumero;
    @FXML
    private TableColumn<Consulta, String> colConsultaStatus;
    @FXML
    private TextArea motivoCancelamento;

    @FXML
    private TableView<Consulta> tabelaMinhasConsultas;
    @FXML
    private TableColumn<Consulta, String> colMinhasConsultaData;
    @FXML
    private TableColumn<Consulta, String> colMinhasConsultaHora;
    @FXML
    private TableColumn<Consulta, String> colMinhasConsultaStatus;
    @FXML
    private TableColumn<Consulta, String> colMinhasConsultaMotivo;

    // --- CAMPOS FXML DA TELA "AGENDAR CONSULTAS" ---
    @FXML
    private ComboBox<String> comboEspecialidades; // MUDOU: Agora é de String

    // --- CAMPOS FXML DA TELA "MEDICO" ---
    @FXML
    private ListView<Consulta> listaDisponibilidades;
    @FXML
    private ListView<Consulta> listaConsultas;

    // --- CAMPOS FXML DA TELA "MINHAS CONSULTAS" ---
    @FXML
    private ListView<Consulta> listaMinhasConsultas;
    @FXML
    private ListView<Consulta> listaAgenda;

    // Sistema compartilhado
    private final SistemaAgendamento sistema = AppContext.sistema;
    //test



    // MÉTODO 1: Verifica ID digitado e direciona para tela correspondente
    @FXML
    private void verificarUsuarioEAvancar(ActionEvent event) {
        String id = campoNomeUsuario.getText().trim();

        if (id.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Digite um ID.");
            return;
        }

        if (sistema.isPaciente(id)) {
            AppContext.usuarioLogadoId = id;
            abrirTela("/view/tela-paciente.fxml", "Área do Paciente");
        } else if (sistema.isMedico(id)) {
            AppContext.usuarioLogadoId = id; // ← salva o ID do médico logado
            abrirTela("/view/tela-medico.fxml", "Área do Médico");
            atualizarTabelaAgenda(id); // Atualiza a tabela de agenda do médico logado
            atualizarListaAgenda(id);
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "ID não encontrado.");
        }
    }

    @FXML
    private void onCadastrarPacienteButton() {
        abrirTela("/view/cadastro-paciente.fxml", "Cadastro de Paciente");
    }

    @FXML
    private void onCadastrarMedicoButton() {
        abrirTela("/view/cadastro-medico.fxml", "Cadastro de Médico");
    }

    @FXML
    private void irparaCadastro() {
        abrirTela("/view/menu-cadastro.fxml", "Cadastro de Paciente ou Médico");
    }

    @FXML
    public void abrirTelaAgendarConsulta(ActionEvent event) {
        abrirTela("/view/tela-agendar-consulta.fxml", "Agendar Nova Consulta");
    }

    @FXML
    public void abrirTelaMinhasConsultas(ActionEvent event) {
        abrirTela("/view/tela-minhas-consultas.fxml", "Minhas Consultas");
    }

    // MÉTODO 2: Cadastrar Paciente
    @FXML
    protected void onCadastrarPaciente() {
        String id = campoIdPaciente.getText().trim();
        String nome = campoNomePaciente.getText().trim();
        String idadeStr = campoIdadePaciente.getText().trim();
        String contato = campoContatoPaciente.getText().trim();

        if (id.isEmpty() || nome.isEmpty() || idadeStr.isEmpty() || contato.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Preencha todos os campos.");
            return;
        }

        int idade;
        try {
            idade = Integer.parseInt(idadeStr);
            if (idade <= 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Idade deve ser maior que zero.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Idade inválida.");
            return;
        }

        Paciente paciente = new Paciente(id, nome, idade, contato);
        sistema.registrarPaciente(paciente);
        ArquivoUtils.salvarPacientes(new ArrayList<>(sistema.getTodosPacientes()));

        mostrarAlerta(Alert.AlertType.INFORMATION, "Paciente cadastrado com sucesso!");

        campoIdPaciente.clear();
        campoNomePaciente.clear();
        campoIdadePaciente.clear();
        campoContatoPaciente.clear();

        // Abre a tela inicial
        abrirTela("/view/menu-inicial.fxml", "Menu Inicial");
    }

    // MÉTODO 3: Cadastrar Médico
    @FXML
    protected void onCadastrarMedico() {
        String id = campoIdMedico.getText().trim();
        String nome = campoNomeMedico.getText().trim();
        String especialidade = campoEspecialidadeMedico.getText().trim();

        if (id.isEmpty() || nome.isEmpty() || especialidade.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Preencha todos os campos.");
            return;
        }

        Medico medico = new Medico(id, nome, especialidade);
        sistema.registrarMedico(medico);
        ArquivoUtils.salvarMedicos(new ArrayList<>(sistema.getTodosMedicos()));

        mostrarAlerta(Alert.AlertType.INFORMATION, "Médico cadastrado com sucesso!");

        campoIdMedico.clear();
        campoNomeMedico.clear();
        campoEspecialidadeMedico.clear();

        // Abre a tela inicial
        abrirTela("/view/menu-inicial.fxml", "Menu Inicial");
    }

    // MÉTODO AUXILIAR: abrir qualquer tela
    private void abrirTela(String caminhoFXML, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFXML));
            Parent root = loader.load();
            Stage stageAtual = (Stage) Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
            if (stageAtual != null) {
                stageAtual.setTitle(titulo);
                stageAtual.setScene(new Scene(root));
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao abrir a tela: " + e.getMessage());
        }
    }


    @FXML
    private DatePicker dataReagendamento;
    @FXML
    private TextField horaInicioReagendamento;
    @FXML
    private TextField horaFimReagendamento;
    @FXML
    private TextArea motivoReagendamento;
    @FXML
    private Label lblMedico;
    @FXML
    private Label lblEspecialidade;
    @FXML
    private Label lblPaciente;
    @FXML
    private Label lblDataAtual;
    @FXML
    private Label lblHoraAtual;

    @FXML
    public void onConfirmarReagendamentoPaciente(ActionEvent event) {
        try {
            if (AppContext.consultaParaReagendar == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Nenhuma consulta selecionada para reagendamento.");
                return;
            }

            if (dataReagendamento.getValue() == null || horaInicioReagendamento.getText().isEmpty() || horaFimReagendamento.getText().isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Preencha todos os campos de data e horário.");
                return;
            }

            LocalDate data = dataReagendamento.getValue();
            LocalTime inicio = LocalTime.parse(horaInicioReagendamento.getText());
            LocalTime fim = LocalTime.parse(horaFimReagendamento.getText());
            LocalDateTime inicioDT = LocalDateTime.of(data, inicio);
            LocalDateTime fimDT = LocalDateTime.of(data, fim);

            if (inicioDT.isAfter(fimDT) || inicioDT.isEqual(fimDT)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Horário de início deve ser antes do horário de fim.");
                return;
            }

            // Create new appointment request
            Consulta novaConsulta = new Consulta(UUID.randomUUID().toString(), AppContext.consultaParaReagendar.getPaciente(), AppContext.consultaParaReagendar.getMedico(), inicioDT, fimDT);
            novaConsulta.setStatus(Consulta.STATUS_SOLICITADO);

            // Cancel old appointment
            sistema.cancelarConsultaPorPaciente(AppContext.consultaParaReagendar);

            // Add new appointment request
            sistema.solicitarReagendamento(novaConsulta);

            // Save changes
            ArquivoUtils.salvarConsultas(AppContext.sistema.getTodosMedicos().stream().flatMap(medico -> AppContext.sistema.getConsultas(medico.getIdMedico()).stream()).collect(Collectors.toList()));

            mostrarAlerta(Alert.AlertType.INFORMATION, "Reagendamento solicitado! Aguarde confirmação do médico.");
            abrirTela("/view/tela-minhas-consultas.fxml", "Minhas Consultas");

        } catch (DateTimeParseException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato de hora inválido (use HH:mm).");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao reagendar: " + e.getMessage());
        }
    }


    @FXML
    public void onConfirmarReagendamentoMedico(ActionEvent event) {
        try {
            if (AppContext.consultaParaReagendar == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Nenhuma consulta selecionada para reagendamento.");
                return;
            }

            String motivo = motivoReagendamento.getText().trim();
            if (motivo.isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Informe o motivo do reagendamento.");
                return;
            }

            if (dataReagendamento.getValue() == null || horaInicioReagendamento.getText().isEmpty() || horaFimReagendamento.getText().isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Preencha todos os campos de data e horário.");
                return;
            }

            LocalDate data = dataReagendamento.getValue();
            LocalTime inicio = LocalTime.parse(horaInicioReagendamento.getText());
            LocalTime fim = LocalTime.parse(horaFimReagendamento.getText());
            LocalDateTime inicioDT = LocalDateTime.of(data, inicio);
            LocalDateTime fimDT = LocalDateTime.of(data, fim);

            if (inicioDT.isAfter(fimDT) || inicioDT.isEqual(fimDT)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Horário de início deve ser antes do horário de fim.");
                return;
            }

            // Create new appointment
            Consulta novaConsulta = new Consulta(UUID.randomUUID().toString(), AppContext.consultaParaReagendar.getPaciente(), AppContext.consultaParaReagendar.getMedico(), inicioDT, fimDT);
            novaConsulta.setStatus(Consulta.STATUS_REAGENDADA);
            novaConsulta.setMotivoCancelamento(motivo);

            // Cancel old appointment
            sistema.cancelarConsultaPorMedico(AppContext.usuarioLogadoId, AppContext.consultaParaReagendar.getIdConsulta());

            // Add new appointment
            sistema.solicitarReagendamento(novaConsulta);

            // Save changes
            ArquivoUtils.salvarConsultas(AppContext.sistema.getTodosMedicos().stream().flatMap(medico -> AppContext.sistema.getConsultas(medico.getIdMedico()).stream()).collect(Collectors.toList()));

            mostrarAlerta(Alert.AlertType.INFORMATION, "Consulta reagendada com sucesso!");
            abrirTela("/view/tela-medico.fxml", "Área do Médico");

        } catch (DateTimeParseException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato de hora inválido (use HH:mm).");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao reagendar: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    @FXML
    public void initialize() {

//        TODO: formatadores de data e hora automatico
//        configurarFormatadores();


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");


        if (tabelaMinhasConsultas != null) {

            colMinhasConsultaMedico.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomeMedico()));

            colMinhasConsultaEspecialidade.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEspecialidadeMedico()));

            // Coluna Data (existente)
            colMinhasConsultaData.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDataHoraInicio().toLocalDate().format(formatter)));

            // Coluna Horário (existente)
            colMinhasConsultaHora.setCellValueFactory(cellData -> {
                String inicio = cellData.getValue().getDataHoraInicio().toLocalTime().format(horaFormatter);
                String fim = cellData.getValue().getDataHoraFim().toLocalTime().format(horaFormatter);
                return new SimpleStringProperty(inicio + " - " + fim);
            });

            // NOVA Coluna Médico
            colMinhasConsultaMedico.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomeMedico()));

            // NOVA Coluna Especialidade
            colMinhasConsultaEspecialidade.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEspecialidadeMedico()));

            // Coluna Status (existente)
            colMinhasConsultaStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

            // Coluna Motivo (existente)
            colMinhasConsultaMotivo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMotivoCancelamento() != null ? cellData.getValue().getMotivoCancelamento() : ""));

            // Carrega os dados na tabela
            tabelaMinhasConsultas.getItems().setAll(sistema.getConsultasPaciente(AppContext.usuarioLogadoId).stream().filter(c -> !"DISPONIVEL".equals(c.getStatus())).collect(Collectors.toList()));
        }

        if (tabelaAgenda != null) {
            colAgendaData.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDataHoraInicio().toLocalDate().format(formatter)));
            colAgendaHora.setCellValueFactory(cellData -> {
                String inicio = cellData.getValue().getDataHoraInicio().toLocalTime().format(horaFormatter);
                String fim = cellData.getValue().getDataHoraFim().toLocalTime().format(horaFormatter);
                return new SimpleStringProperty(inicio + " - " + fim);
            });
            atualizarTabelaAgenda(AppContext.usuarioLogadoId);
        }

        if (tabelaConsultas != null) {
            colConsultaData.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDataHoraInicio().toLocalDate().format(formatter)));
            colConsultaHora.setCellValueFactory(cellData -> {
                String inicio = cellData.getValue().getDataHoraInicio().toLocalTime().format(horaFormatter);
                String fim = cellData.getValue().getDataHoraFim().toLocalTime().format(horaFormatter);
                return new SimpleStringProperty(inicio + " - " + fim);
            });
            colConsultaPaciente.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaciente() != null ? cellData.getValue().getPaciente().getNome() : ""));
            colConsultaNumero.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaciente() != null ? cellData.getValue().getPaciente().getContato() : ""));
            atualizarTabelaConsultasMedico(AppContext.usuarioLogadoId);
        }

        // --- LÓGICA PARA A TELA "MINHAS CONSULTAS" ---
        if (listaMinhasConsultas != null) {
            atualizarListaMinhasConsultas();
        }

        // --- LÓGICA PARA A TELA DO MÉDICO (TableView) ---
        if (tabelaDisponibilidades != null) {
            colInicio.setCellValueFactory(new PropertyValueFactory<>("dataHoraInicio"));
            colFim.setCellValueFactory(new PropertyValueFactory<>("dataHoraFim"));
            colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

            // Formatação das datas
            colInicio.setCellFactory(column -> new TableCell<Consulta, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            });
            colFim.setCellFactory(column -> new TableCell<Consulta, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            });
            atualizarTabelaDisponibilidades(AppContext.usuarioLogadoId);
        }

        // --- LÓGICA PARA A TELA DO MÉDICO (ListView Agenda) ---
        if (listaAgenda != null) {
            atualizarListaAgenda(AppContext.usuarioLogadoId);
        }


        if (comboEspecialidades != null) {
            // Adiciona listener para mudanças de seleção
            comboEspecialidades.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> atualizarListaDisponibilidadesPorEspecialidade()
            );

            // Carrega as especialidades (isso agora vai selecionar automaticamente a primeira)
            carregarEspecialidades();
        }


    }

    private void configurarFormatadores() {
        // Configurar campos de hora
        configurarFormatadorHora(horaInicio);
        configurarFormatadorHora(horaFim);
        configurarFormatadorHora(horaInicioReagendamento);
        configurarFormatadorHora(horaFimReagendamento);
        configurarDatePickers();
    }


    public List<Consulta> getConsultasPaciente(String idPaciente) {
        return sistema.getConsultasPaciente(idPaciente).stream().filter(c -> c.getPaciente() != null && c.getPaciente().getIdPaciente().equals(idPaciente)).collect(Collectors.toList());
    }

    private void carregarEspecialidades() {
        comboEspecialidades.getItems().clear();

        // Get all unique specialties that actually have available appointments
        Set<String> especialidadesComDisponibilidade = AppContext.sistema.getTodasConsultasDisponiveis()
                .stream()
                .filter(c -> c.getMedico() != null)
                .map(c -> c.getMedico().getEspecialidade())
                .filter(especialidade -> especialidade != null && !especialidade.isEmpty())
                .collect(Collectors.toSet());

        if (especialidadesComDisponibilidade.isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Nenhuma especialidade com horários disponíveis no momento.");
        } else {
            comboEspecialidades.getItems().addAll(especialidadesComDisponibilidade);

            // Seleciona a primeira especialidade automaticamente
            if (!especialidadesComDisponibilidade.isEmpty()) {
                comboEspecialidades.getSelectionModel().selectFirst();

                // Força a atualização imediata da lista, independentemente do número de especialidades
                Platform.runLater(() -> {
                    atualizarListaDisponibilidadesPorEspecialidade();
                });
            }
        }
    }


    @SuppressWarnings("unused")
    private void carregarConsultas() {
        listaMinhasConsultas.getItems().clear();
        List<Consulta> consultas = AppContext.sistema.getConsultasPaciente(AppContext.usuarioLogadoId);
        for (Consulta consulta : consultas) {
            listaMinhasConsultas.getItems().add(consulta);
        }
    }

    // MÉTODO AUXILIAR: mostrar alertas
    private void mostrarAlerta(Alert.AlertType tipo, String msg) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    //MÉTODO Auxiliar: Voltar para a tela inicial
    public void onVoltarTelaInicial(ActionEvent actionEvent) {
        abrirTela("/view/menu-inicial.fxml", "Menu Inicial");
    }

    //MÉTODO Auxiliar: Voltar para a tela de Cadastro
    @FXML
    public void onVoltarTelaCadastro(ActionEvent event) {
        abrirTela("/view/menu-cadastro.fxml", "Cadastro de Paciente ou Médico");
    }

    //MÉTODO Auxiliar: Voltar para a tela paciente
    public void onVoltarTelaPaciente(ActionEvent actionEvent) {
        abrirTela("/view/tela-paciente.fxml", "Tela Paciente");
    }

    //MÉTODO Auxiliar: Voltar para a tela paciente
    public void onVoltarTelaMinhasConsultas(ActionEvent actionEvent) {
        abrirTela("/view/tela-minhas-consultas.fxml", "Minhas Consultas");
    }

    //MÉTODO Auxiliar: Voltar para a tela medico
    public void onVoltarTelaMedico(ActionEvent actionEvent) {
        abrirTela("/view/tela-medico.fxml", "Área do Médico");
    }

    //Método Auxiliar
    private void atualizarTabelaDisponibilidades(String idMedico) {
        tabelaDisponibilidades.getItems().setAll(sistema.getConsultas(idMedico).stream().filter(c -> "DISPONIVEL".equals(c.getStatus())).collect(Collectors.toList()));
    }

    @FXML
    public void onCancelarOuReagendarConsulta(ActionEvent event) {
        Consulta consultaSelecionada = tabelaMinhasConsultas.getSelectionModel().getSelectedItem();

        if (consultaSelecionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecione uma consulta para cancelar ou reagendar.");
            return;
        }

        if (!"AGENDADA".equals(consultaSelecionada.getStatus())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Só é possível cancelar ou reagendar consultas agendadas.");
            return;
        }

        // Create dialog with options
        ButtonType cancelarButton = new ButtonType("Cancelar", ButtonBar.ButtonData.OK_DONE);
        ButtonType reagendarButton = new ButtonType("Reagendar", ButtonBar.ButtonData.OK_DONE);
        ButtonType voltarButton = new ButtonType("Voltar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Escolha uma opção");
        alert.setHeaderText("O que deseja fazer com esta consulta?");
        alert.setContentText("Consulta com " + consultaSelecionada.getNomeMedico() + " (" + consultaSelecionada.getEspecialidadeMedico() + ")\n" + "Em: " + consultaSelecionada.getDataHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        alert.getButtonTypes().setAll(cancelarButton, reagendarButton, voltarButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == cancelarButton) {
            sistema.cancelarConsultaPorPaciente(consultaSelecionada);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Consulta cancelada com sucesso!");
        } else if (result.get() == reagendarButton) {
            AppContext.consultaParaReagendar = consultaSelecionada;
            abrirTela("/view/tela-reagendamento-paciente.fxml", "Reagendar Consulta");
            return;
        }

        // Update table
        tabelaMinhasConsultas.getItems().setAll(sistema.getConsultasPaciente(AppContext.usuarioLogadoId).stream().filter(c -> !"DISPONIVEL".equals(c.getStatus())).collect(Collectors.toList()));
    }


    @FXML
    public void onConfirmarConsulta(ActionEvent event) {
        Consulta consultaSelecionada = tabelaConsultas.getSelectionModel().getSelectedItem();
        if (consultaSelecionada == null || !"SOLICITADO".equals(consultaSelecionada.getStatus())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecione uma consulta solicitada para confirmar.");
            return;
        }
        consultaSelecionada.setStatus("AGENDADA");
        mostrarAlerta(Alert.AlertType.INFORMATION, "Consulta confirmada!");
        atualizarTabelaConsultasMedico(AppContext.usuarioLogadoId);
    }

    @FXML
    public void onConcluirConsulta(ActionEvent event) {
        Consulta consultaSelecionada = tabelaConsultas.getSelectionModel().getSelectedItem();
        if (consultaSelecionada == null || !"AGENDADA".equals(consultaSelecionada.getStatus())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecione uma consulta agendada para confirmar.");
            return;
        }
        consultaSelecionada.setStatus("CONCLUIDA");
        mostrarAlerta(Alert.AlertType.INFORMATION, "Consulta finalizada!");
        ArquivoUtils.salvarConsultas(AppContext.sistema.getTodosMedicos().stream().flatMap(medico -> AppContext.sistema.getConsultas(medico.getIdMedico()).stream()).collect(Collectors.toList()));
        atualizarTabelaConsultasMedico(AppContext.usuarioLogadoId);
    }

    @FXML
    public void onRemoverConsultaMinhasConsultas(ActionEvent event) {
        Consulta consultaSelecionada = tabelaMinhasConsultas.getSelectionModel().getSelectedItem();
        if (consultaSelecionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecione uma consulta para remover.");
            return;
        }
        if (!"CANCELADA".equals(consultaSelecionada.getStatus()) && !"CONCLUIDA".equals(consultaSelecionada.getStatus())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Só é possível remover consultas canceladas ou concluídas.");
            return;
        }
        // Remove do histórico do paciente
        if (consultaSelecionada.getPaciente() != null) {
            consultaSelecionada.getPaciente().removerConsultaDoHistorico(consultaSelecionada.getIdConsulta());
        }
        // Remove da agenda do médico
        sistema.getAgendaMedico(consultaSelecionada.getMedico().getIdMedico()).remove(consultaSelecionada.getDataHoraInicio());

        // Atualiza o arquivo
        ArquivoUtils.salvarConsultas(AppContext.sistema.getTodosMedicos().stream().flatMap(medico -> AppContext.sistema.getConsultas(medico.getIdMedico()).stream()).collect(Collectors.toList()));

        // Atualiza a tabela
        tabelaMinhasConsultas.getItems().setAll(sistema.getConsultasPaciente(AppContext.usuarioLogadoId));
        mostrarAlerta(Alert.AlertType.INFORMATION, "Consulta removida com sucesso!");
    }

    @FXML
    public void onReagendarConsultaMedico(ActionEvent event) {
        Consulta consultaSelecionada = tabelaConsultas.getSelectionModel().getSelectedItem();

        if (consultaSelecionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecione uma consulta para reagendar.");
            return;
        }

        if (!"AGENDADA".equals(consultaSelecionada.getStatus())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Só é possível reagendar consultas agendadas.");
            return;
        }

        AppContext.consultaParaReagendar = consultaSelecionada;
        abrirTela("/view/tela-reagendamento-medico.fxml", "Reagendar Consulta");
    }

    public void onVerAgenda(ActionEvent actionEvent) {
    }

    // O método onAgendarConsultaPaciente não precisa mudar, pois a lógica dele já era pegar uma Consulta da lista.
    @FXML
    public void onAgendarConsultaPaciente(ActionEvent event) {
        try {
            // Verifica se há um paciente logado
            if (AppContext.usuarioLogadoId == null || !sistema.isPaciente(AppContext.usuarioLogadoId)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Nenhum paciente logado ou usuário não é paciente.");
                return;
            }

            // Obtém a consulta selecionada
            Consulta consultaSelecionada = listaDisponibilidades.getSelectionModel().getSelectedItem();

            if (consultaSelecionada == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Selecione um horário disponível para agendar.");
                return;
            }

            // Verifica se a consulta ainda está disponível
            if (!"DISPONIVEL".equals(consultaSelecionada.getStatus())) {
                mostrarAlerta(Alert.AlertType.WARNING, "Este horário não está mais disponível.");
                return;
            }

            // Tenta agendar a consulta
            sistema.agendarConsulta(
                    AppContext.usuarioLogadoId,
                    consultaSelecionada.getMedico().getIdMedico(),
                    consultaSelecionada.getDataHoraInicio(),
                    consultaSelecionada.getDataHoraFim()
            );

            mostrarAlerta(Alert.AlertType.INFORMATION, "Consulta agendada com sucesso!");

            // Atualiza as listas
            atualizarListaDisponibilidadesPorEspecialidade();

            // Atualiza o arquivo de consultas
            ArquivoUtils.salvarConsultas(
                    AppContext.sistema.getTodosMedicos().stream()
                            .flatMap(medico -> AppContext.sistema.getConsultas(medico.getIdMedico()).stream())
                            .collect(Collectors.toList())
            );

        } catch (IllegalStateException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao agendar: " + e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onReagendarConsultaPaciente(ActionEvent actionEvent) {
        Consulta consultaSelecionada = tabelaMinhasConsultas.getSelectionModel().getSelectedItem();

        if (consultaSelecionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecione uma consulta para reagendar.");
            return;
        }

        if ("AGENDADA".equals(consultaSelecionada.getStatus())) {
            AppContext.consultaParaReagendar = consultaSelecionada;
            abrirTela("/view/tela-reagendamento-paciente.fxml", "Reagendar Consulta");
            return;
        }

        mostrarAlerta(Alert.AlertType.ERROR, "Só é possível reagendar consultas agendadas.");
    }

    @FXML
    public void onDisponibilidadeSelecionada(MouseEvent event) {
        if (event.getClickCount() == 2) { // Duplo clique
            onAgendarConsultaPaciente(null); // Pode passar null ou criar um ActionEvent
        }
    }

    // O método onCancelarConsultaPaciente também não precisa mudar.
    @FXML
    public void onCancelarConsultaPaciente(ActionEvent actionEvent) {
        Consulta minhaConsulta = listaMinhasConsultas.getSelectionModel().getSelectedItem();
        if (minhaConsulta == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecione uma de suas consultas para cancelar.");
            return;
        }

        if (minhaConsulta.getPaciente() == null || !minhaConsulta.getPaciente().getIdPaciente().equals(AppContext.usuarioLogadoId)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Esta não é uma consulta sua.");
            return;
        }

        sistema.cancelarConsultaPorPaciente(minhaConsulta);
        mostrarAlerta(Alert.AlertType.INFORMATION, "Consulta cancelada com sucesso!");
        atualizarListaMinhasConsultas(); // Atualiza a lista para remover a consulta cancelada

        mostrarAlerta(Alert.AlertType.INFORMATION, "Consulta cancelada com sucesso!");
    }

    /**
     * Método auxiliar para carregar/atualizar ata de consultas do paciente na tela.
     * Ele deve ser chamado no início e após qualquer agendamento/cancelamento.
     */
    private void atualizarTabelaAgenda(String idMedico) {
        if (tabelaAgenda != null) {
            tabelaAgenda.getItems().setAll(sistema.getConsultas(idMedico).stream().filter(c -> "DISPONIVEL".equals(c.getStatus())).collect(Collectors.toList()));
        }
    }

    private void atualizarTabelaConsultasMedico(String idMedico) {
        if (tabelaConsultas != null) {
            tabelaConsultas.getItems().setAll(sistema.getConsultas(idMedico).stream().filter(c -> !"DISPONIVEL".equals(c.getStatus())).collect(Collectors.toList()));
        }
    }

    /**
     * NOVO MÉTODO: Atualiza a lista de horários disponíveis filtrando por especialidade.
     */
    @FXML
    private void atualizarListaDisponibilidadesPorEspecialidade() {
        if (comboEspecialidades == null || listaDisponibilidades == null) return;

        String especialidadeSelecionada = comboEspecialidades.getValue();

        if (especialidadeSelecionada == null || especialidadeSelecionada.isEmpty()) {
            listaDisponibilidades.getItems().clear();
            return;
        }

        // Get all available appointments for the selected specialty
        List<Consulta> consultasDisponiveis = AppContext.sistema.getTodasConsultasDisponiveis()
                .stream()
                .filter(consulta -> consulta.getMedico() != null)
                .filter(consulta -> especialidadeSelecionada.equals(consulta.getMedico().getEspecialidade()))
                .filter(consulta -> "DISPONIVEL".equals(consulta.getStatus()))
                .sorted(Comparator.comparing(Consulta::getDataHoraInicio))
                .collect(Collectors.toList());

        // Update the ListView
        listaDisponibilidades.getItems().setAll(consultasDisponiveis);

        // Mostra mensagem se não houver horários
        if (consultasDisponiveis.isEmpty()) {
            listaDisponibilidades.setPlaceholder(
                    new Label("Nenhum horário disponível para " + especialidadeSelecionada));
        }
    }

    /**
     * Atualiza a lista de consultas que o paciente logado já agendou. (Sem alteração)
     */
    private void atualizarListaMinhasConsultas() {
        if (AppContext.usuarioLogadoId != null) {
            listaMinhasConsultas.getItems().setAll(sistema.getConsultasPaciente(AppContext.usuarioLogadoId));
        }
    }

    public void onVerConsultasPaciente(ActionEvent actionEvent) {
    }

    @FXML
    public void onCancelarDisponibilidade(ActionEvent event) {
        Consulta disponibilidadeSelecionada = tabelaAgenda.getSelectionModel().getSelectedItem();
        if (disponibilidadeSelecionada == null || !"DISPONIVEL".equals(disponibilidadeSelecionada.getStatus())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecione uma disponibilidade para cancelar.");
            return;
        }
        boolean removido = sistema.cancelarConsultaPorMedico(AppContext.usuarioLogadoId, disponibilidadeSelecionada.getIdConsulta());
        if (removido) {
            ArquivoUtils.salvarConsultas(AppContext.sistema.getTodosMedicos().stream().flatMap(medico -> AppContext.sistema.getConsultas(medico.getIdMedico()).stream()).collect(Collectors.toList()));
            atualizarTabelaAgenda(AppContext.usuarioLogadoId);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Disponibilidade cancelada");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao cancelar disponibilidade.");
        }
    }

    public void onCadastrarDisponibilidade(ActionEvent actionEvent) {
        try {
            String idMedico = AppContext.usuarioLogadoId;
            if (idMedico == null || idMedico.isEmpty()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Nenhum médico está logado.");
                return;
            }

            // Validar campos
            if (dataDisponibilidade.getValue() == null || horaInicio.getText().isEmpty() || horaFim.getText().isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Preencha todos os campos.");
                return;
            }

            // Converter data e hora
            LocalDate data = dataDisponibilidade.getValue();
            if (data == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Informe a data.");
                return;
            }
            LocalTime horaInicioParsed = LocalTime.parse(horaInicio.getText());
            LocalTime horaFimParsed = LocalTime.parse(horaFim.getText());

            LocalDateTime inicio = LocalDateTime.of(data, horaInicioParsed);
            LocalDateTime fim = LocalDateTime.of(data, horaFimParsed);

            if (!fim.isAfter(inicio)) {
                mostrarAlerta(Alert.AlertType.WARNING, "A hora de fim deve ser após a hora de início.");
                return;
            }

            // Cadastrar no sistema
            sistema.cadastrarDisponibilidade(idMedico, inicio, fim);

            // Atualizar todas as tabelas/listas relevantes
            if (tabelaDisponibilidades != null) {
                atualizarTabelaDisponibilidades(idMedico);
            }
            if (tabelaAgenda != null) {
                atualizarTabelaAgenda(idMedico);
            }
            if (listaAgenda != null) {
                atualizarListaAgenda(idMedico);
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Disponibilidade cadastrada com sucesso!");

        } catch (DateTimeParseException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato de hora inválido (use HH:mm).");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao cadastrar: " + e.getMessage());
        }
        ArquivoUtils.salvarConsultas(AppContext.sistema.getTodosMedicos().stream().flatMap(medico -> AppContext.sistema.getConsultas(medico.getIdMedico()).stream()).collect(Collectors.toList()));
    }

    // Adicione este método auxiliar ao seu controller:
    private void atualizarListaAgenda(String idMedico) {
        if (listaAgenda != null) {
            listaAgenda.getItems().setAll(sistema.getConsultas(idMedico).stream().filter(c -> "DISPONIVEL".equals(c.getStatus())).collect(Collectors.toList()));
        }
    }

    private void configurarFormatadorHora(TextField campo) {
        campo.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            // Permite apagar
            if (change.isDeleted()) {
                return change;
            }

            // Validação básica - apenas dígitos e ":"
            if (!newText.matches("[0-9:]*")) {
                return null;
            }

            // Remove todos os ":" para facilitar a formatação
            String digits = newText.replaceAll(":", "");

            // Limita a 4 dígitos (HHmm)
            if (digits.length() > 4) {
                return null;
            }

            // Formatação automática
            if (digits.length() >= 2) {
                String formatted = digits.substring(0, 2) + ":" + (digits.length() > 2 ? digits.substring(2) : "");
                change.setText(formatted.substring(change.getRangeStart(), change.getRangeEnd()));
                change.setRange(0, change.getControlText().length());
                change.setText(formatted);
                change.setCaretPosition(formatted.length());
                change.setAnchor(formatted.length());
            }

            return change;
        }));
    }

    private void configurarFormatadorData(TextField campo) {
        campo.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            // Permite apagar
            if (change.isDeleted()) {
                return change;
            }

            // Validação básica - apenas dígitos e "/"
            if (!newText.matches("[0-9/]*")) {
                return null;
            }

            // Remove todas as "/" para facilitar a formatação
            String digits = newText.replaceAll("/", "");

            // Limita a 8 dígitos (DDMMYYYY)
            if (digits.length() > 8) {
                return null;
            }

            // Formatação automática
            if (digits.length() >= 2) {
                String formatted = digits.substring(0, 2) + "/";
                if (digits.length() >= 4) {
                    formatted += digits.substring(2, 4) + "/";
                    if (digits.length() > 4) {
                        // Se o usuário digitou o ano, usa o que foi digitado
                        formatted += digits.substring(4);
                    } else {
                        // Se não digitou o ano, completa com o ano atual
                        formatted += LocalDate.now().getYear();
                    }
                } else {
                    formatted += digits.substring(2);
                }

                change.setText(formatted.substring(change.getRangeStart(), change.getRangeEnd()));
                change.setRange(0, change.getControlText().length());
                change.setText(formatted);
                change.setCaretPosition(formatted.length());
                change.setAnchor(formatted.length());
            }

            return change;
        }));
    }

    private LocalDate parseDataComAnoAtual(String dataTexto) {
        if (dataTexto == null || dataTexto.isEmpty()) {
            return null;
        }

        String[] partes = dataTexto.split("/");
        int dia, mes, ano;

        try {
            dia = Integer.parseInt(partes[0]);
            mes = Integer.parseInt(partes[1]);

            // Se não tiver ano ou estiver incompleto, usa o ano atual
            if (partes.length < 3 || partes[2].isEmpty()) {
                ano = LocalDate.now().getYear();
            } else {
                ano = Integer.parseInt(partes[2]);
                // Se o ano foi digitado com 2 dígitos, assume século 21 (2000 + XX)
                if (ano < 100) {
                    ano += 2000;
                }
            }

            return LocalDate.of(ano, mes, dia);
        } catch (Exception e) {
            return null;
        }
    }

    private void configurarDatePickers() {
        // Configura o formato brasileiro para os DatePickers
        String padraoBrasileiro = "dd/MM/yyyy";

        dataDisponibilidade.setConverter(new StringConverter<LocalDate>() {
            final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(padraoBrasileiro);

            @Override
            public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty()
                        ? LocalDate.parse(string, dateFormatter)
                        : null;
            }
        });

        dataReagendamento.setConverter(new StringConverter<LocalDate>() {
            final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(padraoBrasileiro);

            @Override
            public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty()
                        ? LocalDate.parse(string, dateFormatter)
                        : null;
            }
        });

        // Também pode definir um prompt text
        dataDisponibilidade.setPromptText(padraoBrasileiro.toLowerCase());
        dataReagendamento.setPromptText(padraoBrasileiro.toLowerCase());
    }
}