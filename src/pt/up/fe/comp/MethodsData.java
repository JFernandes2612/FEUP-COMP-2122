package pt.up.fe.comp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.utils.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;

public class MethodsData {
    private int numberOfMethods;
    private Map<String, MethodData> methods;

    public MethodsData(Pair<Integer, Map<String, Map<String, Pair<Integer, Object>>>> methods) {
        this.numberOfMethods = methods.getKey();
        this.methods = methods.getValue().entrySet().stream().collect(
                HashMap::new,
                (m, e) -> m.put(e.getKey(), new MethodData(e.getValue())),
                HashMap::putAll);
    }

    public List<String> getMethods() {
        return new ArrayList<>(this.methods.keySet());
    }

    public Type getReturnType(String methodSignature) {
        MethodData methodData = methods.get(methodSignature);
        return methodData != null ? (Type) methodData.getReturnType() : null;
    }

    public List<Symbol> getParameters(String methodSignature) {
        return (List<Symbol>) methods.get(methodSignature).getParameters();
    }

    public List<Symbol> getLocalVariables(String methodSignature) {
        return (List<Symbol>) methods.get(methodSignature).getLocals();
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public Map<String, MethodData> getMethodsMap() {
        return methods;
    }
}
