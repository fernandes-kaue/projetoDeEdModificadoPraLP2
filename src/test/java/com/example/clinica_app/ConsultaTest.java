package com.example.clinica_app;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ConsultaTest {
    @Test
    void testConsultaDisponivel() {
        Medico medico = new Medico("m1", "Dr. Oliveira", "Ortopedia");
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fim = inicio.plusHours(1);
        
        Consulta consulta = new Consulta("c1", medico, inicio, fim);
        
        assertEquals("DISPONIVEL", consulta.getStatus());
        assertNull(consulta.getPaciente());
        assertEquals(medico, consulta.getMedico());
    }

    @Test
    void testConsultaAgendada() {
        Medico medico = new Medico("m1", "Dr. Oliveira", "Ortopedia");
        Paciente paciente = new Paciente("p1", "Carlos", 40, "555555555");
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fim = inicio.plusHours(1);
        
        Consulta consulta = new Consulta("c1", paciente, medico, inicio, fim);
        
        assertEquals("AGENDADA", consulta.getStatus());
        assertEquals(paciente, consulta.getPaciente());
        assertEquals(medico, consulta.getMedico());
    }

    @Test
    void testCancelarConsulta() {
        Medico medico = new Medico("m1", "Dr. Oliveira", "Ortopedia");
        Paciente paciente = new Paciente("p1", "Carlos", 40, "555555555");
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fim = inicio.plusHours(1);
        
        Consulta consulta = new Consulta("c1", paciente, medico, inicio, fim);
        consulta.cancelar();
        
        assertEquals("CANCELADA", consulta.getStatus());
    }
}