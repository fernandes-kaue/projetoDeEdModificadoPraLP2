package com.example.clinica_app;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class SistemaAgendamento {
    private Map<String, Paciente> pacientes = new HashMap<>();
    private Map<String, Medico> medicos = new HashMap<>();
    private Map<String, TreeMap<LocalDateTime, Consulta>> agendas = new HashMap<>();

    public boolean isPaciente(String id) {
        return pacientes.containsKey(id);
    }

    public boolean isMedico(String id) {
        return medicos.containsKey(id);
    }

    public void registrarPaciente(Paciente paciente) {
        pacientes.put(paciente.getIdPaciente(), paciente);
    }

    public void registrarMedico(Medico medico) {
        medicos.put(medico.getIdMedico(), medico);
        agendas.put(medico.getIdMedico(), new TreeMap<>());
    }

    // Cadastrar disponibilidade de um médico
    public void cadastrarDisponibilidade(String idMedico, LocalDateTime inicio, LocalDateTime fim) {
        Medico medico = medicos.get(idMedico);
        if (medico == null) throw new IllegalArgumentException("Médico não encontrado.");
        TreeMap<LocalDateTime, Consulta> agenda = agendas.get(idMedico);
        if (agenda.containsKey(inicio)) throw new IllegalStateException("Horário já cadastrado");

        Consulta disponibilidade = new Consulta(UUID.randomUUID().toString(), null, medico, inicio, fim);
        disponibilidade.setStatus("DISPONIVEL");
        agenda.put(inicio, disponibilidade);
    }

    // Agendar uma consulta
    public void agendarConsulta(String idPaciente, String idMedico, LocalDateTime inicio, LocalDateTime fim) {
        Paciente paciente = pacientes.get(idPaciente);
        if (paciente == null) throw new IllegalArgumentException("Paciente não encontrado.");
        TreeMap<LocalDateTime, Consulta> agenda = agendas.get(idMedico);
        Consulta consulta = agenda.get(inicio);

        if (consulta == null || !"DISPONIVEL".equals(consulta.getStatus()))
            throw new IllegalStateException("Horário não disponível para agendamento");

        consulta.setPaciente(paciente);
        consulta.setStatus("AGENDADA");
        paciente.adicionarConsultaAoHistorico(consulta);
    }

    // Cancelar consulta por paciente
    public void cancelarConsultaPorPaciente(Consulta consulta) {
        if (consulta == null || consulta.getMedico() == null) {
            throw new IllegalArgumentException("Consulta ou médico inválido.");
        }
        if (!"AGENDADA".equals(consulta.getStatus())) {
            throw new IllegalStateException("Só é possível cancelar consultas agendadas.");
        }
        consulta.setStatus("DISPONIVEL");
        if (consulta.getPaciente() != null) {
            consulta.getPaciente().removerConsultaDoHistorico(consulta.getIdConsulta());
        }
        consulta.setPaciente(null);
    }

    public boolean cancelarConsultaPorMedico(String idMedico, String idConsulta) {
        TreeMap<LocalDateTime, Consulta> agenda = agendas.get(idMedico);
        if (agenda == null) return false;

        for (Consulta consulta : agenda.values()) {
            if (consulta.getIdConsulta().equals(idConsulta) &&
                    consulta.getMedico().getIdMedico().equals(idMedico)) {

                if (consulta.getPaciente() != null) {
                    if (!"AGENDADA".equals(consulta.getStatus()) &&
                            !"SOLICITADO".equals(consulta.getStatus())) {
                        // Só permite cancelar se estiver AGENDADA ou SOLICITADO
                        return false;
                    }
                    consulta.setStatus("CANCELADA");
                    return true;
                } else {
                    agenda.remove(consulta.getDataHoraInicio());
                    return true;
                }
            }
        }
        return false;
    }

    public void solicitarReagendamento(Consulta consulta) {
        String idMedico = consulta.getMedico().getIdMedico();
        agendas.get(idMedico).put(consulta.getDataHoraInicio(), consulta);
        consulta.getPaciente().adicionarConsultaAoHistorico(consulta);
    }

    // Retorna todos os médicos
    public List<Medico> getTodosMedicos() {
        return new ArrayList<>(medicos.values());
    }

    public TreeMap<LocalDateTime, Consulta> getAgendaMedico(String idMedico) {
        return agendas.get(idMedico);
    }

    public List<Paciente> getTodosPacientes() {
        return new ArrayList<>(pacientes.values());
    }

    // Retorna todas as consultas de um paciente
    public List<Consulta> getConsultasPaciente(String idPaciente) {
        return agendas.values().stream()
                .flatMap(agenda -> agenda.values().stream())
                .filter(c -> c.getPaciente() != null && c.getPaciente().getIdPaciente().equals(idPaciente))
                .collect(Collectors.toList());
    }

    // Retorna todas as consultas de um médico
    public List<Consulta> getConsultas(String idMedico) {
        TreeMap<LocalDateTime, Consulta> agenda = agendas.get(idMedico);
        if (agenda == null) return Collections.emptyList();
        return new ArrayList<>(agenda.values());
    }

    // Retorna todas as consultas disponíveis
    public List<Consulta> getTodasConsultasDisponiveis() {
        return agendas.values().stream()
                .flatMap(agenda -> agenda.values().stream())
                .filter(consulta -> "DISPONIVEL".equals(consulta.getStatus()))
                .collect(Collectors.toList());
    }
}