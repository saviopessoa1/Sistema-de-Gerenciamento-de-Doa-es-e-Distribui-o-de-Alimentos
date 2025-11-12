package com.example.SGDDA.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

        linkRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // ★ Lógica do Link Admin: Abre Dialog de Autenticação ★
        linkCadastrarInstituicao.setOnClickListener(v -> showAdminLoginDialog());

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

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Login efetuado com sucesso.", Toast.LENGTH_SHORT).show();
                            abrirDashboard();
                        } else {
                            Toast.makeText(MainActivity.this, "Falha na autenticação: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            abrirDashboard();
        }
    }

    private void abrirDashboard() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ★ Método para mostrar o Dialog de Login do Admin ★
    private void showAdminLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Acesso Administrativo");
        builder.setMessage("Entre com suas credenciais de Admin para cadastrar instituições.");

        // Layout do Dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText inputEmail = new EditText(this);
        inputEmail.setHint("Email do Admin");
        inputEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(inputEmail);

        final EditText inputPassword = new EditText(this);
        inputPassword.setHint("Senha");
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPassword);

        builder.setView(layout);

        // Botões do Dialog
        builder.setPositiveButton("Acessar", (dialog, which) -> {
            // Não faz nada aqui, vamos sobrescrever o botão depois para validar sem fechar se der erro
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Sobrescreve o botão para validar
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Autentica no Firebase (sem mudar o usuário atual da Activity principal se possível,
            // mas como o Firebase Auth é global, vamos autenticar e depois deslogar se for o caso,
            // ou melhor: autenticamos para permitir o acesso e deixamos ele logado se quiser).
            // Para este fluxo "setup", vamos autenticar.

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // 2. Verifica se é Admin
                    String uid = mAuth.getCurrentUser().getUid();
                    db.collection("usuarios").document(uid).get().addOnCompleteListener(taskDoc -> {
                        if (taskDoc.isSuccessful()) {
                            DocumentSnapshot document = taskDoc.getResult();
                            Boolean isAdmin = document.getBoolean("isAdmin");

                            if (isAdmin != null && isAdmin) {
                                // 3. É Admin! Abre a tela de cadastro
                                Toast.makeText(MainActivity.this, "Acesso Admin concedido.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, RegistrarInstituicaoActivity.class);
                                startActivity(intent);
                                dialog.dismiss();
                            } else {
                                // Não é admin
                                Toast.makeText(MainActivity.this, "Acesso negado. Usuário não é administrador.", Toast.LENGTH_LONG).show();
                                mAuth.signOut(); // Desloga pois não era admin
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Erro ao verificar permissões.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Email ou senha incorretos.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}