package guru;

import static guru.Expr.*;
import static guru.Stmt.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import guru.Expr.Anon;
import guru.Expr.ExtendAnon;
import guru.Expr.Set;
import guru.Stmt.Class;
import guru.Stmt.Const;
import guru.Stmt.ExtendClass;
import guru.Stmt.Fun;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    Resolver(Interpreter i) {
        this.interpreter = i;
    }

    @Override
    public Void visitStatementStmt(Statement stmt) {
        stmt.expression.accept(this);
        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {

        declare(stmt.name);
        if (stmt.expression != null) stmt.expression.accept(this);
        define(stmt.name);

        return null;
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }
    private void declare(Token name) {
        if (scopes.isEmpty()) return;
        if (scopes.peek().containsKey(name.lexeme)) {
            Guru.error(name, "Already a variable with this name in this scope.");
        }
        scopes.peek().put(name.lexeme, false);
    }

    @Override
    public Void visitFunStmt(Fun stmt) {

        declare(stmt.name);
        define(stmt.name);
        scopes.push(new HashMap<>());
        for (Token param: stmt.args) {
            declare(param);
            define(param);
        }
        stmt.expression.accept(this);
        scopes.pop();

        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        stmt.expression.accept(this);
        return null;
    }

    @Override
    public Void visitLambdaExpr(Lambda expr) {
        scopes.push(new HashMap<>());
        for (Token param: expr.args) {
            declare(param);
            define(param);
        }
        expr.expression.accept(this);
        scopes.pop();
        return null;
    }

    @Override
    public Void visitBinaryExpr(Binary expr) {
        expr.right.accept(this);
        expr.left.accept(this);
        return null;
    }

    @Override
    public Void visitLogikaExpr(Logika expr) {
        expr.right.accept(this);
        expr.left.accept(this);
        return null;
    }

    @Override
    public Void visitCallExpr(Call expr) {
        expr.callee.accept(this);
        for (Expr arg : expr.args) arg.accept(this);
        return null;
    }

    @Override
    public Void visitWhileExpr(While expr) {
        expr.cond.accept(this);
        expr.body.accept(this);
        return null;
    }

    @Override
    public Void visitWithExpr(With expr) {
        scopes.push(new HashMap<>());
        for (Stmt decl: expr.stmts) decl.accept(this);
        expr.body.accept(this);
        scopes.pop();
        return null;
    }

    @Override
    public Void visitGroupingExpr(Grouping expr) {
        expr.expression.accept(this);
        return null;
    }

    @Override
    public Void visitIfExpr(If expr) {
        expr.cond.accept(this);
        expr.iftrue.accept(this);
        if (expr.iffalse != null) expr.iffalse.accept(this);
        return null;
    }

    @Override
    public Void visitBlockExpr(Block expr) {

        scopes.push(new HashMap<>());
        
        for (Stmt stmt : expr.stmts) {
            stmt.accept(this);
        }

        scopes.pop();

        return null;
    }

    @Override
    public Void visitAssignmentExpr(Assignment expr) {

        expr.value.accept(this);
        resolveLocal(expr, expr.name);

        return null;
    }

    @Override
    public Void visitLiteralExpr(Literal expr) {
        return null;
    }

    @Override
    public Void visitCommaExpressionsExpr(CommaExpressions expr) {
        for (Expr e : expr.expressionList) e.accept(this);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Unary expr) {
        expr.right.accept(this);
        return null;
    }
    public Void resolveLocal(Expr expr, Token name) {
        if (!scopes.isEmpty() && scopes.peek().get(name.lexeme) == Boolean.FALSE) {
            Guru.error(name, "cant read local var in its own initializer");
        }

        for (int i = scopes.size() - 1; i>=0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return null;
            }
        }
        return null;
    }
    @Override
    public Void visitVariableExpr(Variable expr) {
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitClassStmt(Class stmt) {
        declare(stmt.name);
        define(stmt.name);
        for (Stmt.Fun f : stmt.methods) f.accept(this);
        return null;
    }

    @Override
    public Void visitAnonExpr(Anon expr) {
        for (Stmt.Fun f : expr.methods) f.accept(this);
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        expr.obj.accept(this);
        return null;
    }

    @Override
    public Void visitSetExpr(Set expr) {

        expr.obj.accept(this);
        expr.value.accept(this);

        return null;
    }

    @Override
    public Void visitConstStmt(Const stmt) {
        declare(stmt.name);
        stmt.expression.accept(this);
        define(stmt.name);

        return null;
    }

    @Override
    public Void visitExtendClassStmt(ExtendClass stmt) {
        stmt.def.accept(this);
        return null;
    }

    @Override
    public Void visitExtendAnonExpr(ExtendAnon expr) {
        expr.def.accept(this);

        return null;
    }

}
