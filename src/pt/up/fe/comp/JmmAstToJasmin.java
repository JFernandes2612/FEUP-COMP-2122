package pt.up.fe.comp;

import java.util.Collections;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast2jasmin.AstToJasmin;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.visitors.AstToJasminVisitor;

public class JmmAstToJasmin implements AstToJasmin {

    @Override
    public JasminResult toJasmin(JmmSemanticsResult semanticsResult) {
        AstToJasminVisitor visitor = new AstToJasminVisitor(semanticsResult.getSymbolTable());

        visitor.visit(semanticsResult.getRootNode());

        String jasminCode = visitor.toString();
        System.out.println("JasminCode: \n" + jasminCode);

        return new JasminResult(semanticsResult.getSymbolTable().getClassName(), visitor.toString(), Collections.emptyList());
    }

}
