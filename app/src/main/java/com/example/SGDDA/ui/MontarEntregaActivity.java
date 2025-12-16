package com.example.SGDDA.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; 
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.example.SGDDA.adapter.InstituicaoAdapter;
import com.example.SGDDA.adapter.SelecaoEstoqueAdapter;
import com.example.SGDDA.model.DoacaoItem;
import com.example.SGDDA.model.Instituicao;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.text.ParseException; 
import java.text.SimpleDateFormat; 
import java.util.ArrayList;
import java.util.Calendar; 
import java.util.Date; 
import java.util.List;
import java.util.Locale; 
import java.util.Map;

public class MontarEntregaActivity extends AppCompatActivity implements InstituicaoAdapter.OnInstituicaoClickListener {

    private static final String TAG = "MontarEntregaActivity";

    private ImageButton backButton;
    private TextView textInstituicaoSelecionada;
    private Button btnProximo;
    private EditText searchBar;
    private LinearLayout institutionsContainer;

    
    private RecyclerView recyclerInstituicoes;
    private RecyclerView itensEstoqueRecyclerView;

    private FirebaseFirestore db;

    
    private SelecaoEstoqueAdapter adapterEstoque;
    private InstituicaoAdapter adapterInstituicao;

    
    private List<DoacaoItem> listaEstoqueCompleta;
    private List<DoacaoItem> listaParaAdapterEstoque;
    private List<Instituicao> listaInstituicoes;
    private Instituicao instituicaoSelecionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_montar_entrega);

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
        textInstituicaoSelecionada = findViewById(R.id.textInstituicaoSelecionada);
        btnProximo = findViewById(R.id.btnProximo);
        searchBar = findViewById(R.id.searchBar);
        institutionsContainer = findViewById(R.id.institutionsContainer);

        recyclerInstituicoes = findViewById(R.id.recyclerInstituicoes);
        itensEstoqueRecyclerView = findViewById(R.id.itensEstoqueRecyclerView);

        
        listaEstoqueCompleta = new ArrayList<>();
        listaParaAdapterEstoque = new ArrayList<>();
        listaInstituicoes = new ArrayList<>();

        
        
        adapterInstituicao = new InstituicaoAdapter(this, listaInstituicoes, this, "Selecionar");
        recyclerInstituicoes.setLayoutManager(new LinearLayoutManager(this));
        recyclerInstituicoes.setAdapter(adapterInstituicao);

        
        adapterEstoque = new SelecaoEstoqueAdapter(this, listaParaAdapterEstoque, itemClicado -> {
            searchBar.setText("");
            searchBar.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
            }
            institutionsContainer.setVisibility(View.VISIBLE);
        });
        itensEstoqueRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itensEstoqueRecyclerView.setAdapter(adapterEstoque);

        
        loadInstituicoes();
        loadEstoque(); 

        setupListeners();
        setupSearch();
    }

    private void loadInstituicoes() {
        db.collection("instituicoes")
                .orderBy("urgencia", Query.Direction.ASCENDING) 
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    listaInstituicoes.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Instituicao inst = doc.toObject(Instituicao.class);
                            inst.setDocumentId(doc.getId());
                            listaInstituicoes.add(inst);
                        }
                    }
                    adapterInstituicao.notifyDataSetChanged();
                });
    }

    
    private void loadEstoque() {
        db.collection("estoque")
                .orderBy("dataValidade", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Erro ao carregar estoque", error);
                        return;
                    }

                    listaEstoqueCompleta.clear();

                    
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Calendar calHoje = Calendar.getInstance();
                    calHoje.set(Calendar.HOUR_OF_DAY, 0);
                    calHoje.set(Calendar.MINUTE, 0);
                    calHoje.set(Calendar.SECOND, 0);
                    calHoje.set(Calendar.MILLISECOND, 0);
                    Date dataHojeZerada = calHoje.getTime(); 

                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            DoacaoItem item = doc.toObject(DoacaoItem.class);
                            item.setDocumentId(doc.getId());

                            
                            if (item.getDataValidade() == null || item.getDataValidade().isEmpty()) {
                                
                                listaEstoqueCompleta.add(item);
                                continue;
                            }

                            try {
                                Date validade = sdf.parse(item.getDataValidade());

                                
                                if (validade != null && !validade.before(dataHojeZerada)) {
                                    listaEstoqueCompleta.add(item);
                                } else {
                                    
                                    Log.d(TAG, "Item VENCIDO filtrado: " + item.getNomeItem() + " (Vence: " + item.getDataValidade() + ")");
                                }
                            } catch (ParseException e) {
                                Log.e(TAG, "Formato de data inválido, item ignorado: " + item.getNomeItem());
                            }
                            
                        }
                        atualizarListaVisual(searchBar.getText().toString());
                    }
                });
    }

    
    @Override
    public void onInstituicaoClick(Instituicao instituicao) {
        this.instituicaoSelecionada = instituicao;

        
        textInstituicaoSelecionada.setText(instituicao.getNome());

        
        adapterInstituicao.setSelectedId(instituicao.getDocumentId());
    }

    private void setupSearch() {
        searchBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                institutionsContainer.setVisibility(View.GONE);
            } else if (searchBar.getText().toString().isEmpty()) {
                institutionsContainer.setVisibility(View.VISIBLE);
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                atualizarListaVisual(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void atualizarListaVisual(String query) {
        listaParaAdapterEstoque.clear();
        boolean isSearch = !query.isEmpty();

        if (isSearch) {
            String lowerQuery = query.toLowerCase();
            for (DoacaoItem item : listaEstoqueCompleta) {
                if (item.getNomeItem().toLowerCase().contains(lowerQuery)) {
                    listaParaAdapterEstoque.add(item);
                }
            }
        } else {
            Map<String, Integer> selecionados = adapterEstoque.getSelectedQuantitiesMap();
            for (DoacaoItem item : listaEstoqueCompleta) {
                if (selecionados.containsKey(item.getDocumentId()) && selecionados.get(item.getDocumentId()) > 0) {
                    listaParaAdapterEstoque.add(item);
                }
            }
            if (!searchBar.hasFocus()) {
                institutionsContainer.setVisibility(View.VISIBLE);
            }
        }
        adapterEstoque.updateList(listaParaAdapterEstoque, isSearch);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        btnProximo.setOnClickListener(v -> irParaResumo());
    }

    private void irParaResumo() {
        List<DoacaoItem> itensSelecionadosParaEnvio = new ArrayList<>();
        Map<String, Integer> mapSelecionados = adapterEstoque.getSelectedQuantitiesMap();

        for (DoacaoItem itemOriginal : listaEstoqueCompleta) {
            String id = itemOriginal.getDocumentId();
            if (mapSelecionados.containsKey(id)) {
                int qtd = mapSelecionados.get(id);
                if (qtd > 0) {
                    DoacaoItem itemParaEnvio = new DoacaoItem(
                            itemOriginal.getNomeItem(),
                            qtd,
                            itemOriginal.isPerecivel(),
                            itemOriginal.getDataValidade(),
                            itemOriginal.getUidUsuario());
                    itemParaEnvio.setDocumentId(id);
                    itensSelecionadosParaEnvio.add(itemParaEnvio);
                }
            }
        }

        if (instituicaoSelecionada == null) {
            Toast.makeText(this, "Selecione uma instituição.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (itensSelecionadosParaEnvio.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos um item.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ResumoAgendamentoActivity.class);
        intent.putExtra("INSTITUICAO_SELECIONADA", instituicaoSelecionada);
        intent.putExtra("ITENS_SELECIONADOS", (Serializable) itensSelecionadosParaEnvio);
        startActivity(intent);
    }
}