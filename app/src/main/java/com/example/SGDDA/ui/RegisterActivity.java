package com.example.SGDDA.ui;

// --- IMPORTS QUE FALTAVAM ---
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast; // Para feedback

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity; // O import principal que faltava
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.SGDDA.R;
import com.google.android.material.textfield.TextInputEditText; // Import para os campos
// --- FIM DOS IMPORTS ---

public class RegisterActivity extends AppCompatActivity {

    // 1. Declaração dos componentes
    private ImageButton backButton;
    private TextView loginTextView;
    private Button proximoButton;
    // Campos de texto
    private TextInputEditText nomeCompletoEditText, emailEditText, passwordEditText, repetePasswordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> { // R.id.main agora deve ser encontrado
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. Encontrar os componentes
        backButton = findViewById(R.id.backButton);
        loginTextView = findViewById(R.id.loginTextView);
        proximoButton = findViewById(R.id.proximoButton);

        // Campos de texto
        nomeCompletoEditText = findViewById(R.id.nomeCompletoEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        repetePasswordEditText = findViewById(R.id.repetePasswordEditText);


        // 3. Configurar os Listeners (Regras de Negócio)
        // Botão de Voltar
        backButton.setOnClickListener(v -> {
            finish(); // Fecha a tela atual e volta para a anterior (Login)
        });

        // Link "Log in"
        loginTextView.setOnClickListener(v -> {
            finish(); // Também fecha a tela e volta para o Login
        });

        // Botão Próximo
        proximoButton.setOnClickListener(v -> {
            // Pega os dados dos campos
            String nome = nomeCompletoEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String senha = passwordEditText.getText().toString().trim();
            String repeteSenha = repetePasswordEditText.getText().toString().trim();

            // Validação
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios (*)", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!senha.equals(repeteSenha)) {
                Toast.makeText(this, "As senhas não conferem", Toast.LENGTH_SHORT).show();
                return;
            }
            if (senha.length() < 6) {
                Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navega para a Etapa 2 do Registro
            Intent intent = new Intent(RegisterActivity.this, RegisterStep2Activity.class);

            // Passa os dados da Etapa 1 para a próxima tela
            intent.putExtra("NOME", nome);
            intent.putExtra("EMAIL", email);
            intent.putExtra("SENHA", senha);

            startActivity(intent);
        });
    }
}


