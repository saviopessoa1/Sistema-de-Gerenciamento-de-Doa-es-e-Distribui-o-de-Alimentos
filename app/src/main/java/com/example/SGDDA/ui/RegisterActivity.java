package com.example.SGDDA.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast; 

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity; 
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.SGDDA.R;
import com.google.android.material.textfield.TextInputEditText; 


public class RegisterActivity extends AppCompatActivity {

    
    private ImageButton backButton;
    private TextView loginTextView;
    private Button proximoButton;
    
    private TextInputEditText nomeCompletoEditText, emailEditText, passwordEditText, repetePasswordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register); 
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        
        backButton = findViewById(R.id.backButton);
        loginTextView = findViewById(R.id.loginTextView);
        proximoButton = findViewById(R.id.proximoButton);

        
        nomeCompletoEditText = findViewById(R.id.nomeCompletoEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        repetePasswordEditText = findViewById(R.id.repetePasswordEditText);


        
        
        backButton.setOnClickListener(v -> {
            finish(); 
        });

        
        loginTextView.setOnClickListener(v -> {
            finish(); 
        });

        
        proximoButton.setOnClickListener(v -> {
            
            String nome = nomeCompletoEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String senha = passwordEditText.getText().toString().trim();
            String repeteSenha = repetePasswordEditText.getText().toString().trim();

            
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

            
            Intent intent = new Intent(RegisterActivity.this, RegisterStep2Activity.class);

            
            
            intent.putExtra("NOME_COMPLETO", nome); 
            intent.putExtra("EMAIL", email);
            intent.putExtra("SENHA", senha);

            startActivity(intent);
        });
    }
}