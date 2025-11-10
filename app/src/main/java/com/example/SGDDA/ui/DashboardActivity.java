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
// Imports NOVOS
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
// Fim dos imports NOVOS
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    // Componentes do Layout
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;
    private ActionBarDrawerToggle drawerToggle;
    private BottomNavigationView bottomNavigationView; // NOVO
    private FloatingActionButton fabAdicionarDoacao; // NOVO

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Ajuste de layout (EdgeToEdge)
        View mainView = findViewById(R.id.drawer_layout);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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
        bottomNavigationView = findViewById(R.id.bottomNavigationView); // NOVO
        fabAdicionarDoacao = findViewById(R.id.fabAdicionarDoacao); // NOVO

        // Configurar Funções
        setupDrawer();
        setupLogout();
        loadUserData();
        setupFab(); // NOVO
        setupBottomNavigation(); // NOVO
    }

    // NOVA FUNÇÃO: Configura o Botão Flutuante (FAB)
    private void setupFab() {
        fabAdicionarDoacao.setOnClickListener(v -> {
            // Abre a tela de Registrar Doação
            Intent intent = new Intent(DashboardActivity.this, RegistrarDoacaoActivity.class);
            startActivity(intent);
        });
    }

    // NOVA FUNÇÃO: Configura a Navegação Inferior
    private void setupBottomNavigation() {
        // Marca o item "Painel" como selecionado ao iniciar
        bottomNavigationView.setSelectedItemId(R.id.nav_painel);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_painel) {
                // Já estamos aqui, não faz nada
                return true;
            } else if (itemId == R.id.nav_estoque) {
                // Abre a tela de Estoque
                Intent intent = new Intent(DashboardActivity.this, PesquisarEstoqueActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_instituicoes) {
                // Abre a tela de Instituições
                Intent intent = new Intent(DashboardActivity.this, InstituicoesActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_entregas) {
                // Abre a tela de Entregas
                Intent intent = new Intent(DashboardActivity.this, EntregasActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    // Função: Carrega os dados do usuário do Firestore
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

                    navUserName.setText(nome);
                    navUserEmail.setText(email);

                } else {
                    Log.d("Dashboard", "Documento do usuário não encontrado no Firestore.");
                    Toast.makeText(this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Log.e("Dashboard", "Erro ao buscar dados do usuário", e);
                Toast.makeText(this, "Erro de conexão.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Função: Configura o Menu Lateral (Drawer)
    private void setupDrawer() {
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

    // Função: Configura o botão de Logout
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

            drawerLayout.closeDrawer(navigationView);
            return true;
        });
    }
}


