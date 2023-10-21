package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("err: usage: generateast output_dir");
            System.exit(64);
        }
        String outputDir =  args[0];
        defineAst(outputDir, "Stmt", Arrays.asList(
            "Statement : Expr expression",
            "Var : Token name, Expr expression",
            "Const : Token name, Expr expression",
            "Fun : Token name, List<Token> args, Expr expression, Boolean isStatic",
            "Class : Token name, List<Stmt.Fun> methods",
            "ExtendClass : Stmt.Class def, Token father",
            "Print : Expr expression"
        ));
        defineAst(outputDir, "Expr", Arrays.asList(
            "Lambda : List<Token> args, Expr expression, Boolean isStatic",
            "Anon : List<Stmt.Fun> methods",
            "ExtendAnon : Expr.Anon def, Token father",
            "Get : Expr obj, Token name",
            "Set : Expr obj, Token name, Expr value",
            "Binary : Expr left, Token operator, Expr right",
            "Logika : Expr left, Token operator, Expr right",
            "Call : Expr callee, Token paren, List<Expr> args",
            "While : Expr cond, Expr body",
            "With : List<Stmt.Var> stmts, Expr body",
            "Grouping : Expr expression",
            "If : Expr cond, Expr iftrue, Expr iffalse",
            "Block : List<Stmt> stmts",
            "Assignment : Token name, Expr value",
            "Literal : Object value",
            "CommaExpressions : List<Expr> expressionList",
            "Unary : Token operator, Expr right",
            "Variable : Token name"
        ));
    }
    public static void defineAst(String dir, String baseName, List<String> types) throws IOException {
        String path = dir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package guru;");
        writer.println("");
        writer.println("import java.util.List;");
        writer.println("");
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        types.forEach(new Object(){

            public void process(String thing) {
                String className = thing.split(":")[0].trim();
                String fields = thing.split(":")[1].trim();
                defineType(className, fields);
            }

            private void defineType(String className, String fieldList) {
                writer.println("    static class " + className + " extends " + baseName + " {");
                writer.println("        " + className + "(" + fieldList + ") {");
                String[] fields = fieldList.split(", ");
                for (String field : fields) {
                    String name = field.split(" ")[1];
                    writer.println("            this." + name + " = " + name + ";");
                }
                writer.println("        }");
                writer.println();
                for (String field : fields) {
                    writer.println("        final " + field + ";");
                }
                writer.println();
                writer.println("    @Override");
                writer.println("    <R> R accept(Visitor<R> visitor) {");
                writer.println("      return visitor.visit" + className + baseName + "(this);");
                writer.println("    }");
                writer.println(" }");
            }
        }::process);

        writer.println("    abstract <T> T accept(Visitor<T> visitor);");
        writer.println();
        writer.println("}");
        writer.close();
    }

    public static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<T> {");

        types.forEach(new Object() {
            public void process(String type) {
                String typeName = type.split(":")[0].trim();
                writer.println("        T visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
            }
        }::process);

        writer.println("    }");
    }
}
