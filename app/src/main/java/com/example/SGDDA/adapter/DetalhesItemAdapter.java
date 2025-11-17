package com.example.SGDDA.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SGDDA.R;
import com.example.SGDDA.model.DoacaoItem;

import java.util.List;



public class DetalhesItemAdapter extends RecyclerView.Adapter<DetalhesItemAdapter.ViewHolder> {

    private List<DoacaoItem> itemList;
    private Context context;

    public DetalhesItemAdapter(Context context, List<DoacaoItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_detalhes_entrega, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoacaoItem item = itemList.get(position);
        holder.itemName.setText(item.getNomeItem());
        holder.itemDetails.setText("Qtd: " + item.getQuantidade() + " | Vence em: " + item.getDataValidade());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemDetails = itemView.findViewById(R.id.itemDetails);
        }
    }
}


