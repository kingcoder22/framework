package com.framework.Utilitaire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import mg.itu.framework.File.FileUpload;

public class Constructeur {
    public static void construireObject(Object objet, HttpServletRequest req){
        Class<?> clazz = objet.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for(Field field : fields){
            if(field.getType().isPrimitive() || field.getType().getName().startsWith("java.") || field.getType().getName().startsWith("javax.")){
                String paramName = field.getName();
                String paramFullName = clazz.getSimpleName() + "." + paramName;
                if(req.getParameterMap().containsKey(paramFullName)){
                    String paramValue = req.getParameter(paramFullName);
                    try {
                        field.setAccessible(true);
                        if(field.getType() == int.class){
                            field.setInt(objet, Integer.parseInt(paramValue));
                        } else if(field.getType() == double.class){
                            field.setDouble(objet, Double.parseDouble(paramValue));
                        } else if(field.getType() == boolean.class){
                            field.setBoolean(objet, Boolean.parseBoolean(paramValue));
                        } else {
                            field.set(objet, paramValue);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            } else if(field.getType() == FileUpload.class){
                Part filePart = null;
                String nomComplet = clazz.getSimpleName() + "." + field.getName();
                try {
                    filePart = req.getPart(nomComplet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (filePart != null) {
                    String filenom = filePart.getSubmittedFileName();
                    String fileType = filePart.getContentType();
                    long fileSize = filePart.getSize();
                    byte[] fileData = new byte[(int) fileSize];
                    try {
                        filePart.getInputStream().read(fileData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    FileUpload fileUpload = new FileUpload(filenom, fileType, fileSize, fileData);
                    try {
                        field.setAccessible(true);
                        field.set(objet, fileUpload);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            } else if(field.getType() == FileUpload[].class){
                final String nomargument = clazz.getSimpleName() + "." + field.getName();
                Part[] fileParts = null;
                try {
                    fileParts = req.getParts().stream()
                        .filter(part -> part.getName().equals(nomargument))
                        .toArray(Part[]::new);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FileUpload[] fileUploads = new FileUpload[fileParts.length];
                for (int j = 0; j < fileParts.length; j++) {
                    Part filePart = fileParts[j];
                    String filenom = filePart.getSubmittedFileName();
                    String fileType = filePart.getContentType();
                    long fileSize = filePart.getSize();
                    byte[] fileData = new byte[(int) fileSize];
                    try {
                        filePart.getInputStream().read(fileData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    fileUploads[j] = new FileUpload(filenom, fileType, fileSize, fileData);
                }
                try {
                    field.setAccessible(true);
                    field.set(objet, fileUploads);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Object newobjet = field.getType().getDeclaredConstructor().newInstance();
                    construireObject(newobjet, req);
                    field.setAccessible(true);
                    field.set(objet, newobjet);
                    int i = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void construireObjectFromMap(Object objet, Map<String, String[]> paramMap){
        Class<?> clazz = objet.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for(Field field : fields){
            if(field.getType().isPrimitive() || field.getType().getName().startsWith("java.") || field.getType().getName().startsWith("javax.")){
                String paramName = field.getName();
                String paramFullName = clazz.getSimpleName() + "." + paramName;
                if(paramMap.containsKey(paramFullName)){
                    String paramValue = paramMap.get(paramFullName)[0];
                    try {
                        field.setAccessible(true);
                        if(field.getType() == int.class){
                            field.setInt(objet, Integer.parseInt(paramValue));
                        } else if(field.getType() == double.class){
                            field.setDouble(objet, Double.parseDouble(paramValue));
                        } else if(field.getType() == boolean.class){
                            field.setBoolean(objet, Boolean.parseBoolean(paramValue));
                        } else {
                            field.set(objet, paramValue);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    Object newobjet = field.getType().getDeclaredConstructor().newInstance();
                    construireObjectFromMap(newobjet, paramMap);
                    field.setAccessible(true);
                    field.set(objet, newobjet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void construireCollectionFromMap(Collection<Object> collection, Object o, HttpServletRequest req){
        Enumeration<String> noms = req.getParameterNames();
        int index = 0;
        int taille = 0;
        List<String> listeNoms = new ArrayList<>();
        while (noms.hasMoreElements()) {
            taille++;
            listeNoms.add(noms.nextElement());
        }
        System.err.println("Taille de la collection : " + taille);
        while (index < taille) {
            int indexTemp = 0;
            Object newobjet = null;
            try {
                newobjet = o.getClass().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (String nom: listeNoms) {
                String nomTemp = nom;
                if (nom.endsWith("[]")) {
                    nom = nom.substring(0, nom.length() - 2);
                }
                System.err.println("Nom du paramètre : " + nom);

                String[] valeurs = req.getParameterValues(nomTemp);
                String[] map = {valeurs[indexTemp]};
                System.err.println("Valeur du paramètre : " + map[0]);
                Map<String, String[]> mapTemp = new HashMap<>();
                mapTemp.put(nom, map);
                System.err.println("Taille du tableau de valeurs : " + map.length);
                if(map.length > 0){
                    try {
                        construireObjectFromMap(newobjet, mapTemp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    indexTemp++;
                }
            }
            collection.add(newobjet);
            index++;
    }
    System.err.println("Taille de la collection après construction : " + collection.size());
    }
}
