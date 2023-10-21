package guru;

import java.util.List;

interface GCallable {
    boolean isStatic();
    Object call(Interpreter i, List<Object> args);
    int arity();
}
