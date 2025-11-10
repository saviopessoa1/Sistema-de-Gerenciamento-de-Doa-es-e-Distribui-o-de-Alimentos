package com.example.SGDDA.ui;

import android.content.Intent; // Import para Intent
import android.os.Bundle;
import android.view.MenuItem; // Import para item de menu
import android.widget.ImageButton; // Import para o botão de menu

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull; // Import para @NonNull
import androidx.appcompat.app.ActionBarDrawerToggle; // Import para o "hambúrguer"
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat; // Import para fechar o drawer
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout; // Import para o DrawerLayout

import com.example.SGDDA.R;
import com.google.android.material.navigation.NavigationView; // Import para o NavigationView
import com.google.firebase.auth.FirebaseAuth; // Import para o Firebase Auth

public class DashboardActivity extends AppCompatActivity {

    // Declaração dos componentes do Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// ... existing code ...
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> { // ID do XML é drawer_layout
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- INÍCIO DA LÓGICA DO DRAWER E LOGOUT ---

        // 1. Encontrar os componentes
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton = findViewById(R.id.menuButton); // Botão "hambúrguer"

        // 2. Configurar o botão de menu para abrir o drawer
        menuButton.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // 3. Configurar o listener para os cliques nos itens do menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                // Lógica para o item de Logout (nav_logout, ID do drawer_menu.xml)
                if (id == R.id.nav_logout) {
                    // Fazer logout do Firebase
                    FirebaseAuth.getInstance().signOut();

                    // Redirecionar para a tela de Login (MainActivity)
                    Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                    // Limpar o histórico de telas
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Fechar o Dashboard
                }
                // TODO: Adicionar lógica para outros itens do menu (nav_perfil, nav_instituicoes, etc.)

                // Fechar o drawer após o clique
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // --- FIM DA LÓGICA DO DRAWER ---
    }
}