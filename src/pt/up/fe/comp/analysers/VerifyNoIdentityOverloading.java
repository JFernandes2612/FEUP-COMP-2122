package pt.up.fe.comp.analysers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import pt.up.fe.comp.MethodData;
import pt.up.fe.comp.MethodsData;
import pt.up.fe.comp.SemanticAnalyser;
import pt.up.fe.comp.SymbolTableJmm;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class VerifyNoIdentityOverloading implements SemanticAnalyser {
    private final SymbolTableJmm symbolTable;

    public VerifyNoIdentityOverloading(SymbolTableJmm symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public List<Report> getReports() {

        MethodsData methods = symbolTable.getMethodsData();

        for (Entry<String, MethodData> entry : methods.getMethodsMap().entrySet())
        {
            Set<String> parameters = new HashSet<>(entry.getValue().getParameters().stream().map(p -> p.getName()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
            Set<String> locals = new HashSet<>(entry.getValue().getLocals().stream().map(l -> l.getName()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll));

            Set<String> paramsCopy = new HashSet<>(parameters);
            paramsCopy.addAll(locals);
            if (paramsCopy.size() != parameters.size() + locals.size()) {
                return Arrays.asList(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1, "There are variables with the same name in method \"" + entry.getKey() + "\"."));
            }
        }
        return Collections.emptyList();
    }

}
