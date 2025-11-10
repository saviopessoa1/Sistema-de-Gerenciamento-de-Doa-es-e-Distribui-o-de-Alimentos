package com.example.SGDDA.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.SGDDA.R;
import com.example.SGDDA.model.DoacaoItem;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelecaoEstoqueAdapter extends RecyclerView.Adapter<SelecaoEstoqueAdapter.ViewHolder> {

    private List<DoacaoItem> itemList;
    private Context context;
    // Mapa para guardar a quantidade selecionada de cada item (ID do Documento -> Quantidade)
    private Map<String, Integer> selectedQuantities;

    public SelecaoEstoqueAdapter(Context context, List<DoacaoItem> itemList) {
        this.context = context;
        this.itemList = itemList;
        this.selectedQuantities = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reutiliza o layout item_doacao_registrada.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_doacao_registrada, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoacaoItem item = itemList.get(position);
        String itemId = item.getDocumentId();

        if (itemId == null) return; // Proteção contra itens inválidos

        holder.itemName.setText(item.getNomeItem());
        String detalhes = "Qtd. Disp: " + item.getQuantidade() + " | Vence em: " + item.getDataValidade();
        holder.itemDetails.setText(detalhes);

        // Pega a quantidade selecionada ou 0 se for a primeira vez
        int selectedQty = selectedQuantities.getOrDefault(itemId, 0);
        holder.textQuantidade.setText(String.valueOf(selectedQty));

        // Botão de Adicionar (+)
        holder.btnAdd.setOnClickListener(v -> {
            int currentQty = selectedQuantities.getOrDefault(itemId, 0);
            if (currentQty < item.getQuantidade()) { // Não pode selecionar mais do que o disponível
                currentQty++;
                selectedQuantities.put(itemId, currentQty);
                holder.textQuantidade.setText(String.valueOf(currentQty));
            }
        });

        // Botão de Remover (-)
        holder.btnRemove.setOnClickListener(v -> {
            int currentQty = selectedQuantities.getOrDefault(itemId, 0);
            if (currentQty > 0) { // Não pode ser negativo
                currentQty--;
                selectedQuantities.put(itemId, currentQty);
                holder.textQuantidade.setText(String.valueOf(currentQty));
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // Método para pegar os itens que o usuário selecionou (qtd > 0)
    public List<DoacaoItem> getSelectedItems() {
        List<DoacaoItem> selectedItems = new ArrayList<>();
        for (DoacaoItem item : itemList) {
            String itemId = item.getDocumentId();
            if (itemId == null) continue;

            int selectedQty = selectedQuantities.getOrDefault(itemId, 0);
            if (selectedQty > 0) {
                // Cria um *novo* item com a quantidade selecionada
                DoacaoItem selectedItem = new DoacaoItem(
                        item.getNomeItem(),
                        selectedQty,
                        item.isPerecivel(),
                        item.getDataValidade(),
                        item.getUidUsuario()
                );
                // Guarda o ID do item original para sabermos qual atualizar no estoque
                selectedItem.setDocumentId(item.getDocumentId());
                selectedItems.add(selectedItem);
            }
        }
        return selectedItems;
    }

    // Classe ViewHolder (corrigida)
    public class ViewHolder extends RecyclerView.ViewHolder {
        // 1. Declaração das variáveis
        TextView itemName, itemDetails, textQuantidade;
        ImageButton btnRemove, btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 2. Encontra os IDs do layout item_doacao_registrada.xml
            itemName = itemView.findViewById(R.id.itemName);
            itemDetails = itemView.findViewById(R.id.itemDetails);
            textQuantidade = itemView.findViewById(R.id.textQuantidade); // O "1" no meio
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}


