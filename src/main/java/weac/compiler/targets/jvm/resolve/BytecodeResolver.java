package weac.compiler.targets.jvm.resolve;

import org.objectweb.asm.Opcodes;
import weac.compiler.CompileUtils;
import weac.compiler.resolve.NativeCodeResolver;
import weac.compiler.resolve.Resolver;
import weac.compiler.resolve.ResolvingContext;
import weac.compiler.resolve.VariableMap;
import weac.compiler.resolve.insn.ResolvedInsn;
import weac.compiler.resolve.values.NullValue;
import weac.compiler.resolve.values.Value;
import weac.compiler.targets.jvm.resolve.insn.BytecodeSequencesInsn;
import weac.compiler.utils.WeacType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

public class BytecodeResolver extends NativeCodeResolver implements Opcodes {

    private static final Map<String, Integer> opcodeNames = new HashMap<>();

    /**
     * List of the operand count for each JVM opcode. Negative number (denoted <code>a</code>) means "at least <code>a</code> operands"
     */
    private static final int[] opcodeOperandCount = new int[256];
    private static final BytecodeSequence[] opcodeSequences = new BytecodeSequence[256];
    static {
        Field[] fields = Opcodes.class.getFields();
        for(Field f : fields) {
            if(f.getType() == Integer.TYPE) {
                try {
                    opcodeNames.put(f.getName().toLowerCase(), f.getInt(null));
                    System.out.println("!!! "+f.getName());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        setOperandCount(0, NOP, ARRAYLENGTH, AALOAD, BALOAD, CALOAD, DALOAD, FALOAD, IALOAD, LALOAD, SALOAD, AASTORE,
                BASTORE, CASTORE, DASTORE, FASTORE, IASTORE, LASTORE, SASTORE,
                MONITORENTER, MONITOREXIT, ARETURN, ATHROW,
                POP, DRETURN, FRETURN, IRETURN, LRETURN, D2F,
                D2I, D2L, DNEG, F2D, F2I, F2L, FNEG, I2B, I2C, I2D, I2F, I2L, I2S, INEG, L2D, L2F, L2I, LNEG, DUP,
                DADD, DCMPG, DCMPL, DDIV, DMUL, DREM, DSUB, FADD, FCMPG, FCMPL, FDIV, FMUL, FREM, FSUB, IADD, IAND,
                IDIV, IMUL, IOR, IREM, ISHL, ISHR, ISUB, IUSHR, IXOR, LADD, LAND, LCMP, LDIV, LMUL, LOR, LREM, LSHL,
                LSHR, LSUB, LUSHR, LXOR, SWAP, DUP_X1, DUP_X2, DUP2_X1, POP2, DUP2, DUP2_X2, ICONST_M1, ICONST_0,
                DCONST_0, FCONST_0, LCONST_0, ICONST_1, DCONST_1, FCONST_1, LCONST_1, ICONST_2, FCONST_2, ICONST_3,
                ICONST_4, ICONST_5, RETURN, ACONST_NULL);
        setOperandCount(1, NEWARRAY, BIPUSH, RET, ASTORE, DSTORE, FSTORE, ISTORE, LSTORE, ALOAD, DLOAD, FLOAD, ILOAD, LDC, LLOAD,
                ANEWARRAY, CHECKCAST, INSTANCEOF, NEW, IF_ICMPEQ, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ICMPLT, IF_ICMPNE, JSR,
                SIPUSH, GOTO, IFEQ, IFGE, IFGT, IFLE, IFLT, IFNE, IFNONNULL, IFNULL, IF_ACMPEQ, IF_ACMPNE, MULTIANEWARRAY);
        setOperandCount(2, IINC);
        setOperandCount(3, INVOKEDYNAMIC, INVOKEINTERFACE, INVOKESPECIAL, INVOKEVIRTUAL, PUTFIELD, PUTSTATIC, GETSTATIC, GETFIELD,
                INVOKESTATIC);
        setOperandCount(-3, LOOKUPSWITCH, TABLESWITCH);

        setSimpleSequence(SimpleBytecodeSequence::new, NOP, ARRAYLENGTH, AALOAD, BALOAD, CALOAD, DALOAD, FALOAD, IALOAD, LALOAD, SALOAD, AASTORE,
                BASTORE, CASTORE, DASTORE, FASTORE, IASTORE, LASTORE, SASTORE,
                MONITORENTER, MONITOREXIT, ARETURN, ATHROW,
                POP, DRETURN, FRETURN, IRETURN, LRETURN, D2F,
                D2I, D2L, DNEG, F2D, F2I, F2L, FNEG, I2B, I2C, I2D, I2F, I2L, I2S, INEG, L2D, L2F, L2I, LNEG, DUP,
                DADD, DCMPG, DCMPL, DDIV, DMUL, DREM, DSUB, FADD, FCMPG, FCMPL, FDIV, FMUL, FREM, FSUB, IADD, IAND,
                IDIV, IMUL, IOR, IREM, ISHL, ISHR, ISUB, IUSHR, IXOR, LADD, LAND, LCMP, LDIV, LMUL, LOR, LREM, LSHL,
                LSHR, LSUB, LUSHR, LXOR, SWAP, DUP_X1, DUP_X2, DUP2_X1, POP2, DUP2, DUP2_X2, ICONST_M1, ICONST_0,
                DCONST_0, FCONST_0, LCONST_0, ICONST_1, DCONST_1, FCONST_1, LCONST_1, ICONST_2, FCONST_2, ICONST_3,
                ICONST_4, ICONST_5, RETURN, ACONST_NULL);
    }

    private static void setSequence(BytecodeSequence sequence, int... opcodes) {
        for(int op : opcodes) {
            opcodeSequences[op] = sequence;
        }
    }

    private static void setSimpleSequence(Function<Integer, SimpleBytecodeSequence> sequence, int... opcodes) {
        for(int op : opcodes) {
            opcodeSequences[op] = sequence.apply(op);
        }
    }

    private static void setOperandCount(int count, int... opcodes) {
        for(int op : opcodes) {
            opcodeOperandCount[op] = count;
        }
    }

    public BytecodeResolver(Resolver resolver, WeacType selfType, ResolvingContext context) {
        super(resolver, selfType, context);
    }

    @Override
    public void resolve(String code, WeacType currentType, VariableMap map, Map<WeacType, VariableMap> variableMaps, Stack<Value> valueStack, List<ResolvedInsn> insns) {
        List<BytecodeSequence> sequences = new ArrayList<>();
        String[] lines = code.split("\n");
        for(String l : lines) {
            l = CompileUtils.trimStartingSpace(l).replace("\r", "");
            if(l.isEmpty())
                continue;
            int end = l.indexOf(' ');
            if(end < 0)
                end = l.length();
            String opcodeName = l.substring(0, end);
            String[] operands = readOperands(l.substring(opcodeName.length()));
            int opcode = opcodeNames.getOrDefault(opcodeName.toLowerCase(), -1);
            if(opcode < 0) {
                System.err.println("Invalid opcode: "+opcodeName); // todo line and error
            } else {
                int count = operands.length;
                int expectedCount = opcodeOperandCount[opcode];
                if(expectedCount < 0) {
                    if(count < -expectedCount) {
                        System.err.println("Operand count is not enough in "+l+", must be at least "+(-expectedCount)+" (was "+count+")"); // todo line and error
                        continue;
                    }
                } else {
                    if(count != expectedCount) {
                        System.err.println("Operand count is not correct in "+l+", must be exactly "+expectedCount+" (was "+count+")"); // todo line and error
                        continue;
                    }
                }
                sequences.add(opcodeSequences[opcode]);

                // Pushes and pops types from the value stack depending on the opcode
                switch (opcode) {
                    case ARETURN:
                    case IRETURN:
                    case LRETURN:
                    case FRETURN:
                    case DRETURN:
                        valueStack.pop();
                        break;

                    case ACONST_NULL:
                        valueStack.push(new NullValue());
                        break;

                    default:
                        System.err.println("Value stack not yet implemented for "+opcodeName);
                        break;
                }
            }
        }
        insns.add(new BytecodeSequencesInsn(sequences));
    }

    private String[] readOperands(String s) {
        List<String> list = new ArrayList<>();
        String[] parts = s.split(" ");
        for(String p : parts) {
            if(!p.isEmpty())
                list.add(p);
        }
        return list.toArray(new String[list.size()]);
    }
}
