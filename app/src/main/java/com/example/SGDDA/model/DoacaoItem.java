package com.example.SGDDA.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable; 


public class DoacaoItem implements Serializable {

    @Exclude
    private String documentId; 
    private String nomeItem;
    private int quantidade;
    private String descricao;
    private boolean perecivel;
    private String dataValidade;
    private String uidUsuario;

    
    public DoacaoItem() {}

    
    public DoacaoItem(String nomeItem, int quantidade, boolean perecivel, String dataValidade, String uidUsuario) {
        this.nomeItem = nomeItem;
        this.quantidade = quantidade;
        this.perecivel = perecivel;
        this.dataValidade = dataValidade;
        this.uidUsuario = uidUsuario;
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

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


