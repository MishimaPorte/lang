package guru;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


class GClass extends GInstance implements GCallable {

    public static final GClass klass = voidclass;
    public Map<String, Object> methods = new HashMap<>();
    final String name;

    public GClass(String name) {
        this.name = name;
    }

    public GClass() {
        this.name = "anonymous";
    }

    public String toString() {
        return "class [" + name + "]";
    }

    @Override
    public Object call(Interpreter i, List<Object> args) {
        GInstance inst = new GInstance(this);
        methods.forEach(inst.fields::put);

        Object constructor = methods.get("new");
        if (constructor != null) {
            args.add(0, inst);
            ((GCallable) constructor).call(i, args);
        }

        return inst;
    }


    @Override
    public int arity() {
        Object constructor = methods.get("new");
        return constructor == null ? -1 : ((GCallable) constructor).arity()-1;
    }

    @Override
    public boolean isStatic() {
        return true;
    }


}
