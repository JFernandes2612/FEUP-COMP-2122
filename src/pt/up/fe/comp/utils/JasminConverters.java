package pt.up.fe.comp.utils;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class JasminConverters {

    public static String getString(Integer integer) {
        if (integer == 0) {
            return "V";
        }
        else if (integer == 1) {
            return "I";
        }
        else if (integer == 2) {
            return "[I";
        }
        else if (integer == 3) {
            return "Z";
        }

        return "";
    }

    public static String getString(Symbol symbol) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(symbol.getName()).append(" ").append(getString(symbol.getType()));

        return stringBuilder.toString();
    }

    public static String getString(Type type) {

        StringBuilder stringBuilder = new StringBuilder();

        if (type.isArray()) {
            stringBuilder.append("[");
        }

        switch (type.getName()) {
            case "int":
                stringBuilder.append("I");
                break;
            case "boolean":
                stringBuilder.append("Z");
                break;
            case "void":
                stringBuilder.append("V");
                break;
            case "string":
                stringBuilder.append("Ljava/lang/String;");
                break;
            default:
                stringBuilder.append("L").append(type.getName()).append(";");
                break;
        }

        return stringBuilder.toString();

    }

}
