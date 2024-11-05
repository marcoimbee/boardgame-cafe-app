package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("singleton")
public class ModelBean {

    private Map<String, Object> mapBean = new HashMap<String, Object>();
    public void putBean(String key, Object o) {
        this.mapBean.put(key, o);
    }
    public Object getBean(String key) {
        return mapBean.get(key);
    }
}