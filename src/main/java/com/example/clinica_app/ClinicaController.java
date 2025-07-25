package com.example.clinica_app;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class ClinicaController {

    // Sistema compartilhado
    private final SistemaAgendamento sistema = AppContext.sistema;
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
    //test
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
        configurarFormatadores();

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
            // Configura listener para mudanças de seleção
            comboEspecialidades.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> atualizarListaDisponibilidadesPorEspecialidade());

            // Carrega as especialidades (já seleciona "Todas" por padrão)
            carregarEspecialidades();
        }


    }

    private void configurarFormatadores() {
        // Configurar campos de hora
        if (horaInicio != null) configurarFormatadorHora(horaInicio);
        if (horaFim != null) configurarFormatadorHora(horaFim);
        if (horaInicioReagendamento != null) configurarFormatadorHora(horaInicioReagendamento);
        if (horaFimReagendamento != null) configurarFormatadorHora(horaFimReagendamento);

        // Configurar DatePickers
        if (dataDisponibilidade != null) configurarDatePicker(dataDisponibilidade);
        if (dataReagendamento != null) configurarDatePicker(dataReagendamento);
    }


    public List<Consulta> getConsultasPaciente(String idPaciente) {
        return sistema.getConsultasPaciente(idPaciente).stream().filter(c -> c.getPaciente() != null && c.getPaciente().getIdPaciente().equals(idPaciente)).collect(Collectors.toList());
    }

    private void carregarEspecialidades() {
        comboEspecialidades.getItems().clear();

        // Adiciona a opção "Todas" primeiro
        comboEspecialidades.getItems().add("Todas as consultas");

        // Get all unique specialties that actually have available appointments
        Set<String> especialidadesComDisponibilidade = AppContext.sistema.getTodasConsultasDisponiveis().stream().filter(c -> c.getMedico() != null).map(c -> c.getMedico().getEspecialidade()).filter(especialidade -> especialidade != null && !especialidade.isEmpty()).collect(Collectors.toSet());

        // Adiciona as especialidades reais
        comboEspecialidades.getItems().addAll(especialidadesComDisponibilidade);

        // Seleciona "Todas" por padrão
        comboEspecialidades.getSelectionModel().selectFirst();

        // Atualiza a lista imediatamente (mostrando todas)
        Platform.runLater(() -> {
            atualizarListaDisponibilidadesPorEspecialidade();
        });
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
            sistema.agendarConsulta(AppContext.usuarioLogadoId, consultaSelecionada.getMedico().getIdMedico(), consultaSelecionada.getDataHoraInicio(), consultaSelecionada.getDataHoraFim());

            mostrarAlerta(Alert.AlertType.INFORMATION, "Consulta agendada com sucesso!");

            // Atualiza as listas
            atualizarListaDisponibilidadesPorEspecialidade();

            // Atualiza o arquivo de consultas
            ArquivoUtils.salvarConsultas(AppContext.sistema.getTodosMedicos().stream().flatMap(medico -> AppContext.sistema.getConsultas(medico.getIdMedico()).stream()).collect(Collectors.toList()));

        } catch (IllegalStateException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao agendar: " + e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onDisponibilidadeSelecionada(MouseEvent event) {
        if (event.getClickCount() == 2) { // Duplo clique
            onAgendarConsultaPaciente(null); // Pode passar null ou criar um ActionEvent
        }
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

        // Se for "Todas" ou nenhuma selecionada, mostra todas as consultas disponíveis
        if (especialidadeSelecionada == null || especialidadeSelecionada.equals("Todas as consultas")) {
            List<Consulta> todasConsultas = AppContext.sistema.getTodasConsultasDisponiveis().stream().filter(consulta -> "DISPONIVEL".equals(consulta.getStatus())).sorted(Comparator.comparing(Consulta::getDataHoraInicio)).collect(Collectors.toList());

            listaDisponibilidades.getItems().setAll(todasConsultas);

            if (todasConsultas.isEmpty()) {
                listaDisponibilidades.setPlaceholder(new Label("Nenhum horário disponível no momento"));
            }
            return;
        }

        // Filtra por especialidade específica
        List<Consulta> consultasDisponiveis = AppContext.sistema.getTodasConsultasDisponiveis().stream().filter(consulta -> consulta.getMedico() != null).filter(consulta -> especialidadeSelecionada.equals(consulta.getMedico().getEspecialidade())).filter(consulta -> "DISPONIVEL".equals(consulta.getStatus())).sorted(Comparator.comparing(Consulta::getDataHoraInicio)).collect(Collectors.toList());

        listaDisponibilidades.getItems().setAll(consultasDisponiveis);

        if (consultasDisponiveis.isEmpty()) {
            listaDisponibilidades.setPlaceholder(new Label("Nenhum horário disponível para " + especialidadeSelecionada));
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
        if (campo == null) return;

        // Limita o campo a 5 caracteres (HH:MM)
        campo.lengthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() > 5) {
                campo.setText(campo.getText().substring(0, 5));
            }
        });

        // Aceita apenas números e dois-pontos
        campo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^[0-9:]*$")) {
                campo.setText(oldVal);
            }
        });

        // Formatação ao pressionar Enter ou Tab
        campo.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                formatarHora(campo);
                if (event.getCode() == KeyCode.ENTER) {
                    event.consume();
                }
            }
        });

        // Formatação ao perder foco
        campo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                formatarHora(campo);
            }
        });
    }

    private void formatarHora(TextField campo) {
        String texto = campo.getText().replaceAll("[^0-9]", "");

        try {
            if (texto.length() == 2) { // HH
                int hora = Integer.parseInt(texto);
                if (hora >= 0 && hora <= 23) {
                    campo.setText(String.format("%02d:00", hora));
                }
            } else if (texto.length() == 4) { // HHMM
                int hora = Integer.parseInt(texto.substring(0, 2));
                int minuto = Integer.parseInt(texto.substring(2));
                if (hora >= 0 && hora <= 23 && minuto >= 0 && minuto <= 59) {
                    campo.setText(String.format("%02d:%02d", hora, minuto));
                }
            }
        } catch (NumberFormatException ex) {
            // Mantém o texto como está se não for possível formatar
        }

        campo.positionCaret(campo.getText().length());
    }

    private void configurarDatePicker(DatePicker datePicker) {
        if (datePicker == null) return;

        final String padraoBrasileiro = "dd/MM/yyyy";
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(padraoBrasileiro);
        datePicker.setPromptText(padraoBrasileiro.toLowerCase());

        final TextField editor = datePicker.getEditor();

        // Limita o campo a 10 caracteres
        editor.lengthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() > 10) {
                editor.setText(editor.getText().substring(0, 10));
            }
        });

        // Tratamento especial para ENTER e TAB
        editor.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                if (formatarData(editor, datePicker, formatter)) {
                    if (event.getCode() == KeyCode.ENTER) {
                        event.consume();
                    }
                }
            }
        });

        // Formatação ao perder foco
        editor.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                formatarData(editor, datePicker, formatter);
            } else {
                Platform.runLater(editor::selectAll);
            }
        });

        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                try {
                    if (string != null && !string.isEmpty()) {
                        return LocalDate.parse(string, formatter);
                    }
                    return null;
                } catch (Exception e) {
                    return null;
                }
            }
        });
    }

    private boolean formatarData(TextField editor, DatePicker datePicker, DateTimeFormatter formatter) {
        String texto = editor.getText().trim();

        if (texto.isEmpty()) {
            datePicker.setValue(null);
            return false;
        }

        try {
            // Tenta parsear diretamente se já estiver formatado
            LocalDate data = LocalDate.parse(texto, formatter);
            datePicker.setValue(data);
            editor.setText(formatter.format(data));
            return true;
        } catch (Exception e) {
            // Continua para o tratamento de datas parciais
        }

        String apenasDigitos = texto.replaceAll("[^0-9]", "");
        LocalDate hoje = LocalDate.now();

        try {
            int dia = hoje.getDayOfMonth();
            int mes = hoje.getMonthValue();
            int ano = hoje.getYear();

            switch (apenasDigitos.length()) {
                case 1: case 2: // D ou DD
                    dia = Integer.parseInt(apenasDigitos.substring(0, Math.min(apenasDigitos.length(), 2)));
                    break;
                case 3: case 4: // DD M ou DD MM
                    dia = Integer.parseInt(apenasDigitos.substring(0, 2));
                    mes = Integer.parseInt(apenasDigitos.substring(2, Math.min(apenasDigitos.length(), 4)));
                    break;
                case 6: case 8: // DD MM AA ou DD MM AAAA
                    dia = Integer.parseInt(apenasDigitos.substring(0, 2));
                    mes = Integer.parseInt(apenasDigitos.substring(2, 4));
                    ano = apenasDigitos.length() == 6 ?
                            2000 + Integer.parseInt(apenasDigitos.substring(4, 6)) :
                            Integer.parseInt(apenasDigitos.substring(4, 8));
                    break;
                default:
                    throw new IllegalArgumentException("Formato inválido");
            }

            // Validação e ajuste
            mes = Math.max(1, Math.min(mes, 12));
            LocalDate data = LocalDate.of(ano, mes, 1);
            int maxDias = data.lengthOfMonth();
            dia = Math.max(1, Math.min(dia, maxDias));

            String dataFormatada = String.format("%02d/%02d/%04d", dia, mes, ano);
            datePicker.setValue(LocalDate.of(ano, mes, dia));
            editor.setText(dataFormatada);
            return true;

        } catch (Exception e) {
            editor.setText("");
            datePicker.setValue(null);
            return false;
        }
    }
}