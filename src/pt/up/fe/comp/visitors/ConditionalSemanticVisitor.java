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

public class ConditionalSemanticVisitor extends PreorderJmmVisitor<Integer, Type> {
    List<Report> reports = new ArrayList<>();
    SymbolTable symbolTable;

    public ConditionalSemanticVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("IfStatement", this::visitIfStatement);
        addVisit("WhileStatement", this::visitWhileStatement);
    }

    private Type visitWhileStatement(JmmNode whileStatement, Integer dummy) {
        Type type = new Type("", false);

        switch(whileStatement.getJmmChild(0).getKind()) {
            case "IntLiteral":
            case "BooleanType":
            case "NewIntArrVarAttribution":
            case "NewClassAttribution":
            case "Id":
            case "ThisExpr":
            case "NotExpr":{
                VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
                type = variableSemanticVisitor.visit(whileStatement.getJmmChild(0), 0);
                break;
            }
            case "BinOP":{
                BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                type = binOpSemanticVisitor.visit(whileStatement.getJmmChild(0), 0);
                break;
            }
            case "Indexing":{
                IndexingSemanticVisitor indexingSemanticVisitor = new IndexingSemanticVisitor(symbolTable);
                type = indexingSemanticVisitor.visit(whileStatement.getJmmChild(0), 0);
                break;
            }
            case "MethodDeclaration":
            case "MethodCall":{
                MethodSemanticVisitor methodSemanticVisitor = new MethodSemanticVisitor(symbolTable);
                type = methodSemanticVisitor.visit(whileStatement.getJmmChild(0), 0);
                break;
            }
            default:{
                type = visit(whileStatement.getJmmChild(0));
                break;
            }
        }
        int line = Integer.valueOf(whileStatement.getJmmChild(0).get("line"));
        int col = Integer.valueOf(whileStatement.getJmmChild(0).get("col"));
        if(!type.getName().equals("boolean")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error in WhileStatement: condition has to be of type boolean"));
        }
        else if(type.isArray() || type.getName().equals("intArr")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error in WhileStatement: condition cannot be an array"));
        }
        return type;
    }

    private Type visitIfStatement(JmmNode ifStatement, Integer dummy) {
        Type type = new Type("", false);

        switch(ifStatement.getJmmChild(0).getKind()) {
            case "IntLiteral":
            case "BooleanType":
            case "NewIntArrVarAttribution":
            case "NewClassAttribution":
            case "Id":
            case "ThisExpr":
            case "NotExpr":{
                VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
                type = variableSemanticVisitor.visit(ifStatement.getJmmChild(0), 0);
                break;
            }
            case "BinOP":{
                BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                type = binOpSemanticVisitor.visit(ifStatement.getJmmChild(0), 0);
                break;
            }
            case "Indexing":{
                IndexingSemanticVisitor indexingSemanticVisitor = new IndexingSemanticVisitor(symbolTable);
                type = indexingSemanticVisitor.visit(ifStatement.getJmmChild(0), 0);
                break;
            }
            case "MethodDeclaration":
            case "MethodCall":{
                MethodSemanticVisitor methodSemanticVisitor = new MethodSemanticVisitor(symbolTable);
                type = methodSemanticVisitor.visit(ifStatement.getJmmChild(0), 0);
                break;
            }
            default:{
                type = visit(ifStatement.getJmmChild(0));
                break;
            }
        }
        int line = Integer.valueOf(ifStatement.getJmmChild(0).get("line"));
        int col = Integer.valueOf(ifStatement.getJmmChild(0).get("col"));
        if(!type.getName().equals("boolean")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error in IfStatement: condition has to be of type boolean"));
        }
        else if(type.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error in IfStatement: condition cannot be an array"));
        }
        return type;
    }

    public List<Report> getReports() {
        return this.reports;
    }
}
