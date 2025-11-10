package com.example.SGDDA.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SGDDA.R;
import com.example.SGDDA.adapter.DetalhesItemAdapter; // Importa o adapter NOVO
import com.example.SGDDA.model.DoacaoItem;
import com.example.SGDDA.model.Entrega;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DetalhesEntregaActivity extends AppCompatActivity {

    private static final String TAG = "DetalhesEntrega";

    // Componentes
    private ImageButton backButton;
    private TextView textInstituicao, textEndereco, textVoluntario;
    private Button btnLigarVoluntario, btnConfirmarColeta, btnConfirmarEntrega;
    private RecyclerView itensRecyclerView;

    // Firebase e Dados
    private FirebaseFirestore db;
    private DetalhesItemAdapter adapter;
    private List<DoacaoItem> itemList;
    private String entregaId;
    private Entrega currentEntrega;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhes_entrega);

        // Ajuste de layout (EdgeToEdge)
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Pegar o ID da Entrega (vindo do EntregaAdapter)
        if (getIntent().hasExtra("ENTREGA_ID")) {
            entregaId = getIntent().getStringExtra("ENTREGA_ID");
        } else {
            Toast.makeText(this, "Erro: ID da entrega não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar
        db = FirebaseFirestore.getInstance();
        itemList = new ArrayList<>();
        adapter = new DetalhesItemAdapter(this, itemList);

        // Encontrar Componentes
        backButton = findViewById(R.id.backButton);
        textInstituicao = findViewById(R.id.textInstituicao);
        textEndereco = findViewById(R.id.textEndereco);
        textVoluntario = findViewById(R.id.textVoluntario);
        btnLigarVoluntario = findViewById(R.id.btnLigarVoluntario);
        btnConfirmarColeta = findViewById(R.id.btnConfirmarColeta);
        btnConfirmarEntrega = findViewById(R.id.btnConfirmarEntrega);
        itensRecyclerView = findViewById(R.id.itensRecyclerView);

        // Configurar RecyclerView
        itensRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itensRecyclerView.setAdapter(adapter);

        // Carregar Dados e Configurar Cliques
        loadEntregaDetails();
        setupListeners();
    }

    private void loadEntregaDetails() {
        if (entregaId == null) return;

        db.collection("entregas").document(entregaId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        currentEntrega = snapshot.toObject(Entrega.class);
                        if (currentEntrega != null) {
                            // Preenche os campos
                            textInstituicao.setText(currentEntrega.getInstituicaoNome());
                            textEndereco.setText(currentEntrega.getInstituicaoEndereco());
                            textVoluntario.setText(currentEntrega.getVoluntarioNome());

                            // Atualiza a lista de itens
                            itemList.clear();
                            if (currentEntrega.getItens() != null) {
                                itemList.addAll(currentEntrega.getItens());
                            }
                            adapter.notifyDataSetChanged();

                            // Atualiza o estado dos botões
                            updateButtonStates(currentEntrega.getStatus());
                        }
                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(this, "Entrega não encontrada.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    // Controla quais botões estão ativos
    private void updateButtonStates(String status) {
        if ("Pendente".equals(status)) {
            btnConfirmarColeta.setEnabled(true);
            btnConfirmarColeta.setText("Confirmar Coleta no Estoque");
            btnConfirmarEntrega.setEnabled(false);
            btnConfirmarEntrega.setText("Confirmar Entrega");
        } else if ("Em Coleta".equals(status)) {
            btnConfirmarColeta.setEnabled(false);
            btnConfirmarColeta.setText("Coleta Confirmada");
            btnConfirmarEntrega.setEnabled(true);
            btnConfirmarEntrega.setText("Confirmar Entrega");
        } else if ("Concluída".equals(status)) {
            btnConfirmarColeta.setEnabled(false);
            btnConfirmarColeta.setText("Coleta Confirmada");
            btnConfirmarEntrega.setEnabled(false);
            btnConfirmarEntrega.setText("Entrega Concluída");
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        // TODO: Adicionar lógica para buscar o telefone do voluntário
        btnLigarVoluntario.setOnClickListener(v -> {
            Toast.makeText(this, "Ligando para Voluntário...", Toast.LENGTH_SHORT).show();
            // String phone = currentEntrega.getVoluntarioTelefone(); // (Precisamos adicionar isso no modelo)
            // Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            // startActivity(intent);
        });

        // Botão "Confirmar Coleta"
        btnConfirmarColeta.setOnClickListener(v -> {
            updateStatus("Em Coleta");
        });

        // Botão "Confirmar Entrega"
        btnConfirmarEntrega.setOnClickListener(v -> {
            updateStatus("Concluída");
            // TODO: Abrir a tela de Observações (image_512928.png)
            // Intent intent = new Intent(this, ObservacoesEntregaActivity.class);
            // intent.putExtra("ENTREGA_ID", entregaId);
            // startActivity(intent);
            // finish(); // Fecha esta tela
        });
    }

    // Função para atualizar o status no Firestore
    private void updateStatus(String newStatus) {
        if (entregaId == null) return;

        db.collection("entregas").document(entregaId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status atualizado para: " + newStatus, Toast.LENGTH_SHORT).show();
                    // O SnapshotListener vai atualizar a UI automaticamente
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao atualizar status.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating status", e);
                });
    }
}


