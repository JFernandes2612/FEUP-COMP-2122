package pt.up.fe.comp.visitors;

import java.util.ArrayList;
import java.util.List;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class VariableSemanticVisitor extends PreorderJmmVisitor<Integer, Type> {
    List<Report> reports = new ArrayList<>();
    SymbolTable symbolTable;

    public VariableSemanticVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("IntLiteral", this::visitIntLiteral);
        addVisit("BooleanType", this::visitBooleanType);
        addVisit("NewIntArrVarAttribution", this::visitNewIntArrVarAttribution);
        addVisit("NewClassAttribution", this::visitNewClassAttribution);
        addVisit("Id", this::visitId);
        addVisit("ThisExpr", this::visitThisExpr);
        addVisit("NotExpr", this::visitNotExpr);
    }

    private Type visitNotExpr(JmmNode visitNotExpr, Integer dummy) {
        Type r = new Type("", false);

        int line = Integer.valueOf(visitNotExpr.get("line"));
        int col = Integer.valueOf(visitNotExpr.get("col"));

        switch(visitNotExpr.getJmmChild(0).getKind()) {
            case "BinOP": {
                BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                r = binOpSemanticVisitor.visit(visitNotExpr.getJmmChild(0),0);
                break;
            }
            case "LengthMethod":
            case "Indexing":{
                IndexingSemanticVisitor indexingSemanticVisitor = new IndexingSemanticVisitor(symbolTable);
                r = indexingSemanticVisitor.visit(visitNotExpr.getJmmChild(0), 0);
                break;
            }
            case "MethodDeclaration":
            case "MethodCall":{
                MethodSemanticVisitor methodSemanticVisitor = new MethodSemanticVisitor(symbolTable);
                r = methodSemanticVisitor.visit(visitNotExpr.getJmmChild(0), 0);
                break;
            }
            default:{
                r = visit(visitNotExpr.getJmmChild(0));
                break;
            }
        }

        if (!r.getName().equals("boolean")) {
            Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Not operation can only be applied to boolean types");
            reports.add(report);
        }

        return new Type("boolean", false);
    }

    private Type visitThisExpr(JmmNode visitThisExpr, Integer dummy) {
        JmmNode parent = visitThisExpr.getJmmParent();
        Integer line = Integer.valueOf(visitThisExpr.get("line"));
        Integer col = Integer.valueOf(visitThisExpr.get("col"));
        while(!parent.getKind().equals("MethodDeclaration") && !parent.getKind().equals("ImportDeclaration")) {
            parent = parent.getJmmParent();
        }
        if (parent.getKind().equals("MethodDeclaration")) {
            if (parent.get("name").equals("main")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col,
                "This keyword cannot be used in main method"));
            }
        }
        return new Type(this.symbolTable.getClassName(), false);
    }

    private Type visitNewClassAttribution(JmmNode newClassAttribution, Integer dummy) {
        return new Type(newClassAttribution.getJmmChild(0).get("name"), false);
    }

    private Type visitNewIntArrVarAttribution(JmmNode newIntArrVarAttribution, Integer dummy) {
        Type varType = null;
        switch(newIntArrVarAttribution.getJmmChild(0).getKind()) {
            case "BinOP": {
                BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                varType = binOpSemanticVisitor.visit(newIntArrVarAttribution.getJmmChild(0),0);
                break;
            }
            case "MethodDeclaration":
            case "MethodCall":{
                MethodSemanticVisitor methodSemanticVisitor = new MethodSemanticVisitor(symbolTable);
                varType = methodSemanticVisitor.visit(newIntArrVarAttribution.getJmmChild(0), 0);
                break;
            }
            case "LengthMethod":
            case "Indexing":{
                IndexingSemanticVisitor indexingSemanticVisitor = new IndexingSemanticVisitor(symbolTable);
                varType = indexingSemanticVisitor.visit(newIntArrVarAttribution.getJmmChild(0), 0);
                break;
            }
            default:{
                varType = visit(newIntArrVarAttribution.getJmmChild(0));
                break;
            }
        }
        Integer line = Integer.valueOf(newIntArrVarAttribution.getJmmChild(0).get("line"));
        Integer col = Integer.valueOf(newIntArrVarAttribution.getJmmChild(0).get("col"));
        if (varType.getName() != "int") {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col,
                    "New array inititalization needs int as size"));
        }
        return new Type("int", true);
    }

    private Type visitId(JmmNode id, Integer dummy) {
        String name = id.get("name");
        int line = Integer.valueOf(id.get("line"));
        int col = Integer.valueOf(id.get("col"));
        JmmNode parent = id.getJmmParent();
        while(!parent.getKind().equals("MethodDeclaration") && !parent.getKind().equals("ImportDeclaration")) {
            parent = parent.getJmmParent();
        }

        if(!parent.getKind().equals("ImportDeclaration")) {
            String method = parent.get("name");
            List<Symbol> locals = symbolTable.getLocalVariables(method);
            for(Symbol local : locals) {
                if(local.getName().equals(name)) {
                    return local.getType();
                }
            }
            List<Symbol> params = symbolTable.getParameters(method);
            for(Symbol param : params) {
                if(param.getName().equals(name)) {
                    return param.getType();
                }
            }
            List<Symbol> fields = symbolTable.getFields();
            for(Symbol field : fields) {
                if(field.getName().equals(name)) {
                    if (parent.get("name").equals("main")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col,
                                "Variable " + name + " is a field and cannot be used in main method"));
                    }
                    return field.getType();
                }
            }
            if((symbolTable.getImports() == null || !symbolTable.getImports().contains(name)) && (symbolTable.getSuper() == null || !symbolTable.getSuper().equals(name)) && !symbolTable.getClassName().equals(name)) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error: variable " + name + " not declared"));
            }
        }
        return new Type("none", false);
    }

    private Type visitIntLiteral(JmmNode intLiteral, Integer dummy) {
        return new Type("int", false);
    }

    private Type visitBooleanType(JmmNode booleanType, Integer dummy) {
        return new Type("boolean", false);
    }

    public List<Report> getReports() {
        return this.reports;
    }
}
