package weac.compiler.targets.jvm;

import weac.compiler.utils.Constants;
import weac.compiler.utils.WeacType;

public class JVMWeacTypes {
    public static final WeacType JOBJECT_TYPE = new WeacType(null, "java.lang.Object", true);
    public static final WeacType INTERVAL_TYPE = new WeacType(JOBJECT_TYPE, "weac.lang.Interval", true);
    public static final WeacType STRING_TYPE = new WeacType(JOBJECT_TYPE, "java.lang.String", true);
    public static final WeacType VOID_TYPE = new WeacType(JOBJECT_TYPE, "weac.lang.Void", false);
    public static final WeacType OBJECT_TYPE = new WeacType(JOBJECT_TYPE, Constants.BASE_CLASS, true);
    public static final WeacType ARRAY_TYPE = new WeacType(OBJECT_TYPE, "$$Array", true);
    public static final WeacType POINTER_TYPE = new WeacType(OBJECT_TYPE, "weac.lang.Pointer<Type>", true);
    public static final WeacType NULL_TYPE = new WeacType(JOBJECT_TYPE, OBJECT_TYPE.getIdentifier());
    public static final WeacType PRIMITIVE_TYPE = new WeacType(OBJECT_TYPE, "weac.lang.Primitive", true);
    public static final WeacType CHAR_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Char", true);
    public static final WeacType SHORT_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Short", true);
    public static final WeacType LONG_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Long", true);
    public static final WeacType INTEGER_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Int", true);
    public static final WeacType FLOAT_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Float", true);
    public static final WeacType DOUBLE_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Double", true);
    public static final WeacType BYTE_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Byte", true);
    public static final WeacType BOOLEAN_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Boolean", true);
}
