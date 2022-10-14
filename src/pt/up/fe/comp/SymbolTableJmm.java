package pt.up.fe.comp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.visitors.ClassVisitor;
import pt.up.fe.comp.visitors.FieldsVisitor;
import pt.up.fe.comp.visitors.ImportDeclarationVisitor;
import pt.up.fe.comp.visitors.MethodsVisitor;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.utils.Pair;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class SymbolTableJmm implements SymbolTable {

    private JmmParserResult parserResult;

    private ImportData imports;
    private ClassData classData;
    private FieldsData fields;
    private MethodsData methods;

    public SymbolTableJmm(JmmParserResult parserResult)
    {
        this.parserResult = parserResult;

        this.imports = new ImportData(generateImports());
        this.classData = new ClassData(generateClasses());
        this.fields = new FieldsData(generateFields());
        this.methods = new MethodsData(generateMethods());
    }

    private List<String> generateImports() {
        List<String> imports = new ArrayList<>();

        ImportDeclarationVisitor importVisitor = new ImportDeclarationVisitor();
        importVisitor.visit(this.parserResult.getRootNode(), imports);

        return imports;
    }

    @Override
    public List<String> getImports() {
        return this.imports.getImports();
    }

    private Map<String, String> generateClasses() {
        Map<String, String> classes = new HashMap<String, String>();
        ClassVisitor classVisitor = new ClassVisitor();
        classVisitor.visit(this.parserResult.getRootNode(), classes);
        return classes;
    }

    @Override
    public String getClassName() {
        return this.classData.getClassName();
    }

    @Override
    public String getSuper() {
        return this.classData.getExtends();
    }

    private Pair<Integer, Map<String, Symbol>> generateFields() {
        Map<String, Symbol> fields = new HashMap<String, Symbol>();
        FieldsVisitor fieldsVisitor = new FieldsVisitor();
        fieldsVisitor.visit(this.parserResult.getRootNode(), fields);
        Pair<Integer, Map<String, Symbol>> fieldsReturn = new Pair<Integer, Map<String, Symbol>>(fieldsVisitor.getFieldsCounter(), fields);
        return fieldsReturn;
    }

    @Override
    public List<Symbol> getFields() {
        return this.fields.getFieldsList();
    }

    private Pair<Integer, Map<String, Map<String, Pair<Integer, Object>>>> generateMethods() {
        Map<String, Map<String, Pair<Integer, Object>>> methods = new HashMap<>();
        MethodsVisitor methodsVisitor = new MethodsVisitor();
        methodsVisitor.visit(this.parserResult.getRootNode(), methods);
        Pair<Integer, Map<String, Map<String, Pair<Integer, Object>>>> methodsReturn = new Pair<>(methodsVisitor.getMethodCounter(), methods);
        return methodsReturn;
    }

    @Override
    public List<String> getMethods() {
        return this.methods.getMethods();
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return this.methods.getReturnType(methodSignature);
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return this.methods.getParameters(methodSignature);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return this.methods.getLocalVariables(methodSignature);
    }

    public MethodsData getMethodsData() {
        return this.methods;
    }

    public FieldsData getFieldsData() {
        return this.fields;
    }

    public ClassData getClassData() {
        return this.classData;
    }

    public ImportData getImportsData() {
        return this.imports;
    }

}
