import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestIntegration extends Tests {

    @Test
    public void test0() throws IOException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
        String[] names = {
                "weac.lang.Object", "weac.lang.Void", "weac.lang.Primitive", "weac.lang.JavaPrimitive",
                "weac.lang.Double", "weac.lang.Float", "weac.lang.Int", "weac.lang.Boolean", "weac.lang.Pointer",
                "weac.lang.Math"
        };
        for(String n : names)
        {
            System.out.println(define(n).getCanonicalName());
        }
        Class<?> mathClass = Class.forName("weac.lang.Math");
        if(mathClass != null)
        {
            Method m = mathClass.getDeclaredMethod("fact", Integer.TYPE);
            m.setAccessible(true);
            Object instance = mathClass.getDeclaredField("__instance__").get(null);
            Object val = m.invoke(instance, 5);
            System.out.println("Math.__instance__.fact(5) = "+val);
        }
    }

    private Class<?> define(String name) throws InvocationTargetException, IllegalAccessException, IOException {
        byte[] classData = readRaw("./monolith/"+name.replace(".", "/")+".class", false);
        return defineClass(name, classData);
    }

    private static Class<?> defineClass(String name, byte[] classData) throws InvocationTargetException, IllegalAccessException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if(cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        try {
            Method m = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            m.setAccessible(true);
            Class<?> result = (Class<?>) m.invoke(cl, name, classData, 0, classData.length);
            return result;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
