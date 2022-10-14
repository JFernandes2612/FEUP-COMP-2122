package pt.up.fe.comp;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class LineColAnnotator extends PreorderJmmVisitor<Integer, Integer> {

    public LineColAnnotator() {
        setDefaultVisit(this::anotateLineCol);
    }

    private Integer anotateLineCol(JmmNode node, Integer dummy) {
        var baseNode = (BaseNode) node;

        node.put("line", Integer.toString(baseNode.getBeginLine()));
        node.put("col", Integer.toString(baseNode.getBeginColumn()));

        return 0;
    }
}
