package com.example.clinica_app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PacienteTest {
    private Paciente paciente;
    private Consulta consulta;
    private Medico medico;

    @BeforeEach
    void setUp() {
        // Inicializa os objetos antes de cada teste
        paciente = new Paciente("p1", "Maria", 25, "987654321");
        medico = new Medico("m1", "Dr. Souza", "Pediatria");
        
        // Cria uma consulta válida com paciente e médico
        consulta = new Consulta("c1", paciente, medico, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(1));
    }

    @Test
    void testAdicionarConsultaAoHistorico() {
        assertEquals(0, paciente.getHistorico().size());
        
        paciente.adicionarConsultaAoHistorico(consulta);
        
        assertEquals(1, paciente.getHistorico().size());
        assertEquals(consulta, paciente.getHistorico().get(0));
    }

    @Test
    void testRemoverConsultaDoHistorico() {
        // Primeiro adiciona a consulta
        paciente.adicionarConsultaAoHistorico(consulta);
        assertEquals(1, paciente.getHistorico().size());
        
        // Depois remove
        paciente.removerConsultaDoHistorico("c1");
        
        assertEquals(0, paciente.getHistorico().size());
    }
}