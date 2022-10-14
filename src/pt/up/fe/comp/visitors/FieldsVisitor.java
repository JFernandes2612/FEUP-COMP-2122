package pt.up.fe.comp.visitors;

import java.util.List;
import java.util.Map;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class FieldsVisitor extends PreorderJmmVisitor<Map<String, Symbol>, Boolean>{

        int fieldsCounter = 0;

        public FieldsVisitor() {
            addVisit("ClassDeclaration", this::visitFields);
        }

        private Boolean visitFields(JmmNode classDeclaration, Map<String, Symbol> fields) {

            List<JmmNode> fieldsDeclaration = classDeclaration.getChildren();

            int counter = 0;
            for (JmmNode jmmnode : fieldsDeclaration) {
                if (jmmnode.getKind().equals("VarDeclaration")) {
                    counter++;
                    String name = jmmnode.get("name");
                    String type = jmmnode.getJmmChild(0).get("type");
                    boolean isArray = type.contains("Arr");
                    if (isArray) {
                        type = type.substring(0, type.length() - 3);
                    }
                    Type fieldType = new Type(type, isArray);
                    fields.put(name, new Symbol(fieldType, name));
                }
            }

            this.fieldsCounter = counter;
            return true;
        }

        public int getFieldsCounter() {
            return fieldsCounter;
        }

}
