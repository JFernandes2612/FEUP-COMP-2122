package pt.up.fe.comp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.up.fe.comp.analysers.AttributionSemanticAnalyser;
import pt.up.fe.comp.analysers.BinOpSemanticAnalyser;
import pt.up.fe.comp.analysers.ConditionalSemanticAnalyser;
import pt.up.fe.comp.analysers.IndexingSemanticAnalyser;
import pt.up.fe.comp.analysers.MethodSemanticAnalyser;
import pt.up.fe.comp.analysers.VariableSemanticAnalyser;
import pt.up.fe.comp.analysers.VerifyNoClassFieldOverload;
import pt.up.fe.comp.analysers.VerifyNoFieldOverload;
import pt.up.fe.comp.analysers.VerifyNoIdentityOverloading;
import pt.up.fe.comp.analysers.VerifyNoMethodArgsOverload;
import pt.up.fe.comp.analysers.VerifyNoMethodOverload;
import pt.up.fe.comp.analysers.VerifyNoMethodLocalsOverload;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

public class JmmAnalyser implements JmmAnalysis {

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        SymbolTableJmm symbolTable = new SymbolTableJmm(parserResult);

        System.out.println("SymbolTable: \n" + symbolTable.print());

        List<Report> reports = new ArrayList<Report>();

        List<SemanticAnalyser> analysers = new ArrayList<>();
        analysers.add(new VerifyNoClassFieldOverload(symbolTable));
        analysers.add(new VerifyNoMethodOverload(symbolTable));
        analysers.add(new VerifyNoMethodArgsOverload(symbolTable));
        analysers.add(new VerifyNoMethodLocalsOverload(symbolTable));
        analysers.add(new VerifyNoFieldOverload(symbolTable));
        analysers.add(new VerifyNoIdentityOverloading(symbolTable));
        analysers.add(new VariableSemanticAnalyser(symbolTable, parserResult));
        analysers.add(new BinOpSemanticAnalyser(symbolTable, parserResult));
        analysers.add(new AttributionSemanticAnalyser(symbolTable, parserResult));
        analysers.add(new IndexingSemanticAnalyser(symbolTable, parserResult));
        analysers.add(new ConditionalSemanticAnalyser(symbolTable, parserResult));
        analysers.add(new MethodSemanticAnalyser(symbolTable, parserResult));
        for(SemanticAnalyser analyser : analysers) {
            reports.addAll(analyser.getReports());
        }
        reports.removeAll(Collections.singleton(null));
        return new JmmSemanticsResult(parserResult, symbolTable, reports);

    }
}
