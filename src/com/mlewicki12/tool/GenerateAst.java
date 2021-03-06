
package com.mlewicki12.tool;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException  {
        if(args.length != 1) {
            System.err.println("usage: generate_ast <output directory>");
            System.exit(64);
        }

        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Ternary    :   Expr condition, Expr left, Expr right",
                "Binary     :   Expr left, Token operator, Expr right",
                "Grouping   :   Expr expression",
                "Literal    :   Object value",
                "Unary      :   Token operator, Expr right",
                "Exit       :   Token exit"    // the way it's structured rn, each Expr needs a token, so I'm giving it the actual exit call
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println();
        writer.println("package com.mlewicki12.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        writer.println("    abstract <R> R accept(Visitor<R> visitor);");
        writer.println();

        defineVisitor(writer, baseName, types);
        writer.println();

        for(String type : types) {
            String className = type.split(":")[0].trim();   // get the name of the type
            String fields    = type.split(":")[1].trim();   // get the field names
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.println();
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");

        for(String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("    static class " + className + " extends " + baseName + " {");

        String[] fields = fieldList.split(", ");                                          // define fields
        for (String field : fields) {
            writer.println("        final " + field + ";");
        }

        writer.println();
        writer.println("        " + className + "(" + fieldList + ") {" );                      // constructor

        for(String field : fields) {                                                            // assign fields
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }

        writer.println("        }");

        writer.println();                                                                       // define structure for accepting visitors
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        writer.println("    }");
        writer.println();
    }
}
