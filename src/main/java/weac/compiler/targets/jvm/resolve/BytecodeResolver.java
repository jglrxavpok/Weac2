package weac.compiler.targets.jvm.resolve;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import weac.compiler.CompileUtils;
import weac.compiler.resolve.NativeCodeResolver;
import weac.compiler.resolve.Resolver;
import weac.compiler.resolve.ResolvingContext;
import weac.compiler.resolve.VariableMap;
import weac.compiler.resolve.insn.ResolveOpcodes;
import weac.compiler.resolve.insn.ResolvedInsn;
import weac.compiler.resolve.values.ConstantValue;
import weac.compiler.resolve.values.FieldValue;
import weac.compiler.resolve.values.NullValue;
import weac.compiler.resolve.values.Value;
import weac.compiler.targets.jvm.JVMWeacTypes;
import weac.compiler.targets.jvm.compile.JVMCompiler;
import weac.compiler.targets.jvm.compile.PseudoInterpreter;
import weac.compiler.targets.jvm.resolve.insn.BytecodeSequencesInsn;
import weac.compiler.utils.Identifier;
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
    private static final PseudoInterpreter interpreter = new PseudoInterpreter();
    static {
        Field[] fields = Opcodes.class.getFields();
        for(Field f : fields) {
            if(f.getType() == Integer.TYPE) {
                try {
                    opcodeNames.put(f.getName().toLowerCase(), f.getInt(null));
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
                BytecodeSequence sequence = opcodeSequences[opcode];
                if(sequence != null) {
                    sequences.add(sequence);
                }

                switch(opcode) {
                    case GETSTATIC:
                    case GETFIELD:
                    case PUTSTATIC:
                    case PUTFIELD: {
                        String owner = operands[0];
                        checkValidInternalName(owner);
                        String name = operands[1];
                        checkValidUnqualifiedName(name);
                        String desc = operands[2];
                        checkValidDesc(desc);
                        sequences.add((mv, varIndex) -> mv.visitFieldInsn(opcode, owner, name, desc));

                        if (opcode == GETFIELD || opcode == PUTFIELD)
                            valueStack.pop();

                        if (opcode == GETFIELD || opcode == GETSTATIC) {
                            valueStack.push(new FieldValue(name, getTypeFromInternal(owner), getType(desc)));
                        } else {
                            valueStack.pop();
                        }
                    }
                        break;

                    case INVOKEDYNAMIC:
                    case INVOKEINTERFACE:
                    case INVOKESPECIAL:
                    case INVOKEVIRTUAL: {
                        String owner = operands[0];
                        checkValidInternalName(owner);
                        String name = operands[1];
                        checkValidUnqualifiedName(name);
                        String desc = operands[2];
                        checkValidMethodDesc(desc);
                        boolean fromInterface = opcode == INVOKEINTERFACE;
                        sequences.add((mv, varIndex) -> mv.visitMethodInsn(opcode, owner, name, desc, fromInterface));

                        String[] argumentDescriptions = splitDescList(desc.substring(1, desc.indexOf(')')));

                        if(opcode == INVOKEVIRTUAL || opcode == INVOKESPECIAL) // TODO: invokedynamic ?
                            valueStack.pop();
                        for(int i = 0;i<argumentDescriptions.length;i++)
                            valueStack.pop();
                        String returnTypeDesc = desc.substring(desc.indexOf(')')+1);
                        WeacType returnType = getType(returnTypeDesc);
                        if(!returnType.equals(JVMWeacTypes.VOID_TYPE)) {
                            valueStack.push(new ConstantValue(returnType));
                        }

                    }
                        break;

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

                    case POP2:
                        valueStack.pop();
                    case POP:
                        valueStack.pop();
                        break;

                    case LDC:
                        String loaded = operands[0];
                        if(loaded.endsWith(".class")) {
                            valueStack.push(new ConstantValue(JVMWeacTypes.CLASS_TYPE));
                            WeacType classType = getOwner().getTypeResolver().resolveType(new Identifier((loaded.substring(0, loaded.length()-6).replace(".", "/")), true), getContext());
                            sequences.add((mv, varOffset) -> mv.visitLdcInsn(JVMCompiler.toJVMType(classType)));
                        } else if(loaded.startsWith("\"") && loaded.endsWith("\"")) {
                            sequences.add((mv, o) -> mv.visitLdcInsn(loaded.substring(1, loaded.length()-1)));
                            valueStack.add(new ConstantValue(JVMWeacTypes.STRING_TYPE));
                        } else {
                            // load number
                            ResolvedInsn numberInsn = getOwner().getNumberResolver().resolve(loaded);
                            List<ResolvedInsn> list = Collections.singletonList(numberInsn);
                            Object value = interpreter.interpret(list);
                            sequences.add((mv, o) -> mv.visitLdcInsn(value));

                            valueStack.push(new ConstantValue(extractType(numberInsn)));
                        }
                        break;

                    case IADD:
                    case ISUB:
                    case IREM:
                    case IAND:
                    case IOR:
                    case IDIV:
                    case IMUL:
                        valueStack.pop();
                        valueStack.pop();
                        valueStack.push(new ConstantValue(JVMWeacTypes.INTEGER_TYPE));
                        break;

                    case FADD:
                    case FSUB:
                    case FREM:
                    case FDIV:
                    case FMUL:
                        valueStack.pop();
                        valueStack.pop();
                        valueStack.push(new ConstantValue(JVMWeacTypes.FLOAT_TYPE));
                        break;

                    case DADD:
                    case DSUB:
                    case DREM:
                    case DDIV:
                    case DMUL:
                        valueStack.pop();
                        valueStack.pop();
                        valueStack.push(new ConstantValue(JVMWeacTypes.DOUBLE_TYPE));
                        break;

                    case LADD:
                    case LSUB:
                    case LREM:
                    case LAND:
                    case LOR:
                    case LDIV:
                    case LMUL:
                        valueStack.pop();
                        valueStack.pop();
                        valueStack.push(new ConstantValue(JVMWeacTypes.LONG_TYPE));
                        break;

                    default:
                        System.err.println("Value stack not yet implemented for "+opcodeName);
                        break;
                }
            }
        }
        insns.add(new BytecodeSequencesInsn(sequences));
    }

    private WeacType extractType(ResolvedInsn number) {
        switch (number.getOpcode()) {
            case ResolveOpcodes.LOAD_BYTE_CONSTANT:
                return JVMWeacTypes.BYTE_TYPE;

            case ResolveOpcodes.LOAD_DOUBLE_CONSTANT:
                return JVMWeacTypes.DOUBLE_TYPE;

            case ResolveOpcodes.LOAD_FLOAT_CONSTANT:
                return JVMWeacTypes.FLOAT_TYPE;

            case ResolveOpcodes.LOAD_INTEGER_CONSTANT:
                return JVMWeacTypes.INTEGER_TYPE;

            case ResolveOpcodes.LOAD_LONG_CONSTANT:
                return JVMWeacTypes.LONG_TYPE;

            case ResolveOpcodes.LOAD_SHORT_CONSTANT:
                return JVMWeacTypes.SHORT_TYPE;

        }
        return JVMWeacTypes.VOID_TYPE;
    }


    private WeacType getTypeFromInternal(String internalName) {
        return getOwner().getTypeResolver().resolveType(new Identifier(internalName.replace("/", "."), true), getContext());
    }

    private String[] splitDescList(String list) {
        List<String> descList = new ArrayList<>();
        for (int i = 0; i < list.length(); i++) {
            i += extractOneDescription(list, i, descList);
        }
        return descList.toArray(new String[descList.size()]);
    }

    private int extractOneDescription(String list, int i, List<String> descList) {
        final int start = i;
        char c = list.charAt(i);
        switch (c) {
            case 'I':
            case 'F':
            case 'Z':
            case 'J':
            case 'D':
            case 'S':
            case 'B':
            case 'C':
            case 'V':
                descList.add(String.valueOf(c));
                break;

            case '[':
                List<String> result = new ArrayList<>();
                i += extractOneDescription(list, i+1, descList)+1;
                descList.add("["+result.get(0));
                break;

            case 'L':
                for (int j = i; j < list.length(); j++) {
                    char c1 = list.charAt(j);
                    if(c1 == ';') {
                        descList.add(list.substring(i, j+1));
                        return j-start;
                    }
                }
                throw new IllegalArgumentException("Could not find end of object descriptor in "+list);

            default:
                throw new IllegalArgumentException("Invalid descriptor character ('"+c+"') in descriptor list: "+list);
        }
        return i-start;
    }

    private WeacType getType(String desc) {
        switch (desc) {
            case "V":
                return JVMWeacTypes.VOID_TYPE;
            case "J":
                return JVMWeacTypes.LONG_TYPE;
            case "B":
                return JVMWeacTypes.BYTE_TYPE;
            case "C":
                return JVMWeacTypes.CHAR_TYPE;
            case "I":
                return JVMWeacTypes.INTEGER_TYPE;
            case "F":
                return JVMWeacTypes.FLOAT_TYPE;
            case "D":
                return JVMWeacTypes.DOUBLE_TYPE;
            case "[":
                return new WeacType(JVMWeacTypes.OBJECT_TYPE, getType(desc.substring(1)).getIdentifier());
            case "S":
                return JVMWeacTypes.SHORT_TYPE;
            case "Z":
                return JVMWeacTypes.BOOLEAN_TYPE;

            case "L":
                String internalName = desc.substring(1, desc.indexOf(';')).replace("/", ".");
                return getOwner().getTypeResolver().resolveType(new Identifier(internalName, true), getContext());
        }
        return null;
    }

    private void checkValidMethodDesc(String desc) {
        if(!desc.startsWith("("))
            throw new IllegalArgumentException("Valid method descriptions start with '(': "+desc);
        if(!desc.contains(")"))
            throw new IllegalArgumentException("Valid method descriptions must have ')' in them: "+desc);
        String argumentDescriptors = desc.substring(1, desc.indexOf(')'));
        checkValidDescList(argumentDescriptors);
        String returnTypeDescriptor = desc.substring(desc.indexOf(')')+1);
        checkValidDesc(returnTypeDescriptor);
    }

    private void checkValidDescList(String list) {
        String[] descriptors = splitDescList(list);
        for (String d : descriptors) {
            checkValidDesc(d);
        }
    }

    private void checkValidDesc(String desc) {
        if(desc.isEmpty())
            throw new IllegalArgumentException("Description cannot be empty");
        switch(desc) {
            case "I":
            case "F":
            case "Z":
            case "J":
            case "D":
            case "S":
            case "B":
            case "C":
            case "V":
                break;

            case "[":
                checkValidDesc(desc.substring(1)); // check if element type is valid
                break;

            default:
                if(!desc.startsWith("L"))
                    throw new IllegalArgumentException("Invalid description start, must be one of I, F, Z, J, D, S, B, C, L: "+desc);
                if(!desc.contains(";"))
                    throw new IllegalArgumentException("Invalid description, object types must contain ';' : "+desc);
                String fullName = desc.substring(1, desc.indexOf(';'));
                checkValidInternalName(fullName);
                break;
        }
    }

    private void checkValidInternalName(String name) {
        if(name.isEmpty()) {
            throw new IllegalArgumentException("Cannot use an empty name as a valid internal name");
        }
        if(Character.isDigit(name.charAt(0))) {
            throw new IllegalArgumentException("A valid internal name cannot start with a digit ("+name+")");
        }
        String[] identifierParts = name.split("/");
        for(String id : identifierParts) {
            checkValidUnqualifiedName(id);
        }
    }

    private void checkValidUnqualifiedName(String name) {
        if(name.contains(".") || name.contains(";") || name.contains("[") || name.contains("/"))
            throw new IllegalArgumentException("Unqualified name \""+name+"\" cannot contain any of these characters: . ; [ /");
    }

    private String[] readOperands(String s) {
        List<String> list = new ArrayList<>();
        boolean inQuote = false;
        boolean isEscaped = false;
        StringBuilder builder = new StringBuilder();
        for(char c : s.toCharArray()) {
            if(c == '\\') {
                isEscaped = !isEscaped;
                if(!isEscaped)
                    builder.append(c);
            } else if(c == ' ' && !inQuote) {
                if(builder.length() > 0)
                    list.add(builder.toString());
                builder.delete(0, builder.length());
            } else if(c == '"' && !isEscaped) {
                inQuote = !inQuote;
                builder.append(c);
            } else {
                isEscaped = false;
                builder.append(c);
            }
        }
        if(builder.length() > 0)
            list.add(builder.toString());
        return list.toArray(new String[list.size()]);
    }
}
