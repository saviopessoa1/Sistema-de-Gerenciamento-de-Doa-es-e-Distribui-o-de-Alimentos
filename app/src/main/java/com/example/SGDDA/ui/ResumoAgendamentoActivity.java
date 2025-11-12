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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Import necessário
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
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

// ★ PASSO 1: Implementar a interface do adapter
public class ResumoAgendamentoActivity extends AppCompatActivity implements ResumoItemAdapter.OnItemRemovedListener {

    private static final String TAG = "ResumoAgendamento";

    private ImageButton backButton;
    private RecyclerView resumoRecyclerView;
    private EditText dataEditText, horarioEditText, voluntarioEditText, obsEditText;
    private Button btnConfirmarEntrega;

    private Instituicao instituicao;
    private List<DoacaoItem> itensSelecionados;

    private FirebaseFirestore db;
    private ResumoItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resumo_agendamento);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();

        backButton = findViewById(R.id.backButton);
        resumoRecyclerView = findViewById(R.id.resumoRecyclerView);
        dataEditText = findViewById(R.id.dataEditText);
        horarioEditText = findViewById(R.id.horarioEditText);
        voluntarioEditText = findViewById(R.id.voluntarioEditText);
        obsEditText = findViewById(R.id.obsEditText);
        btnConfirmarEntrega = findViewById(R.id.btnConfirmarEntrega);

        // Configura campos para não abrir teclado (apenas clique)
        dataEditText.setFocusable(false);
        dataEditText.setClickable(true);
        horarioEditText.setFocusable(false);
        horarioEditText.setClickable(true);

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

        if (itensSelecionados == null) {
            itensSelecionados = new ArrayList<>();
        }

        // ★ PASSO 2: Passar 'this' (a Activity) como listener para o adapter
        adapter = new ResumoItemAdapter(this, itensSelecionados, this);
        resumoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resumoRecyclerView.setAdapter(adapter);

        setupListeners();

        // ★ PASSO 3: Checar o estado inicial do botão
        checkIfListIsEmpty();
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        dataEditText.setOnClickListener(v -> showDatePicker());
        horarioEditText.setOnClickListener(v -> showTimePicker());
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

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Selecione o horário")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", picker.getHour(), picker.getMinute());
            horarioEditText.setText(formattedTime);
        });

        picker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    // ★ PASSO 4: Implementar o método da interface
    @Override
    public void onListEmpty() {
        checkIfListIsEmpty();
    }

    // ★ PASSO 5: Criar método helper para centralizar a lógica do botão
    private void checkIfListIsEmpty() {
        if (itensSelecionados.isEmpty()) {
            btnConfirmarEntrega.setEnabled(false);
            btnConfirmarEntrega.setBackgroundColor(ContextCompat.getColor(this, R.color.app_primary_light));
            btnConfirmarEntrega.setText("Adicione itens para continuar");
        } else {
            btnConfirmarEntrega.setEnabled(true);
            btnConfirmarEntrega.setBackgroundColor(ContextCompat.getColor(this, R.color.app_accent_green));
            btnConfirmarEntrega.setText("Confirmar Entrega");
        }
    }

    private void confirmarEntrega() {
        String data = dataEditText.getText().toString().trim();
        String horario = horarioEditText.getText().toString().trim();
        String voluntario = voluntarioEditText.getText().toString().trim();
        String obs = obsEditText.getText().toString().trim();

        // ★ PASSO 6: Validação principal (redundância de segurança)
        if (itensSelecionados.isEmpty()) {
            Toast.makeText(this, "Não é possível agendar uma entrega vazia.", Toast.LENGTH_SHORT).show();
            checkIfListIsEmpty(); // Garante que o botão esteja desabilitado
            return;
        }

        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(horario) || TextUtils.isEmpty(voluntario)) {
            Toast.makeText(this, "Preencha Data, Horário e Voluntário.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmarEntrega.setEnabled(false);
        Toast.makeText(this, "Processando entrega...", Toast.LENGTH_SHORT).show();

        String dataHoraEntrega = data + " às " + horario;

        Entrega novaEntrega = new Entrega(
                instituicao.getDocumentId(),
                instituicao.getNome(),
                instituicao.getEndereco(),
                instituicao.getUrgencia(),
                voluntario,
                dataHoraEntrega,
                "Pendente",
                itensSelecionados
        );
        novaEntrega.setObservacoes(obs);

        // --- TRANSAÇÃO CORRIGIDA (Leituras PRIMEIRO, depois Escritas) ---
        db.runTransaction((Transaction.Function<Void>) transaction -> {

            // 1. LEITURAS (READS)
            Map<DocumentReference, Long> atualizacoesEstoque = new HashMap<>();
            List<DocumentReference> remocoesEstoque = new ArrayList<>();

            for (DoacaoItem itemSelecionado : itensSelecionados) {
                DocumentReference estoqueItemRef = db.collection("estoque").document(itemSelecionado.getDocumentId());
                DocumentSnapshot snapshot = transaction.get(estoqueItemRef); // LEITURA

                if (!snapshot.exists()) {
                    continue;
                }

                Long qtdLong = snapshot.getLong("quantidade");
                long quantidadeAtual = (qtdLong != null) ? qtdLong : 0;
                long quantidadeSaida = itemSelecionado.getQuantidade();

                if (quantidadeAtual < quantidadeSaida) {
                    throw new FirebaseFirestoreException(
                            "Estoque insuficiente para: " + itemSelecionado.getNomeItem(),
                            FirebaseFirestoreException.Code.ABORTED
                    );
                }

                long novoEstoque = quantidadeAtual - quantidadeSaida;
                if (novoEstoque == 0) {
                    remocoesEstoque.add(estoqueItemRef);
                } else {
                    atualizacoesEstoque.put(estoqueItemRef, novoEstoque);
                }
            }

            // 2. ESCRITAS (WRITES)
            DocumentReference entregaRef = db.collection("entregas").document();
            transaction.set(entregaRef, novaEntrega); // Salva a entrega

            for (Map.Entry<DocumentReference, Long> entry : atualizacoesEstoque.entrySet()) {
                transaction.update(entry.getKey(), "quantidade", entry.getValue());
            }
            for (DocumentReference ref : remocoesEstoque) {
                transaction.delete(ref);
            }

            return null;

        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(ResumoAgendamentoActivity.this, "Entrega agendada com sucesso!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ResumoAgendamentoActivity.this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Erro na transação", e);
            String msg = e.getMessage();
            if (msg != null && msg.contains("Estoque insuficiente")) {
                Toast.makeText(ResumoAgendamentoActivity.this, msg, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ResumoAgendamentoActivity.this, "Falha ao agendar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            btnConfirmarEntrega.setEnabled(true);
        });
    }
}