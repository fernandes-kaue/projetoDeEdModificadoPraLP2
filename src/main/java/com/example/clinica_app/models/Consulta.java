package com.example.clinica_app.models;

import java.time.LocalDateTime;

public class Consulta {
    private String idConsulta;
    private Paciente paciente;
    private Medico medico;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private String status;
    private String notasMedico;

    public Consulta(String idConsulta, Paciente paciente, Medico medico,
                    LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim) {
        this.idConsulta = idConsulta;
        this.paciente = paciente;
        this.medico = medico;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.status = "AGENDADA";
    }

    public String getIdConsulta() { return idConsulta; }
    public Paciente getPaciente() { return paciente; }
    public Medico getMedico() { return medico; }
    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public String getStatus() { return status; }

    public void cancelar() {
        if (status.equals("REALIZADA")) throw new IllegalStateException("Consulta já realizada");
        status = "CANCELADA";
    }
}