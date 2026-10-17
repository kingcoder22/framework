package com.framework.corps;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.framework.mapping.Methode;
import mg.itu.framework.mapping.ModelAndView;
import mg.itu.framework.mapping.UrlMethod;
import com.framework.WebApi.WebApi;
import com.google.gson.Gson;
import java.util.Collection;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;

public class FrontControllerServlet extends HttpServlet {
    List<String> controllers = new ArrayList<>();
    Map<UrlMethod, Methode> mappings = new HashMap<>();
    Map<String, Object> controllerss = new HashMap<>();

    String viewPrefix;
    String viewSuffix;

    @Override
    public void init() {
        controllers = (List<String>) getServletContext().getAttribute("controllers");
        mappings = (Map<UrlMethod, Methode>) getServletContext().getAttribute("mappings");
        controllerss = (Map<String, Object>) getServletContext().getAttribute("controllerss");

        viewPrefix = (String)getServletContext().getInitParameter("viewPrefix");
        viewSuffix = (String)getServletContext().getInitParameter("viewSuffix");
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        
        String uri = req.getRequestURI();
        String url = req.getRequestURL().toString();
        String chemin = req.getServletPath();
        String contexte = req.getContextPath();
        String parametres = req.getQueryString();

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        // out.println("<!DOCTYPE html><html><head><title>Front Controller</title></head><body>");
        // out.println("<h1>URL capturee par le Front Controller !</h1><hr/>");
        // out.println("<h2>Informations sur l'URL :</h2><ul>");
        // out.println("<li><strong>URI :</strong> " + uri + "</li>");
        // out.println("<li><strong>URL complete :</strong> " + url + "</li>");
        // out.println("<li><strong>Chemin :</strong> " + chemin + "</li>");
        // out.println("<li><strong>Contexte :</strong> " + contexte + "</li>");
        // out.println("<li><strong>Paramètres :</strong> " + (parametres != null ? parametres : "aucune") + "</li>");
        // out.println("</ul>");

        // out.println("<h1> Liste des controllers</h1><ul>");
        // for (String c : controllers) out.println("<li>" + c + "</li>");
        // out.println("</ul>");

        // System.out.println("URI: " + uri + " | URL: " + url + " | Chemin: " + chemin);

        // out.println("DispatcherType = " + req.getDispatcherType());
        // out.println("URI = " + req.getRequestURI());
        // out.println("Forward URI = " +
        //     req.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI));

        // out.println("<hr/><p>Genere par FrontControllerServlet</p></body></html>");

        String path = uri.substring(contexte.length());
        path = path.replace("/app","");
        UrlMethod urlMethod = new UrlMethod(path, req.getMethod());
        // out.println("path : " + path + "est vrai" +mappings.containsKey(urlMethod));

        for(Map.Entry<UrlMethod, Methode> entry : mappings.entrySet()) {
            UrlMethod key = entry.getKey();
            Methode value = entry.getValue();
            // out.println("<p>URL : " + key.getUrl() + " - Méthode HTTP : " + key.getMethod() + " - Méthode Java : " + value.getMethodName() + "</p>");
        }
        if (mappings.containsKey(urlMethod)) {
            Methode methode = mappings.get(urlMethod);
            String controllerName = methode.getClassName();
            String nom_complet = "controllerpackage" + "." + controllerName;
            Object controller = controllerss.get(controllerName);
            Method methodereel = null;
            try {
                Class<?> clazz = controller.getClass();

                for(Method method : clazz.getDeclaredMethods()){
                    if(method.getName().equals(methode.getMethodName())){
                        methodereel = method;
                    }
                }

                Parameter[] arguments = methodereel.getParameters();
                Object[] args = new Object[arguments.length];
                
                for (int i = 0; i < arguments.length; i++) {
                    Class<?> typeParam = arguments[i].getType();
                    String nomClasse = typeParam.getName();
                    // out.print("Argument " + i + ": " + arguments[i].getName() + "<br>");
                    if (typeParam.isPrimitive() || nomClasse.startsWith("java.") || nomClasse.startsWith("javax.")) {
                        if (typeParam.isArray()) {
                            Class<?> typeElement = typeParam.getComponentType();
                            System.out.println("Type des éléments : "
                                + typeElement.getName());
                            Collection<Object> collection = new ArrayList<>();
                            construireCollectionFromMap(collection, typeElement.getDeclaredConstructor().newInstance(), req);
                            args[i] = collection;

                        } else if (Collection.class.isAssignableFrom(typeParam)) {

                            Type genericType = arguments[i].getParameterizedType();
                            ParameterizedType paramType = (ParameterizedType) genericType;

                            Type typeElement = paramType.getActualTypeArguments()[0];
                            Class<?> elementClass = (Class<?>) typeElement;


                            Object object = elementClass
                                .getDeclaredConstructor()
                                .newInstance();
                            Collection<Object> collection;

                            if(List.class.isAssignableFrom(typeParam)) {
                                collection = new ArrayList<>();
                            } else if(Set.class.isAssignableFrom(typeParam)) {
                                collection = new HashSet<>();
                            } else {
                                collection = (Collection<Object>)
                                    typeParam.getDeclaredConstructor().newInstance();
                            }
                            construireCollectionFromMap(collection, object, req);
                            System.err.println("Taille de la collection construite : " + collection.size());
                            System.err.println("Type de la collection : " + collection.getClass().getName());
                            System.err.println("Type des éléments de la collection : " + elementClass.getName());
                            args[i] = collection;
                        }else {
                            System.out.println("Argument " + i + ": " + arguments[i].getName() + " is primitive");
                            System.out.println("voici le type de l'argument: " + arguments[i].getType());
                            if(req.getParameterMap().containsKey(arguments[i].getName())) {
                                args[i] = req.getParameter(arguments[i].getName());
                            }
                        }
                    } else {
                        System.out.println("Argument " + i + ": " + arguments[i].getName() + " is not primitive");
                        System.out.println("voici le type de l'argument: " + arguments[i].getType());
                        Class<?> paramType = arguments[i].getType();
                        Object paramObject = paramType.getDeclaredConstructor().newInstance();
                        construireObject(paramObject, req);
                        args[i] = paramObject;
                    }
                }
                if(methodereel.isAnnotationPresent(WebApi.class)) {
                    Object result = methodereel.invoke(controller, args);
                    resp.setContentType("application/json;charset=UTF-8");

                    Gson gson = new Gson();
                    String json;

                    if (result instanceof String) {
                        json = (String) result;
                    } else {
                        json = gson.toJson(result);
                    }
                    
                    out.print(json);
                    out.flush();
                    return;
                } else {
                    ModelAndView model = (ModelAndView) methodereel.invoke(controller);
                    
                    for(Map.Entry<String, Object> entry : model.getModel().entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }
                    String cheminview = viewPrefix + "/" + model.getView() + viewSuffix;
                    RequestDispatcher dispatcher = req.getRequestDispatcher(cheminview);
                    dispatcher.forward(req, resp);

                    return;
                }} catch (Exception e) {
                e.printStackTrace();
                out.println("Erreur: " + e.getMessage());
            }
            }
            
            else {
            out.println("<p>URLs disponibles:</p><ul>");
            for (String controllerr : controllers) {
                out.println("<p>Controller : " + controllerr + "</p>");
            }
            for (UrlMethod urls : mappings.keySet()) {
                out.println("<li><strong>URL :</strong> " + urls.getUrl() + " - <strong>Méthode HTTP :</strong> " + urls.getMethod() + " - <strong>Méthode Java :</strong> " + mappings.get(urls).getMethodName() + "</li>");
            }
            out.println("</ul>");
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }
    public void construireObject(Object objet, HttpServletRequest req){
        Class<?> clazz = objet.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for(Field field : fields){
            if(field.getType().isPrimitive() || field.getType().getName().startsWith("java.") || field.getType().getName().startsWith("javax.")){
                String paramName = field.getName();
                if(req.getParameterMap().containsKey(paramName)){
                    String paramValue = req.getParameter(paramName);
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
                    construireObject(newobjet, req);
                    field.setAccessible(true);
                    field.set(objet, newobjet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void construireObjectFromMap(Object objet, Map<String, String[]> paramMap){
        Class<?> clazz = objet.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for(Field field : fields){
            if(field.getType().isPrimitive() || field.getType().getName().startsWith("java.") || field.getType().getName().startsWith("javax.")){
                String paramName = field.getName();
                if(paramMap.containsKey(paramName)){
                    String paramValue = paramMap.get(paramName)[0];
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
    public void construireCollectionFromMap(Collection<Object> collection, Object o, HttpServletRequest req){
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