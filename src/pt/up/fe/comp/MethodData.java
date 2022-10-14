package pt.up.fe.comp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.utils.Pair;

public class MethodData {
    private Type returnType;
    private Map<String, Symbol> parameters;
    private int numberOfParameters;
    private Map<String, Symbol> locals;
    private int numberOfLocals;

    public MethodData(Map<String, Pair<Integer, Object>> methodData) {
        this.returnType = (Type) methodData.get("returnType").getValue();
        this.numberOfParameters = methodData.get("args").getKey();
        this.parameters = (LinkedHashMap<String, Symbol>) methodData.get("args").getValue();
        this.numberOfLocals = methodData.get("locals").getKey();
        this.locals = (Map<String, Symbol>) methodData.get("locals").getValue();
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Symbol> getParameters() {
        return new ArrayList<>(parameters.values());
    }

    public List<Symbol> getLocals() {
        return new ArrayList<>(locals.values());
    }

    public int getNumberOfLocals() {
        return numberOfLocals;
    }

    public int getNumberOfParameters() {
        return numberOfParameters;
    }
}
