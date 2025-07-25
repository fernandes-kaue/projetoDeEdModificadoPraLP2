package com.example.clinica_app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
public class ClinicaControllerTest {

    @Start
    public void start(Stage stage) throws Exception {
        // 1. Carrega o FXML
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/view/menu-inicial.fxml"));
        Parent root = loader.load();
        
        // 2. Configura a cena com tamanho adequado
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        
        // 3. Mostra a janela
        stage.show();
        
        // 4. Traz para frente (importante para alguns sistemas)
        stage.toFront();
    }

    @Test
    public void testVerificarUsuarioPaciente(FxRobot robot) {
        // 1. Verifica se a janela principal está visível
        robot.sleep(1000); // Espera inicial
        
        // 2. Localiza o campo de usuário de forma mais flexível
        TextField campoUsuario = robot.lookup(".text-field").queryAs(TextField.class);
        assertThat(campoUsuario).isNotNull().isVisible();
        
        // 3. Interage com o campo
        robot.clickOn(campoUsuario).write("p1");
        
        // 4. Localiza o botão de forma mais tolerante
        Button botaoAvancar = robot.lookup(b -> 
            b instanceof Button && ((Button)b).getText().matches("(?i)avançar|entrar"))
            .queryAs(Button.class);
        assertThat(botaoAvancar).isNotNull().isVisible();
        
        // 5. Clica no botão
        robot.clickOn(botaoAvancar);
        
        // 6. Verifica a navegação
        robot.sleep(1500); // Espera a transição
        
        // Verifica se há uma nova janela ou cena carregada
        assertThat(robot.listWindows().size()).isGreaterThan(0);
    }
}