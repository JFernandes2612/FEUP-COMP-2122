package pt.up.fe.comp.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.utils.JasminConverters;
import pt.up.fe.comp.utils.Pair;

public class AstToJasminVisitor extends AJmmVisitor<Integer, Integer> {
    private StringBuilder jasminCode = new StringBuilder();
    private SymbolTable symbolTable;

    private Map<String, Symbol> fields;
    private Map<String, Pair<Type, Integer>> currentMethodRegisters = new HashMap<>();
    private int labelCounter = 0;

    private int currentStack = 0;
    private int maxStack = 0;
    private StringBuilder currentMethod = null;

    private void manageStack(int stack) {
        this.currentStack+=stack;

        if (this.currentStack > this.maxStack) {
            this.maxStack = this.currentStack;
        }
    }

    public AstToJasminVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.fields = symbolTable.getFields().stream().collect(Collectors.toMap(Symbol::getName, s -> s));

        addVisit("Program", this::visitDown);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("MethodDeclaration", this::visitMethodDeclaration);
        addVisit("Body", this::visitDown);
        addVisit("ExprStmt", this::visitDown);
        addVisit("MethodCall", this::visitMethodCall);
        addVisit("Id", this::visitId);
        addVisit("IntLiteral", this::visitIntLiteral);
        addVisit("BooleanType", this::visitBooleanType);
        addVisit("BinOP", this::visitBinOp);
        addVisit("Attribution", this::visitAttribution);
        addVisit("NewIntArrVarAttribution", this::visitNewIntArrVarAttribution);
        addVisit("NewClassAttribution", this::visitNewClassAttribution);
        addVisit("ArrIndexAttribution", this::visitArrIndexAttribution);
        addVisit("Indexing", this::visitIndexing);
        addVisit("ThisExpr", this::visitThisExpr);
        addVisit("NotExpr", this::visitNotExpr);
        addVisit("Scope", this::visitDown);
        addVisit("IfStatement", this::visitIfStatement);
        addVisit("ElseStatement", this::visitDown);
        addVisit("WhileStatement", this::visitWhileStatement);
        addVisit("LengthMethod", this::visitLengthMethod);
    }

    private Integer visitClassDeclaration(JmmNode classDeclaration, Integer dummy) {

        jasminCode.append(".class public ").append(symbolTable.getClassName()).append("\n");

        var superClass = symbolTable.getSuper();

        var superClassValue = "";

        if (superClass != null) {
            jasminCode.append(".super ").append(superClass).append("\n\n");
            superClassValue = superClass;
        } else {
            jasminCode.append(".super java/lang/Object\n\n");
            superClassValue = "java/lang/Object";
        }

        for (Symbol field : symbolTable.getFields()) {
            jasminCode.append(".field private '").append(field.getName()).append("' ").append(JasminConverters.getString(field.getType())).append("\n");
        }

        jasminCode.append("\n.method public <init>()V\n");
        jasminCode.append("aload_0\n");
        jasminCode.append("invokenonvirtual ").append(superClassValue).append("/<init>()V\n");
        jasminCode.append("return\n");
        jasminCode.append(".end method\n\n");

        for (JmmNode child : classDeclaration.getChildren()) {
            visit(child);
        }

        return 0;
    }

    private Integer visitMethodDeclaration(JmmNode methodDeclaration, Integer dummy) {
        String methodName = methodDeclaration.get("name");
        this.generateRegisterLocalVariableAlocation(methodName);

        jasminCode.append(".method public ");

        if (methodName.equals("main")) {
            jasminCode.append("static ");
        }

        jasminCode.append(methodName).append("(");

        List<Type> parameters = this.symbolTable.getParameters(methodName).stream().map(mapper -> mapper.getType())
                .collect(Collectors.toList());
        Type returnType = this.symbolTable.getReturnType(methodName);

        jasminCode.append(parameters.stream().map(JasminConverters::getString).collect(Collectors.joining("")));
        jasminCode.append(")").append(JasminConverters.getString(returnType)).append("\n");

        jasminCode.append(".limit locals ");
        jasminCode.append(this.currentMethodRegisters.size());
        jasminCode.append("\n");

        this.currentMethod = new StringBuilder();
        this.currentStack = 0;
        this.maxStack = 0;

        for (JmmNode child : methodDeclaration.getChildren()) {
            visit(child);
        }

        jasminCode.append(".limit stack ").append(this.maxStack).append("\n");
        jasminCode.append(this.currentMethod);

        if (returnType.getName().equals("void")) {
            jasminCode.append("return\n");
        } else if (returnType.getName().equals("int") && !returnType.isArray()
                || returnType.getName().equals("boolean")) {
            jasminCode.append("ireturn\n");
        } else {
            jasminCode.append("areturn\n");
        }

        jasminCode.append(".end method\n\n");
        this.currentMethodRegisters = new HashMap<>();

        return 0;
    }

    public Integer visitMethodCall(JmmNode methodCall, Integer returnTypeInt) {
        String methodName = methodCall.get("name");
        String scopeName = "";

        JmmNode scopeCall = methodCall.getJmmChild(0);
        boolean classFuntion = false;
        String invokeType = "invokestatic ";

        if (scopeCall.getKind().equals("ThisExpr")) {
            scopeName = "this";
            invokeType = "invokevirtual ";
        } else if (scopeCall.getKind().equals("NewClassAttribution")) {
            classFuntion = false;
            visit(scopeCall);
            scopeName = this.symbolTable.getClassName();
            invokeType = "invokevirtual ";
        }
        else {
            scopeName = scopeCall.get("name");
        }

        classFuntion = this.currentMethodRegisters.containsKey(scopeName);

        if (classFuntion) {
            visit(scopeCall);
            invokeType = "invokevirtual ";
        }

        JmmNode Args = methodCall.getJmmChild(1);

        List<Integer> argsType = new ArrayList<>();

        for (JmmNode Arg : Args.getChildren()) {
            argsType.add(visit(Arg));
        }

        currentMethod.append(invokeType);


        currentMethod.append(classFuntion
                ? this.currentMethodRegisters.get(scopeName).getKey().getName()
                : scopeName).append("/");
        currentMethod.append(methodName).append("(");
        for (int argType : argsType) {
            if (argType == 1) {
                currentMethod.append("I");
            }
            if (argType == 2) {
                currentMethod.append("[I");
            }
            if (argType == 3) {
                currentMethod.append("Z");
            }
        }

        Type returnType = this.symbolTable.getReturnType(methodName);
        int returnValueInt = 0;

        if (methodCall.getJmmParent().getKind().equals("MethodDeclaration")) {
            currentMethod.append(")").append(JasminConverters.getString(this.symbolTable.getReturnType(methodCall.getJmmParent().get("name")))).append("\n");
        } else {
            currentMethod.append(")").append(returnType != null ? JasminConverters.getString(returnType) : returnTypeInt != null ? JasminConverters.getString(returnTypeInt) : "V")
                    .append("\n");
            if (returnType != null) {
                if (returnType.getName().equals("int") && !returnType.isArray())
                    returnValueInt = 1;
                else if (returnType.getName().equals("int") && returnType.isArray())
                    returnValueInt = 2;
                else if (returnType.getName().equals("boolean"))
                    returnValueInt = 3;
            }
        }

        this.manageStack(-argsType.size());

        if (methodCall.getJmmParent().getKind().equals("ExprStmt") && returnValueInt != 0) {
            currentMethod.append("pop\n");
            this.manageStack(-1);
        }
        else {
            this.manageStack(1);
        }

        return returnValueInt;
    }

    private void generateRegisterLocalVariableAlocation(String methodName) {
        this.currentMethodRegisters.put("this",
                new Pair<Type, Integer>(new Type(this.symbolTable.getClassName(), false), 0));
        int registerIndex = 1;

        for (int i = 0; i < this.symbolTable.getParameters(methodName).size(); i++) {
            this.currentMethodRegisters.put(this.symbolTable.getParameters(methodName).get(i).getName(),
                    new Pair<Type, Integer>(this.symbolTable.getParameters(methodName).get(i).getType(),
                            registerIndex));
            registerIndex++;
        }

        for (int i = 0; i < this.symbolTable.getLocalVariables(methodName).size(); i++) {
            this.currentMethodRegisters.put(this.symbolTable.getLocalVariables(methodName).get(i).getName(),
                    new Pair<Type, Integer>(this.symbolTable.getLocalVariables(methodName).get(i).getType(),
                            registerIndex));
            registerIndex++;
        }
    }

    public Integer visitAttribution(JmmNode Attribution, Integer dummy) {
        JmmNode left = Attribution.getJmmChild(0);
        String varName = left.get("name");
        Type varType = this.currentMethodRegisters.containsKey(varName) ? this.currentMethodRegisters.get(varName).getKey() : this.fields.get(varName).getType();
        Integer varTypeNum = varType.getName().equals("int") && !varType.isArray() ? 1 : varType.getName().equals("int") && varType.isArray() ? 2 : varType.getName().equals("boolean") ? 3 : 0;

        JmmNode right = Attribution.getJmmChild(1);

        if (!this.currentMethodRegisters.containsKey(varName)) {
            currentMethod.append("aload_0\n");
            this.manageStack(1);
        }

        visit(right, varTypeNum);

        if (this.currentMethodRegisters.containsKey(varName)) {
            Pair<Type, Integer> varRegister = this.currentMethodRegisters.get(varName);

            if (varRegister.getKey().getName().equals("int") && !varRegister.getKey().isArray()
                    || varRegister.getKey().getName().equals("boolean")) {
                this.currentMethod.append("istore ");
                this.manageStack(-1);
            } else {
                this.currentMethod.append("astore ");
                this.manageStack(-1);
            }

            this.currentMethod.append(varRegister.getValue()).append("\n");
        } else if (this.fields.containsKey(varName)) {
            this.currentMethod.append("putfield ").append(this.symbolTable.getClassName()).append("/")
                    .append(JasminConverters.getString(this.fields.get(varName))).append("\n");
            this.manageStack(-1);
        }

        return 0;
    }

    public Integer visitNewIntArrVarAttribution(JmmNode NewIntArrVarAttribution, Integer dummy) {
        visit(NewIntArrVarAttribution.getJmmChild(0));
        this.currentMethod.append("newarray int\n");

        return 2;
    }

    public Integer visitNewClassAttribution(JmmNode NewClassAttribution, Integer dummy) {
        String className = NewClassAttribution.getJmmChild(0).get("name");

        this.currentMethod.append("new ").append(className).append("\n");
        this.currentMethod.append("dup\n");
        this.manageStack(2);
        this.currentMethod.append("invokespecial ").append(className).append("/<init>()V\n");
        this.manageStack(-1);

        return 0;
    }

    public Integer visitArrIndexAttribution(JmmNode ArrIndexAttribution, Integer dummy) {
        JmmNode left = ArrIndexAttribution.getJmmChild(0);
        JmmNode index = ArrIndexAttribution.getJmmChild(1);
        JmmNode right = ArrIndexAttribution.getJmmChild(2);

        String varName = left.get("name");

        if (this.currentMethodRegisters.containsKey(varName)) {

            Pair<Type, Integer> varRegister = this.currentMethodRegisters.get(varName);

            this.currentMethod.append("aload ").append(varRegister.getValue()).append("\n");
            this.manageStack(1);

            visit(index);
            visit(right, 1);

            this.currentMethod.append("iastore").append("\n");
            this.manageStack(-3);
        } else if (this.fields.containsKey(varName)) {
            this.currentMethod.append("aload_0\n");
            this.currentMethod.append("getfield ").append(this.symbolTable.getClassName()).append("/")
                    .append(JasminConverters.getString(this.fields.get(varName))).append("\n");
            this.manageStack(1);

            visit(index);
            visit(right, 1);

            this.currentMethod.append("iastore").append("\n");
            this.manageStack(-3);
        }

        return 0;
    }

    public Integer visitIndexing(JmmNode Indexing, Integer dummy) {
        JmmNode left = Indexing.getJmmChild(0);
        JmmNode index = Indexing.getJmmChild(1);

        String varName = left.get("name");

        if (this.currentMethodRegisters.containsKey(varName)) {
            Pair<Type, Integer> varRegister = this.currentMethodRegisters.get(varName);

            this.currentMethod.append("aload ").append(varRegister.getValue()).append("\n");
            this.manageStack(1);

            visit(index);

            this.currentMethod.append("iaload").append("\n");
            this.manageStack(-2);
        } else if (this.fields.containsKey(varName)) {
            this.currentMethod.append("aload_0\n");
            this.currentMethod.append("getfield ").append(this.symbolTable.getClassName()).append("/")
                    .append(JasminConverters.getString(this.fields.get(varName))).append("\n");
            this.manageStack(1);

            visit(index);

            this.currentMethod.append("iaload").append("\n");
            this.manageStack(-2);
        }

        return 1;
    }

    public Integer visitBinOp(JmmNode BinOp, Integer dummy) {
        JmmNode left = BinOp.getJmmChild(0);
        JmmNode right = BinOp.getJmmChild(1);

        visit(left);
        visit(right);

        String operator = BinOp.get("op");

        switch (operator) {
            case "+":
                currentMethod.append("iadd\n");
                this.manageStack(-1);
                return 1;
            case "-":
                currentMethod.append("isub\n");
                this.manageStack(-1);
                return 1;
            case "*":
                currentMethod.append("imul\n");
                this.manageStack(-1);
                return 1;
            case "/":
                currentMethod.append("idiv\n");
                this.manageStack(-1);
                return 1;
            case "&&":
                currentMethod.append("iand\n");
                this.manageStack(-1);
                return 3;
            case "<":
                String lable1 = "label" + this.labelCounter;
                String lable2 = "label" + (this.labelCounter + 1);
                this.labelCounter += 2;
                currentMethod.append("if_icmplt ").append(lable1).append("\n");
                currentMethod.append("iconst_0\n");
                currentMethod.append("goto ").append(lable2).append("\n");
                currentMethod.append(lable1).append(":\n");
                currentMethod.append("iconst_1\n");
                currentMethod.append(lable2).append(":\n");
                this.manageStack(-1);
                return 3;
            default:
                return 0;
        }
    }

    public Integer visitIfStatement(JmmNode IfStatement, Integer dummy) {
        JmmNode condition = IfStatement.getJmmChild(0);
        JmmNode thenStatement = IfStatement.getJmmChild(1);
        JmmNode elseStatement = IfStatement.getJmmChild(2);

        visit(condition);

        String lable1 = "label" + this.labelCounter;
        String lable2 = "label" + (this.labelCounter + 1);
        this.labelCounter += 2;

        currentMethod.append("ifeq ").append(lable1).append("\n");
        visit(thenStatement);
        currentMethod.append("goto ").append(lable2).append("\n");
        currentMethod.append(lable1).append(":\n");
        visit(elseStatement);
        currentMethod.append(lable2).append(":\n");

        return 0;
    }

    public Integer visitWhileStatement(JmmNode WhileStatement, Integer dummy) {
        JmmNode condition = WhileStatement.getJmmChild(0);
        JmmNode body = WhileStatement.getJmmChild(1);

        String lable1 = "label" + this.labelCounter;
        String lable2 = "label" + (this.labelCounter + 1);
        this.labelCounter += 2;

        currentMethod.append(lable1).append(":\n");
        visit(condition);
        currentMethod.append("ifeq ").append(lable2).append("\n");
        visit(body);
        currentMethod.append("goto ").append(lable1).append("\n");
        currentMethod.append(lable2).append(":\n");

        return 0;
    }

    public Integer visitIntLiteral(JmmNode intLiteral, Integer dummy) {
        int value = Integer.parseInt(intLiteral.get("value"));

        switch (value) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                currentMethod.append("iconst_").append(value).append("\n");
                break;
            default:
                if (value >= -128 && value <= 127) {
                    currentMethod.append("bipush ").append(value).append("\n");
                } else if (value >= -32768 && value <= 32767) {
                    currentMethod.append("sipush ").append(value).append("\n");
                } else {
                    currentMethod.append("ldc ").append(value).append("\n");
                }
        }

        this.manageStack(1);
        return 1;
    }

    public Integer visitBooleanType(JmmNode booleanType, Integer dummy) {
        String value = booleanType.get("value");

        if (value.equals("true")) {
            currentMethod.append("iconst_1\n");
        } else {
            currentMethod.append("iconst_0\n");
        }

        this.manageStack(1);

        return 3;
    }

    public Integer visitId(JmmNode id, Integer dummy) {
        String name = id.get("name");

        if (this.currentMethodRegisters.containsKey(name)) {
            Pair<Type, Integer> register = this.currentMethodRegisters.get(name);

            if (register.getKey().getName().equals("int") && !register.getKey().isArray()
                    || register.getKey().getName().equals("boolean")) {
                currentMethod.append("iload ").append(register.getValue()).append("\n");
                this.manageStack(1);
                if (register.getKey().getName().equals("int"))
                    return 1;
                else if (register.getKey().getName().equals("boolean"))
                    return 3;
            } else {
                currentMethod.append("aload ").append(register.getValue()).append("\n");
                this.manageStack(1);
                if (register.getKey().isArray())
                    return 2;
            }

        } else if (this.fields.containsKey(name)) {
            Symbol field = this.fields.get(name);
            currentMethod.append("aload_0\n");
            this.manageStack(1);
            currentMethod.append("getfield ").append(this.symbolTable.getClassName()).append("/")
                    .append(JasminConverters.getString(field)).append("\n");
            if (field.getType().getName().equals("int") && !field.getType().isArray())
                return 1;
            else if (field.getType().getName().equals("int") && field.getType().isArray())
                return 2;
            else if (field.getType().getName().equals("boolean"))
                return 3;
        }

        return 0;
    }

    public Integer visitLengthMethod(JmmNode lengthMethod, Integer dummy) {
        JmmNode array = lengthMethod.getJmmChild(0);

        visit(array);

        currentMethod.append("arraylength\n");

        return 1;
    }

    public Integer visitDown(JmmNode body, Integer dummy) {
        for (JmmNode child : body.getChildren()) {
            visit(child);
        }

        return 0;
    }

    public Integer visitThisExpr(JmmNode ThisExpr, Integer dummy) {
        currentMethod.append("aload_0").append("\n");
        this.manageStack(1);

        return 0;
    }

    public Integer visitNotExpr(JmmNode NotExpr, Integer dummy) {
        JmmNode child = NotExpr.getJmmChild(0);
        visit(child);

        this.currentMethod.append("iconst_1").append("\n");
        this.manageStack(1);
        this.currentMethod.append("ixor").append("\n");
        this.manageStack(-1);

        return 3;
    }

    @Override
    public String toString() {
        return jasminCode.toString();
    }

}
