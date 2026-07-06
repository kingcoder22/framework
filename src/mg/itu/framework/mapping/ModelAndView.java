package mg.itu.framework.mapping;
import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    String view;
    Map<String, Object> model;

    public ModelAndView(String view, Map<String, Object> model) {
        this.view = view;
        this.model = model;
    }
    public ModelAndView(){
        this.model = new HashMap<>();
    }
    public String getView() {
        return view;
    }
    public Map<String, Object> getModel() {
        return model;
    }
    public void setView(String view) {
        this.view = view;
    }
    public void setAttribute(String key, Object value) {
        model.put(key, value);
    }
}
