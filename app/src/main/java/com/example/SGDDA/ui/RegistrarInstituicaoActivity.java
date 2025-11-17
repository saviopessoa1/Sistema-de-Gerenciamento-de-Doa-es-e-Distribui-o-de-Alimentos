package com.example.SGDDA.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.SGDDA.R;
import com.example.SGDDA.model.Instituicao;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth; 
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistrarInstituicaoActivity extends AppCompatActivity {

    private static final String TAG = "RegInstituicaoActivity";

    
    private ImageButton backButton;
    private TextInputEditText nomeEditText, enderecoEditText, telefoneEditText, responsavelEditText;
    private Button cadastrarButton;
    private Button finalizarButton;

    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar_instituicao);

        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); 

        
        backButton = findViewById(R.id.backButton);
        nomeEditText = findViewById(R.id.nomeEditText);
        enderecoEditText = findViewById(R.id.enderecoEditText);
        telefoneEditText = findViewById(R.id.telefoneEditText);
        responsavelEditText = findViewById(R.id.responsavelEditText);
        cadastrarButton = findViewById(R.id.cadastrarButton);
        finalizarButton = findViewById(R.id.finalizarButton);

        
        setupListeners();
    }

    private void setupListeners() {
        
        backButton.setOnClickListener(v -> {
            mAuth.signOut();
            finish();
        });

        
        cadastrarButton.setOnClickListener(v -> salvarInstituicao());

        
        finalizarButton.setOnClickListener(v -> {
            mAuth.signOut(); 
            Toast.makeText(this, "Saindo do modo Admin...", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void salvarInstituicao() {
        
        String nome = nomeEditText.getText().toString().trim();
        String endereco = enderecoEditText.getText().toString().trim();
        String telefone = telefoneEditText.getText().toString().trim();
        String responsavel = responsavelEditText.getText().toString().trim();

        
        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(endereco) || TextUtils.isEmpty(telefone) || TextUtils.isEmpty(responsavel)) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        
        String urgencia = "Normal";

        
        Instituicao instituicao = new Instituicao();
        instituicao.setNome(nome);
        instituicao.setEndereco(endereco);
        instituicao.setTelefone(telefone);
        instituicao.setResponsavel(responsavel);
        instituicao.setUrgencia(urgencia);

        
        db.collection("instituicoes").document(nome)
                .set(instituicao)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegistrarInstituicaoActivity.this, "Instituição '" + nome + "' cadastrada!", Toast.LENGTH_SHORT).show();
                    limparCampos();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegistrarInstituicaoActivity.this, "Erro ao cadastrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao salvar instituição", e);
                });
    }

    private void limparCampos() {
        nomeEditText.setText("");
        enderecoEditText.setText("");
        telefoneEditText.setText("");
        responsavelEditText.setText("");
        nomeEditText.requestFocus();
    }
}