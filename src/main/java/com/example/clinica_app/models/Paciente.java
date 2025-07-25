package com.example.clinica_app.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Paciente {
    private String idPaciente;
    private String nome;
    private LocalDate dataNascimento;
    private String contato;
    private List<Consulta> historicoConsultas;

    public Paciente(String idPaciente, String nome, LocalDate dataNascimento, String contato) {
        this.idPaciente = idPaciente;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.contato = contato;
        this.historicoConsultas = new ArrayList<>();
    }

    public String getIdPaciente() { return idPaciente; }
    public String getNome() { return nome; }
    public List<Consulta> getHistorico() { return historicoConsultas; }
    public void adicionarConsultaAoHistorico(Consulta consulta) {
        historicoConsultas.add(consulta);
    }
}