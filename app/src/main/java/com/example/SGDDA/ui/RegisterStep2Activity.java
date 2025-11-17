package com.example.SGDDA.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterStep2Activity extends AppCompatActivity {

    
    private ImageButton backButton;
    private TextView loginTextView;
    private Button registrarButton;
    private TextInputEditText cpfEditText, celularEditText, telefoneEditText;

    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    
    private String nomeCompleto, email, senha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_step_2);

        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        
        backButton = findViewById(R.id.backButton);
        
        registrarButton = findViewById(R.id.registrarButton);
        cpfEditText = findViewById(R.id.cpfEditText);
        celularEditText = findViewById(R.id.celularEditText);
        telefoneEditText = findViewById(R.id.telefoneEditText);

        
        loginTextView = findViewById(R.id.loginTextView);

        
        Intent intent = getIntent();
        if (intent != null) {
            nomeCompleto = intent.getStringExtra("NOME_COMPLETO"); 
            email = intent.getStringExtra("EMAIL");
            senha = intent.getStringExtra("SENHA");
        }

        
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (loginTextView != null) {
            loginTextView.setOnClickListener(v -> {
                Intent loginIntent = new Intent(RegisterStep2Activity.this, MainActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish();
            });
        }

        if (registrarButton != null) {
            registrarButton.setOnClickListener(v -> {
                registrarUsuario();
            });
        } else {
            Log.e("RegisterStep2", "Botão registrarButton é nulo! Verifique o ID no XML.");
        }
    }

    private void registrarUsuario() {
        
        String cpf = cpfEditText.getText() != null ? cpfEditText.getText().toString().trim() : "";
        String celular = celularEditText.getText() != null ? celularEditText.getText().toString().trim() : "";
        String telefone = telefoneEditText.getText() != null ? telefoneEditText.getText().toString().trim() : "";

        
        if (cpf.isEmpty() || celular.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha CPF e Celular.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email == null || senha == null) {
            Toast.makeText(this, "Erro: Dados da etapa anterior perdidos.", Toast.LENGTH_SHORT).show();
            return;
        }

        
        registrarButton.setEnabled(false);
        Toast.makeText(this, "Cadastrando...", Toast.LENGTH_SHORT).show();

        
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            salvarDadosUsuario(userId, nomeCompleto, email, cpf, celular, telefone);
                        } else {
                            registrarButton.setEnabled(true);
                            String erro = task.getException() != null ? task.getException().getMessage() : "Erro desconhecido";
                            Toast.makeText(RegisterStep2Activity.this, "Falha no cadastro: " + erro, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void salvarDadosUsuario(String userId, String nome, String email, String cpf, String celular, String telefone) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("uid", userId);
        usuario.put("nomeCompleto", nome);
        usuario.put("email", email);
        usuario.put("cpf", cpf);
        usuario.put("celular", celular);
        usuario.put("telefone", telefone);

        db.collection("usuarios").document(userId)
                .set(usuario)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RegisterStep2Activity.this, "Sucesso!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterStep2Activity.this, DashboardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        registrarButton.setEnabled(true);
                        Toast.makeText(RegisterStep2Activity.this, "Erro ao salvar dados: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}