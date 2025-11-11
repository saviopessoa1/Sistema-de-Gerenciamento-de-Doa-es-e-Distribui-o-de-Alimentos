package com.example.SGDDA.ui;

import android.content.Intent;
import android.content.res.ColorStateList; // NOVO
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

    // Componentes do Layout
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;
    private ActionBarDrawerToggle drawerToggle;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAdicionarDoacao;
    private TextView textViewTitle; // Adicionado para ajuste de margem se necessário

    // --- NOVOS COMPONENTES DO DASHBOARD ---
    private PieChart pieChartEstoque;
    private TextView textViewAtencao;
    private TextView textViewNaoPerecivelValor;
    private TextView textViewPerecivelValor;
    private TextView textViewDoacoesMes; // (Será o total de itens)
    private LinearLayout layoutProximasEntregas;
    private TextView textViewProximaEntrega1, textViewProximaEntrega2, textViewProximaEntrega3;
    // --- NOVOS TEXTVIEWS PARA HISTÓRICO ---
    private LinearLayout layoutHistoricoContainer;
    private TextView textViewHistorico1, textViewHistorico2, textViewHistorico3;
    // --- FIM DOS NOVOS COMPONENTES ---

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

        // --- ENCONTRAR NOVOS IDs ---
        pieChartEstoque = findViewById(R.id.pieChartEstoque);
        textViewAtencao = findViewById(R.id.textViewAtencao);
        textViewNaoPerecivelValor = findViewById(R.id.textViewNaoPerecivelValor);
        textViewPerecivelValor = findViewById(R.id.textViewPerecivelValor);
        textViewDoacoesMes = findViewById(R.id.textViewDoacoesMes);
        layoutProximasEntregas = findViewById(R.id.layoutProximasEntregas);
        textViewProximaEntrega1 = findViewById(R.id.textViewProximaEntrega1);
        textViewProximaEntrega2 = findViewById(R.id.textViewProximaEntrega2);
        textViewProximaEntrega3 = findViewById(R.id.textViewProximaEntrega3);
        // --- ENCONTRAR NOVOS IDs DE HISTÓRICO ---
        layoutHistoricoContainer = findViewById(R.id.layoutHistoricoContainer);
        textViewHistorico1 = findViewById(R.id.textViewHistorico1);
        textViewHistorico2 = findViewById(R.id.textViewHistorico2);
        textViewHistorico3 = findViewById(R.id.textViewHistorico3);
        // --- FIM DOS NOVOS IDs ---


        // Configurar Funções
        setupDrawer();
        setupLogout();
        loadUserData();
        setupFab();
        setupBottomNavigation();

        // --- NOVAS FUNÇÕES CHAMADAS ---
        setupPieChart();
        loadDashboardData();
        // --- FIM DAS NOVAS FUNÇÕES ---
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

    // --- MÉTODOS NOVOS PARA O DASHBOARD ---

    private void setupPieChart() {
        pieChartEstoque.setUsePercentValues(false);
        pieChartEstoque.getDescription().setEnabled(false);
        pieChartEstoque.setDrawHoleEnabled(true);
        pieChartEstoque.setHoleColor(Color.TRANSPARENT);
        pieChartEstoque.setTransparentCircleRadius(0f);
        pieChartEstoque.getLegend().setEnabled(false); // Desativa a legenda do gráfico
        pieChartEstoque.setTouchEnabled(false); // Desativa toque
        pieChartEstoque.setExtraOffsets(0, 0, 0, 0);

        // --- ESTA É A CORREÇÃO ---
        // Desabilita os rótulos ("Perecível", "Não Perecível") de dentro do gráfico
        pieChartEstoque.setDrawEntryLabels(false);

        // --- NOVA LINHA PARA REMOVER O TEXTO DO CENTRO ---
        pieChartEstoque.setDrawCenterText(false);
        // --- FIM DA NOVA LINHA ---

        // Configuração do texto central (NÃO É MAIS NECESSÁRIA)
        // pieChartEstoque.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        // pieChartEstoque.setCenterTextColor(Color.WHITE);
        // pieChartEstoque.setCenterTextSize(16f);
    }

    private void loadDashboardData() {
        loadEstoqueData();
        loadEntregasData();
        loadHistoricoData(); // <-- CHAMADA DA NOVA FUNÇÃO
    }

    private void loadEstoqueData() {
        db.collection("estoque")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Dashboard", "Erro ao carregar estoque", error);
                        return;
                    }

                    long totalItens = 0;
                    long totalPerecivel = 0;
                    long totalNaoPerecivel = 0;
                    int countVencendo = 0;

                    // Data de hoje + 7 dias
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_YEAR, 7);
                    Date dataLimite = cal.getTime();
                    // Define o formato esperado da data vinda do Firestore
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            DoacaoItem item = doc.toObject(DoacaoItem.class);

                            // 1. Soma Quantidade Total
                            totalItens += item.getQuantidade();

                            // 2. Soma Perecível vs Não Perecível
                            if (item.isPerecivel()) {
                                totalPerecivel += item.getQuantidade();
                            } else {
                                totalNaoPerecivel += item.getQuantidade();
                            }

                            // 3. Verifica Vencimento (apenas para perecíveis)
                            if (item.isPerecivel() && item.getDataValidade() != null) {
                                try {
                                    Date dataValidade = sdf.parse(item.getDataValidade());
                                    // Compara se a data de validade é ANTES da data limite (hoje + 7 dias)
                                    // E se a data de validade é DEPOIS de ontem (para não contar itens já vencidos)
                                    Calendar ontem = Calendar.getInstance();
                                    ontem.add(Calendar.DAY_OF_YEAR, -1);

                                    if (dataValidade != null && dataValidade.before(dataLimite) && dataValidade.after(ontem.getTime())) {
                                        countVencendo++; // Conta por *tipo* de item, não por quantidade
                                    }
                                } catch (ParseException e) {
                                    Log.e("Dashboard", "Erro ao parsear data: " + item.getDataValidade(), e);
                                }
                            }
                        }
                    }

                    // 4. Atualiza a UI
                    textViewAtencao.setText("Atenção: " + countVencendo + " itens vencem nesta semana");
                    textViewNaoPerecivelValor.setText(String.valueOf(totalNaoPerecivel));
                    textViewPerecivelValor.setText(String.valueOf(totalPerecivel));
                    textViewDoacoesMes.setText(String.valueOf(totalItens)); // Usando total de itens aqui

                    updatePieChart(totalNaoPerecivel, totalPerecivel, totalItens);
                });
    }

    private void loadEntregasData() {
        db.collection("entregas")
                .whereIn("status", List.of("Pendente", "Em Coleta"))
                .orderBy("dataEntrega", Query.Direction.ASCENDING)
                .limit(3)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Dashboard", "Erro ao carregar entregas", error);
                        return;
                    }

                    List<Entrega> proximasEntregas = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            proximasEntregas.add(doc.toObject(Entrega.class));
                        }
                    }

                    // Atualiza os TextViews
                    textViewProximaEntrega1.setText(proximasEntregas.size() > 0 ? "• " + proximasEntregas.get(0).getInstituicaoNome() : "Nenhuma entrega pendente");
                    textViewProximaEntrega2.setText(proximasEntregas.size() > 1 ? "• " + proximasEntregas.get(1).getInstituicaoNome() : "");
                    textViewProximaEntrega3.setText(proximasEntregas.size() > 2 ? "• " + proximasEntregas.get(2).getInstituicaoNome() : "");

                    // Esconde os TextViews se estiverem vazios
                    textViewProximaEntrega2.setVisibility(proximasEntregas.size() > 1 ? View.VISIBLE : View.GONE);
                    textViewProximaEntrega3.setVisibility(proximasEntregas.size() > 2 ? View.VISIBLE : View.GONE);
                });
    }

    // --- FUNÇÃO NOVA PARA O HISTÓRICO ---
    private void loadHistoricoData() {
        db.collection("entregas")
                .whereEqualTo("status", "Concluída") // Busca apenas entregas Concluídas
                .orderBy("dataEntrega", Query.Direction.DESCENDING) // Mais recentes primeiro
                .limit(3) // Pega só as 3 últimas
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Dashboard", "Erro ao carregar histórico", error);
                        return;
                    }

                    List<Entrega> historicoEntregas = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            historicoEntregas.add(doc.toObject(Entrega.class));
                        }
                    }

                    // Prepara o ícone de checkmark
                    int checkIcon = R.drawable.ic_menu_save; // Usando o ícone de salvar que já temos
                    int checkColor = ContextCompat.getColor(this, R.color.app_accent_green);

                    // Atualiza os TextViews
                    if (historicoEntregas.size() > 0) {
                        textViewHistorico1.setText(historicoEntregas.get(0).getInstituicaoNome());
                        textViewHistorico1.setCompoundDrawablesWithIntrinsicBounds(checkIcon, 0, 0, 0);
                        textViewHistorico1.setCompoundDrawableTintList(ColorStateList.valueOf(checkColor));
                    } else {
                        textViewHistorico1.setText("Nenhuma entrega concluída");
                        textViewHistorico1.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0); // Remove o ícone
                    }

                    if (historicoEntregas.size() > 1) {
                        textViewHistorico2.setText(historicoEntregas.get(1).getInstituicaoNome());
                        textViewHistorico2.setCompoundDrawablesWithIntrinsicBounds(checkIcon, 0, 0, 0);
                        textViewHistorico2.setCompoundDrawableTintList(ColorStateList.valueOf(checkColor));
                        textViewHistorico2.setVisibility(View.VISIBLE);
                    } else {
                        textViewHistorico2.setVisibility(View.GONE);
                    }

                    if (historicoEntregas.size() > 2) {
                        textViewHistorico3.setText(historicoEntregas.get(2).getInstituicaoNome());
                        textViewHistorico3.setCompoundDrawablesWithIntrinsicBounds(checkIcon, 0, 0, 0);
                        textViewHistorico3.setCompoundDrawableTintList(ColorStateList.valueOf(checkColor));
                        textViewHistorico3.setVisibility(View.VISIBLE);
                    } else {
                        textViewHistorico3.setVisibility(View.GONE);
                    }
                });
    }


    private void updatePieChart(long totalNaoPerecivel, long totalPerecivel, long totalItens) {
        if (totalItens == 0) {
            pieChartEstoque.clear();
            // pieChartEstoque.setCenterText("0\nItens"); // LINHA REMOVIDA
            pieChartEstoque.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(totalNaoPerecivel, "Não Perecível"));
        entries.add(new PieEntry(totalPerecivel, "Perecível"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                ContextCompat.getColor(this, R.color.app_accent_green),
                ContextCompat.getColor(this, R.color.app_accent_blue)
        );
        dataSet.setDrawValues(false); // Não mostra valores no gráfico
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        pieChartEstoque.setData(data);
        // pieChartEstoque.setCenterText(totalItens + "\nItens"); // LINHA REMOVIDA
        pieChartEstoque.invalidate(); // Atualiza o gráfico
    }
    // --- FIM DOS MÉTODOS NOVOS ---
}