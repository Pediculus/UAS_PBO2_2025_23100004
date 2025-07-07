/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.mavenproject4;

/**
 *
 * @author ASUS
 */

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;

public class Mavenproject4 extends JFrame {

    private JTable visitTable;
    private DefaultTableModel tableModel;
    
    private JTextField nameField;
    private JTextField nimField;
    private JComboBox<String> studyProgramBox;
    private JComboBox<String> purposeBox;
    private JButton addButton;
    private JButton clearButton;
    private ArrayList<VisitLog> logs;
    
    private boolean actionColumnsAdded = false;

    public Mavenproject4() {
        setTitle("Library Visit Log");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        nameField = new JTextField();
        nimField = new JTextField();
        studyProgramBox = new JComboBox<>(new String[] {"Sistem dan Teknologi Informasi", "Bisnis Digital", "Kewirausahaan"});
        purposeBox = new JComboBox<>(new String[] {"Membaca", "Meminjam/Mengembalikan Buku", "Research", "Belajar"});
        addButton = new JButton("Add");
        clearButton = new JButton("Clear");

        inputPanel.setBorder(BorderFactory.createTitledBorder("Visit Entry Form"));
        inputPanel.add(new JLabel("NIM:"));
        inputPanel.add(nimField);
        inputPanel.add(new JLabel("Name Mahasiswa:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Program Studi:"));
        inputPanel.add(studyProgramBox);
        inputPanel.add(new JLabel("Tujuan Kunjungan:"));
        inputPanel.add(purposeBox);
        inputPanel.add(addButton);
        inputPanel.add(clearButton);

        add(inputPanel, BorderLayout.NORTH);

        String[] columns = {"Waktu Kunjungan", "NIM", "Nama", "Program Studi", "Tujuan Kunjungan"};
        tableModel = new DefaultTableModel(columns, 0);
        visitTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(visitTable);
        add(scrollPane, BorderLayout.CENTER);
        
        getLog();
        
        setVisible(true);
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("control G"), "showActions");

        getRootPane().getActionMap().put("showActions", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!actionColumnsAdded) {
                    addActionColumns();
                    actionColumnsAdded = true;
                }
            }
        });
        
        addButton.addActionListener(e -> {tambahProduct(); });
        
        clearButton.addActionListener(e ->{
            int row = visitTable.getSelectedRow();
            if (row != -1) {
                try {
                    String id = logs.get(row).toString();
                    String query = String.format("mutation {deleteProduct(id: %s)}", id);
                    String jsonRequest = new Gson().toJson(new GraphQLQuery(query));
                    String response = sendGraphQLRequest(jsonRequest);

                    getLog();
                    tabling();
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }            
        });
    }

    private void tambahProduct(){
        try{
            String query = String.format(
                "mutation { addLog(studentName: \"%s\", studentId: \"%s\", studyProgram: \"%s\", purpose: \"%s\"){id studentName}}",
                    nameField.getText(),
                    nimField.getText(),
                    studyProgramBox.getSelectedItem(),
                    purposeBox.getSelectedItem()
            );
            
            String jsonRequest = new Gson().toJson(new GraphQLQuery(query));
            String response = sendGraphQLRequest(jsonRequest);
            getLog();
            tabling();
            
        }catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void addActionColumns() {
        tableModel.addColumn("Action");

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt("Action", i, tableModel.getColumnCount() - 2);
        }

        visitTable.getColumn("Action").setCellRenderer(new ButtonRenderer());

        visitTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox()));
    }
    
    private void getLog(){
        try{
            String query = "query { allLog{ id studentName studentId studyProgram purpose} }";
            String jsonRequest = new Gson().toJson(new GraphQLQuery(query));
            String response = sendGraphQLRequest(jsonRequest);
            
            JsonObject data = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("data");
            JsonArray arr = data.getAsJsonArray("allLog");
            
            LocalDateTime time = LocalDateTime.now();
            
            logs = new ArrayList<>();
            for(JsonElement el : arr){
                JsonObject obj = el.getAsJsonObject();
                VisitLog l = new VisitLog(
                        obj.get("id").getAsInt(),
                        obj.get("studentName").getAsString(),
                        obj.get("studentId").getAsString(),
                        obj.get("studyProgram").getAsString(),
                        obj.get("purpose").getAsString()
                );
                logs.add(l);
                tableModel.addRow(new Object[]{time, l.getStudentId(), l.getStudentName(), l.getStudyProgram(), l.getPurpose()});

            }

        } catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void tabling(){
        logs.clear();
        tableModel.setRowCount(0);
        getLog();
    }
    
    private String sendGraphQLRequest(String json) throws Exception{
        URL url = new URL("http://localhost:4567/graphql");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try(OutputStream os = conn.getOutputStream()){
            os.write(json.getBytes());
        }
        try(BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream()))){
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = reader.readLine())!= null) sb.append(line).append("\n");
            return sb.toString();
        }
    }    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Mavenproject4::new);
    }
    
    class GraphQLQuery{
        String query;
        GraphQLQuery(String query){
            this.query = query;
        }
    }    
}
