package com.example.SGDDA.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SGDDA.R;
import com.example.SGDDA.model.DoacaoItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelecaoEstoqueAdapter extends RecyclerView.Adapter<SelecaoEstoqueAdapter.ViewHolder> {

    private List<DoacaoItem> itemList;
    private Context context;
    private Map<String, Integer> selectedQuantities;
    private boolean isSearchMode;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DoacaoItem item);
    }

    public SelecaoEstoqueAdapter(Context context, List<DoacaoItem> itemList, OnItemClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.selectedQuantities = new HashMap<>();
        this.listener = listener;
        this.isSearchMode = false;
    }

    public void updateList(List<DoacaoItem> newList, boolean isSearch) {
        this.itemList = newList;
        this.isSearchMode = isSearch;
        notifyDataSetChanged();
    }

    public Map<String, Integer> getSelectedQuantitiesMap() {
        return selectedQuantities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        
        View view = LayoutInflater.from(context).inflate(R.layout.item_selecao_estoque, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoacaoItem item = itemList.get(position);
        String itemId = item.getDocumentId();

        holder.itemName.setText(item.getNomeItem());
        String detalhes = "Disp: " + item.getQuantidade() + " | Vence: " + item.getDataValidade();
        holder.itemDetails.setText(detalhes);

        int currentQty = selectedQuantities.getOrDefault(itemId, 0);

        
        if (currentQty > 0) {
            holder.btnSelecionar.setText("Qtd: " + currentQty);
            
            holder.btnSelecionar.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.app_accent_green)));
            holder.btnSelecionar.setTextColor(ContextCompat.getColor(context, R.color.app_primary_dark));
        } else {
            holder.btnSelecionar.setText("Selecionar");
            
            holder.btnSelecionar.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.app_accent_blue)));
            holder.btnSelecionar.setTextColor(ContextCompat.getColor(context, R.color.white));
        }

        
        holder.btnSelecionar.setOnClickListener(v -> {
            showQuantityDialog(item, itemId, holder);
        });

        
        holder.itemView.setOnClickListener(v -> {
            showQuantityDialog(item, itemId, holder);
        });
    }

    private void showQuantityDialog(DoacaoItem item, String itemId, ViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Quantidade para " + item.getNomeItem());
        builder.setMessage("Disponível no estoque: " + item.getQuantidade());

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        
        int current = selectedQuantities.getOrDefault(itemId, 0);
        input.setText(current > 0 ? String.valueOf(current) : "");
        input.setHint("Digite a quantidade");

        
        android.widget.FrameLayout container = new android.widget.FrameLayout(context);
        android.widget.FrameLayout.LayoutParams params = new  android.widget.FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 50; 
        params.rightMargin = 50;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                try {
                    int newQty = Integer.parseInt(text);
                    if (newQty <= item.getQuantidade() && newQty >= 0) {
                        if (newQty == 0) {
                            selectedQuantities.remove(itemId);
                            
                            if (!isSearchMode) {
                                int pos = holder.getAdapterPosition();
                                if (pos != RecyclerView.NO_POSITION) {
                                    itemList.remove(pos);
                                    notifyItemRemoved(pos);
                                }
                            }
                        } else {
                            selectedQuantities.put(itemId, newQty);
                            
                            if (isSearchMode && listener != null) {
                                listener.onItemClick(item);
                            }
                        }
                        
                        notifyItemChanged(holder.getAdapterPosition());
                    } else {
                        Toast.makeText(context, "Quantidade inválida (Máx: " + item.getQuantidade() + ")", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Número inválido", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        input.requestFocus();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemDetails;
        Button btnSelecionar; 

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemDetails = itemView.findViewById(R.id.itemDetails);
            btnSelecionar = itemView.findViewById(R.id.btnSelecionar);
        }
    }
}