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
import com.example.SGDDA.adapter.DoacaoItemAdapter; // Importe o Adapter
import com.example.SGDDA.model.DoacaoItem; // Importe o Modelo

// IMPORTS NOVOS PARA O DATE PICKER
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

    // Componentes do Layout
    private ImageButton backButton;
    private EditText nomeItemEditText, quantidadeEditText, validadeEditText;
    private RadioGroup radioGroupPerecivel;
    private RadioButton radioNaoPerecivel; // Default
    private Button adicionarItemButton, confirmarDoacaoButton;

    // RecyclerView
    private RecyclerView itensRegistradosRecyclerView;
    private DoacaoItemAdapter adapter;
    private List<DoacaoItem> listaItensTemporaria;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String uidUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar_doacao);

        // Ajuste de layout (EdgeToEdge)
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            uidUsuario = mAuth.getCurrentUser().getUid();
        } else {
            // Se o usuário não estiver logado, não deve estar aqui.
            Toast.makeText(this, "Erro: Usuário não logado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Encontrar Componentes
        backButton = findViewById(R.id.backButton);
        nomeItemEditText = findViewById(R.id.nomeItemEditText);
        quantidadeEditText = findViewById(R.id.quantidadeEditText);
        validadeEditText = findViewById(R.id.validadeEditText); // Campo de data
        radioGroupPerecivel = findViewById(R.id.radioGroupPerecivel);
        radioNaoPerecivel = findViewById(R.id.radioNaoPerecivel);
        adicionarItemButton = findViewById(R.id.adicionarItemButton);
        confirmarDoacaoButton = findViewById(R.id.confirmarDoacaoButton);
        itensRegistradosRecyclerView = findViewById(R.id.itensRegistradosRecyclerView);

        // Configurar RecyclerView
        listaItensTemporaria = new ArrayList<>();
        adapter = new DoacaoItemAdapter(this, listaItensTemporaria);
        itensRegistradosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itensRegistradosRecyclerView.setAdapter(adapter);

        // Configurar Cliques (Listeners)
        setupListeners();
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish()); // Volta para a tela anterior

        // Botão ADICIONAR ITEM
        adicionarItemButton.setOnClickListener(v -> adicionarItemNaLista());

        // Botão CONFIRMAR DOAÇÃO
        confirmarDoacaoButton.setOnClickListener(v -> salvarDoacaoNoFirebase());

        // NOVO: Adiciona o listener para o campo de validade
        validadeEditText.setOnClickListener(v -> showDatePicker());
    }

    private void adicionarItemNaLista() {
        // 1. Validar Entradas
        String nomeItem = nomeItemEditText.getText().toString().trim();
        String qtdStr = quantidadeEditText.getText().toString().trim();
        String validade = validadeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nomeItem) || TextUtils.isEmpty(qtdStr) || TextUtils.isEmpty(validade)) {
            Toast.makeText(this, "Preencha todos os campos do item.", Toast.LENGTH_SHORT).show();
            return;
        }

        // A validação de data agora só precisa checar se não está vazia
        // pois o DatePicker já garante o formato.

        int quantidade;
        try {
            quantidade = Integer.parseInt(qtdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantidade inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isPerecivel = radioGroupPerecivel.getCheckedRadioButtonId() == R.id.radioPerecivel;

        // 2. Criar o objeto DoacaoItem
        DoacaoItem item = new DoacaoItem(nomeItem, quantidade, isPerecivel, validade, uidUsuario);

        // 3. Adicionar na lista temporária
        listaItensTemporaria.add(item);

        // 4. Notificar o Adapter (para atualizar o RecyclerView)
        adapter.notifyDataSetChanged();

        // 5. Limpar os campos
        nomeItemEditText.setText("");
        quantidadeEditText.setText("");
        validadeEditText.setText(""); // Limpa o campo de data
        radioNaoPerecivel.setChecked(true);
        nomeItemEditText.requestFocus();
    }

    private void salvarDoacaoNoFirebase() {
        if (listaItensTemporaria.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos um item antes de confirmar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Registrando doação...", Toast.LENGTH_SHORT).show();

        // Itera sobre a lista e salva cada item.
        // Em um app real complexo, usaríamos um WriteBatch, mas para este caso
        // salvar um por um é mais simples de depurar.
        for (DoacaoItem item : listaItensTemporaria) {
            // Salva cada item na coleção "estoque"
            db.collection("estoque")
                    .add(item) // .add() cria um ID aleatório para o documento
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Item salvo com ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Erro ao salvar item", e);
                        // Mostra erro, mas continua tentando salvar os outros
                        Toast.makeText(this, "Erro ao salvar item: " + item.getNomeItem(), Toast.LENGTH_SHORT).show();
                    });
        }

        // Após iniciar o salvamento (não precisamos esperar)
        Toast.makeText(this, "Doação registrada com sucesso!", Toast.LENGTH_LONG).show();
        listaItensTemporaria.clear();
        adapter.notifyDataSetChanged();
        finish(); // Fecha a tela e volta ao Dashboard
    }

    // ESTA É A FUNÇÃO QUE ESTAVA FALTANDO
    private void showDatePicker() {
        // Cria o construtor do DatePicker
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Selecione a data de validade");
        // Define a data de hoje como seleção inicial
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        // Constrói o DatePicker
        MaterialDatePicker<Long> datePicker = builder.build();

        // Adiciona o listener para quando o usuário clica em "OK"
        datePicker.addOnPositiveButtonClickListener(selection -> {
            // O 'selection' vem como um Long (milissegundos em UTC).
            // Precisamos formatar para DD/MM/AAAA no fuso horário local.

            // Corrige o fuso horário para não pegar o dia anterior
            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setTimeZone(tz);
            String formattedDate = sdf.format(new Date(selection));

            // Define a data formatada no campo de texto
            validadeEditText.setText(formattedDate);
        });

        // Mostra o DatePicker
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }
}