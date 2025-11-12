package com.example.SGDDA.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.SGDDA.R;
import com.example.SGDDA.model.DoacaoItem;
import com.example.SGDDA.model.Entrega;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";
    // ★★★ DEFINA SEU E-MAIL DE ADMIN AQUI ★★★
    private static final String ADMIN_EMAIL = "saviopessoa345@gmail.com"; // Troque pelo seu e-mail

    // Componentes do Layout
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;
    private ActionBarDrawerToggle drawerToggle;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAdicionarDoacao;

    // Componentes do Dashboard
    private PieChart pieChartEstoque;
    private TextView textLegendaNaoPerecivelQtd, textLegendaPerecivelQtd, textAlertaVencimentoQtd;
    private TextView textProximaEntrega1, textProximaEntrega2, textProximaEntrega3;
    private TextView textHistorico1, textHistorico2, textHistorico3; // Para Histórico
    private LinearLayout historicoContainer; // Para Histórico

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Ajuste de Padding
        View mainContent = findViewById(R.id.main);
        if (mainContent != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContent, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, systemBars.top, 0, 0);
                return insets;
            });
        }

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Encontrar Componentes
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton = findViewById(R.id.menuButton);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fabAdicionarDoacao = findViewById(R.id.fabAdicionarDoacao);

        // Componentes do Card (IDs Corrigidos)
        pieChartEstoque = findViewById(R.id.pieChartEstoque);
        textLegendaNaoPerecivelQtd = findViewById(R.id.textViewNaoPerecivelValor);
        textLegendaPerecivelQtd = findViewById(R.id.textViewPerecivelValor);
        textAlertaVencimentoQtd = findViewById(R.id.textViewAtencao);
        textProximaEntrega1 = findViewById(R.id.textViewProximaEntrega1);
        textProximaEntrega2 = findViewById(R.id.textViewProximaEntrega2);
        textProximaEntrega3 = findViewById(R.id.textViewProximaEntrega3);
        textHistorico1 = findViewById(R.id.textViewHistorico1);
        textHistorico2 = findViewById(R.id.textViewHistorico2);
        textHistorico3 = findViewById(R.id.textViewHistorico3);
        historicoContainer = findViewById(R.id.layoutHistoricoContainer);

        // Configurar Funções
        setupDrawer();
        setupBottomNavigation();
        setupFab();
        setupPieChart();

        // Carregar Dados (só se o usuário estiver logado)
        if (currentUser != null) {
            loadUserData(); // Carrega dados do usuário (e verifica se é admin)
            loadEstoqueData(); // Carrega dados do gráfico e alertas
            loadEntregasData(); // Carrega próximas entregas
            loadHistoricoData(); // Carrega histórico
        } else {
            // Se por algum motivo o usuário não estiver logado, volta ao Login
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
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
        if (currentUser == null) return;

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

                // ★★★ LÓGICA DO ADMIN FOI REMOVIDA DAQUI ★★★

            } else {
                Log.d("Dashboard", "Documento do usuário não encontrado.");
            }
        }).addOnFailureListener(e -> {
            Log.e("Dashboard", "Erro ao buscar dados do usuário", e);
        });
    }

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

        // Listener para os cliques nos itens do menu lateral
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_logout) {
                mAuth.signOut();
                Toast.makeText(DashboardActivity.this, "Deslogado.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            // ★★★ LÓGICA DO ADMIN FOI REMOVIDA DAQUI ★★★

            drawerLayout.closeDrawer(navigationView);
            return true;
        });
    }

    // --- Lógica dos Cards do Dashboard ---

    private void setupPieChart() {
        pieChartEstoque.getDescription().setEnabled(false);
        pieChartEstoque.getLegend().setEnabled(false);
        pieChartEstoque.setDrawHoleEnabled(true);
        pieChartEstoque.setHoleColor(Color.TRANSPARENT);
        pieChartEstoque.setTransparentCircleRadius(0f);
        pieChartEstoque.setHoleRadius(75f); // Raio do buraco
        pieChartEstoque.setRotationEnabled(false);
        pieChartEstoque.setTouchEnabled(false);
        pieChartEstoque.setDrawEntryLabels(false); // Remove "Perecível" de dentro
        pieChartEstoque.setDrawCenterText(false); // Remove o texto do centro
    }

    private void loadEstoqueData() {
        db.collection("estoque")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Erro ao carregar estoque", error);
                        return;
                    }

                    int totalItens = 0;
                    int pereciveis = 0;
                    int naoPereciveis = 0;
                    int vencendoEm7Dias = 0;

                    if (value != null) {
                        // Data de hoje + 7 dias para o alerta
                        Calendar cal7Dias = Calendar.getInstance();
                        cal7Dias.add(Calendar.DAY_OF_YEAR, 7);
                        Date dataLimite = cal7Dias.getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                        for (QueryDocumentSnapshot doc : value) {
                            DoacaoItem item = doc.toObject(DoacaoItem.class);
                            int qtd = item.getQuantidade();
                            totalItens += qtd;

                            if (item.isPerecivel()) {
                                pereciveis += qtd;

                                // Tenta verificar a data de validade
                                try {
                                    Date dataValidade = sdf.parse(item.getDataValidade());
                                    if (dataValidade != null && dataValidade.before(dataLimite)) {
                                        vencendoEm7Dias += qtd;
                                    }
                                } catch (ParseException e) {
                                    Log.e(TAG, "Formato de data inválido: " + item.getDataValidade());
                                }
                            } else {
                                naoPereciveis += qtd;
                            }
                        }
                    }

                    // 1. Atualizar o Gráfico
                    ArrayList<PieEntry> entries = new ArrayList<>();
                    entries.add(new PieEntry(naoPereciveis, "Não Perecível"));
                    entries.add(new PieEntry(pereciveis, "Perecível"));

                    ArrayList<Integer> colors = new ArrayList<>();
                    colors.add(ContextCompat.getColor(this, R.color.app_accent_green));
                    colors.add(ContextCompat.getColor(this, R.color.app_accent_blue));

                    PieDataSet dataSet = new PieDataSet(entries, "");
                    dataSet.setColors(colors);
                    dataSet.setDrawValues(false); // Não mostrar valores (ex: 70%) no gráfico

                    PieData data = new PieData(dataSet);
                    pieChartEstoque.setData(data);
                    pieChartEstoque.invalidate(); // Redesenha o gráfico

                    // 2. Atualizar Legendas e Alertas
                    textLegendaNaoPerecivelQtd.setText(String.valueOf(naoPereciveis));
                    textLegendaPerecivelQtd.setText(String.valueOf(pereciveis));
                    // Esta linha depende da string 'alerta_vencimento_dinamico' estar no strings.xml
                    textAlertaVencimentoQtd.setText(getString(R.string.alerta_vencimento_dinamico, vencendoEm7Dias));
                });
    }

    private void loadEntregasData() {
        db.collection("entregas")
                .whereIn("status", List.of("Pendente", "Em Coleta"))
                .orderBy("dataEntrega", Query.Direction.ASCENDING)
                .limit(3)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Erro ao carregar entregas", error);
                        return;
                    }

                    List<Entrega> proximasEntregas = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            proximasEntregas.add(doc.toObject(Entrega.class));
                        }
                    }

                    // Preencher os TextViews
                    textProximaEntrega1.setVisibility(View.GONE);
                    textProximaEntrega2.setVisibility(View.GONE);
                    textProximaEntrega3.setVisibility(View.GONE);

                    if (proximasEntregas.size() > 0) {
                        textProximaEntrega1.setText("• " + proximasEntregas.get(0).getInstituicaoNome());
                        textProximaEntrega1.setVisibility(View.VISIBLE);
                    }
                    if (proximasEntregas.size() > 1) {
                        textProximaEntrega2.setText("• " + proximasEntregas.get(1).getInstituicaoNome());
                        textProximaEntrega2.setVisibility(View.VISIBLE);
                    }
                    if (proximasEntregas.size() > 2) {
                        textProximaEntrega3.setText("• " + proximasEntregas.get(2).getInstituicaoNome());
                        textProximaEntrega3.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void loadHistoricoData() {
        db.collection("entregas")
                .whereEqualTo("status", "Concluída")
                .orderBy("dataEntrega", Query.Direction.DESCENDING) // Mais recentes primeiro
                .limit(3)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Erro ao carregar histórico", error);
                        return;
                    }

                    List<Entrega> historico = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            historico.add(doc.toObject(Entrega.class));
                        }
                    }

                    // Preencher os TextViews
                    textHistorico1.setVisibility(View.GONE);
                    textHistorico2.setVisibility(View.GONE);
                    textHistorico3.setVisibility(View.GONE);

                    if (historico.size() > 0) {
                        textHistorico1.setText(historico.get(0).getInstituicaoNome());
                        textHistorico1.setVisibility(View.VISIBLE);
                    }
                    if (historico.size() > 1) {
                        textHistorico2.setText(historico.get(1).getInstituicaoNome());
                        textHistorico2.setVisibility(View.VISIBLE);
                    }
                    if (historico.size() > 2) {
                        textHistorico3.setText(historico.get(2).getInstituicaoNome());
                        textHistorico3.setVisibility(View.VISIBLE);
                    }

                    // Se não houver histórico, mostra uma mensagem (opcional)
                    if (historico.isEmpty()) {
                        if (historicoContainer != null) historicoContainer.setVisibility(View.GONE);
                    } else {
                        if (historicoContainer != null) historicoContainer.setVisibility(View.VISIBLE);
                    }
                });
    }
}