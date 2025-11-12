package com.example.SGDDA.model;

import java.util.Comparator;

public class Notificacao {

    private String tipo; // "VENCIMENTO", "URGENCIA", "ENTREGA"
    private String titulo;
    private String descricao;
    private long timestamp;

    public Notificacao(String tipo, String titulo, String descricao, long timestamp) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.descricao = descricao;
        this.timestamp = timestamp;
    }

    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public long getTimestamp() { return timestamp; }

    // Comparador para ordenar do mais novo para o mais antigo
    public static Comparator<Notificacao> TimestampComparator = (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp());
}