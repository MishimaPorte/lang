package guru;

import static guru.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import guru.Expr.Anon;
import guru.Expr.Assignment;
import guru.Expr.Block;
import guru.Expr.Call;
import guru.Expr.ExtendAnon;
import guru.Expr.Get;
import guru.Expr.If;
import guru.Expr.Lambda;
import guru.Expr.Logika;
import guru.Expr.Set;
import guru.Expr.Variable;
import guru.Expr.While;
import guru.Expr.With;
import guru.Parser.Void;
import guru.Stmt.Class;
import guru.Stmt.Const;
import guru.Stmt.ExtendClass;
import guru.Stmt.Fun;
import guru.Stmt.Print;
import guru.Stmt.Statement;
import guru.Stmt.Var;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment globals = new Environment();
    private Environment env = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    public Interpreter() {
        GInstance clocks = new GInstance();
        clocks.fields.put("measure", new GCallable() {
            public static final String name = "measure"; 
            @Override
            public String toString() { return "function [native] " + name; }
            @Override
            public int arity() { return -1; }
            @Override
            public Object call(Interpreter i, List<Object> args) {
                double start = System.currentTimeMillis() / 1000.0;
                GCallable f = (GCallable) args.get(0);
                if (args.size()-1 > f.arity()) return null;
                f.call(i, args.subList(1, args.size()));
                return (Double) (System.currentTimeMillis() / 1000.0) - start;
            }
            @Override
            public boolean isStatic() {
                return true;
            }
        });
        clocks.fields.put("start", (Double) (System.currentTimeMillis() / 1000.0));
        clocks.fields.put("now", new GCallable() {
            public static final String name = "elapsed"; 
            @Override
            public String toString() { return "function [native] " + name; }
            @Override
            public int arity() { return 0; }
            @Override
            public Object call(Interpreter i, List<Object> args) {
                return (double) System.currentTimeMillis() / 1000.0;
            }
            @Override
            public boolean isStatic() {
                return true;
            }
        });
        GInstance runtime = new GInstance();
        runtime.fields.put("UnixTime", clocks);
        runtime.fields.put("print", new GCallable() {
            public static final String name = "print"; 
            @Override
            public String toString() { return "function [native] " + name; }
            @Override
            public int arity() { return -1; }
            @Override
            public Object call(Interpreter i, List<Object> args) {
                StringBuilder sb = new StringBuilder();
                for (Object obj: args) {
                    sb.append(obj != null ? obj != Parser.Void.VOID ? obj : "void" : "nothing");
                    sb.append(" ");
                }
                System.out.println(sb);
                return null;
            }
            @Override
            public boolean isStatic() {
                return true;
            }
        });
        globals.def("runtime", runtime);
    }

    static class RE extends RuntimeException {
        final Token token;

        public RE(String message, Token token) {
            super(message);
            this.token = token;
        }

        public static Double chNum(Token operator, Object operand) {
            if (operand instanceof Double) return (Double) operand;
            throw new RE("Operand must be a number.", operator);
        }
    }

    public Object run(Expr expr) {
        try {
            return expr.accept(this);
        } catch (RE err) {
            System.out.println("RUNTIME ERROR [line: " + err.token.line + "]:");
            System.out.println("    " + err.getMessage() + " | " + err.token.type);
            return null;
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;

        return true;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(expr.right.accept(this));
            case MINUS:
                return - RE.chNum(expr.operator, expr.right.accept(this));
            default:
                return null;
        }
    }
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = expr.left.accept(this);
        Object right = expr.right.accept(this);
        switch (expr.operator.type) {
            case BANG_EQUAL:
                return !(left == null ? right == null ? true : false : left.equals(right));
            case EQUAL_EQUAL:
                return left == null ? right == null ? true : false : left.equals(right);
            case GREATER_EQUAL:
                return RE.chNum(expr.operator, left) >= RE.chNum(expr.operator, right);
            case GREATER:
                return RE.chNum(expr.operator, left) > RE.chNum(expr.operator, right);
            case LESS_EQUAL:
                return RE.chNum(expr.operator, left) <= RE.chNum(expr.operator, right);
            case LESS:
                return RE.chNum(expr.operator, left) < RE.chNum(expr.operator, right);
            case MINUS:
                return RE.chNum(expr.operator, left) - RE.chNum(expr.operator, right);
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (Double) left + (Double) right;
                if (left instanceof String && right instanceof String)
                    return (String) left + (String) right;
                throw new RE("invalid types for sum operation;", expr.operator);
            case SLASH:
                return RE.chNum(expr.operator, left) / RE.chNum(expr.operator, right);
            case STAR:
                return RE.chNum(expr.operator, left) * RE.chNum(expr.operator, right);
            default:
                return null;
        }
    }

    @Override
    public Object visitCommaExpressionsExpr(Expr.CommaExpressions expr) {
        Object ret = null;
        for (Expr e : expr.expressionList) {
            ret = e.accept(this);
        }
        return ret;
    }
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public Object visitBlockExpr(Block expr) {
        Environment prev = this.env;
        try {
            Object ret = null;
            this.env = new Environment(this.env);
            for (Stmt stmt : expr.stmts) {
                if (stmt instanceof Stmt.Statement) {
                    ret = ((Stmt.Statement) stmt).expression.accept(this);
                    continue;
                }
                stmt.accept(this);
            }
            return ret;
        } finally {
            this.env = prev;
        }
    }

    @Override
    public Object visitIfExpr(If expr) {
        if (isTruthy(expr.cond.accept(this))) {
            return expr.iftrue.accept(this);
        }

        return expr.iffalse == null ? null : expr.iffalse.accept(this);
    }

    @Override
    public Object visitAssignmentExpr(Assignment expr) {
        Object val = expr.value.accept(this);
        Integer distance = locals.get(expr);
        if (distance != null) {
            env.assignAt(distance, expr.name.lexeme, val);
        } else {
            globals.assign(expr.name, val);
        }
        return val;
    }



    @Override
    public Object visitLambdaExpr(Lambda expr) {
        Environment e = env;
        return new GCallable() {

            @Override
            public String toString() {
                return "function anon";
            }
            @Override
            public int arity() {
                return expr.args.size();
            }
            @Override
            public Object call(Interpreter i, List<Object> args) {
                Environment prev = i.env;
                try {
                    i.env = new Environment(e);
                    int index = 0;
                    for (Token argname : expr.args) {
                        i.env.def(argname.lexeme, args.get(index++));
                    }
                    return expr.expression.accept(i);
                } finally {
                    i.env = prev;
                }
            }
            @Override
            public boolean isStatic() {
                return expr.isStatic;
            }
        };
    }

    @Override
    public Void visitFunStmt(Fun stmt) {
        Environment e = env;
        env.def(stmt.name.lexeme, new GCallable() {
            @Override
            public String toString() {
                return "function " + stmt.name.lexeme;
            }
            @Override
            public int arity() {
                return stmt.args.size();
            }
            @Override
            public Object call(Interpreter i, List<Object> args) {
                Environment prev = i.env;
                try {
                    i.env = new Environment(e);
                    int index = 0;
                    for (Token argname : stmt.args) {
                        i.env.def(argname.lexeme, args.get(index++));
                    }
                    return stmt.expression.accept(i);
                } finally {
                    i.env = prev;
                }
            }
            @Override
            public boolean isStatic() {
                return stmt.isStatic;
            }
        });

        return null;
    }

    @Override
    public Void visitStatementStmt(Statement stmt) {
        stmt.expression.accept(this);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object val = stmt.expression.accept(this);
        System.out.println(val == null ? "nothing" : val);
        return null;
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return env.getAt(distance, expr.name.lexeme);
        } else {
            return globals.eval(expr.name);
        }
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        env.def(stmt.name.lexeme, stmt.expression == null ? null : stmt.expression.accept(this));
        return null;
    }

    @Override
    public Object visitLogikaExpr(Logika expr) {
        Object left = expr.left.accept(this);

        if (expr.operator.type == OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }
        return expr.right.accept(this);
    }

    @Override
    public Object visitWhileExpr(While expr) {
        Object res = null;
        while (isTruthy(expr.cond.accept(this))) {
            res = expr.body.accept(this);
        }

        return res;
    }

    @Override
    public Object visitWithExpr(With expr) {
        Environment prev = this.env;
        try {
            this.env = new Environment(this.env);
            for (Stmt stmt : expr.stmts) {
                stmt.accept(this);
            }
            return expr.body.accept(this);
        } finally {
            this.env = prev;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }

    @Override
    public Object visitCallExpr(Call expr) {
        Object callee = expr.callee.accept(this);
        if (!(callee instanceof GCallable)) throw new RE("not a funciton, you moron", expr.paren);
        GCallable func = (GCallable) callee;
        List<Object> fargs = new ArrayList<>(expr.args.size());
        for (Expr arg : expr.args) {
            fargs.add(arg.accept(this));
        }
        if (expr.args.size() < func.arity()) {
            return new GCallable() {

                @Override
                public String toString() {
                    return "function curried ["+(func.toString()) + "] with args ("+expr.args.size()+")";
                }
                @Override
                public Object call(Interpreter i, List<Object> args) {
                    List<Object> curried = new ArrayList<>(expr.args.size() + args.size());
                    fargs.forEach(curried::add);
                    args.forEach(curried::add);
                    return func.call(i, curried);
                }

                @Override
                public int arity() {
                    return func.arity() - expr.args.size();
                }
                @Override
                public boolean isStatic() {
                    return func.isStatic();
                }

            };
        }
        if (expr.args.size() != func.arity() && func.arity() != -1) throw new RE("Not Enough (or too many) arguments: expected "+ func.arity() + ", got: "+ expr.args.size(), expr.paren);

        return func.call(this, fargs);
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    @Override
    public Void visitClassStmt(Class stmt) {
        env.def(stmt.name.lexeme, null); 
        GClass cl = new GClass(stmt.name.lexeme);
        env.assign(stmt.name, cl);
        Environment prev = this.env;
        try {
            this.env = new Environment(this.env);
            for (Stmt.Fun f : stmt.methods) {
                f.accept(this);
            }
            this.env.map.forEach(new Object() {
                void run(String name, Object val) {
                    cl.methods.put(name, val);
                    if (((GCallable)val).isStatic()) cl.fields.put(name, val);
                };
            }::run);
        } finally {
            this.env = prev;
        }
        return null;
    }

    @Override
    public Object visitAnonExpr(Anon expr) {
        GClass cl = new GClass();
        Environment prev = this.env;
        try {
            this.env = new Environment(this.env);
            for (Stmt.Fun f : expr.methods) {
                f.accept(this);
            }
            this.env.map.forEach(new Object() {
                void run(String name, Object val) {
                    cl.methods.put(name, val);
                    if (((GCallable)val).isStatic()) cl.fields.put(name, val);
                };
            }::run);
        } finally {
            this.env = prev;
        }

        return cl;
    }

    @Override
    public Object visitGetExpr(Get expr) {
        Object obj = expr.obj.accept(this);

        if (obj instanceof GInstance) {
            Object o = ((GInstance) obj).get(expr.name);
            if (expr.name.lexeme.equals("new")) throw new RE("no direct constructor invocation, you moron", expr.name);
            if (o instanceof GCallable) {
                GCallable orig = (GCallable) o;
                int arity = orig.arity();
                return orig.isStatic() ? orig : new GCallable() {
                    @Override
                    public Object call(Interpreter i, List<Object> args) {
                        args.add(0, obj);
                        return orig.call(i, args);
                    }
                    @Override
                    public int arity() {
                        return arity == -1 ? arity : arity - 1;
                    }
                    @Override
                    public boolean isStatic() {
                        return true;
                    }
                };
            }
            return o;
        }

        return new RE("no such thing here", expr.name);
    }

    @Override
    public Object visitSetExpr(Set expr) {
        Object val = expr.value.accept(this);
        Object obj = expr.obj.accept(this);

        if (obj instanceof GInstance) {
            ((GInstance) obj).assign(expr.name, val);
            return val;
        }

        throw new RE("could not assign unassignable", expr.name);
    }

    @Override
    public Void visitConstStmt(Const stmt) {
        env.constdef(stmt.name.lexeme, stmt.expression == null ? null : stmt.expression.accept(this));
        return null;
    }

    @Override
    public Void visitExtendClassStmt(ExtendClass stmt) {
        GClass sup = (GClass) env.eval(stmt.father);
        if (sup == null) throw new RE("no father class with such a name found", stmt.father);
        stmt.def.accept(this);
        GClass sub = (GClass) env.eval(stmt.def.name);
        for (String name : sup.methods.keySet()) {
            if (!sub.methods.containsKey(name)) sub.methods.put(name, sup.methods.get(name));
        }

        return null;
    }

    @Override
    public Object visitExtendAnonExpr(ExtendAnon expr) {
        GClass sup = (GClass) env.eval(expr.father);
        if (sup == null) throw new RE("no father class with such a name found", expr.father);
        GClass son = (GClass) expr.def.accept(this);
        for (String name : sup.methods.keySet()) {
            if (!son.methods.containsKey(name)) son.methods.put(name, sup.methods.get(name));
        }

        return son;
    }

}
