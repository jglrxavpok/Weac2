package org.jglr.weac.precompile.insn;

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
            ARGUMENT_SEPARATOR = 0x11
    ;

    HashMap<Integer, String> names = new HashMap<>();

    static String getName(int opcode) {
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
