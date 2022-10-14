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

public class BinOpSemanticVisitor extends PreorderJmmVisitor<Integer, Type>{
    List<Report> reports = new ArrayList<>();
    SymbolTable symbolTable;

    public BinOpSemanticVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("BinOP", this::visitBinOp);
    }

    private Type visitBinOp(JmmNode binOp, Integer dummy){
        String op = binOp.get("op");
        Type l = new Type("", false);
        Type r = new Type("", false);

        switch(binOp.getJmmChild(0).getKind()) {
            case "IntLiteral":
            case "BooleanType":
            case "NewIntArrVarAttribution":
            case "NewClassAttribution":
            case "Id":
            case "ThisExpr":
            case "NotExpr":{
                VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
                l = variableSemanticVisitor.visit(binOp.getJmmChild(0), 0);
                break;
            }
            case "LengthMethod":
            case "Indexing":{
                IndexingSemanticVisitor indexingSemanticVisitor = new IndexingSemanticVisitor(symbolTable);
                l = indexingSemanticVisitor.visit(binOp.getJmmChild(0), 0);
                break;
            }
            case "MethodDeclaration":
            case "MethodCall":{
                MethodSemanticVisitor methodSemanticVisitor = new MethodSemanticVisitor(symbolTable);
                l = methodSemanticVisitor.visit(binOp.getJmmChild(0), 0);
                break;
            }
            default:{
                l = visit(binOp.getJmmChild(0));
                break;
            }
        }

        switch(binOp.getJmmChild(1).getKind()) {
            case "IntLiteral":
            case "BooleanType":
            case "NewIntArrVarAttribution":
            case "NewClassAttribution":
            case "Id":
            case "ThisExpr":
            case "NotExpr":{
                VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
                r = variableSemanticVisitor.visit(binOp.getJmmChild(1), 0);
                break;
            }
            case "LengthMethod":
            case "Indexing":{
                IndexingSemanticVisitor indexingSemanticVisitor = new IndexingSemanticVisitor(symbolTable);
                r = indexingSemanticVisitor.visit(binOp.getJmmChild(1), 0);
                break;
            }
            case "MethodDeclaration":
            case "MethodCall":{
                MethodSemanticVisitor methodSemanticVisitor = new MethodSemanticVisitor(symbolTable);
                r = methodSemanticVisitor.visit(binOp.getJmmChild(1), 0);
                break;
            }
            default:{
                r = visit(binOp.getJmmChild(1));
                break;
            }
        }

        int lineLeft = Integer.valueOf(binOp.getJmmChild(0).get("line"));
        int colLeft = Integer.valueOf(binOp.getJmmChild(0).get("col"));
        int lineRight = Integer.valueOf(binOp.getJmmChild(1).get("line"));
        int colRight = Integer.valueOf(binOp.getJmmChild(1).get("col"));

        if(!l.getName().equals(r.getName())){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, lineRight, colRight, "Error in operation " + op + " : operands have different types"));
        }
        else if( ( l.isArray() || r.isArray() ) && ( op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") || op.equals("<") ) ) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, lineLeft, colLeft, "Error in operation " + op + " : array cannot be used in this operation"));
        }
        else if(!l.getName().equals("int") && ( op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") || op.equals("<") ) ) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, lineLeft, colLeft, "Error in operation " + op + " : operands have invalid types for this operation. " + op + " expects operands of type integer"));
        }
        else if(!l.getName().equals("boolean") && ( op.equals("&&") || op.equals("!") )) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, lineLeft, lineLeft, "Error in operation "+ op + " : operands have invalid types for this operation. " + op + " expects operands of type boolean"));
        }
        else {
            switch(op) {
                case "<":
                case "&&":
                    return new Type("boolean", false);
                case "+":
                case "-":
                case "/":
                case "*":
                    return new Type("int", false);
                default:
                    return new Type(l.getName(), l.isArray());
            }
        }
        return new Type(l.getName(), l.isArray());
    }

    public List<Report> getReports() {
        return this.reports;
    }
}
