package com.example.SGDDA.ui;

import android.content.Intent;
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
import com.example.SGDDA.adapter.EstoqueAdapter;
import com.example.SGDDA.model.DoacaoItem;
import com.google.android.material.bottomnavigation.BottomNavigationView; // Importe isso
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PesquisarEstoqueActivity extends AppCompatActivity {

    private static final String TAG = "PesquisarEstoque";

    private RecyclerView estoqueRecyclerView;
    private ImageButton backButton;
    private BottomNavigationView bottomNavigationView; // Declarar

    private FirebaseFirestore db;
    private EstoqueAdapter adapter;
    private List<DoacaoItem> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pesquisar_estoque);

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
        estoqueRecyclerView = findViewById(R.id.estoqueRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView); // Encontrar

        itemList = new ArrayList<>();
        adapter = new EstoqueAdapter(this, itemList);
        estoqueRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        estoqueRecyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());

        // --- CONFIGURAÇÃO DA BARRA DE NAVEGAÇÃO ---
        setupBottomNavigation();

        loadEstoqueData();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_estoque); // Marca Estoque como ativo

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_painel) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_estoque) {
                return true; // Já estamos aqui
            } else if (itemId == R.id.nav_instituicoes) {
                startActivity(new Intent(this, InstituicoesActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_entregas) {
                startActivity(new Intent(this, EntregasActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadEstoqueData() {
        db.collection("estoque")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }
                    itemList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            DoacaoItem item = doc.toObject(DoacaoItem.class);
                            itemList.add(item);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}