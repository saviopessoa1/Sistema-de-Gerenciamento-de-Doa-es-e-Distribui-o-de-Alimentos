package com.example.SGDDA.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.SGDDA.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;
    private TextView linkForgotPassword;
    private TextView linkRegister;
    private TextView linkCadastrarInstituicao;
    private TextInputEditText emailEditText, passwordEditText;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginButton = findViewById(R.id.loginButton);
        linkForgotPassword = findViewById(R.id.linkForgotPassword);
        linkRegister = findViewById(R.id.linkRegister);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        linkCadastrarInstituicao = findViewById(R.id.linkCadastrarInstituicao);

        // Inicialmente, esconde o link de admin
        linkCadastrarInstituicao.setVisibility(View.GONE);

        setupListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserTypeAndRedirect(currentUser);
        }
    }

    private void setupListeners() {
        linkRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        linkCadastrarInstituicao.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegistrarInstituicaoActivity.class));
        });

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email é obrigatório.");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Senha é obrigatória.");
                return;
            }

            loginUser(email, password);
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Login realizado com sucesso.", Toast.LENGTH_SHORT).show();
                        checkUserTypeAndRedirect(user);
                    } else {
                        Toast.makeText(MainActivity.this, "Falha na autenticação: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserTypeAndRedirect(FirebaseUser user) {
        if (user == null) return;

        // Busca os dados do usuário no Firestore para verificar se é admin
        db.collection("usuarios").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Verifica o campo 'isAdmin' (pode ser boolean ou string, vamos tratar ambos)
                            boolean isAdmin = false;
                            if (document.contains("isAdmin")) {
                                Object adminField = document.get("isAdmin");
                                if (adminField instanceof Boolean) {
                                    isAdmin = (Boolean) adminField;
                                } else if (adminField instanceof String) {
                                    isAdmin = Boolean.parseBoolean((String) adminField);
                                }
                            }

                            if (isAdmin) {
                                // Se for admin, mostra o link especial
                                linkCadastrarInstituicao.setVisibility(View.VISIBLE);
                                // E também redireciona para o Dashboard (ou fica na tela se preferir dar a opção)
                                // Por padrão, vamos para o Dashboard, mas o admin pode voltar e ver o link
                                abrirDashboard();
                            } else {
                                // Se não for admin, garante que o link está oculto e vai para o Dashboard
                                linkCadastrarInstituicao.setVisibility(View.GONE);
                                abrirDashboard();
                            }
                        } else {
                            // Usuário sem cadastro no banco (erro de integridade), manda pro dashboard como user comum
                            abrirDashboard();
                        }
                    } else {
                        // Erro ao buscar, assume usuário comum por segurança
                        abrirDashboard();
                    }
                });
    }

    private void abrirDashboard() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}