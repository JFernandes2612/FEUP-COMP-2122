package pt.up.fe.comp.visitors;

import java.util.List;
import java.util.stream.Collectors;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class ImportDeclarationVisitor extends PreorderJmmVisitor<List<String>, Boolean> {

    public ImportDeclarationVisitor() {
        addVisit("ImportDeclaration", this::visitImport);
    }

    private Boolean visitImport(JmmNode importDecl, List<String> imports) {

        String importString = importDecl.getChildren().stream()
                .map(id -> id.get("name"))
                .collect(Collectors.joining("."));

        imports.add(importString);

        return true;
    }

}
