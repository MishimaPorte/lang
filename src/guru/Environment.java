package guru;

import java.util.HashMap;
import java.util.Map;

import guru.Interpreter.RE;

class Environment {
    private final Environment parent;
    public final Map<String, Object> map = new HashMap<>();
    private final Map<String, Object> consts = new HashMap<>();

    Environment() {
        this.parent = null;
    }

    Environment(Environment parent) {
        this.parent = parent;
    }

    public void def(String name, Object val) {
        map.put(name, val);
    }

    public void constdef(String name, Object val) {
        consts.put(name, val);
    }
    public void assign(Token name, Object val) {
        if (!map.containsKey(name.lexeme)) {
            if (parent == null) throw new RE("Undefined variable '" + name.lexeme + "'.", name);
            parent.assign(name, val);
            return;
        }

        map.put(name.lexeme, val);
    }

    public boolean has(String name) {
        return map.containsKey(name) || parent == null || parent.has(name);
    }
    public Object assignAt(int dist, String name, Object val) {
        Environment environment = this;
        for (int i = 0; i < dist; i++) {
            environment = environment.parent;
        }

        return environment.map.put(name, val);
    }

    public Object getAt(int dist, String name) {
        Environment environment = this;
        for (int i = 0; i < dist; i++) {
            environment = environment.parent;
        }

        return environment.map.get(name);
    }
    public Object constget(Token name) {
        if (!consts.containsKey(name.lexeme)) {
            if (parent == null) return null;
            return parent.constget(name);
        }

        return consts.get(name.lexeme);
    }

    public Object eval(Token name) {
        Object clookup = constget(name);
        if (clookup != null) return clookup;
        return vareval(name);
    }

    private Object vareval(Token name) {
        if (!map.containsKey(name.lexeme)) {
            if (parent == null) throw new RE("Undefined variable '" + name.lexeme + "'.", name);
            return parent.vareval(name);
        }

        return map.get(name.lexeme);
    }
}
