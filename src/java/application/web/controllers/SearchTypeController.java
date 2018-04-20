package application.web.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import application.web.enums.SearchType;

@ManagedBean
@RequestScoped
public class SearchTypeController {

    private static Map<String, SearchType> searchList = new HashMap<String, SearchType>(); // хранит все виды поисков

    public SearchTypeController() {
        ResourceBundle bundle = ResourceBundle.getBundle("application.web.locales.messages", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        searchList.clear();
        searchList.put(bundle.getString("search_author"), SearchType.AUTHOR);
        searchList.put(bundle.getString("search_name"), SearchType.BOOK_NAME);
        searchList.put(bundle.getString("search_publisher"), SearchType.PUBLISHER);
        searchList.put(bundle.getString("search_publishing_year"), SearchType.PUBLISHING_YEAR);
        searchList.put(bundle.getString("search_extension"), SearchType.EXTENSION);
    }

    public Map<String, SearchType> getSearchList() {
        return searchList;
    }
}
