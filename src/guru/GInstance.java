package guru;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ViewportLayout;

public class GInstance {
    public final Map<String,Object> fields = new HashMap<>();
    public final GClass klass;
    public static final GClass voidclass = new GClass("void");

    public GInstance(GClass klass) {
        this.klass = klass;
        fields.put("__CLASS__", klass);
    }
    public GInstance() {
        this.klass = voidclass;
        fields.put("__CLASS__", this.klass);
    }

    public Object get(Token name) {
        Object obj = fields.get(name.lexeme);
        return obj == null ? Parser.Void.VOID : obj;
    }

    public void assign(Token name, Object value) {
        if (klass == voidclass) throw new Interpreter.RE("no assigning to native things", name);
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass.toString() + " instance";
    }

}
