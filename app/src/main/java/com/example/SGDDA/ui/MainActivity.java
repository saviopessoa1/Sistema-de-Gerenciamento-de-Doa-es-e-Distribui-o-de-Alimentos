package com.example.SGDDA.ui; // Atualizado para seu pacote

// Imports necessários para fazer o app funcionar
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.SGDDA.R; // Atualizado para seu pacote

public class MainActivity extends AppCompatActivity {

    // 1. Declarar as variáveis para os componentes do seu XML
    private Button loginButton;
    private TextView linkForgotPassword;
    private TextView linkRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 2. Conectar este código Java com o seu layout XML
        setContentView(R.layout.activity_main);

        // 3. Encontrar os componentes no XML pelos seus IDs
        loginButton = findViewById(R.id.loginButton);
        linkForgotPassword = findViewById(R.id.linkForgotPassword);
        linkRegister = findViewById(R.id.linkRegister);

        // --- ESTA É A REGRA DE NEGÓCIO ---
        // 4. Criar o "ouvinte" de clique para o link de Registro
        linkRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 5. Quando clicado, criar uma "Intenção" de abrir a RegisterActivity
                // (Verifique se o nome da sua classe Java de cadastro é "RegisterActivity")
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);

                // 6. Executar a Intenção (abrir a nova tela)
                startActivity(intent);
            }
        });

        // 7. Lógica do botão de Login (POR ENQUANTO, só abre o Painel)
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Adicionar lógica real de login (validar usuário e senha)

                // Por enquanto, vamos só abrir a tela do Painel (Dashboard)
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

        // (Aqui você pode adicionar a lógica para o linkForgotPassword)
    }
}

