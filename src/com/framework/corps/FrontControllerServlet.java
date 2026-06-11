package com.framework.corps;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;

public class FrontControllerServlet extends HttpServlet{
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        String uri = req.getRequestURI();
        String url = req.getRequestURL().toString();
        String chemin = req.getServletPath();
        String contexte = req.getContextPath();
        String parametres = req.getQueryString();

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Front Controller - URL Capturée</title></head>");
        out.println("<body>");
        out.println("<h1>URL capturee par le Front Controller !</h1>");
        out.println("<hr/>");
        out.println("<h2>Informations sur l'URL :</h2>");
        out.println("<ul>");
        out.println("<li><strong>URI :</strong> " + uri + "</li>");
        out.println("<li><strong>URL complete :</strong> " + url + "</li>");
        out.println("<li><strong>Chemin :</strong> " + chemin + "</li>");
        out.println("<li><strong>Contexte :</strong> " + contexte + "</li>");
        out.println("<li><strong>Paramètres :</strong> " + (parametres != null ? parametres : "aucune") + "</li>");
        out.println("</ul>");


        System.out.println("url capturee");
        System.out.println("URI: " + uri);
        System.out.println("URL: " + url);
        System.out.println("Chemin: " + chemin);
        System.out.println("Contexte: " + contexte);
        System.out.println("Paramètres: " + parametres);
        system.out.println("Fin de la capture d'URL");

        out.println("<hr/>");
        out.println("<p>Cette page a ete genere par FrontControllerServlet</p>");
        out.println("</body>");
        out.println("</html>");
    }
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        doGet(req, resp);
    }
}

