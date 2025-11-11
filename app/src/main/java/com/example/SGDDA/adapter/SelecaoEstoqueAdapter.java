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

    private List<DoacaoItem> itemList; // Lista exibida atualmente (pode ser busca ou selecionados)
    private Context context;
    // Mapa para guardar a quantidade selecionada de cada item (ID do Documento -> Quantidade)
    // Isso garante que a quantidade se mantenha mesmo trocando a lista visual
    private Map<String, Integer> selectedQuantities;

    public SelecaoEstoqueAdapter(Context context, List<DoacaoItem> itemList) {
        this.context = context;
        this.itemList = itemList;
        this.selectedQuantities = new HashMap<>();
    }

    // --- NOVO MÉTODO: Atualiza a lista exibida ---
    public void updateList(List<DoacaoItem> newList) {
        this.itemList = newList;
        notifyDataSetChanged();
    }

    // --- NOVO MÉTODO: Retorna o mapa de seleções para a Activity usar ---
    public Map<String, Integer> getSelectedQuantitiesMap() {
        return selectedQuantities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doacao_registrada, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoacaoItem item = itemList.get(position);
        String itemId = item.getDocumentId();

        if (itemId == null) return;

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
                if (currentQty == 0) {
                    selectedQuantities.remove(itemId); // Remove do mapa se for 0
                } else {
                    selectedQuantities.put(itemId, currentQty);
                }
                holder.textQuantidade.setText(String.valueOf(currentQty));
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public List<DoacaoItem> getSelectedItems() {
        List<DoacaoItem> selectedItems = new ArrayList<>();
        // Aqui precisamos iterar sobre os itens originais para pegar os dados completos
        // Mas como itemList pode estar filtrado, essa lógica precisa ser feita com cuidado na Activity
        // ou passamos a lista completa aqui. Para simplificar, vamos iterar sobre o itemList atual
        // que pode não ter todos os itens se estiver filtrado.
        // CORREÇÃO: O método getSelectedItems deve ser chamado pela Activity usando a lista completa dela
        // Ou podemos iterar sobre a lista atual, mas corremos o risco de perder itens selecionados que não estão na busca.

        // Melhor abordagem: A Activity vai montar a lista final baseada no mapa `selectedQuantities`.
        return selectedItems;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemDetails, textQuantidade;
        ImageButton btnRemove, btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemDetails = itemView.findViewById(R.id.itemDetails);
            textQuantidade = itemView.findViewById(R.id.textQuantidade);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}

