package com.example.SGDDA.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SGDDA.R;
import com.example.SGDDA.adapter.DetalhesItemAdapter;
import com.example.SGDDA.model.DoacaoItem;
import com.example.SGDDA.model.Entrega;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class RelatoriosActivity extends AppCompatActivity {

    private static final String TAG = "RelatoriosActivity";

    private ImageButton backButton;
    private TextView textDemandasAtendidas, textDesperdicio;
    private BarChart barChartDistribuicao;
    private RecyclerView recyclerVencidos;

    private FirebaseFirestore db;
    private DetalhesItemAdapter vencidosAdapter;
    private List<DoacaoItem> listaVencidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_relatorios);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();

        // Componentes
        backButton = findViewById(R.id.backButton);
        textDemandasAtendidas = findViewById(R.id.textDemandasAtendidas);
        textDesperdicio = findViewById(R.id.textDesperdicio);
        barChartDistribuicao = findViewById(R.id.barChartDistribuicao);
        recyclerVencidos = findViewById(R.id.recyclerVencidos);

        // Configurar Lista de Vencidos
        listaVencidos = new ArrayList<>();
        vencidosAdapter = new DetalhesItemAdapter(this, listaVencidos);
        recyclerVencidos.setLayoutManager(new LinearLayoutManager(this));
        recyclerVencidos.setAdapter(vencidosAdapter);

        backButton.setOnClickListener(v -> finish());

        // Configurações Iniciais do Gráfico
        setupBarChart();

        // Carregar Dados
        carregarDadosDesperdicio();
        carregarDadosDistribuicao();
    }

    private void setupBarChart() {
        barChartDistribuicao.getDescription().setEnabled(false);
        barChartDistribuicao.setDrawGridBackground(false);
        barChartDistribuicao.setDrawBarShadow(false);
        barChartDistribuicao.setDrawBorders(false);
        barChartDistribuicao.getLegend().setEnabled(false);
        barChartDistribuicao.setNoDataText("Carregando dados...");
        barChartDistribuicao.setNoDataTextColor(Color.WHITE);

        // Eixo X (Meses)
        XAxis xAxis = barChartDistribuicao.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(6); // Limita a quantidade de labels para não ficar poluído

        // Eixos Y
        barChartDistribuicao.getAxisLeft().setTextColor(Color.WHITE);
        barChartDistribuicao.getAxisLeft().setAxisMinimum(0f); // Começa do 0
        barChartDistribuicao.getAxisRight().setEnabled(false); // Desativa eixo direito
    }

    private void carregarDadosDesperdicio() {
        // Busca TODOS os itens no estoque
        db.collection("estoque").get().addOnSuccessListener(queryDocumentSnapshots -> {
            listaVencidos.clear();
            int totalItensVencidos = 0;

            Calendar calHoje = Calendar.getInstance();
            // Zera hora para comparar apenas datas
            calHoje.set(Calendar.HOUR_OF_DAY, 0);
            calHoje.set(Calendar.MINUTE, 0);
            calHoje.set(Calendar.SECOND, 0);
            calHoje.set(Calendar.MILLISECOND, 0);
            Date dataHojeZerada = calHoje.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                DoacaoItem item = doc.toObject(DoacaoItem.class);

                if (item.getDataValidade() != null && !item.getDataValidade().isEmpty()) {
                    try {
                        Date validade = sdf.parse(item.getDataValidade());

                        // Se a validade for ANTES de hoje (00:00), significa que venceu ontem ou antes.
                        if (validade != null && validade.before(dataHojeZerada)) {
                            listaVencidos.add(item);
                            totalItensVencidos += item.getQuantidade();
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, "Erro ao analisar data: " + item.getDataValidade());
                    }
                }
            }

            // Atualiza UI
            textDesperdicio.setText(String.valueOf(totalItensVencidos));
            vencidosAdapter.notifyDataSetChanged();

        }).addOnFailureListener(e -> Log.e(TAG, "Erro ao buscar estoque", e));
    }

    private void carregarDadosDistribuicao() {
        db.collection("entregas")
                .whereEqualTo("status", "Concluída")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalAtendimentos = queryDocumentSnapshots.size();
                    textDemandasAtendidas.setText(String.valueOf(totalAtendimentos));

                    // Mapa: "MM/yyyy" -> Quantidade de Itens
                    Map<String, Integer> distribuicaoPorMes = new HashMap<>();
                    // Mapa auxiliar para ordenação: Date -> "MM/yyyy"
                    Map<Date, String> ordemMeses = new TreeMap<>();

                    SimpleDateFormat sdfDoc = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    SimpleDateFormat sdfMes = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                    SimpleDateFormat sdfMesLabel = new SimpleDateFormat("MMM", Locale.getDefault());

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Entrega entrega = doc.toObject(Entrega.class);
                        String dataString = entrega.getDataEntrega();

                        // Trata formato "dd/MM/yyyy às HH:mm"
                        if (dataString != null && dataString.contains(" ")) {
                            dataString = dataString.split(" ")[0];
                        }

                        try {
                            if (dataString != null) {
                                Date data = sdfDoc.parse(dataString);
                                if (data != null) {
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(data);
                                    cal.set(Calendar.DAY_OF_MONTH, 1);
                                    Date mesDate = cal.getTime();

                                    String chaveMes = sdfMes.format(data);

                                    int qtdItensEntrega = 0;
                                    if (entrega.getItens() != null) {
                                        for (DoacaoItem item : entrega.getItens()) {
                                            qtdItensEntrega += item.getQuantidade();
                                        }
                                    }

                                    distribuicaoPorMes.put(chaveMes, distribuicaoPorMes.getOrDefault(chaveMes, 0) + qtdItensEntrega);
                                    ordemMeses.put(mesDate, chaveMes);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Erro data gráfico: " + dataString);
                        }
                    }

                    ArrayList<BarEntry> entries = new ArrayList<>();
                    ArrayList<String> labels = new ArrayList<>();
                    int index = 0;

                    for (Map.Entry<Date, String> entry : ordemMeses.entrySet()) {
                        String chave = entry.getValue();
                        Integer qtd = distribuicaoPorMes.get(chave);

                        entries.add(new BarEntry(index, qtd));
                        labels.add(sdfMesLabel.format(entry.getKey()));
                        index++;
                    }

                    if (!entries.isEmpty()) {
                        BarDataSet dataSet = new BarDataSet(entries, "Itens Distribuídos");
                        dataSet.setColor(ContextCompat.getColor(this, R.color.app_accent_blue));
                        dataSet.setValueTextColor(Color.WHITE);
                        dataSet.setValueTextSize(12f);

                        BarData barData = new BarData(dataSet);
                        // Configura a largura das barras para ficarem bonitas
                        barData.setBarWidth(0.5f);

                        barChartDistribuicao.setData(barData);
                        barChartDistribuicao.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                        barChartDistribuicao.invalidate(); // Redesenha
                        barChartDistribuicao.animateY(1000); // Animação de subida
                    } else {
                        barChartDistribuicao.clear(); // Limpa se não tiver dados
                        barChartDistribuicao.setNoDataText("Sem dados de entregas ainda.");
                    }

                })
                .addOnFailureListener(e -> Log.e(TAG, "Erro ao buscar entregas", e));
    }
}


