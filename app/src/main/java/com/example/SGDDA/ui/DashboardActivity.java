package com.example.SGDDA.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView; // Import do TextView
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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Import do FirebaseUser
import com.google.firebase.firestore.DocumentReference; // Import do DocumentReference
import com.google.firebase.firestore.DocumentSnapshot; // Import do DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore; // Import do Firestore

public class DashboardActivity extends AppCompatActivity {

    // Componentes do Layout
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;
    private ActionBarDrawerToggle drawerToggle;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // NOVO: Instância do Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Ajuste de layout (EdgeToEdge)
        View mainView = findViewById(R.id.drawer_layout); // ID do DrawerLayout
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // NOVO: Inicializa o Firestore

        // Encontrar Componentes
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton = findViewById(R.id.menuButton);

        // Configurar Funções
        setupDrawer();
        setupLogout();

        // NOVO: Chamar a função para carregar dados do usuário
        loadUserData();
    }

    // NOVA FUNÇÃO: Carrega os dados do usuário do Firestore
    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("usuarios").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // O documento do usuário foi encontrado
                    String nome = documentSnapshot.getString("nomeCompleto");
                    String email = documentSnapshot.getString("email");

                    // Atualizar o cabeçalho do NavigationView
                    View headerView = navigationView.getHeaderView(0); // Pega o cabeçalho
                    TextView navUserName = headerView.findViewById(R.id.navHeaderUserName);
                    TextView navUserEmail = headerView.findViewById(R.id.navHeaderUserEmail);

                    navUserName.setText(nome);
                    navUserEmail.setText(email);

                } else {
                    // Documento não encontrado (raro, mas pode acontecer)
                    Log.d("Dashboard", "Documento do usuário não encontrado no Firestore.");
                    Toast.makeText(this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                // Falha ao buscar os dados
                Log.e("Dashboard", "Erro ao buscar dados do usuário", e);
                Toast.makeText(this, "Erro de conexão.", Toast.LENGTH_SHORT).show();
            });
        }
    }


    // Função para configurar o Menu Lateral (Drawer)
    private void setupDrawer() {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Lidar com o clique no botão de menu (hamburger)
        menuButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        });
    }

    // Função para configurar o botão de Logout
    private void setupLogout() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_logout) {
                // Usuário clicou em Logout
                mAuth.signOut(); // Desloga do Firebase
                Toast.makeText(DashboardActivity.this, "Deslogado.", Toast.LENGTH_SHORT).show();

                // Envia de volta para a MainActivity (Login)
                Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Fecha o Dashboard
                return true;
            }

            // (Adicione outros 'else if' aqui para outros botões do menu, se necessário)

            drawerLayout.closeDrawer(navigationView); // Fecha o menu
            return true;
        });
    }

    // (Strings necessárias para o ActionBarDrawerToggle, se não existirem)
    // (O Android Studio pode pedir para você criar isso em res/values/strings.xml)
    // <string name="navigation_drawer_open">Open navigation drawer</string>
    // <string name="navigation_drawer_close">Close navigation drawer</string>
}


