package application.web.beans;

import java.io.Serializable;
import java.sql.Date;

public class Author implements Serializable {

    private long id;
    private String fio;
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    @Override
    public String toString() {
        return fio;
    }
}
