package com.example.SGDDA.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.SGDDA.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore; // Import do Firestore

import java.util.HashMap; // Import do HashMap
import java.util.Map; // Import do Map

public class RegisterStep2Activity extends AppCompatActivity {

    // 1. Declaração dos Componentes
    private ImageButton backButton;
    private TextView loginTextView;
    private Button registrarButton;
    private TextInputEditText cpfEditText, celularEditText, telefoneEditText;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Instância do Banco de Dados (Firestore)

    // Dados da tela anterior
    private String nomeCompleto, email, senha;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_step_2);

        // Ajuste de layout (EdgeToEdge)
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // 2. Encontrar Componentes
        backButton = findViewById(R.id.backButton);
        loginTextView = findViewById(R.id.loginTextView);
        registrarButton = findViewById(R.id.registrarButton);
        cpfEditText = findViewById(R.id.cpfEditText);
        celularEditText = findViewById(R.id.celularEditText);
        telefoneEditText = findViewById(R.id.telefoneEditText);

        // 3. Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Inicializa o Firestore

        // 4. Receber dados da Intent (da Tela 1)
        Intent intent = getIntent();
        nomeCompleto = intent.getStringExtra("NOME_COMPLETO");
        email = intent.getStringExtra("EMAIL");
        senha = intent.getStringExtra("SENHA");

        // 5. Configurar Listeners
        backButton.setOnClickListener(v -> finish()); // Volta para a tela anterior
        loginTextView.setOnClickListener(v -> {
            // Volta para a tela de Login
            Intent loginIntent = new Intent(RegisterStep2Activity.this, MainActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish();
        });

        registrarButton.setOnClickListener(v -> {
            // Chama a função de registro
            registrarUsuario();
        });
    }

    private void registrarUsuario() {
        // Pegar os dados desta tela (Etapa 2)
        String cpf = cpfEditText.getText().toString().trim();
        String celular = celularEditText.getText().toString().trim();
        String telefone = telefoneEditText.getText().toString().trim(); // Opcional

        // Validação básica
        if (cpf.isEmpty() || celular.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha CPF e Celular.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Criar o usuário no Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Autenticação foi um sucesso!
                            Log.d("RegisterStep2", "createUserWithEmail: Sucesso");
                            String userId = mAuth.getCurrentUser().getUid(); // Pega o ID único do usuário

                            // 2. Agora, salvar os dados extras no Firestore
                            salvarDadosUsuario(userId, nomeCompleto, email, cpf, celular, telefone);

                        } else {
                            // Se falhar a autenticação
                            Log.w("RegisterStep2", "createUserWithEmail: Falha", task.getException());
                            Toast.makeText(RegisterStep2Activity.this, "Falha no cadastro: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // NOVA FUNÇÃO para salvar no Banco de Dados
    private void salvarDadosUsuario(String userId, String nome, String email, String cpf, String celular, String telefone) {
        // Criar um "mapa" (um objeto) com os dados do usuário
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("uid", userId);
        usuario.put("nomeCompleto", nome);
        usuario.put("email", email);
        usuario.put("cpf", cpf);
        usuario.put("celular", celular);
        usuario.put("telefone", telefone);
        // (Você pode adicionar mais campos aqui, ex: "tipoUsuario" = "voluntario")

        // Salvar no Firestore
        // Vamos criar uma coleção "usuarios" e salvar um "documento" com o ID do usuário
        db.collection("usuarios").document(userId)
                .set(usuario)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Dados salvos com sucesso!
                        Log.d("RegisterStep2", "Usuário salvo no Firestore com sucesso!");
                        Toast.makeText(RegisterStep2Activity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

                        // 3. Só então, navegar para o Dashboard
                        Intent intent = new Intent(RegisterStep2Activity.this, DashboardActivity.class);
                        // Limpa as telas de Login/Cadastro da pilha
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish(); // Fecha a tela de registro
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Se falhar o salvamento no banco de dados
                        Log.w("RegisterStep2", "Erro ao salvar dados no Firestore", e);
                        Toast.makeText(RegisterStep2Activity.this, "Erro ao salvar dados: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        // (O usuário foi criado no Auth, mas os dados não foram salvos.
                        // Em um app real, teríamos que tratar isso, talvez deletando o usuário do Auth
                        // ou pedindo para ele tentar de novo)
                    }
                });
    }

}


