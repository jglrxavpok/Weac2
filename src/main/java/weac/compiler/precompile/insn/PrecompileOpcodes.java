package weac.compiler.precompile.insn;

import java.lang.reflect.Field;
import java.util.HashMap;

public interface PrecompileOpcodes {

    int     NULL = 0x00,
            LOAD_NUMBER_CONSTANT = 0x01,
            FUNCTION_CALL = 0x02,
            LOAD_VARIABLE = 0x03,
            POP = 0x04,
            UNARY_OPERATOR = 0x05,
            BINARY_OPERATOR = 0x06,
            CREATE_ARRAY = 0x07,
            NEW = 0x08,
            RETURN = 0x09,
            LOAD_STRING_CONSTANT = 0x0A,
            LOAD_CHARACTER_CONSTANT = 0x0B,
            STORE_ARRAY = 0x0C,
            JUMP_IF_NOT_TRUE = 0x0D,
            LABEL = 0x0E,
            LOAD_BOOLEAN_CONSTANT = 0x0F,
            THIS = 0x10,
            ARGUMENT_SEPARATOR = 0x11,
            LOAD_NULL = 0x12,
            FUNCTION_START = 0x13,
            CAST = 0x14,
            DUP = 0x15,
            NEW_LOCAL = 0x16,
            JUMP = 0x17,
            THROW = 0x18,
            POP_INSTANCE_STACK = 0x19,
            ARRAY_START = 0x20,
            NATIVE_CODE = 0x21,
            LINE_NUMBER = 0x22
    ;

    HashMap<Integer, String> names = new HashMap<>();

    public static String getName(int opcode) {
        if(names.isEmpty()) {
            Field[] fields = PrecompileOpcodes.class.getDeclaredFields();
            for(Field field : fields) {
                if(field.getType() == Integer.TYPE) {
                    try {
                        names.put(field.getInt(null), field.getName());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if(names.containsKey(opcode)) {
            return names.get(opcode);
        }
        return "NULL";
    }
}
