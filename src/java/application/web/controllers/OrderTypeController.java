package application.web.controllers;

import application.web.enums.OrderType;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@RequestScoped
public class OrderTypeController {
    
    private static Map<String, OrderType> orderList = new HashMap<String, OrderType>();
    
    public OrderTypeController() {
        ResourceBundle bundle = ResourceBundle.getBundle("application.web.locales.messages", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        orderList.clear();
        orderList.put(bundle.getString("sort_alphabet"), OrderType.NAME);
        orderList.put(bundle.getString("sort_rating"), OrderType.RATING);
    }
    
    public Map<String, OrderType> getOrderList() {
        return orderList;
    }
}
