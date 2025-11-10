package com.example.SGDDA.ui; // Atualizado para seu pacote

// Imports necessários para fazer o app funcionar
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils; // Import para validar campos
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // Para feedback
import androidx.appcompat.app.AppCompatActivity;
import com.example.SGDDA.R; // Atualizado para seu pacote

// Imports do Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.textfield.TextInputEditText; // Import para os campos

public class MainActivity extends AppCompatActivity {

    // 1. Declarar as variáveis para os componentes do seu XML
    private Button loginButton;
    private TextView linkForgotPassword;
    private TextView linkRegister;
    private TextInputEditText emailEditText, passwordEditText; // Campos de login

    // Declaração do Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 2. Conectar este código Java com o seu layout XML
        setContentView(R.layout.activity_main);

        // Inicializa o Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 3. Encontrar os componentes no XML pelos seus IDs
        loginButton = findViewById(R.id.loginButton);
        linkForgotPassword = findViewById(R.id.linkForgotPassword);
        linkRegister = findViewById(R.id.linkRegister);
        emailEditText = findViewById(R.id.emailEditText); // ID do XML
        passwordEditText = findViewById(R.id.passwordEditText); // ID do XML

        // --- ESTA É A REGRA DE NEGÓCIO ---
        // 4. Criar o "ouvinte" de clique para o link de Registro
        linkRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 5. Quando clicado, criar uma "Intenção" de abrir a RegisterActivity
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);

                // 6. Executar a Intenção (abrir a nova tela)
                startActivity(intent);
            }
        });

        // 7. Lógica do botão de Login (AGORA COM FIREBASE)
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pegar o email e senha dos EditTexts
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Validação de campos
                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("Email é obrigatório.");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("Senha é obrigatória.");
                    return;
                }

                // Fazer login com Firebase
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Login com sucesso, vai para o Dashboard
                                Toast.makeText(MainActivity.this, "Login efetuado com sucesso.", Toast.LENGTH_SHORT).show();
                                abrirDashboard();
                            } else {
                                // Se falhar, mostra uma mensagem
                                Toast.makeText(MainActivity.this, "Falha na autenticação: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        // (Aqui você pode adicionar a lógica para o linkForgotPassword)
    }

    @Override
    public void onStart() {
        super.onStart();
        // Verifica se o usuário já está logado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Se sim, vai direto para o Dashboard
            abrirDashboard();
        }
    }

    private void abrirDashboard() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        // Limpa o histórico para que o usuário não volte para a tela de login
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}


