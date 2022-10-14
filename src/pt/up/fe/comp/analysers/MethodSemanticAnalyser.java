package pt.up.fe.comp.analysers;

import java.util.List;

import pt.up.fe.comp.SemanticAnalyser;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.visitors.MethodSemanticVisitor;

public class MethodSemanticAnalyser implements SemanticAnalyser {

    private final SymbolTable symbolTable;
    private final JmmParserResult parserResult;

    public MethodSemanticAnalyser(SymbolTable symbolTable, JmmParserResult parserResult) {
        this.symbolTable = symbolTable;
        this.parserResult = parserResult;
    }

    @Override
    public List<Report> getReports() {
        MethodSemanticVisitor methodSemanticVisitor = new MethodSemanticVisitor(symbolTable);
        methodSemanticVisitor.visit(this.parserResult.getRootNode(), 0);
        return methodSemanticVisitor.getReports();
    }
    
}
