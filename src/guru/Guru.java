package guru;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Guru {
    static boolean inError;
    static Interpreter i = new Interpreter();


    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("usage: guruj [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            System.out.println("repl starting");
            repl();
        }
    }
    private static void runFile(String file) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(file));
        Scanner scanner = new Scanner(new String(bytes, Charset.defaultCharset()));
        List<Token> tokens = scanner.scanTokens();

        Resolver resolver = new Resolver(i);
        List<Stmt> stmts = new Parser(tokens).parse();

        stmts.forEach(new Object() {
            public void run(Stmt stmt) {
                stmt.accept(resolver);
            }
        }::run);
        if (inError) System.exit(64);
        try {
            stmts.forEach(new Object() {
                public void run(Stmt stmt) {
                    stmt.accept(Guru.i);
                }
            }::run);
        } catch (Interpreter.RE e) {
            System.out.println("RUNTIME ERROR: " + e.getMessage());
            System.out.println("    TOKEN: [" + e.token.lexeme + "]");
            System.out.println("    LINE: [" + e.token.line + "]");
            inError = true;
        };
        if (inError) System.exit(64);
    }
    private static void repl() throws IOException {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(isr);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            Object val = run(line);
            if (val != null) System.out.println(val);
            inError = false;
        }
    }
    private static Object run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        parser.parse().forEach(new Object() {
            public void run(Stmt stmt) {
                if (stmt == null) return;
                stmt.accept(Guru.i);
            }
        }::run);
        // Stop if there was a syntax error.
        if (inError) return null;
        return null;
    }

    

    static void error(int n, String msg) {
        report(n, "", msg);
    }
    static void error(Token token, String msg) {
        if (token.type == TokenType.EOF) {
            report(token.line, "at the end |", msg);
        } else {
            report(token.line, "at '"+ token.lexeme + "' |", msg);
        }
    }
    private static void report(int n, String where, String msg) {
        System.err.println("[line " + n + "] Error" + where + ": " + msg);
        inError = true;
    }
}
