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

public class IndexingSemanticVisitor extends PreorderJmmVisitor<Integer, Type> {
    List<Report> reports = new ArrayList<>();
    SymbolTable symbolTable;

    public IndexingSemanticVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("Indexing", this::visitIndex);
        addVisit("LengthMethod", this::visitLength);
    }

    private Type visitLength(JmmNode lengthNode, Integer dummy) {
        VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
        Type type = variableSemanticVisitor.visit(lengthNode.getJmmChild(0));
        int line = Integer.valueOf(lengthNode.getJmmChild(0).get("line"));
        int col = Integer.valueOf(lengthNode.getJmmChild(0).get("col"));
        if(!type.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error in LengthMethod: " + lengthNode.getJmmChild(0).get("name") + " is not an array"));
            return new Type("none", false);
        }
        return new Type("int", false);
    }

    private Type visitIndex(JmmNode index, Integer dummy) {
        Type l = new Type("", false);
        Type r = new Type("", false);

        switch(index.getJmmChild(0).getKind()) {
            case "BinOP":{
                BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                l = binOpSemanticVisitor.visit(index.getJmmChild(0), 0);
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
                l = variableSemanticVisitor.visit(index.getJmmChild(0), 0);
                break;
            }
            case "MethodDeclaration":
            case "MethodCall":{
                MethodSemanticVisitor methodSemanticVisitor = new MethodSemanticVisitor(symbolTable);
                l = methodSemanticVisitor.visit(index.getJmmChild(0), 0);
                break;
            }
            default:{
                l = visit(index.getJmmChild(0));
                break;
            }
        }

        switch(index.getJmmChild(1).getKind()) {
            case "BinOP":{
                BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                r = binOpSemanticVisitor.visit(index.getJmmChild(1), 0);
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
                r = variableSemanticVisitor.visit(index.getJmmChild(1), 0);
                break;
            }
            case "MethodDeclaration":
            case "MethodCall":{
                MethodSemanticVisitor methodSemanticVisitor = new MethodSemanticVisitor(symbolTable);
                r = methodSemanticVisitor.visit(index.getJmmChild(1), 0);
                break;
            }
            default:{
                r = visit(index.getJmmChild(1));
                break;
            }
        }

        int lineLeft = Integer.valueOf(index.getJmmChild(0).get("line"));
        int colLeft = Integer.valueOf(index.getJmmChild(0).get("col"));
        int lineRight = Integer.valueOf(index.getJmmChild(1).get("line"));
        int colRight= Integer.valueOf(index.getJmmChild(1).get("col"));
        if(!l.isArray() && !l.getName().equals("intArr")){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, lineLeft, colLeft, "Error in Indexing: variable " + index.getJmmChild(0).get("name") + " is not an array"));
        }
        else if(!r.getName().equals("int") || r.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, lineRight, colRight, "Error in Indexing: index value must be of type int"));
        }

        return new Type("int", false);
    }

    public List<Report> getReports() {
        return this.reports;
    }
}
