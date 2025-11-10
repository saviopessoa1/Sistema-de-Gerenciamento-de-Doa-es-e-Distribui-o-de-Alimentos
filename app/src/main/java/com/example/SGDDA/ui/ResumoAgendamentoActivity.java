package com.example.SGDDA.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SGDDA.R;
import com.example.SGDDA.adapter.ResumoItemAdapter;
import com.example.SGDDA.model.DoacaoItem;
import com.example.SGDDA.model.Entrega;
import com.example.SGDDA.model.Instituicao;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot; // <-- NOVO IMPORT
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction; // <-- NOVO IMPORT
import com.google.firebase.firestore.WriteBatch;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ResumoAgendamentoActivity extends AppCompatActivity {

    private static final String TAG = "ResumoAgendamento";

    // Componentes
    private ImageButton backButton;
    private RecyclerView resumoRecyclerView;
    private EditText dataEditText, horarioEditText, voluntarioEditText, obsEditText;
    private Button btnConfirmarEntrega;

    // Dados Recebidos
    private Instituicao instituicao;
    private List<DoacaoItem> itensSelecionados;

    // Firebase
    private FirebaseFirestore db;
    private ResumoItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resumo_agendamento);

        // Ajuste de layout (EdgeToEdge)
        View mainView = findViewById(R.id.main); // Garanta que o ID "main" exista no XML
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Encontrar Componentes
        backButton = findViewById(R.id.backButton);
        resumoRecyclerView = findViewById(R.id.resumoRecyclerView);
        dataEditText = findViewById(R.id.dataEditText);
        horarioEditText = findViewById(R.id.horarioEditText);
        voluntarioEditText = findViewById(R.id.voluntarioEditText);
        obsEditText = findViewById(R.id.obsEditText);
        btnConfirmarEntrega = findViewById(R.id.btnConfirmarEntrega);

        // Pegar dados da Intent
        if (getIntent().hasExtra("INSTITUICAO_SELECIONADA") && getIntent().hasExtra("ITENS_SELECIONADOS")) {
            instituicao = (Instituicao) getIntent().getSerializableExtra("INSTITUICAO_SELECIONADA");
            Serializable itemsSerializable = getIntent().getSerializableExtra("ITENS_SELECIONADOS");
            if (itemsSerializable instanceof List) {
                itensSelecionados = (List<DoacaoItem>) itemsSerializable;
            } else {
                itensSelecionados = new ArrayList<>();
            }
        } else {
            Toast.makeText(this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar RecyclerView de Resumo
        if (itensSelecionados == null) {
            itensSelecionados = new ArrayList<>();
        }
        adapter = new ResumoItemAdapter(this, itensSelecionados);
        resumoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resumoRecyclerView.setAdapter(adapter);

        // Configurar Listeners
        backButton.setOnClickListener(v -> finish());
        dataEditText.setOnClickListener(v -> showDatePicker());
        btnConfirmarEntrega.setOnClickListener(v -> confirmarEntrega());
    }

    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Selecione a data da entrega");
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());
        MaterialDatePicker<Long> datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setTimeZone(tz);
            String formattedDate = sdf.format(new Date(selection));
            dataEditText.setText(formattedDate);
        });
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void confirmarEntrega() {
        // 1. Validar campos
        String data = dataEditText.getText().toString().trim();
        String horario = horarioEditText.getText().toString().trim();
        String voluntario = voluntarioEditText.getText().toString().trim();
        String obs = obsEditText.getText().toString().trim();

        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(horario) || TextUtils.isEmpty(voluntario)) {
            Toast.makeText(this, "Preencha Data, Horário e Voluntário.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Desabilita o botão para evitar cliques duplos
        btnConfirmarEntrega.setEnabled(false);
        Toast.makeText(this, "Processando entrega...", Toast.LENGTH_SHORT).show();

        // 2. Criar o objeto Entrega
        Entrega novaEntrega = new Entrega(instituicao.getDocumentId(),
                instituicao.getNome(),
                instituicao.getEndereco(),
                instituicao.getUrgencia(),
                data, horario, voluntario, obs,
                "Pendente", itensSelecionados);

        // 3. EXECUTAR A TRANSAÇÃO (Salvar Entrega E Atualizar Estoque)
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // Passo A: Salvar a nova entrega na coleção "entregas"
            DocumentReference entregaRef = db.collection("entregas").document();
            transaction.set(entregaRef, novaEntrega);

            // Passo B: Atualizar (subtrair) as quantidades no "estoque"
            for (DoacaoItem itemSelecionado : itensSelecionados) {
                // Pega a referência do item no estoque pelo ID do documento
                DocumentReference estoqueItemRef = db.collection("estoque").document(itemSelecionado.getDocumentId());

                // Lê o item do estoque DENTRO da transação
                DocumentSnapshot snapshot = transaction.get(estoqueItemRef);

                long quantidadeAtual = snapshot.getLong("quantidade"); // Pega a qtd atual no DB
                long quantidadeSaida = itemSelecionado.getQuantidade();

                if (quantidadeAtual < quantidadeSaida) {
                    // Se não tiver estoque suficiente, falha a transação
                    throw new FirebaseFirestoreException(
                            "Estoque insuficiente para " + itemSelecionado.getNomeItem(),
                            FirebaseFirestoreException.Code.ABORTED
                    );
                }

                // Calcula o novo estoque
                long novoEstoque = quantidadeAtual - quantidadeSaida;

                if (novoEstoque == 0) {
                    // Se o estoque zerar, deleta o item
                    transaction.delete(estoqueItemRef);
                } else {
                    // Se não, atualiza a quantidade
                    transaction.update(estoqueItemRef, "quantidade", novoEstoque);
                }
            }

            // Se chegou aqui, a transação deu certo
            return null;

        }).addOnSuccessListener(aVoid -> {
            // SUCESSO DE TUDO
            Toast.makeText(ResumoAgendamentoActivity.this, "Entrega agendada e estoque atualizado!", Toast.LENGTH_LONG).show();

            // Limpa as telas de montagem/resumo e volta para o Dashboard
            Intent intent = new Intent(ResumoAgendamentoActivity.this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }).addOnFailureListener(e -> {
            // FALHA DA TRANSAÇÃO
            Log.e(TAG, "Erro na transação", e);
            Toast.makeText(ResumoAgendamentoActivity.this, "Falha: " + e.getMessage(), Toast.LENGTH_LONG).show();
            btnConfirmarEntrega.setEnabled(true); // Reabilita o botão
        });
    }
}


