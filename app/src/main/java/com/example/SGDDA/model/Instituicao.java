package com.example.SGDDA.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable; // Importar

// Adicionar "implements Serializable"
public class Instituicao implements Serializable {

    @Exclude
    private String documentId; // Para guardar o ID do Firestore

    private String nome;
    private String endereco;
    private String urgencia; // ★ CAMPO ADICIONADO DE VOLTA
    private String responsavel;
    private String telefone;

    // Construtor vazio (obrigatório para o Firestore)
    public Instituicao() {}

    // Getters e Setters (obrigatórios para o Firestore)

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    // ★ GETTER E SETTER ADICIONADOS DE VOLTA
    public String getUrgencia() { return urgencia; }
    public void setUrgencia(String urgencia) { this.urgencia = urgencia; }


    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}


