package guru;

import java.util.List;

abstract class Stmt {
    interface Visitor<T> {
        T visitStatementStmt(Statement stmt);
        T visitVarStmt(Var stmt);
        T visitConstStmt(Const stmt);
        T visitFunStmt(Fun stmt);
        T visitClassStmt(Class stmt);
        T visitExtendClassStmt(ExtendClass stmt);
        T visitPrintStmt(Print stmt);
    }
    static class Statement extends Stmt {
        Statement(Expr expression) {
            this.expression = expression;
        }

        final Expr expression;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitStatementStmt(this);
    }
 }
    static class Var extends Stmt {
        Var(Token name, Expr expression) {
            this.name = name;
            this.expression = expression;
        }

        final Token name;
        final Expr expression;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }
 }
    static class Const extends Stmt {
        Const(Token name, Expr expression) {
            this.name = name;
            this.expression = expression;
        }

        final Token name;
        final Expr expression;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitConstStmt(this);
    }
 }
    static class Fun extends Stmt {
        Fun(Token name, List<Token> args, Expr expression, Boolean isStatic) {
            this.name = name;
            this.args = args;
            this.expression = expression;
            this.isStatic = isStatic;
        }

        final Token name;
        final List<Token> args;
        final Expr expression;
        final Boolean isStatic;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunStmt(this);
    }
 }
    static class Class extends Stmt {
        Class(Token name, List<Stmt.Fun> methods) {
            this.name = name;
            this.methods = methods;
        }

        final Token name;
        final List<Stmt.Fun> methods;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }
 }
    static class ExtendClass extends Stmt {
        ExtendClass(Stmt.Class def, Token father) {
            this.def = def;
            this.father = father;
        }

        final Stmt.Class def;
        final Token father;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExtendClassStmt(this);
    }
 }
    static class Print extends Stmt {
        Print(Expr expression) {
            this.expression = expression;
        }

        final Expr expression;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }
 }
    abstract <T> T accept(Visitor<T> visitor);

}
