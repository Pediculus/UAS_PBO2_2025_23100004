/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject4;
import java.time.LocalDateTime;
/**
 *
 * @author ITBSS
 */
import java.util.*;

public class VisitRepository {
    private static List<VisitLog> visitList = new ArrayList<>();
    private static int idCounter = 1;

    // Updated method to accept stock
    public static VisitLog add(String name, String studentId, String studyProgram, String purpose) {
        VisitLog logs = new VisitLog(idCounter++, name, studentId, studyProgram, purpose);
        visitList.add(logs);
        return logs;
    }


    public static List<VisitLog> findAll() {
        return visitList;
    }

    public static VisitLog findById(Long id) {
        return visitList.stream().filter(v -> v.id ==(id)).findFirst().orElse(null);
    }

    public static boolean delete(Long id) {
        return visitList.removeIf(v -> v.id == (id));
    }
    
    
}