package pt.up.fe.comp.analysers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import pt.up.fe.comp.MethodData;
import pt.up.fe.comp.MethodsData;
import pt.up.fe.comp.SemanticAnalyser;
import pt.up.fe.comp.SymbolTableJmm;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class VerifyNoMethodLocalsOverload implements SemanticAnalyser {
    private final SymbolTableJmm symbolTable;

    public VerifyNoMethodLocalsOverload(SymbolTableJmm symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public List<Report> getReports() {
        MethodsData methods = symbolTable.getMethodsData();

        for (Entry<String, MethodData> entry : methods.getMethodsMap().entrySet())
        {
            MethodData method = entry.getValue();
            if (method.getNumberOfLocals() != method.getLocals().size()) {
                return Arrays.asList(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1, "There are locals with the same name in method \"" + entry.getKey() + "\"."));
            }
        }
        return Collections.emptyList();
    }
}
