package com.example.SGDDA.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.SGDDA.R;
import com.example.SGDDA.model.Instituicao;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistrarInstituicaoActivity extends AppCompatActivity {

    private static final String TAG = "RegInstituicaoActivity";

    // Componentes do Layout
    private ImageButton backButton;
    private TextInputEditText nomeEditText, enderecoEditText, telefoneEditText, responsavelEditText;
    private RadioGroup radioGroupUrgencia;
    private Button cadastrarButton;

    // Firebase
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar_instituicao);

        // Ajuste de layout (EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Encontrar Componentes
        backButton = findViewById(R.id.backButton);
        nomeEditText = findViewById(R.id.nomeEditText);
        enderecoEditText = findViewById(R.id.enderecoEditText);
        telefoneEditText = findViewById(R.id.telefoneEditText);
        responsavelEditText = findViewById(R.id.responsavelEditText);
        radioGroupUrgencia = findViewById(R.id.radioGroupUrgencia);
        cadastrarButton = findViewById(R.id.cadastrarButton);

        // Configurar Listeners
        setupListeners();
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        cadastrarButton.setOnClickListener(v -> salvarInstituicao());
    }

    private void salvarInstituicao() {
        // 1. Pegar os dados dos campos
        String nome = nomeEditText.getText().toString().trim();
        String endereco = enderecoEditText.getText().toString().trim();
        String telefone = telefoneEditText.getText().toString().trim();
        String responsavel = responsavelEditText.getText().toString().trim();

        // 2. Validar campos
        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(endereco) || TextUtils.isEmpty(telefone) || TextUtils.isEmpty(responsavel)) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Pegar a Urgência
        String urgencia = "Normal"; // Padrão
        int selectedId = radioGroupUrgencia.getCheckedRadioButtonId();
        if (selectedId == R.id.radioAlta) {
            urgencia = "Alta";
        } else if (selectedId == R.id.radioMedia) {
            urgencia = "Média";
        }

        // 4. Criar o objeto Instituicao
        Instituicao instituicao = new Instituicao();
        instituicao.setNome(nome);
        instituicao.setEndereco(endereco);
        instituicao.setTelefone(telefone);
        instituicao.setResponsavel(responsavel);
        instituicao.setUrgencia(urgencia);

        // 5. Salvar no Firestore
        // Usamos o NOME como ID do documento para evitar duplicatas
        db.collection("instituicoes").document(nome)
                .set(instituicao)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegistrarInstituicaoActivity.this, "Instituição cadastrada!", Toast.LENGTH_SHORT).show();
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
        radioGroupUrgencia.check(R.id.radioNormal); // Volta ao padrão
        nomeEditText.requestFocus(); // Foca no primeiro campo
    }
}