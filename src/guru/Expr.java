package guru;

import java.util.List;

abstract class Expr {
    interface Visitor<T> {
        T visitLambdaExpr(Lambda expr);
        T visitAnonExpr(Anon expr);
        T visitExtendAnonExpr(ExtendAnon expr);
        T visitGetExpr(Get expr);
        T visitSetExpr(Set expr);
        T visitBinaryExpr(Binary expr);
        T visitLogikaExpr(Logika expr);
        T visitCallExpr(Call expr);
        T visitWhileExpr(While expr);
        T visitWithExpr(With expr);
        T visitGroupingExpr(Grouping expr);
        T visitIfExpr(If expr);
        T visitBlockExpr(Block expr);
        T visitAssignmentExpr(Assignment expr);
        T visitLiteralExpr(Literal expr);
        T visitCommaExpressionsExpr(CommaExpressions expr);
        T visitUnaryExpr(Unary expr);
        T visitVariableExpr(Variable expr);
    }
    static class Lambda extends Expr {
        Lambda(List<Token> args, Expr expression, Boolean isStatic) {
            this.args = args;
            this.expression = expression;
            this.isStatic = isStatic;
        }

        final List<Token> args;
        final Expr expression;
        final Boolean isStatic;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLambdaExpr(this);
    }
 }
    static class Anon extends Expr {
        Anon(List<Stmt.Fun> methods) {
            this.methods = methods;
        }

        final List<Stmt.Fun> methods;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAnonExpr(this);
    }
 }
    static class ExtendAnon extends Expr {
        ExtendAnon(Expr.Anon def, Token father) {
            this.def = def;
            this.father = father;
        }

        final Expr.Anon def;
        final Token father;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExtendAnonExpr(this);
    }
 }
    static class Get extends Expr {
        Get(Expr obj, Token name) {
            this.obj = obj;
            this.name = name;
        }

        final Expr obj;
        final Token name;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetExpr(this);
    }
 }
    static class Set extends Expr {
        Set(Expr obj, Token name, Expr value) {
            this.obj = obj;
            this.name = name;
            this.value = value;
        }

        final Expr obj;
        final Token name;
        final Expr value;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetExpr(this);
    }
 }
    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        final Expr left;
        final Token operator;
        final Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
 }
    static class Logika extends Expr {
        Logika(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        final Expr left;
        final Token operator;
        final Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogikaExpr(this);
    }
 }
    static class Call extends Expr {
        Call(Expr callee, Token paren, List<Expr> args) {
            this.callee = callee;
            this.paren = paren;
            this.args = args;
        }

        final Expr callee;
        final Token paren;
        final List<Expr> args;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }
 }
    static class While extends Expr {
        While(Expr cond, Expr body) {
            this.cond = cond;
            this.body = body;
        }

        final Expr cond;
        final Expr body;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileExpr(this);
    }
 }
    static class With extends Expr {
        With(List<Stmt.Var> stmts, Expr body) {
            this.stmts = stmts;
            this.body = body;
        }

        final List<Stmt.Var> stmts;
        final Expr body;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWithExpr(this);
    }
 }
    static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }

        final Expr expression;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
 }
    static class If extends Expr {
        If(Expr cond, Expr iftrue, Expr iffalse) {
            this.cond = cond;
            this.iftrue = iftrue;
            this.iffalse = iffalse;
        }

        final Expr cond;
        final Expr iftrue;
        final Expr iffalse;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfExpr(this);
    }
 }
    static class Block extends Expr {
        Block(List<Stmt> stmts) {
            this.stmts = stmts;
        }

        final List<Stmt> stmts;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockExpr(this);
    }
 }
    static class Assignment extends Expr {
        Assignment(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        final Token name;
        final Expr value;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignmentExpr(this);
    }
 }
    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        final Object value;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
 }
    static class CommaExpressions extends Expr {
        CommaExpressions(List<Expr> expressionList) {
            this.expressionList = expressionList;
        }

        final List<Expr> expressionList;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCommaExpressionsExpr(this);
    }
 }
    static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        final Token operator;
        final Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
 }
    static class Variable extends Expr {
        Variable(Token name) {
            this.name = name;
        }

        final Token name;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }
 }
    abstract <T> T accept(Visitor<T> visitor);

}
