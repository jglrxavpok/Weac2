package org.jglr.weac.precompile.insn;

public interface Opcodes {

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
            LOAD_CHARACTER_CONSTANT = 0x0B
    ;
}
