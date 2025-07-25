package com.example.clinica_app.models;

public class Medico {
    private String idMedico;
    private String nome;
    private String especialidade;

    public Medico(String idMedico, String nome, String especialidade) {
        this.idMedico = idMedico;
        this.nome = nome;
        this.especialidade = especialidade;
    }

    public String getIdMedico() { return idMedico; }
    public String getNome() { return nome; }
    public String getEspecialidade() { return especialidade; }
}