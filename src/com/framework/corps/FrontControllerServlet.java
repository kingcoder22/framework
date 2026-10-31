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

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
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
import mg.itu.framework.File.FileUpload;
import mg.itu.framework.Session.Session;
import com.framework.Authentification.Authentification;
import com.framework.Utilitaire.Constructeur;
import com.framework.Role.*;
@MultipartConfig 
public class FrontControllerServlet extends HttpServlet {
    List<String> controllers = new ArrayList<>();
    Map<UrlMethod, Methode> mappings = new HashMap<>();
    Map<String, Object> controllerss = new HashMap<>();
    boolean isauthentification;
    String viewPrefix;
    String viewSuffix;

    @Override
    public void init() {
        controllers = (List<String>) getServletContext().getAttribute("controllers");
        mappings = (Map<UrlMethod, Methode>) getServletContext().getAttribute("mappings");
        controllerss = (Map<String, Object>) getServletContext().getAttribute("controllerss");

        viewPrefix = (String)getServletContext().getInitParameter("viewPrefix");
        viewSuffix = (String)getServletContext().getInitParameter("viewSuffix");
        isauthentification = (boolean)getServletContext().getAttribute("isauthentification");
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
                int i = 0;
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
                            Constructeur.construireCollectionFromMap(collection, typeElement.getDeclaredConstructor().newInstance(), req);
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
                            Constructeur.construireCollectionFromMap(collection, object, req);
                            System.err.println("Taille de la collection construite : " + collection.size());
                            System.err.println("Type de la collection : " + collection.getClass().getName());
                            System.err.println("Type des éléments de la collection : " + elementClass.getName());
                            args[i] = collection;
                        } else{
                            System.out.println("Argument " + i + ": " + arguments[i].getName() + " is primitive");
                            System.out.println("voici le type de l'argument: " + arguments[i].getType());
                            if(req.getParameterMap().containsKey(arguments[i].getName())) {
                                args[i] = req.getParameter(arguments[i].getName());
                            }
                        }
                    } else if(typeParam == FileUpload.class){
                            Part filePart = req.getPart(arguments[i].getName());
                            if (filePart != null) {
                                String filenom = filePart.getSubmittedFileName();
                                String fileType = filePart.getContentType();
                                long fileSize = filePart.getSize();
                                byte[] fileData = new byte[(int) fileSize];
                                filePart.getInputStream().read(fileData);
                                FileUpload fileUpload = new FileUpload(filenom, fileType, fileSize, fileData);
                                args[i] = fileUpload;
                            }
                    }else if(typeParam == FileUpload[].class){
                            final String nomargument = arguments[i].getName();
                            Part[] fileParts = req.getParts().stream()
                                .filter(part -> part.getName().equals(nomargument))
                                .toArray(Part[]::new);
                            FileUpload[] fileUploads = new FileUpload[fileParts.length];
                            for (int j = 0; j < fileParts.length; j++) {
                                Part filePart = fileParts[j];
                                String filenom = filePart.getSubmittedFileName();
                                String fileType = filePart.getContentType();
                                long fileSize = filePart.getSize();
                                byte[] fileData = new byte[(int) fileSize];
                                filePart.getInputStream().read(fileData);
                                fileUploads[j] = new FileUpload(filenom, fileType, fileSize, fileData);
                            }
                            args[i] = fileUploads;
                    } else if(typeParam == Session.class){
                        Map<String, Session> sessions = (Map<String, Session>) getServletContext().getAttribute("sessions");
                        // verifiena raha misy session efa misy ho an'ny session id an'ny mpampiasa
                        if(!sessions.containsKey(req.getSession().getId())){
                            sessions.put(req.getSession().getId(), new Session(req.getSession()));
                        }
                        Session session = sessions.get(req.getSession().getId());
                        args[i] = session;
                    } else {
                        System.out.println("Argument " + i + ": " + arguments[i].getName() + " is not primitive");
                        System.out.println("voici le type de l'argument: " + arguments[i].getType());
                        Class<?> paramType = arguments[i].getType();
                        Object paramObject = paramType.getDeclaredConstructor().newInstance();
                        // construction anle objet hiparametrena avy amin'ny parametres an'ny request
                        Constructeur.construireObject(paramObject, req);
                        args[i] = paramObject;
                    }
                }
                if(methodereel.isAnnotationPresent(Authentification.class)) {
                    Authentification annotation = methodereel.getAnnotation(Authentification.class);
                    String valSession = annotation.valSession();
                    String redirectTo = annotation.redirectTo();
                    Map<String, Session> sessions = (Map<String, Session>) getServletContext().getAttribute("sessions");
                    if(!sessions.containsKey(req.getSession().getId())){
                        sessions.put(req.getSession().getId(), new Session(req.getSession()));
                    }
                    Session session = sessions.get(req.getSession().getId());
                    if(session.getAttribut(valSession) == null){
                        resp.sendRedirect(contexte + redirectTo);
                        return;
                    }
                    isauthentification = true;
                }
                if(isauthentification){
                    if(methodereel.isAnnotationPresent(RoleAuth.class)){
                        com.framework.Role.RoleAuth annotation = methodereel.getAnnotation(RoleAuth.class);
                        String valSession = annotation.valSession();
                        String[] roles = annotation.roles();
                        Map<String, Session> sessions = (Map<String, Session>) getServletContext().getAttribute("sessions");
                        if(!sessions.containsKey(req.getSession().getId())){
                            sessions.put(req.getSession().getId(), new Session(req.getSession()));
                        }
                        Session session = sessions.get(req.getSession().getId());
                        Object roleObject = session.getAttribut(valSession);
                        if(roleObject == null){
                            // afficher message d'erreur
                            out.println("<p>Vous n'avez pas le rôle requis pour accéder à cette ressource.</p>");
                            return;
                        }
                        String role = roleObject.toString();
                        boolean hasRole = false;
                        for(String r : roles){
                            if(r.equals(role)){
                                hasRole = true;
                                break;
                            }
                        }
                        if(!hasRole){
                            // afficher message d'erreur
                            out.println("<p>Vous n'avez pas le rôle requis pour accéder à cette ressource.</p>");
                            resp.sendRedirect(contexte + "/login");
                            return;
                        }
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
    
}