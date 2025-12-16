package com.example.SGDDA.ui;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.SGDDA.R;
import com.example.SGDDA.model.DoacaoItem;
import com.example.SGDDA.model.Entrega;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RelatoriosActivity extends AppCompatActivity {

    private Button btnExportarPdf;
    private ImageButton backButton;
    private FirebaseFirestore db;
    private static final int REQUEST_CODE_WRITE_STORAGE = 101;

    // Listas para armazenar dados para o PDF
    private List<Entrega> listaEntregasParaPdf = new ArrayList<>();
    private List<DoacaoItem> listaPerdasParaPdf = new ArrayList<>();

    // Variáveis para contagem
    private long countTotalDoacoes = 0;
    private long countTotalEstoque = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorios);

        // Inicializa Views
        btnExportarPdf = findViewById(R.id.btnExportarPdf);
        backButton = findViewById(R.id.backButton);
        db = FirebaseFirestore.getInstance();

        // Configuração do Botão Voltar
        backButton.setOnClickListener(v -> finish());

        // Configuração do Botão PDF
        btnExportarPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verificarPermissao()) {
                    prepararDadosParaPdf();
                }
            }
        });
    }

    // --- Lógica de Geração do PDF ---

    private void prepararDadosParaPdf() {
        Toast.makeText(this, "Gerando relatório PDF...", Toast.LENGTH_SHORT).show();
        btnExportarPdf.setEnabled(false);
        btnExportarPdf.setText("Gerando...");

        Task<QuerySnapshot> taskDoacoes = db.collection("doacoes").get();
        Task<QuerySnapshot> taskEntregas = db.collection("entregas").get();
        Task<QuerySnapshot> taskEstoque = db.collection("estoque").get();

        Tasks.whenAllSuccess(taskDoacoes, taskEntregas, taskEstoque).addOnSuccessListener(results -> {

            // 1. Processar Doações
            QuerySnapshot snapDoacoes = (QuerySnapshot) results.get(0);
            countTotalDoacoes = snapDoacoes.size();

            // 2. Processar Entregas
            QuerySnapshot snapEntregas = (QuerySnapshot) results.get(1);
            listaEntregasParaPdf.clear();
            for (QueryDocumentSnapshot doc : snapEntregas) {
                listaEntregasParaPdf.add(doc.toObject(Entrega.class));
            }

            // 3. Processar Estoque e Perdas
            QuerySnapshot snapEstoque = (QuerySnapshot) results.get(2);
            listaPerdasParaPdf.clear();
            long qtdEstoqueTemp = 0;

            for (QueryDocumentSnapshot doc : snapEstoque) {
                DoacaoItem item = doc.toObject(DoacaoItem.class);
                qtdEstoqueTemp += item.getQuantidade();

                if (isVencido(item.getValidade())) {
                    listaPerdasParaPdf.add(item);
                }
            }
            countTotalEstoque = qtdEstoqueTemp;

            gerarPdfFinal();
            btnExportarPdf.setEnabled(true);
            btnExportarPdf.setText("EXPORTAR PDF");

        }).addOnFailureListener(e -> {
            Toast.makeText(RelatoriosActivity.this, "Erro ao buscar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnExportarPdf.setEnabled(true);
            btnExportarPdf.setText("EXPORTAR PDF");
        });
    }

    private void gerarPdfFinal() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint sectionPaint = new Paint();

        // Configuração A4
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Estilos
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.textSize = 24;
        titlePaint.setColor(Color.BLACK);

        sectionPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        sectionPaint.textSize = 18;
        sectionPaint.setColor(Color.BLUE);

        paint.setTypeface(Typeface.DEFAULT);
        paint.textSize = 12;
        paint.setColor(Color.BLACK);

        int y = 50;
        int x = 20;

        // Cabeçalho PDF
        canvas.drawText("Relatório Geral - SGDDA", x, y, titlePaint);
        y += 30;
        String dataHoje = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText("Gerado em: " + dataHoje, x, y, paint);
        y += 40;

        // Resumo
        canvas.drawText("Resumo Geral", x, y, sectionPaint);
        y += 25;
        paint.setTextSize(14);
        canvas.drawText("- Total Doações Recebidas: " + countTotalDoacoes, x + 10, y, paint);
        y += 20;
        canvas.drawText("- Itens em Estoque (Total): " + countTotalEstoque, x + 10, y, paint);
        y += 20;
        canvas.drawText("- Entregas Realizadas: " + listaEntregasParaPdf.size(), x + 10, y, paint);
        y += 20;
        canvas.drawText("- Itens Vencidos (Perdas): " + listaPerdasParaPdf.size(), x + 10, y, paint);
        y += 40;

        // Tabela Perdas
        canvas.drawText("Detalhamento: Itens Vencidos / Perdas", x, y, sectionPaint);
        y += 25;

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(12);
        canvas.drawText("Item", x, y, paint);
        canvas.drawText("Validade", x + 200, y, paint);
        canvas.drawText("Qtd", x + 350, y, paint);
        paint.setTypeface(Typeface.DEFAULT);
        y += 5;
        canvas.drawLine(x, y, 550, y, paint);
        y += 20;

        if (listaPerdasParaPdf.isEmpty()) {
            canvas.drawText("Nenhum item vencido.", x, y, paint);
            y += 20;
        } else {
            for (DoacaoItem item : listaPerdasParaPdf) {
                if (y > 800) {
                    pdfDocument.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDocument.getPages().size() + 1).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }
                String nome = item.getDescricao() != null ? item.getDescricao() : "Sem descrição";
                String validade = item.getValidade() != null ? item.getValidade() : "--";
                canvas.drawText(nome, x, y, paint);
                canvas.drawText(validade, x + 200, y, paint);
                canvas.drawText(String.valueOf(item.getQuantidade()), x + 350, y, paint);
                y += 20;
            }
        }
        y += 30;

        // Tabela Entregas
        if (y > 750) {
            pdfDocument.finishPage(page);
            pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDocument.getPages().size() + 1).create();
            page = pdfDocument.startPage(pageInfo);
            canvas = page.getCanvas();
            y = 50;
        }

        canvas.drawText("Histórico de Entregas", x, y, sectionPaint);
        y += 25;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Data", x, y, paint);
        canvas.drawText("Status", x + 150, y, paint);
        canvas.drawText("Instituição", x + 300, y, paint);
        paint.setTypeface(Typeface.DEFAULT);
        y += 5;
        canvas.drawLine(x, y, 550, y, paint);
        y += 20;

        if (listaEntregasParaPdf.isEmpty()) {
            canvas.drawText("Nenhuma entrega registrada.", x, y, paint);
        } else {
            for (Entrega entrega : listaEntregasParaPdf) {
                if (y > 800) {
                    pdfDocument.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDocument.getPages().size() + 1).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }
                String dataEnt = entrega.getDataEntrega() != null ? entrega.getDataEntrega() : "--";
                String status = entrega.getStatus() != null ? entrega.getStatus() : "--";
                String inst = entrega.getInstituicaoId() != null ? entrega.getInstituicaoId() : "--";

                canvas.drawText(dataEnt, x, y, paint);
                canvas.drawText(status, x + 150, y, paint);
                canvas.drawText(inst, x + 300, y, paint);
                y += 20;
            }
        }

        pdfDocument.finishPage(page);
        salvarPdf(pdfDocument);
    }

    private void salvarPdf(PdfDocument pdfDocument) {
        String nomeArquivo = "Relatorio_SGDDA_" + System.currentTimeMillis() + ".pdf";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, nomeArquivo);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues);
                if (uri != null) {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    pdfDocument.writeTo(outputStream);
                    if (outputStream != null) outputStream.close();
                    Toast.makeText(this, "PDF salvo em Downloads!", Toast.LENGTH_LONG).show();
                }
            } else {
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                java.io.File file = new java.io.File(path, nomeArquivo);
                pdfDocument.writeTo(new java.io.FileOutputStream(file));
                Toast.makeText(this, "PDF salvo: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }

    private boolean isVencido(String dataValidade) {
        if (dataValidade == null || dataValidade.isEmpty()) return false;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date dataItem = sdf.parse(dataValidade);
            Date hoje = new Date();
            return dataItem != null && dataItem.before(hoje);
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean verificarPermissao() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_WRITE_STORAGE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_STORAGE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            prepararDadosParaPdf();
        }
    }
}