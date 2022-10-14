package pt.up.fe.comp;

import java.util.Map;

public class ClassData {
    private String name;
    private String extend;

    public ClassData(Map<String, String> mapData) {
        this.name = mapData.get("name");
        this.extend = mapData.get("extends");
    }


    public String getClassName() {
        return this.name;
    }

    public String getExtends() {
        return this.extend;
    }

}
