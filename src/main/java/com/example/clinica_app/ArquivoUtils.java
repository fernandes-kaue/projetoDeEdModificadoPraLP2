package com.example.clinica_app;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ArquivoUtils {

    private static final String ARQUIVO_PACIENTES = "pacientes.txt";
    private static final String ARQUIVO_MEDICOS = "medicos.txt";
    private static final String ARQUIVO_CONSULTAS = "consultas.txt";

    // ===== PACIENTES =====
    public static void salvarPacientes(List<Paciente> pacientes) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARQUIVO_PACIENTES))) {
            for (Paciente p : pacientes) {
                String linha = p.getIdPaciente() + "," +
                        p.getNome() + "," +
                        p.getIdade() + "," +
                        p.getContato();
                writer.write(linha);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar pacientes: " + e.getMessage());
        }
    }

    public static List<Paciente> carregarPacientes() {
        List<Paciente> pacientes = new ArrayList<>();
        File arquivo = new File(ARQUIVO_PACIENTES);

        if (!arquivo.exists()) return pacientes;

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(",");
                if (partes.length == 4) {
                    String id = partes[0];
                    String nome = partes[1];
                    int idade = Integer.parseInt(partes[2]);
                    String contato = partes[3];
                    Paciente paciente = new Paciente(id, nome, idade, contato);
                    pacientes.add(paciente);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar pacientes: " + e.getMessage());
        }
        return pacientes;
    }

    // ===== MÉDICOS =====
    public static void salvarMedicos(List<Medico> medicos) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARQUIVO_MEDICOS))) {
            for (Medico m : medicos) {
                String linha = m.getIdMedico() + "," +
                        m.getNome() + "," +
                        m.getEspecialidade();
                writer.write(linha);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar médicos: " + e.getMessage());
        }
    }

    public static List<Medico> carregarMedicos() {
        List<Medico> medicos = new ArrayList<>();
        File arquivo = new File(ARQUIVO_MEDICOS);

        if (!arquivo.exists()) return medicos;

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(",");
                if (partes.length == 3) {
                    String id = partes[0];
                    String nome = partes[1];
                    String especialidade = partes[2];
                    Medico medico = new Medico(id, nome, especialidade);
                    medicos.add(medico);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar médicos: " + e.getMessage());
        }
        return medicos;
    }

    // ===== CONSULTAS =====
    public static void salvarConsultas(List<Consulta> novasConsultas) {
        // Salva todas as consultas no arquivo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARQUIVO_CONSULTAS))) {
            for (Consulta consulta : novasConsultas) {
                String idPaciente = consulta.getPaciente() != null ? consulta.getPaciente().getIdPaciente() : "";
                String idMedico = consulta.getMedico() != null ? consulta.getMedico().getIdMedico() : "";
                String linha = String.join(",",
                        consulta.getIdConsulta(),
                        idPaciente,
                        idMedico,
                        consulta.getDataHoraInicio().toString(),
                        consulta.getDataHoraFim().toString(),
                        consulta.getStatus(),
                        consulta.getMotivoCancelamento() != null ? consulta.getMotivoCancelamento() : ""
                );
                writer.write(linha);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar consultas: " + e.getMessage());
        }

        // Recarrega as consultas do arquivo e sincroniza as agendas dos médicos
        List<Consulta> consultasAtualizadas = carregarConsultas(
                AppContext.sistema.getTodosPacientes().stream()
                        .collect(Collectors.toMap(Paciente::getIdPaciente, p -> p)),
                AppContext.sistema.getTodosMedicos().stream()
                        .collect(Collectors.toMap(Medico::getIdMedico, m -> m))
        );

        for (Medico medico : AppContext.sistema.getTodosMedicos()) {
            List<Consulta> consultasMedico = consultasAtualizadas.stream()
                    .filter(c -> c.getMedico() != null && c.getMedico().getIdMedico().equals(medico.getIdMedico()))
                    .collect(Collectors.toList());
            TreeMap<LocalDateTime, Consulta> agenda = AppContext.sistema.getAgendaMedico(medico.getIdMedico());
            agenda.clear();
            for (Consulta consulta : consultasMedico) {
                agenda.put(consulta.getDataHoraInicio(), consulta);
            }
        }
    }

    public static List<Consulta> carregarConsultas(Map<String, Paciente> pacientes, Map<String, Medico> medicos) {
        List<Consulta> consultas = new ArrayList<>();
        File arquivo = new File(ARQUIVO_CONSULTAS);

        if (!arquivo.exists()) return consultas;

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(",", -1);
                if (partes.length >= 7) {
                    String idConsulta = partes[0];
                    String idPaciente = partes[1];
                    String idMedico = partes[2];
                    LocalDateTime inicio = LocalDateTime.parse(partes[3]);
                    LocalDateTime fim = LocalDateTime.parse(partes[4]);
                    String status = partes[5];
                    String motivo = partes[6];

                    Paciente paciente = pacientes.get(idPaciente);
                    Medico medico = medicos.get(idMedico);

                    Consulta consulta = new Consulta(idConsulta, paciente, medico, inicio, fim);
                    consulta.setStatus(status);
                    consulta.setMotivoCancelamento(motivo);

                    consultas.add(consulta);

                    // Adiciona ao histórico do paciente, se aplicável
                    if (paciente != null) {
                        paciente.adicionarConsultaAoHistorico(consulta);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar consultas: " + e.getMessage());
        }
        return consultas;
    }

}