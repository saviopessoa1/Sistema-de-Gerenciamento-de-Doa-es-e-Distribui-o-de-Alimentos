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
import com.example.SGDDA.adapter.DetalhesItemAdapter; 
import com.example.SGDDA.model.DoacaoItem;
import com.example.SGDDA.model.Entrega;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DetalhesEntregaActivity extends AppCompatActivity {

    private static final String TAG = "DetalhesEntrega";

    
    private ImageButton backButton;
    private TextView textInstituicao, textEndereco, textVoluntario;
    private Button btnLigarVoluntario, btnConfirmarColeta, btnConfirmarEntrega;
    private RecyclerView itensRecyclerView;

    
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

        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        
        if (getIntent().hasExtra("ENTREGA_ID")) {
            entregaId = getIntent().getStringExtra("ENTREGA_ID");
        } else {
            Toast.makeText(this, "Erro: ID da entrega não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        
        db = FirebaseFirestore.getInstance();
        itemList = new ArrayList<>();
        adapter = new DetalhesItemAdapter(this, itemList);

        
        backButton = findViewById(R.id.backButton);
        textInstituicao = findViewById(R.id.textInstituicao);
        textEndereco = findViewById(R.id.textEndereco);
        textVoluntario = findViewById(R.id.textVoluntario);
        btnLigarVoluntario = findViewById(R.id.btnLigarVoluntario);
        btnConfirmarColeta = findViewById(R.id.btnConfirmarColeta);
        btnConfirmarEntrega = findViewById(R.id.btnConfirmarEntrega);
        itensRecyclerView = findViewById(R.id.itensRecyclerView);

        
        itensRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itensRecyclerView.setAdapter(adapter);

        
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
                            
                            textInstituicao.setText(currentEntrega.getInstituicaoNome());
                            textEndereco.setText(currentEntrega.getInstituicaoEndereco());
                            textVoluntario.setText(currentEntrega.getVoluntarioNome());

                            
                            itemList.clear();
                            if (currentEntrega.getItens() != null) {
                                itemList.addAll(currentEntrega.getItens());
                            }
                            adapter.notifyDataSetChanged();

                            
                            updateButtonStates(currentEntrega.getStatus());
                        }
                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(this, "Entrega não encontrada.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    
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

        
        btnLigarVoluntario.setOnClickListener(v -> {
            Toast.makeText(this, "Ligando para Voluntário...", Toast.LENGTH_SHORT).show();
            
            
            
        });

        
        btnConfirmarColeta.setOnClickListener(v -> {
            updateStatus("Em Coleta");
        });

        
        btnConfirmarEntrega.setOnClickListener(v -> {
            updateStatus("Concluída");
            
            
            
            
            
        });
    }

    
    private void updateStatus(String newStatus) {
        if (entregaId == null) return;

        db.collection("entregas").document(entregaId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status atualizado para: " + newStatus, Toast.LENGTH_SHORT).show();

                    
                    if ("Concluída".equals(newStatus) && currentEntrega != null && currentEntrega.getInstituicaoId() != null) {
                        resetarUrgenciaInstituicao(currentEntrega.getInstituicaoId());
                    }
                    
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao atualizar status.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating status", e);
                });
    }

    
    private void resetarUrgenciaInstituicao(String instituicaoId) {
        db.collection("instituicoes").document(instituicaoId)
                .update("urgencia", "Normal")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Urgência da instituição " + instituicaoId + " resetada para 'Normal'.");
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Erro ao resetar urgência da instituição " + instituicaoId, e);
                    
                });
    }
}


