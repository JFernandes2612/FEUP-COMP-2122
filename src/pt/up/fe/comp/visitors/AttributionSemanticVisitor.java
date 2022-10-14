package pt.up.fe.comp.visitors;

import java.util.ArrayList;
import java.util.List;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class AttributionSemanticVisitor extends PreorderJmmVisitor<Integer, Type> {
    List<Report> reports = new ArrayList<>();
    SymbolTable symbolTable;

    public AttributionSemanticVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("Attribution", this::visitAttribution);
        addVisit("ArrIndexAttribution", this::visitArrIndexAttribution);
    }

    private Type visitAttribution(JmmNode atribution, Integer dummy){
        Type l = new Type("", false);
        Type r = new Type("", false);

        switch(atribution.getJmmChild(0).getKind()) {
            case "BinOP":{
                BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                l = binOpSemanticVisitor.visit(atribution.getJmmChild(0), 0);
                break;
            }
            case "IntLiteral":
            case "BooleanType":
            case "NewIntArrVarAttribution":
            case "NewClassAttribution":
            case "Id":
            case "ThisExpr":
            case "NotExpr":{
                VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
                l = variableSemanticVisitor.visit(atribution.getJmmChild(0), 0);
                break;
            }
            case "Indexing":
            case "LengthMethod":{
                IndexingSemanticVisitor indexingSemanticVisitor = new IndexingSemanticVisitor(symbolTable);
                l = indexingSemanticVisitor.visit(atribution.getJmmChild(0), 0);
                break;
            }
            default:{
                l = visit(atribution.getJmmChild(0));
                break;
            }
        }

        switch(atribution.getJmmChild(1).getKind()) {
            case "BinOP":{
                BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                r = binOpSemanticVisitor.visit(atribution.getJmmChild(1), 0);
                break;
            }
            case "IntLiteral":
            case "BooleanType":
            case "NewIntArrVarAttribution":
            case "NewClassAttribution":
            case "Id":
            case "ThisExpr":
            case "NotExpr":{
                VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
                r = variableSemanticVisitor.visit(atribution.getJmmChild(1), 0);
                break;
            }
            case "Indexing":
            case "LengthMethod":{
                IndexingSemanticVisitor indexingSemanticVisitor = new IndexingSemanticVisitor(symbolTable);
                r = indexingSemanticVisitor.visit(atribution.getJmmChild(1), 0);
                break;
            }
            case "MethodDeclaration":
            case "MethodCall":{
                MethodSemanticVisitor methodSemanticVisitor = new MethodSemanticVisitor(symbolTable);
                r = methodSemanticVisitor.visit(atribution.getJmmChild(1), 0);
                break;
            }
            default:{
                r = visit(atribution.getJmmChild(1));
                break;
            }
        }
        int line = Integer.valueOf(atribution.getJmmChild(0).get("line"));
        int col = Integer.valueOf(atribution.getJmmChild(0).get("col"));

        if (l.isArray() && !r.isArray() && !atribution.getJmmChild(1).getKind().equals("NewIntArrVarAttribution"))
        {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Arrays are not allowed to be assigned"));
        }

        if(!l.getName().equals(r.getName()) &&
            ( !symbolTable.getImports().contains(l.getName()) || !symbolTable.getImports().contains(r.getName())) &&
            (!l.getName().equals(symbolTable.getSuper()) || !r.getName().equals(symbolTable.getClassName())) &&
            !(l.getName().equals("intArr") && r.getName().equals("int") && r.isArray() == true)){
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error in attribuition: assignee is not compatible with the assigned"));
        }
        return new Type(l.getName(), l.isArray());
    }

    private Type visitArrIndexAttribution(JmmNode atribution, Integer dummy) {
        Type l = new Type("", false);
        Type r = new Type("", false);
        Type v = new Type("", false);

        switch(atribution.getJmmChild(0).getKind()) {
            case "BinOP":{
                BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                l = binOpSemanticVisitor.visit(atribution.getJmmChild(0), 0);
                break;
            }
            case "IntLiteral":
            case "BooleanType":
            case "NewIntArrVarAttribution":
            case "NewClassAttribution":
            case "Id":{
                VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
                l = variableSemanticVisitor.visit(atribution.getJmmChild(0), 0);
                break;
            }
            case "Indexing":
            case "LengthMethod":{
                IndexingSemanticVisitor indexingSemanticVisitor = new IndexingSemanticVisitor(symbolTable);
                l = indexingSemanticVisitor.visit(atribution.getJmmChild(0), 0);
                break;
            }
            default:{
                l = visit(atribution.getJmmChild(0));
                break;
            }
        }

        switch(atribution.getJmmChild(1).getKind()) {
            case "BinOP":{
                BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                r = binOpSemanticVisitor.visit(atribution.getJmmChild(1), 0);
                break;
            }
            case "IntLiteral":
            case "BooleanType":
            case "NewIntArrVarAttribution":
            case "NewClassAttribution":
            case "Id":{
                VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
                r = variableSemanticVisitor.visit(atribution.getJmmChild(1), 0);
                break;
            }
            case "Indexing":
            case "LengthMethod":{
                IndexingSemanticVisitor indexingSemanticVisitor = new IndexingSemanticVisitor(symbolTable);
                r = indexingSemanticVisitor.visit(atribution.getJmmChild(1), 0);
                break;
            }
            case "MethodDeclaration":
            case "MethodCall":{
                MethodSemanticVisitor methodSemanticVisitor = new MethodSemanticVisitor(symbolTable);
                r = methodSemanticVisitor.visit(atribution.getJmmChild(1), 0);
                break;
            }
            default:{
                r = visit(atribution.getJmmChild(1));
                break;
            }
        }

        switch(atribution.getJmmChild(2).getKind()) {
            case "BinOP":{
                BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                v = binOpSemanticVisitor.visit(atribution.getJmmChild(2), 0);
                break;
            }
            case "IntLiteral":
            case "BooleanType":
            case "NewIntArrVarAttribution":
            case "NewClassAttribution":
            case "Id":{
                VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
                v = variableSemanticVisitor.visit(atribution.getJmmChild(2), 0);
                break;
            }
            case "Indexing":
            case "LengthMethod":{
                IndexingSemanticVisitor indexingSemanticVisitor = new IndexingSemanticVisitor(symbolTable);
                v = indexingSemanticVisitor.visit(atribution.getJmmChild(2), 0);
                break;
            }
            case "MethodDeclaration":
            case "MethodCall":{
                MethodSemanticVisitor methodSemanticVisitor = new MethodSemanticVisitor(symbolTable);
                v = methodSemanticVisitor.visit(atribution.getJmmChild(2), 0);
                break;
            }
            default:{
                v = visit(atribution.getJmmChild(2));
                break;
            }
        }

        int line = Integer.valueOf(atribution.getJmmChild(0).get("line"));
        int col = Integer.valueOf(atribution.getJmmChild(0).get("col"));

        if (!l.isArray())
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error in attribuition: assignee is not an array"));
        if (!r.getName().equals("int"))
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error in attribuition: attribution index is not int"));
        if (!v.getName().equals("int"))
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error in attribuition: assigned value is not int"));

        return new Type("int", true);
    }

    public List<Report> getReports() {
        return this.reports;
    }
}
