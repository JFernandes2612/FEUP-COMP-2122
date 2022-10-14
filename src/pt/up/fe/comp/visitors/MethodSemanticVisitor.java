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

public class MethodSemanticVisitor extends PreorderJmmVisitor<Integer, Type> {
    List<Report> reports = new ArrayList<>();
    SymbolTable symbolTable;

    public MethodSemanticVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("MethodCall", this::visitMethodCall);
        addVisit("MethodDeclaration", this::visitMethodDeclaration);
    }

    private Type visitMethodDeclaration(JmmNode methodDeclaration, Integer dummy) {
        int numOfChildren = methodDeclaration.getChildren().size() - 1;
        if(numOfChildren > -1) {
            Type type = new Type("", false);

            switch(methodDeclaration.getJmmChild(numOfChildren).getKind()) {
                case "IntLiteral":
                case "BooleanType":
                case "NewIntArrVarAttribution":
                case "NewClassAttribution":
                case "Id":
                case "ThisExpr":
                case "NotExpr":{
                    VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
                    type = variableSemanticVisitor.visit(methodDeclaration.getJmmChild(numOfChildren), 0);
                    break;
                }
                case "BinOP": {
                    BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                    type = binOpSemanticVisitor.visit(methodDeclaration.getJmmChild(numOfChildren),0);
                    break;
                }
                default:{
                    type = visit(methodDeclaration.getJmmChild(numOfChildren));
                    break;
                }
            }

            int line = Integer.valueOf(methodDeclaration.getJmmChild(numOfChildren).get("line"));
            int col = Integer.valueOf(methodDeclaration.getJmmChild(numOfChildren).get("col"));
            if(type != null && !type.isArray()) {
                if(!type.getName().equals(methodDeclaration.getJmmChild(0).get("type"))) {
                    if(methodDeclaration.getJmmChild(numOfChildren).getKind().equals("MethodCall")) {
                        Type callerType = new Type("", false);
                        switch(methodDeclaration.getJmmChild(numOfChildren).getJmmChild(0).getKind()) {
                            case "IntLiteral":
                            case "BooleanType":
                            case "NewIntArrVarAttribution":
                            case "NewClassAttribution":
                            case "Id":
                            case "ThisExpr":
                            case "NotExpr":{
                                VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
                                callerType = variableSemanticVisitor.visit(methodDeclaration.getJmmChild(numOfChildren).getJmmChild(0), 0);
                                break;
                            }
                            case "BinOP": {
                                BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                                callerType = binOpSemanticVisitor.visit(methodDeclaration.getJmmChild(numOfChildren).getJmmChild(0),0);
                                break;
                            }
                            default:{
                                callerType = visit(methodDeclaration.getJmmChild(numOfChildren).getJmmChild(0));
                                break;
                            }
                        }
                        if(!symbolTable.getImports().contains(callerType.getName())) {
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error: invalid return type on method " + methodDeclaration.get("name") + " method not declared or imported"));
                        }
                    }
                    else {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error: invalid return type on method " + methodDeclaration.get("name") + ". Expected " + methodDeclaration.getJmmChild(0).get("type") + " but got " + type.getName()));
                    }
                }
            }
        }
        return new Type("none", false);
    }

    private Type visitMethodCall(JmmNode methodCall, Integer dummy) {
        Type type = this.symbolTable.getReturnType(methodCall.get("name"));

        int line = Integer.valueOf(methodCall.get("line"));
        int col = Integer.valueOf(methodCall.get("col"));

        if (type == null && symbolTable.getSuper() != null || type == null && !symbolTable.getImports().isEmpty()) {
            switch (methodCall.getJmmParent().getKind()) {
                case "Attribution":{
                    JmmNode child = methodCall.getJmmParent().getJmmChild(0);
                    VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
                    type = variableSemanticVisitor.visit(child);
                    break;
                }
                default:{
                    type = visit(methodCall.getJmmChild(0));
                    break;
                }
            }
            return type;
        } else if (type == null && methodCall.getJmmChild(0).getKind().equals("ThisExpr")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error on method " + methodCall.get("name") + ": Method Undeclared"));
            return new Type("", false);
        } else if(type == null && symbolTable.getSuper() == null && symbolTable.getImports().isEmpty()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, "Error on method " + methodCall.get("name") + ": Method Undeclared"));
            return new Type("", false);
        }

        for(int i = 0; i < methodCall.getJmmChild(1).getChildren().size(); i++){
            Type argType = new Type("", false);

            switch(methodCall.getJmmChild(1).getJmmChild(i).getKind()) {
                case "IntLiteral":
                case "BooleanType":
                case "NewIntArrVarAttribution":
                case "NewClassAttribution":
                case "Id":
                case "ThisExpr":
                case "NotExpr":{
                    VariableSemanticVisitor variableSemanticVisitor = new VariableSemanticVisitor(symbolTable);
                    argType = variableSemanticVisitor.visit(methodCall.getJmmChild(1).getJmmChild(i), 0);
                    break;
                }
                case "Indexing":
                case "LengthMethod":{
                    IndexingSemanticVisitor indexingSemanticVisitor = new IndexingSemanticVisitor(symbolTable);
                    argType = indexingSemanticVisitor.visit(methodCall.getJmmChild(1).getJmmChild(i), 0);
                    break;
                }
                case "BinOP": {
                    BinOpSemanticVisitor binOpSemanticVisitor = new BinOpSemanticVisitor(symbolTable);
                    argType = binOpSemanticVisitor.visit(methodCall.getJmmChild(1).getJmmChild(i),0);
                    break;
                }
                default:{
                    argType = visit(methodCall.getJmmChild(1).getJmmChild(i));
                    break;
                }
            }
            int argLine = Integer.valueOf(methodCall.getJmmChild(1).getJmmChild(i).get("line"));
            int argCol = Integer.valueOf(methodCall.getJmmChild(1).getJmmChild(i).get("col"));
            List<Symbol> params = symbolTable.getParameters(methodCall.get("name"));
            if(!params.get(i).getType().equals(argType)) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, argLine, argCol, "Error on method " + methodCall.get("name") + ": invalid method call, types of parameters are invalid. Parameter " + params.get(i).getName() + " expected " + params.get(i).getType() + " but got " + argType));
            }
        }
        return type;
    }

    public List<Report> getReports() {
        return this.reports;
    }
}
