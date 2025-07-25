package com.example.clinica_app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.ArrayList; 

class SistemaAgendamentoTest {
    private SistemaAgendamento sistema;
    private Paciente paciente;
    private Medico medico;

    @BeforeEach
    void setUp() {
        sistema = new SistemaAgendamento();
        paciente = new Paciente("p1", "João", 30, "123456789");
        medico = new Medico("m1", "Dr. Silva", "Cardiologia");
        
        sistema.registrarPaciente(paciente);
        sistema.registrarMedico(medico);
    }

    @Test
    void testRegistrarPaciente() {
        assertTrue(sistema.isPaciente("p1"));
        assertFalse(sistema.isPaciente("p2"));
    }

    @Test
    void testRegistrarMedico() {
        assertTrue(sistema.isMedico("m1"));
        assertFalse(sistema.isMedico("m2"));
    }

    @Test
    void testCadastrarDisponibilidade() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fim = inicio.plusHours(1);
        
        sistema.cadastrarDisponibilidade("m1", inicio, fim);
        
        List<Consulta> consultas = sistema.getConsultas("m1");
        assertEquals(1, consultas.size());
        assertEquals("DISPONIVEL", consultas.get(0).getStatus());
    }

    @Test
    void testAgendarConsulta() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fim = inicio.plusHours(1);
        
        sistema.cadastrarDisponibilidade("m1", inicio, fim);
        sistema.agendarConsulta("p1", "m1", inicio, fim);
        
        List<Consulta> consultas = sistema.getConsultas("m1");
        assertEquals("AGENDADA", consultas.get(0).getStatus());
        assertEquals(paciente, consultas.get(0).getPaciente());
    }
}