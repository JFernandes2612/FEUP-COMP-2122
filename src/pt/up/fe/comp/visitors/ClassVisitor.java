package pt.up.fe.comp.visitors;

import java.util.Map;

import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class ClassVisitor extends PreorderJmmVisitor<Map<String, String>, Boolean> {

    public ClassVisitor() {
        addVisit("ClassDeclaration", this::visitClass);
    }

    private Boolean visitClass(JmmNode classDecl, Map<String, String> classData) {

        String classString = classDecl.get("name");
        classData.put("name", classString);

        try {
            String classExtends = classDecl.get("extends");
            classData.put("extends", classExtends);
        } catch (Exception e) {}

        return true;
    }

}
