package pt.up.fe.comp.analysers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pt.up.fe.comp.MethodsData;
import pt.up.fe.comp.SemanticAnalyser;
import pt.up.fe.comp.SymbolTableJmm;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class VerifyNoMethodOverload implements SemanticAnalyser {
    private final SymbolTableJmm symbolTable;
    
    public VerifyNoMethodOverload(SymbolTableJmm symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public List<Report> getReports() {
        MethodsData methods = symbolTable.getMethodsData();
        if (methods.getMethods().size() != methods.getNumberOfMethods()) {
            return Arrays.asList(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1, "There are methods with the same signature"));
        }
        return Collections.emptyList();
    }
}