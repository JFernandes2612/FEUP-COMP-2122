package pt.up.fe.comp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.utils.Pair;

public class FieldsData {
    private Map<String, Symbol> fields;
    private int numberOfFields;

    public FieldsData(Pair<Integer, Map<String, Symbol>> fields) {
        this.fields = fields.getValue();
        this.numberOfFields = fields.getKey();
    }

    public Map<String, Symbol> getFields() {
        return fields;
    }

    public List<Symbol> getFieldsList() {
        return new ArrayList<Symbol>(this.fields.values());
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }
}
