package pt.up.fe.comp.analysers;

import java.util.List;

import pt.up.fe.comp.SemanticAnalyser;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.visitors.ConditionalSemanticVisitor;

public class ConditionalSemanticAnalyser implements SemanticAnalyser {

    private final SymbolTable symbolTable;
    private final JmmParserResult parserResult;

    public ConditionalSemanticAnalyser(SymbolTable symbolTable, JmmParserResult parserResult) {
        this.symbolTable = symbolTable;
        this.parserResult = parserResult;
    }

    @Override
    public List<Report> getReports() {
        ConditionalSemanticVisitor conditionalSemanticVisitor = new ConditionalSemanticVisitor(symbolTable);
        conditionalSemanticVisitor.visit(this.parserResult.getRootNode(), 0);
        return conditionalSemanticVisitor.getReports();
    }
    
}
