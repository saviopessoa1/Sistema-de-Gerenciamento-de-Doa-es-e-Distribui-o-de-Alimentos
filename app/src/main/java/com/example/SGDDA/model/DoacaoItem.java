package com.example.SGDDA.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable; // Importar

// Adicionar "implements Serializable"
public class DoacaoItem implements Serializable {

    @Exclude
    private String documentId; // Para guardar o ID do Firestore
    private String nomeItem;
    private int quantidade;
    private boolean perecivel;
    private String dataValidade;
    private String uidUsuario;

    // Construtor vazio (Necessário para o Firestore)
    public DoacaoItem() {}

    // Construtor usado no app
    public DoacaoItem(String nomeItem, int quantidade, boolean perecivel, String dataValidade, String uidUsuario) {
        this.nomeItem = nomeItem;
        this.quantidade = quantidade;
        this.perecivel = perecivel;
        this.dataValidade = dataValidade;
        this.uidUsuario = uidUsuario;
    }

    // --- Getters e Setters ---
    // (O Firestore precisa deles para funcionar)

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getNomeItem() { return nomeItem; }
    public void setNomeItem(String nomeItem) { this.nomeItem = nomeItem; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public boolean isPerecivel() { return perecivel; }
    public void setPerecivel(boolean perecivel) { this.perecivel = perecivel; }

    public String getDataValidade() { return dataValidade; }
    public void setDataValidade(String dataValidade) { this.dataValidade = dataValidade; }

    public String getUidUsuario() { return uidUsuario; }
    public void setUidUsuario(String uidUsuario) { this.uidUsuario = uidUsuario; }
}


