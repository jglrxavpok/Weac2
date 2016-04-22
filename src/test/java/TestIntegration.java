import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class TestIntegration extends Tests {

    @Test
    public void test0() throws IOException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, InstantiationException {
        String[] names = {
                "weac.lang.Object", "weac.lang.Void", "weac.lang.Primitive", "weac.lang.JavaPrimitive",
                "weac.lang.Double", "weac.lang.Float", "weac.lang.Int", "weac.lang.Boolean", "weac.lang.Pointer",
                "weac.lang.Math", "weac.lang.Console", "tests.TestMixin", "weac.lang.Application", "weac.lang.Interval", "tests.HelloWorld"
        };
        for(String n : names)
        {
            System.out.println(define(n).getCanonicalName());
        }
        Class<?> mathClass = Class.forName("weac.lang.Math");
        Class<?> pointerClass = Class.forName("weac.lang.Pointer");
        Class<?> intervalClass = Class.forName("weac.lang.Interval");
        if(mathClass != null)
        {
            Method m = mathClass.getDeclaredMethod("isInteger", Double.TYPE);
            m.setAccessible(true);
            Object instance = mathClass.getDeclaredField("__instance__").get(null);
            Object val = m.invoke(instance, 5.5);
            System.out.println("Math.__instance__.isInteger(5.5) = "+val);

            Method fact = mathClass.getDeclaredMethod("fact", Integer.TYPE);
            fact.setAccessible(true);
            val = fact.invoke(instance, 5);
            System.out.println("Math.__instance__.fact(5) = "+val);
        }

        Class<?> helloWorldClass = Class.forName("tests.HelloWorld");
        if(helloWorldClass != null) {
            Method m = helloWorldClass.getDeclaredMethod("main", String[].class);
            m.setAccessible(true);
            System.out.println("Helloworld: "+m.toGenericString());
            m.invoke(null, new Object[]{new String[] {"Test"}});
        }


        if(intervalClass != null) {
            double start = 0.1;
            double end = 100.1;
            double step = 1;
            Object intervalInstance = intervalClass.getDeclaredConstructor(Double.TYPE, Double.TYPE, Double.TYPE).newInstance(start, end, step);
            Method m = intervalClass.getDeclaredMethod("isIn", Double.TYPE);
            double index = 10.1;
            boolean result = (boolean) m.invoke(intervalInstance, index);
            System.out.println("["+start+".."+end+":"+step+"]isIn("+index+") = "+result);
            System.out.println("Java: "+isIn(index, start, end, step));
        }
    }

    private boolean isIn(double value, double start, double end, double step) {
        Boolean inRange = value >= start & value <= end;
        Boolean isValue;
        if(step == 0D) {
            isValue = true;
        } else {
            isValue = isInteger((value-start)/step);
        }
        return inRange & isValue;
    }

    private boolean isInteger(double value) {
        return (value % 1) == 0;
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
