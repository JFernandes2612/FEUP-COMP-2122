package pt.up.fe.comp.visitors;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import pt.up.fe.comp.utils.Pair;

public class MethodsVisitor extends PreorderJmmVisitor<Map<String, Map<String, Pair<Integer, Object>>>, Boolean>{

        private int methodCounter = 0;

        public MethodsVisitor() {
            addVisit("MethodDeclaration", this::visitMethods);
        }

        private Boolean visitMethods(JmmNode methodDeclaration, Map<String, Map<String, Pair<Integer, Object>>> methods) {

            Map<String, Pair<Integer, Object>> returnMap = new HashMap<>();

            String methodName = methodDeclaration.get("name");

            String returnTypeName = methodDeclaration.getJmmChild(0).get("type");
            boolean isArray = returnTypeName.contains("Arr");
            if (isArray) {
                returnTypeName = returnTypeName.substring(0, returnTypeName.length() - 3);
            }
            Type returnType = new Type(returnTypeName, isArray);
            returnMap.put("returnType", new Pair<>(-1, returnType));

            JmmNode args = methodDeclaration.getJmmChild(1);
            Map<String, Symbol> argValues = new LinkedHashMap<>();
            int numberOfArgs = 0;

            for (JmmNode arg : args.getChildren()) {
                String argName = arg.get("name");
                String argTypeName = arg.getJmmChild(0).get("type");
                boolean isArrayArgs = argTypeName.contains("Arr");
                if (isArrayArgs) {
                    argTypeName = argTypeName.substring(0, argTypeName.length() - 3);
                }
                Type argType = new Type(argTypeName, isArrayArgs);
                argValues.put(argName, new Symbol(argType, argName));
                numberOfArgs++;
            }

            returnMap.put("args", new Pair<>(numberOfArgs, argValues));

            Map<String, Symbol> locals = new HashMap<>();
            int numberOfLocals = 0;

            JmmNode varDeclarations = methodDeclaration.getJmmChild(2);

            for (JmmNode varDeclaration : varDeclarations.getChildren()) {
                String name = varDeclaration.get("name");
                String type = varDeclaration.getJmmChild(0).get("type");
                boolean isArrayVar = type.endsWith("Arr");
                if (isArrayVar) {
                    type = type.substring(0, type.length() - 3);
                }
                Type varType = new Type(type, isArrayVar);
                locals.put(name, new Symbol(varType, name));
                numberOfLocals++;
            }

            returnMap.put("locals", new Pair<>(numberOfLocals, locals));
            methods.put(methodName, returnMap);
            methodCounter++;

            return true;
        }

        public int getMethodCounter() {
            return methodCounter;
        }

}
