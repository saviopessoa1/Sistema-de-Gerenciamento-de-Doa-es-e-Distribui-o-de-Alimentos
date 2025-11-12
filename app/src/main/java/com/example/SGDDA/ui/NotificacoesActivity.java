package com.example.SGDDA.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.SGDDA.adapter.NotificacaoAdapter;
import com.example.SGDDA.model.DoacaoItem;
import com.example.SGDDA.model.Entrega;
import com.example.SGDDA.model.Instituicao;
import com.example.SGDDA.model.Notificacao;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificacoesActivity extends AppCompatActivity {

    private static final String TAG = "NotificacoesActivity";

    private ImageButton backButton;
    private RecyclerView recyclerViewNotificacoes;
    private TextView textEmpty;

    private FirebaseFirestore db;
    private NotificacaoAdapter adapter;
    private List<Notificacao> listaNotificacoes;

    // Contadores para saber quando todas as buscas terminaram
    private int buscasPendentes = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notificacoes);

        // Ajuste de layout (EdgeToEdge)
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();

        // Encontrar Componentes
        backButton = findViewById(R.id.backButton);
        recyclerViewNotificacoes = findViewById(R.id.recyclerViewNotificacoes);
        textEmpty = findViewById(R.id.textEmpty);

        // Configurar RecyclerView
        listaNotificacoes = new ArrayList<>();
        adapter = new NotificacaoAdapter(this, listaNotificacoes);
        recyclerViewNotificacoes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotificacoes.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());

        // Carregar dados
        carregarTodasNotificacoes();
    }

    private void carregarTodasNotificacoes() {
        listaNotificacoes.clear();
        buscasPendentes = 3; // Reseta o contador

        gerarNotificacoesVencimento();
        gerarNotificacoesUrgencia();
        gerarNotificacoesEntregasAmanha();
    }

    // Função chamada após CADA busca no Firebase
    private synchronized void onBuscaConcluida() {
        buscasPendentes--;
        if (buscasPendentes == 0) {
            // Todas as buscas terminaram, hora de atualizar o adapter

            // 1. Ordenar a lista (mais novo primeiro)
            Collections.sort(listaNotificacoes, Notificacao.TimestampComparator);

            // 2. Atualizar a UI
            adapter.notifyDataSetChanged();

            // 3. Mostrar/Esconder mensagem de "vazio"
            if (listaNotificacoes.isEmpty()) {
                textEmpty.setVisibility(View.VISIBLE);
                recyclerViewNotificacoes.setVisibility(View.GONE);
            } else {
                textEmpty.setVisibility(View.GONE);
                recyclerViewNotificacoes.setVisibility(View.VISIBLE);
            }
        }
    }

    // BUSCA 1: Itens vencendo esta semana
    private void gerarNotificacoesVencimento() {
        Calendar calHoje = Calendar.getInstance();
        Calendar cal7Dias = Calendar.getInstance();
        cal7Dias.add(Calendar.DAY_OF_YEAR, 7);
        Date hoje = calHoje.getTime();
        Date dataLimite = cal7Dias.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        db.collection("estoque").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int itensVencendo = 0;
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    DoacaoItem item = doc.toObject(DoacaoItem.class);
                    if (item.getDataValidade() != null && !item.getDataValidade().isEmpty()) {
                        try {
                            Date validade = sdf.parse(item.getDataValidade());
                            // Se vence DEPOIS de hoje E ANTES de 7 dias
                            if (validade != null && validade.after(hoje) && validade.before(dataLimite)) {
                                itensVencendo += item.getQuantidade();
                            }
                        } catch (ParseException e) {
                            Log.e(TAG, "Formato de data inválido: " + item.getDataValidade());
                        }
                    }
                }

                if (itensVencendo > 0) {
                    String desc = (itensVencendo == 1) ? "1 item vence esta semana." : itensVencendo + " itens vencem esta semana.";
                    listaNotificacoes.add(new Notificacao(
                            "VENCIMENTO",
                            "Alerta de Vencimento",
                            desc,
                            System.currentTimeMillis() - 1000 // (Prioridade alta)
                    ));
                }
            } else {
                Log.e(TAG, "Erro ao buscar estoque p/ vencimento", task.getException());
            }
            onBuscaConcluida(); // Marca esta busca como concluída
        });
    }

    // BUSCA 2: Instituições com urgência "Alta"
    private void gerarNotificacoesUrgencia() {
        db.collection("instituicoes")
                .whereEqualTo("urgencia", "Alta")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Instituicao inst = doc.toObject(Instituicao.class);
                            listaNotificacoes.add(new Notificacao(
                                    "URGENCIA",
                                    "Urgência Alta",
                                    inst.getNome() + " está com prioridade alta de recebimento.",
                                    System.currentTimeMillis() - 2000 // (Prioridade média)
                            ));
                        }
                    } else {
                        Log.e(TAG, "Erro ao buscar instituições", task.getException());
                    }
                    onBuscaConcluida(); // Marca esta busca como concluída
                });
    }

    // BUSCA 3: Entregas agendadas para amanhã
    private void gerarNotificacoesEntregasAmanha() {
        // Pega a data de amanhã formatada
        Calendar c1 = Calendar.getInstance();
        c1.add(Calendar.DAY_OF_YEAR, 1);
        String amanhaStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(c1.getTime());

        db.collection("entregas")
                .whereIn("status", Arrays.asList("Pendente", "Em Coleta"))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Entrega entrega = doc.toObject(Entrega.class);
                            String dataEntrega = entrega.getDataEntrega();

                            // Extrai a data (ex: "13/11/2025") do texto (ex: "13/11/2025 às 14:00")
                            if (dataEntrega != null && dataEntrega.contains(" ")) {
                                dataEntrega = dataEntrega.split(" ")[0];
                            }

                            if (amanhaStr.equals(dataEntrega)) {
                                listaNotificacoes.add(new Notificacao(
                                        "ENTREGA",
                                        "Entrega Amanhã",
                                        "Entrega para " + entrega.getInstituicaoNome() + " agendada para amanhã.",
                                        System.currentTimeMillis() - 3000 // (Prioridade baixa)
                                ));
                            }
                        }
                    } else {
                        Log.e(TAG, "Erro ao buscar entregas", task.getException());
                    }
                    onBuscaConcluida(); // Marca esta busca como concluída
                });
    }
}