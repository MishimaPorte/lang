package guru;
import static guru.TokenType.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    public enum Void {VOID};
    private List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> stmts = new ArrayList<>();

        while (!isAtEnd()) {
            try {
                stmts.add(statement());
            } catch (ParseError err) {
                synchronize();
            }
        }

        return stmts;
    }

    Stmt statement() {
        Expr expr;
        Stmt stmt;
        Token name;
        boolean exported = match(EXPORT);
        switch (peek().type) {
        case STRUCTURE:
            advance();
            Token superclass = null;
            name = advance();
            if (match(FROM)) superclass = advance();
            List<Stmt.Fun> methods = new ArrayList<>();
            consume(LEFT_BRACE, "missing opening parentheses for func decalaration");
            while (true) {
                if (match(RIGHT_BRACE)) break;
                Stmt meth = statement();
                if (!(meth instanceof Stmt.Fun)) Guru.error(peek(1), "foreign thing inside class declaration");
                methods.add((Stmt.Fun) meth);
                if (match(RIGHT_BRACE)) break;
            }
            stmt = new Stmt.Class(name, methods);
            if (superclass != null) stmt = new Stmt.ExtendClass((Stmt.Class)stmt, superclass);
            break;
        case STATIC:
        case FUN:
            boolean isStatic = match(STATIC);
            consume(FUN, "must declare");
            name = advance();
            List<Token> args = new ArrayList<>();
            consume(LEFT_PAREN, "missing opening parentheses for func decalaration");
            while (true) {
                if (match(RIGHT_PAREN)) break;
                args.add(advance());
                if (match(RIGHT_PAREN)) break;
                consume(COMMA, "missing comma between func declaration for func decalaration");
            }
            Expr body = comma();
            stmt = new Stmt.Fun(name, args, body, isStatic);
            break;
        case CONST:
            advance();
            name = advance();
            consume(EQUAL, "must init a constant");
            expr = comma();
            stmt = new Stmt.Const(name, expr);
            break;
        case VAR:
            advance();
            name = advance();
            if (match(EQUAL)) {
                expr = comma();
                stmt = new Stmt.Var(name, expr);
            } else {
                stmt = new Stmt.Var(name, null);
            }
            break;
        default:
            expr = comma();
            stmt = new Stmt.Statement(expr);
            break;
        }
        consume(SEMICOLON, "you are expected to end your futile statements with semicolons, moron");
        return stmt;
    }

    private Expr comma() {
        Expr expr = block();
        if (match(COMMA)) {
            List<Expr> commas = new ArrayList<Expr>(2);
            Expr more = block();
            commas.add(expr);
            commas.add(more);
            while (match(COMMA)) {
                more = block();
                commas.add(more);
            }
            return new Expr.CommaExpressions(commas);
        }
        return expr;
    }

    private Expr block() {
        if (match(WITH)) {
            consume(LEFT_PAREN, "missing opening parentheses for if expression");
            Stmt stmt = statement();
            List<Stmt.Var> declrs = new ArrayList<>();
            if (stmt instanceof Stmt.Var) {
                declrs.add((Stmt.Var)stmt);
                while (!match(RIGHT_PAREN)) {
                    stmt = statement();
                    if (stmt instanceof Stmt.Var) {
                        declrs.add((Stmt.Var)stmt);
                    } else throw error(peek(1), "variable decalration expected");
                }
                Expr body = block();
                return new Expr.With(declrs, body);
            }
        }
        if (match(WHILE)) {
            consume(LEFT_PAREN, "missing opening parentheses for if expression");
            Expr cond = comma();
            consume(RIGHT_PAREN, "missing opening parentheses for if expression");
            Expr body = block();
            return new Expr.While(cond, body);
        };
        if (match(IF)) {
            consume(LEFT_PAREN, "missing opening parentheses for if expression");
            Expr cond = comma();
            consume(RIGHT_PAREN, "missing opening parentheses for if expression");
            Expr iftrue = block();
            Expr iffalse = null;
            if (match(ELSE)) iffalse = block();
            return new Expr.If(cond, iftrue, iffalse);
        };
        if (match(LEFT_BRACE)) {
            List<Stmt> stmts = new ArrayList<>();
            while (!match(RIGHT_BRACE) && !isAtEnd()) {
                stmts.add(statement());
                match(COMMA);
            }
            if (isAtEnd()) throw error(peek(1), "expected a brace to close a block");
            return new Expr.Block(stmts);
        }

        return assignment();
    }

    private Expr assignment() {
        Expr expr = disjuntion();

        if (match(EQUAL)) {
            Token t = peek(1);
            Expr val = block();

            if (expr instanceof Expr.Variable) {
                return new Expr.Assignment(((Expr.Variable) expr).name, val);
            } else if (expr instanceof Expr.Get) {
                return new Expr.Set(((Expr.Get)expr).obj, ((Expr.Get)expr).name, val);
            }

            error(t, "Invalid assignment target");
        }

        return expr;
    }

    private Expr disjuntion() {
        Expr expr = conjuntion();

        while (match(OR)) {
            Token op = peek(1);
            Expr right = conjuntion();
            expr = new Expr.Logika(expr, op, right);
        }

        return expr;
    }

    private Expr conjuntion() {
        Expr expr = eq();

        while (match(AND)) {
            Token op = peek(1);
            Expr right = eq();
            expr = new Expr.Logika(expr, op, right);
        }

        return expr;
    }
    private Expr eq() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = peek(1);
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    
    private boolean match(TokenType... tts) {
        for (TokenType tt : tts) {
            if (check(tt)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType tt) {
        if (isAtEnd()) return false;

        return peek().type == tt;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return tokens.get(current-1);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }
    private Token peek(int n) {
        return tokens.get(current-n);
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = peek(1);
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = peek(1);
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = peek(1);
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = peek(1);
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = call(expr);
            } else if (match(DOT)) {
                expr = new Expr.Get(expr, consume(IDENTIFIER, "expected identifier"));
            } else break;
        };

        return expr;
    }

    private Expr call(Expr expr) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                arguments.add(assignment());
            } while (match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
        return new Expr.Call(expr, paren, arguments);
    }

    private Expr primary() {
        if (match(STRUCTURE)) {
            List<Stmt.Fun> methods = new ArrayList<>();
            Token father = null;
            if (match(FROM)) father = consume(IDENTIFIER, "need base class name");
            consume(LEFT_BRACE, "missing opening parentheses for func decalaration");
            while (true) {
                if (match(RIGHT_BRACE)) break;
                Stmt meth = statement();
                if (!(meth instanceof Stmt.Fun)) Guru.error(peek(1), "foreign thing inside class declaration");
                methods.add((Stmt.Fun) meth);
                if (match(RIGHT_BRACE)) break;
            }
            return father == null ? new Expr.Anon(methods) : new Expr.ExtendAnon(new Expr.Anon(methods), father);
        }
        if (match(STATIC)) {
            consume(FUN, "must declare a lambda");
            List<Token> args = new ArrayList<>();
            consume(LEFT_PAREN, "missing opening parentheses for func decalaration");
            while (true) {
                if (match(RIGHT_PAREN)) break;
                args.add(advance());
                if (match(RIGHT_PAREN)) break;
                consume(COMMA, "missing comma between func declaration for func decalaration");
            }
            Expr body = block();
            return new Expr.Lambda(args, body, true);
        }
        if (match(FUN)) {
            List<Token> args = new ArrayList<>();
            consume(LEFT_PAREN, "missing opening parentheses for func decalaration");
            while (true) {
                if (match(RIGHT_PAREN)) break;
                args.add(advance());
                if (match(RIGHT_PAREN)) break;
                consume(COMMA, "missing comma between func declaration for func decalaration");
            }
            Expr body = block();
            return new Expr.Lambda(args, body, false);
        }
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NOTHING)) return new Expr.Literal(null);
        if (match(VOID)) return new Expr.Literal(Void.VOID);
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(peek(1).literal);
        } if (match(LEFT_PAREN)) {
            Expr expr = comma();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        if (match(IDENTIFIER)) {
            return new Expr.Variable(peek(1));
        }
        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType tokenType, String message) {
        if (check(tokenType)) return advance();

        throw error(peek(), message);
    }

    private static class ParseError extends RuntimeException {}

    private ParseError error(Token token, String message) {
        Guru.error(token, message);
        return new ParseError();
    }
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (peek(1).type == SEMICOLON) return;
            switch (peek().type) {
                case STRUCTURE: case FUN: case VAR:
                case IF: case WHILE: case CONST:
                    return;
                default:
            }
            advance();
        }
    }
}
