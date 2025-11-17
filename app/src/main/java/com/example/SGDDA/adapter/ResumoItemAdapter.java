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

    
    private OnItemRemovedListener listener;

    public interface OnItemRemovedListener {
        void onListEmpty();
    }
    

    
    public ResumoItemAdapter(Context context, List<DoacaoItem> itemList, OnItemRemovedListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener; 
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        
        View view = LayoutInflater.from(context).inflate(R.layout.item_resumo_agendamento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoacaoItem item = itemList.get(position);

        holder.itemName.setText(item.getNomeItem());
        
        String detalhes = "Qtd: " + item.getQuantidade() + " | Vence em: " + item.getDataValidade();
        holder.itemDetails.setText(detalhes);

        
        holder.deleteButton.setOnClickListener(v -> {
            
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                
                itemList.remove(currentPosition);
                
                notifyItemRemoved(currentPosition);
                
                notifyItemRangeChanged(currentPosition, itemList.size());

                
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
            
            itemName = itemView.findViewById(R.id.itemName);
            itemDetails = itemView.findViewById(R.id.itemDetails);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}