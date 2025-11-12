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
import java.util.List;

public class ResumoItemAdapter extends RecyclerView.Adapter<ResumoItemAdapter.ViewHolder> {

    private List<DoacaoItem> itemList;
    private Context context;

    // ★ PASSO 1: Criar interface e variável de listener
    private OnItemRemovedListener listener;

    public interface OnItemRemovedListener {
        void onListEmpty();
    }
    // ★ FIM PASSO 1

    // ★ PASSO 2: Modificar construtor
    public ResumoItemAdapter(Context context, List<DoacaoItem> itemList, OnItemRemovedListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener; // Salva o listener
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usa o layout item_resumo_agendamento.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_resumo_agendamento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoacaoItem item = itemList.get(position);

        holder.itemName.setText(item.getNomeItem());
        // Mostra a QUANTIDADE SELECIONADA
        String detalhes = "Qtd: " + item.getQuantidade() + " | Vence em: " + item.getDataValidade();
        holder.itemDetails.setText(detalhes);

        // Ação de deletar (remover da lista temporária)
        holder.deleteButton.setOnClickListener(v -> {
            // Pega a posição correta (necessário caso a lista mude)
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                // Remove o item da lista
                itemList.remove(currentPosition);
                // Notifica o adapter que um item foi removido
                notifyItemRemoved(currentPosition);
                // Notifica o adapter que as posições dos itens mudaram
                notifyItemRangeChanged(currentPosition, itemList.size());

                // ★ PASSO 3: Chamar o listener se a lista estiver vazia
                if (itemList.isEmpty() && listener != null) {
                    listener.onListEmpty();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemDetails;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Encontra os IDs do layout item_resumo_agendamento.xml
            itemName = itemView.findViewById(R.id.itemName);
            itemDetails = itemView.findViewById(R.id.itemDetails);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}