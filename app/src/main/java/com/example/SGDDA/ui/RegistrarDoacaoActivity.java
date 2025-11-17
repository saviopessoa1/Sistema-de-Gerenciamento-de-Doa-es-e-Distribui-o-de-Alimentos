package com.example.SGDDA.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SGDDA.R;
import com.example.SGDDA.adapter.DoacaoItemAdapter; 
import com.example.SGDDA.model.DoacaoItem; 


import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RegistrarDoacaoActivity extends AppCompatActivity {

    
    private ImageButton backButton;
    private EditText nomeItemEditText, quantidadeEditText, validadeEditText;
    private RadioGroup radioGroupPerecivel;
    private RadioButton radioNaoPerecivel; 
    private Button adicionarItemButton, confirmarDoacaoButton;

    
    private RecyclerView itensRegistradosRecyclerView;
    private DoacaoItemAdapter adapter;
    private List<DoacaoItem> listaItensTemporaria;

    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String uidUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar_doacao);

        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            uidUsuario = mAuth.getCurrentUser().getUid();
        } else {
            
            Toast.makeText(this, "Erro: Usuário não logado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        
        backButton = findViewById(R.id.backButton);
        nomeItemEditText = findViewById(R.id.nomeItemEditText);
        quantidadeEditText = findViewById(R.id.quantidadeEditText);
        validadeEditText = findViewById(R.id.validadeEditText); 
        radioGroupPerecivel = findViewById(R.id.radioGroupPerecivel);
        radioNaoPerecivel = findViewById(R.id.radioNaoPerecivel);
        adicionarItemButton = findViewById(R.id.adicionarItemButton);
        confirmarDoacaoButton = findViewById(R.id.confirmarDoacaoButton);
        itensRegistradosRecyclerView = findViewById(R.id.itensRegistradosRecyclerView);

        
        listaItensTemporaria = new ArrayList<>();
        adapter = new DoacaoItemAdapter(this, listaItensTemporaria);
        itensRegistradosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itensRegistradosRecyclerView.setAdapter(adapter);

        
        setupListeners();
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish()); 

        
        adicionarItemButton.setOnClickListener(v -> adicionarItemNaLista());

        
        confirmarDoacaoButton.setOnClickListener(v -> salvarDoacaoNoFirebase());

        
        validadeEditText.setOnClickListener(v -> showDatePicker());
    }

    private void adicionarItemNaLista() {
        
        String nomeItem = nomeItemEditText.getText().toString().trim();
        String qtdStr = quantidadeEditText.getText().toString().trim();
        String validade = validadeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nomeItem) || TextUtils.isEmpty(qtdStr) || TextUtils.isEmpty(validade)) {
            Toast.makeText(this, "Preencha todos os campos do item.", Toast.LENGTH_SHORT).show();
            return;
        }

        
        

        int quantidade;
        try {
            quantidade = Integer.parseInt(qtdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantidade inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isPerecivel = radioGroupPerecivel.getCheckedRadioButtonId() == R.id.radioPerecivel;

        
        DoacaoItem item = new DoacaoItem(nomeItem, quantidade, isPerecivel, validade, uidUsuario);

        
        listaItensTemporaria.add(item);

        
        adapter.notifyDataSetChanged();

        
        nomeItemEditText.setText("");
        quantidadeEditText.setText("");
        validadeEditText.setText(""); 
        radioNaoPerecivel.setChecked(true);
        nomeItemEditText.requestFocus();
    }

    private void salvarDoacaoNoFirebase() {
        if (listaItensTemporaria.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos um item antes de confirmar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Registrando doação...", Toast.LENGTH_SHORT).show();

        
        
        
        for (DoacaoItem item : listaItensTemporaria) {
            
            db.collection("estoque")
                    .add(item) 
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Item salvo com ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Erro ao salvar item", e);
                        
                        Toast.makeText(this, "Erro ao salvar item: " + item.getNomeItem(), Toast.LENGTH_SHORT).show();
                    });
        }

        
        Toast.makeText(this, "Doação registrada com sucesso!", Toast.LENGTH_LONG).show();
        listaItensTemporaria.clear();
        adapter.notifyDataSetChanged();
        finish(); 
    }

    
    private void showDatePicker() {
        
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Selecione a data de validade");
        
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        
        MaterialDatePicker<Long> datePicker = builder.build();

        
        datePicker.addOnPositiveButtonClickListener(selection -> {
            
            

            
            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setTimeZone(tz);
            String formattedDate = sdf.format(new Date(selection));

            
            validadeEditText.setText(formattedDate);
        });

        
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }
}