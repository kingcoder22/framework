package mg.itu.framework.Session;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpSession;


public class Session {
    Map<String, Object> sessionMap;
    HttpSession session;

    public Session(HttpSession session){
        sessionMap = new HashMap<>();
        Enumeration<String> attributeNames = session.getAttributeNames();
        while(attributeNames.hasMoreElements()){
            String attributeName = attributeNames.nextElement();
            Object attributeValue = session.getAttribute(attributeName);
            sessionMap.put(attributeName, attributeValue);
        }
        this.session = session;
    }
    public Object getAttribut(String name){
        return sessionMap.get(name);
    }
    public void setAttribut(String name, Object value){
        sessionMap.put(name, value);
    }
    public void removeAttribut(String name){
        sessionMap.remove(name);
    }
    public void commit(){
        java.util.Enumeration<String> attributes = session.getAttributeNames();
        while (attributes.hasMoreElements()) {
            session.removeAttribute(attributes.nextElement());
        }
        for (Map.Entry<String, Object> entry : sessionMap.entrySet()) {
            session.setAttribute(entry.getKey(), entry.getValue());
        }
    }
}