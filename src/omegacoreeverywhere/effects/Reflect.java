package omegacoreeverywhere.effects;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

/**
 * Just enough reflection to read the game's own screens: call a public no-argument method by name,
 * and find the building a panel belongs to.
 *
 * Starsector refuses java.lang.reflect to mod code when a class names it, so those classes are
 * fetched by name from the boot loader and driven through method handles, the same way the UI
 * framework in StopStackingMe and Hullmods - Renewed does it. No class of this mod names them.
 */
final class Reflect {

    private static final MethodHandle GET_METHOD;
    private static final MethodHandle INVOKE;
    private static final MethodHandle METHOD_ACCESSIBLE;
    private static final MethodHandle DECLARED_FIELDS;
    private static final MethodHandle FIELD_TYPE;
    private static final MethodHandle FIELD_GET;
    private static final MethodHandle FIELD_ACCESSIBLE;

    static {
        try {
            ClassLoader boot = Class.class.getClassLoader();
            Class<?> method = Class.forName("java.lang.reflect.Method", false, boot);
            Class<?> field = Class.forName("java.lang.reflect.Field", false, boot);
            Class<?> fields = Class.forName("[Ljava.lang.reflect.Field;", false, boot);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            GET_METHOD = lookup.findVirtual(Class.class, "getMethod",
                    MethodType.methodType(method, String.class, Class[].class));
            INVOKE = lookup.findVirtual(method, "invoke",
                    MethodType.methodType(Object.class, Object.class, Object[].class));
            METHOD_ACCESSIBLE = lookup.findVirtual(method, "setAccessible",
                    MethodType.methodType(void.class, boolean.class));
            DECLARED_FIELDS = lookup.findVirtual(Class.class, "getDeclaredFields", MethodType.methodType(fields));
            FIELD_TYPE = lookup.findVirtual(field, "getType", MethodType.methodType(Class.class));
            FIELD_GET = lookup.findVirtual(field, "get", MethodType.methodType(Object.class, Object.class));
            FIELD_ACCESSIBLE = lookup.findVirtual(field, "setAccessible",
                    MethodType.methodType(void.class, boolean.class));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Omega Core Everywhere cannot read the game's screens", e);
        }
    }

    private static final Object MISSING = new Object();
    /** Per class and name: the method, or MISSING. Looked up once, since the screen walk asks often. */
    private static final Map<Class<?>, Map<String, Object>> METHODS = new ConcurrentHashMap<>();
    /** Per class: the fields that can hold a building, so each class is searched once. */
    private static final Map<Class<?>, List<Object>> INDUSTRY_FIELDS = new ConcurrentHashMap<>();

    private Reflect() {}

    /** Calls a public method without arguments, or returns null if the object has none by that name. */
    static Object call(Object target, String name) {
        if (target == null) return null;
        Object method = METHODS.computeIfAbsent(target.getClass(), c -> new ConcurrentHashMap<>())
                .computeIfAbsent(name, n -> find(target.getClass(), n));
        if (method == MISSING) return null;
        try {
            return INVOKE.invoke(method, target, new Object[0]);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    private static Object find(Class<?> type, String name) {
        try {
            Object method = GET_METHOD.invoke(type, name, new Class<?>[0]);
            METHOD_ACCESSIBLE.invoke(method, true);
            return method;
        } catch (NoSuchMethodException e) {
            return MISSING;
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    /** The components directly inside a panel. */
    @SuppressWarnings("unchecked")
    static List<UIComponentAPI> children(UIPanelAPI panel) {
        Object children = call(panel, "getChildrenCopy");
        return children instanceof List ? (List<UIComponentAPI>) children : Collections.emptyList();
    }

    /** The building a panel was made for, if it keeps one, such as a row of the core picker. */
    static Industry industryOf(Object owner) {
        for (Object field : industryFields(owner.getClass())) {
            try {
                Object value = FIELD_GET.invoke(field, owner);
                if (value instanceof Industry) return (Industry) value;
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }
        return null;
    }

    private static List<Object> industryFields(Class<?> type) {
        List<Object> known = INDUSTRY_FIELDS.get(type);
        if (known != null) return known;
        List<Object> found = new ArrayList<>();
        try {
            for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Object field : (Object[]) DECLARED_FIELDS.invoke(c)) {
                    Class<?> fieldType = (Class<?>) FIELD_TYPE.invoke(field);
                    if (!Industry.class.isAssignableFrom(fieldType)) continue;
                    FIELD_ACCESSIBLE.invoke(field, true);
                    found.add(field);
                }
            }
        } catch (Throwable t) {
            throw rethrow(t);
        }
        INDUSTRY_FIELDS.put(type, found);
        return found;
    }

    private static RuntimeException rethrow(Throwable t) {
        if (t instanceof RuntimeException) return (RuntimeException) t;
        if (t instanceof Error) throw (Error) t;
        return new IllegalStateException(t);
    }
}
