package weac.compiler.resolve.insn;

import java.lang.reflect.Field;
import java.util.HashMap;

public interface ResolveOpcodes {

    int     NULL = 0x00,

            LABEL = 0x01,

            LOAD_FLOAT_CONSTANT = 0x02,
            LOAD_DOUBLE_CONSTANT = 0x03,
            LOAD_INTEGER_CONSTANT = 0x04,
            LOAD_SHORT_CONSTANT = 0x05,
            LOAD_LONG_CONSTANT = 0x06,
            LOAD_BOOL_CONSTANT = 0x07,
            LOAD_BYTE_CONSTANT = 0x08,
            LOAD_STRING_CONSTANT = 0x09,
            LOAD_CHARACTER_CONSTANT = 0x0A,
            FUNCTION_CALL = 0x0B,
            LOAD_NULL = 0x0C,

            RETURN = 0x10,
            OBJ_RETURN = 0x11,
            INT_RETURN = 0x12,
            FLOAT_RETURN = 0x13,
            SHORT_RETURN = 0x14,
            LONG_RETURN = 0x15,
            BOOL_RETURN = 0x16,
            CHAR_RETURN = 0x17,
            DOUBLE_RETURN = 0x18,
            BYTE_RETURN = 0x19,

            // Room for other types

            FIRST_RETURN_OPCODE = RETURN,
            LAST_RETURN_OPCODE = BYTE_RETURN,

            LOAD_LOCAL_VARIABLE = 0x20,
            LOAD_FIELD = 0x21,
            STORE_LOCAL_VARIABLE = 0x22,
            STORE_FIELD = 0x23,
            POP = 0x24,

            OBJ_EQUAL = 0x25,
            DUP = 0x26,
            NEW = 0x27,
            THROW = 0x28,
            IF_NOT_TRUE_JUMP = 0x29,
            JUMP = 0x2A,

            // Operations
            MULTIPLY = 0x30,
            ADD = 0x31,
            SUBTRACT = 0x32,
            DIVIDE = 0x33,
            MODULUS = 0x34,

            // comparison
            IS_ZERO = 0x40,
            COMPARE_ZERO = 0x41,
            LESS = 0x42,
            LESS_OR_EQUAL = 0x43,
            GREATER = 0x44,
            GREATER_OR_EQUAL = 0x45,

            CAST = 0x50,


            // Helper opcodes, not used by the JVM compiler
            FUNCTION_START = 0x100,
            LOCAL_VARIABLE_TABLE = 0x101,
            MAXS = 0x102

    ;

    HashMap<Integer, String> names = new HashMap<>();

    static String getName(int opcode) {
        if(names.isEmpty()) {
            Field[] fields = ResolveOpcodes.class.getDeclaredFields();
            for(Field field : fields) {
                if(field.getType() == Integer.TYPE) {
                    try {
                        int value = field.getInt(null);
                        if(!names.containsKey(value))
                            names.put(value, field.getName());
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
