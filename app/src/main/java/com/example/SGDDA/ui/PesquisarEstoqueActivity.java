package com.example.SGDDA.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SGDDA.R;
import com.example.SGDDA.adapter.EstoqueAdapter; // Importa o novo adapter
import com.example.SGDDA.model.DoacaoItem; // Reutiliza o modelo
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query; // Import para ordenação

import java.util.ArrayList;
import java.util.List;

public class PesquisarEstoqueActivity extends AppCompatActivity {

    private static final String TAG = "PesquisarEstoque";

    // Componentes
    private RecyclerView estoqueRecyclerView;
    private ImageButton backButton;

    // Firebase e Adapter
    private FirebaseFirestore db;
    private EstoqueAdapter adapter;
    private List<DoacaoItem> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pesquisar_estoque);

        // Ajuste de layout (EdgeToEdge)
        // Precisamos adicionar o ID "main" no XML
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
        estoqueRecyclerView = findViewById(R.id.estoqueRecyclerView);

        // Configurar RecyclerView
        itemList = new ArrayList<>();
        adapter = new EstoqueAdapter(this, itemList);
        estoqueRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        estoqueRecyclerView.setAdapter(adapter);

        // Configurar Listeners
        backButton.setOnClickListener(v -> finish());

        // Carregar os dados
        loadEstoqueData();
    }

    private void loadEstoqueData() {
        // Esta função ouve o banco de dados em tempo real
        db.collection("estoque")
                //.orderBy("nomeItem", Query.Direction.ASCENDING) // Podemos ordenar por nome
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        Toast.makeText(this, "Erro ao carregar estoque.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Limpa a lista antiga
                    itemList.clear();
                    // Itera sobre os documentos recebidos
                    for (QueryDocumentSnapshot doc : value) {
                        if (doc != null) {
                            // Converte o documento do Firestore para o nosso objeto DoacaoItem
                            DoacaoItem item = doc.toObject(DoacaoItem.class);
                            itemList.add(item);
                        }
                    }
                    // Notifica o adapter que a lista mudou
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Estoque carregado: " + itemList.size() + " items.");
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Começa a ouvir as mudanças quando a tela é aberta
        // (O SnapshotListener já faz isso, mas podemos adicionar um refresh aqui se necessário)
    }
}

