package pt.up.fe.comp.analysers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pt.up.fe.comp.FieldsData;
import pt.up.fe.comp.SemanticAnalyser;
import pt.up.fe.comp.SymbolTableJmm;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class VerifyNoClassFieldOverload implements SemanticAnalyser {
    private final SymbolTableJmm symbolTable;

    public VerifyNoClassFieldOverload(SymbolTableJmm symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public List<Report> getReports() {
        FieldsData fields = symbolTable.getFieldsData();

        if (fields.getFields().size() != fields.getNumberOfFields()) {
            return Arrays.asList(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1, "There are fields with the same name."));
        }
        return Collections.emptyList();
    }
}
