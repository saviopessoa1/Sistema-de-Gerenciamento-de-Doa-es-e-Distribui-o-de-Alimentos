package com.example.SGDDA.ui;

// --- IMPORTS NECESSÁRIOS ---
import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Para Debug
import android.widget.Button;
import android.widget.ImageButton;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast; // Para feedback

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity; // O import principal que faltava
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.SGDDA.R;
import com.google.android.material.textfield.TextInputEditText; // Import para os campos

// Imports do Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap; // Para salvar no Firestore
import java.util.Map; // Para salvar no Firestore
// --- FIM DOS IMPORTS ---

public class RegisterStep2Activity extends AppCompatActivity {

    // 1. Declaração dos componentes
    private ImageButton backButton;
    private TextView loginTextView;
    private Button registrarButton;
    private TextInputEditText cpfEditText, celularEditText, telefoneEditText;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Dados da Etapa 1
    private String nome;
    private String email;
    private String senha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_step_2);

        // Inicializa Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Recupera dados da Intent da Etapa 1
        Intent intent = getIntent();
        nome = intent.getStringExtra("NOME");
        email = intent.getStringExtra("EMAIL");
        senha = intent.getStringExtra("SENHA");

        // Se os dados não vieram, é um erro. Volta para a Etapa 1.
        if (nome == null || email == null || senha == null) {
            Toast.makeText(this, "Erro ao carregar dados. Tente novamente.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tenta encontrar o 'main'
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } else {
            // Log de aviso se o 'main' não for encontrado (pode ser o ID do ConstraintLayout)
            Log.w("RegisterStep2Activity", "ID 'main' não encontrado no layout 'activity_register_step_2.xml'");
            // Tenta o ID raiz do seu XML de step 2 se 'main' não existir.
            // Se o ID raiz for outro, ajuste aqui.
        }


        // 2. Encontrar os componentes
        backButton = findViewById(R.id.backButton);
        loginTextView = findViewById(R.id.loginTextView);
        registrarButton = findViewById(R.id.registrarButton);
        cpfEditText = findViewById(R.id.cpfEditText);
        celularEditText = findViewById(R.id.celularEditText);
        telefoneEditText = findViewById(R.id.telefoneEditText);


        // 3. Configurar os Listeners

        // Botão de voltar
        backButton.setOnClickListener(v -> {
            finish(); // Fecha a Etapa 2 e volta para a Etapa 1
        });

        // Link "Log in"
        loginTextView.setOnClickListener(v -> {
            // Navega de volta para o Login (MainActivity)
            Intent loginIntent = new Intent(RegisterStep2Activity.this, MainActivity.class);
            // Limpa todas as telas anteriores (Etapa 1) do histórico
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish(); // Fecha a tela atual
        });

        // Botão Registrar
        registrarButton.setOnClickListener(v -> {
            // Pegar dados desta tela
            String cpf = cpfEditText.getText().toString().trim();
            String celular = celularEditText.getText().toString().trim();
            String telefone = telefoneEditText.getText().toString().trim(); // Opcional

            // Validação
            if (cpf.isEmpty() || celular.isEmpty()) {
                Toast.makeText(this, "Preencha os campos obrigatórios (*)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Inicia o processo de criação de usuário
            criarUsuarioFirebase(email, senha, nome, cpf, celular, telefone);
        });
    }

    private void criarUsuarioFirebase(String email, String password, String nome, String cpf, String celular, String telefone) {
        // 3. Chamar o Firebase Auth para criar o usuário
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sucesso na autenticação
                        Log.d("FIREBASE_AUTH", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        // 4. Salvar dados (Nome, CPF, Tel) no Firestore
                        salvarDadosUsuario(user.getUid(), nome, email, cpf, celular, telefone);

                    } else {
                        // Falha na autenticação
                        Log.w("FIREBASE_AUTH", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterStep2Activity.this, "Falha no registro: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void salvarDadosUsuario(String uid, String nome, String email, String cpf, String celular, String telefone) {
        // Cria um "mapa" (objeto) com os dados do usuário
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nome", nome);
        usuario.put("email", email);
        usuario.put("cpf", cpf);
        usuario.put("celular", celular);
        usuario.put("telefone", telefone);
        usuario.put("tipo", "instituicao"); // Define um tipo padrão

        // Salva no Firestore na coleção "usuarios" com o ID do usuário (uid)
        db.collection("usuarios").document(uid)
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    // 5. Sucesso ao salvar! Navegar para o Dashboard
                    Log.d("FIRESTORE", "DocumentSnapshot successfully written!");
                    abrirDashboard();
                })
                .addOnFailureListener(e -> {
                    Log.w("FIRESTORE", "Error writing document", e);
                    Toast.makeText(RegisterStep2Activity.this, "Erro ao salvar dados.", Toast.LENGTH_SHORT).show();
                });
    }

    private void abrirDashboard() {
        Intent intent = new Intent(RegisterStep2Activity.this, DashboardActivity.class);
        // Limpa todo o histórico de login/registro
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Fecha a tela atual
    }
}


