package com.example.SGDDA.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.SGDDA.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    // Componentes do Layout
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;
    private ActionBarDrawerToggle drawerToggle;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAdicionarDoacao;
    private TextView textViewTitle; // Adicionado para ajuste de margem se necessário

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // CORREÇÃO: Ajuste de Padding para a Barra de Status
        // Em vez de aplicar no drawer_layout (que afeta tudo), vamos aplicar no container principal
        // ou ajustar as margens dos elementos do topo.
        View mainContent = findViewById(R.id.main); // O ConstraintLayout principal dentro do Drawer

        if (mainContent != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContent, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                // Aplica padding no topo para empurrar o conteúdo para baixo da barra de status
                v.setPadding(0, systemBars.top, 0, 0);
                return insets;
            });
        }

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Encontrar Componentes
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton = findViewById(R.id.menuButton);
        textViewTitle = findViewById(R.id.textViewTitle);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fabAdicionarDoacao = findViewById(R.id.fabAdicionarDoacao);

        // Configurar Funções
        setupDrawer();
        setupLogout();
        loadUserData();
        setupFab();
        setupBottomNavigation();
    }

    private void setupFab() {
        fabAdicionarDoacao.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, RegistrarDoacaoActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_painel);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_painel) {
                return true;
            } else if (itemId == R.id.nav_estoque) {
                startActivity(new Intent(DashboardActivity.this, PesquisarEstoqueActivity.class));
                // Não finalizamos o Dashboard para ele ser a "base", mas depende da sua navegação
                return true;
            } else if (itemId == R.id.nav_instituicoes) {
                startActivity(new Intent(DashboardActivity.this, InstituicoesActivity.class));
                return true;
            } else if (itemId == R.id.nav_entregas) {
                startActivity(new Intent(DashboardActivity.this, EntregasActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("usuarios").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nome = documentSnapshot.getString("nomeCompleto");
                    String email = documentSnapshot.getString("email");

                    View headerView = navigationView.getHeaderView(0);
                    TextView navUserName = headerView.findViewById(R.id.navHeaderUserName);
                    TextView navUserEmail = headerView.findViewById(R.id.navHeaderUserEmail);

                    if (navUserName != null) navUserName.setText(nome);
                    if (navUserEmail != null) navUserEmail.setText(email);

                } else {
                    Log.d("Dashboard", "Documento não encontrado.");
                }
            }).addOnFailureListener(e -> {
                Log.e("Dashboard", "Erro ao buscar dados", e);
            });
        }
    }

    private void setupDrawer() {
        // Configuração básica do Drawer (sem alterar a cor do ícone via código, já está no XML)
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        menuButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        });
    }

    private void setupLogout() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_logout) {
                mAuth.signOut();
                Toast.makeText(DashboardActivity.this, "Deslogado.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            // Adicione outros itens do menu lateral aqui se precisar

            drawerLayout.closeDrawer(navigationView);
            return true;
        });
    }
}

