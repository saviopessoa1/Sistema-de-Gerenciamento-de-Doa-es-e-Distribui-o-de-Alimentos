package com.example.SGDDA.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SGDDA.R;
import com.example.SGDDA.model.Notificacao;

import java.util.List;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.ViewHolder> {

    private List<Notificacao> notificacaoList;
    private Context context;

    public NotificacaoAdapter(Context context, List<Notificacao> notificacaoList) {
        this.context = context;
        this.notificacaoList = notificacaoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notificacao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacao notificacao = notificacaoList.get(position);

        holder.textTitulo.setText(notificacao.getTitulo());
        holder.textDescricao.setText(notificacao.getDescricao());
        // holder.textTimestamp.setText(...); // Lógica de "há 5 min" (simplificado por enquanto)

        // Define ícone e cor com base no tipo
        switch (notificacao.getTipo()) {
            case "VENCIMENTO":
                holder.iconNotificacao.setImageResource(R.drawable.ic_warning);
                holder.iconBackground.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.app_accent_red)
                ));
                break;
            case "URGENCIA":
                holder.iconNotificacao.setImageResource(R.drawable.ic_domain);
                holder.iconBackground.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.app_accent_yellow)
                ));
                break;
            case "ENTREGA":
                holder.iconNotificacao.setImageResource(R.drawable.ic_local_shipping);
                holder.iconBackground.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.app_accent_blue)
                ));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return notificacaoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconBackground;
        ImageView iconNotificacao;
        TextView textTitulo, textDescricao, textTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            iconNotificacao = itemView.findViewById(R.id.iconNotificacao);
            textTitulo = itemView.findViewById(R.id.textTitulo);
            textDescricao = itemView.findViewById(R.id.textDescricao);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
        }
    }
}