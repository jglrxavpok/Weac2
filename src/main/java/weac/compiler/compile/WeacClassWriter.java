package weac.compiler.compile;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;

public class WeacClassWriter extends MethodVisitor {

    public WeacClassWriter(MethodVisitor parent) {
        super(findAPIVersion(parent), parent);
    }

    private static int findAPIVersion(MethodVisitor parent) {
        try {
            Field field = MethodVisitor.class.getField("api");
            return field.getInt(parent);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return Opcodes.ASM5;
    }

    @Override
    public void visitInsn(int opcode) {
        if(opcode == Opcodes.RETURN) {

        } else {
            super.visitInsn(opcode);
        }
    }
}
