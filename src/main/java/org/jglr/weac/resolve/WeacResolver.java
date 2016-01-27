package org.jglr.weac.resolve;

import org.jglr.weac.WeacCompileUtils;
import org.jglr.weac.precompile.WeacPreCompiler;
import org.jglr.weac.precompile.insn.PrecompileOpcodes;
import org.jglr.weac.precompile.insn.WeacLoadBooleanConstant;
import org.jglr.weac.precompile.insn.WeacLoadNumberConstant;
import org.jglr.weac.precompile.insn.WeacPrecompiledInsn;
import org.jglr.weac.precompile.structure.*;
import org.jglr.weac.resolve.insn.*;
import org.jglr.weac.resolve.structure.*;
import org.jglr.weac.utils.Identifier;
import org.jglr.weac.utils.WeacImport;
import org.jglr.weac.utils.WeacType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class WeacResolver extends WeacCompileUtils {

    public WeacResolvedSource process(WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        WeacResolvedSource resolved = new WeacResolvedSource();
        resolved.packageName = source.packageName;
        resolved.classes = new ArrayList<>();

        source.classes.forEach(s -> resolved.classes.add(resolve(source, s, sideClasses)));
        return resolved;
    }

    private WeacResolvedClass resolve(WeacPrecompiledSource source, WeacPrecompiledClass aClass, WeacPrecompiledClass[] sideClasses) {
        WeacResolvedClass resolvedClass = new WeacResolvedClass();
        resolvedClass.access = aClass.access;
        resolvedClass.annotations = aClass.annotations;
        resolvedClass.classType = aClass.classType;
        resolvedClass.parents = getInterfaces(aClass, aClass.interfacesImplemented, source, sideClasses);
        resolvedClass.fullName = getFullName(aClass);
        resolvedClass.enumConstants = resolveEnums(aClass.enumConstants);
        resolvedClass.isAbstract = aClass.isAbstract;
        resolvedClass.isMixin = aClass.isMixin;
        resolvedClass.name = aClass.name;

        WeacMixedContentClass toMixIn = resolveMixins(resolvedClass, resolvedClass.parents.getMixins());
        resolvedClass.methods = resolveMethods(resolvedClass, aClass, source, sideClasses, toMixIn);

        resolvedClass.fields = resolveFields(resolvedClass, aClass, source, sideClasses, toMixIn);
        return resolvedClass;
    }

    private List<WeacResolvedField> resolveFields(WeacResolvedClass resolvedClass, WeacPrecompiledClass aClass, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses, WeacMixedContentClass toMixIn) {
        if(resolvedClass.isMixin) {
            return Collections.emptyList();
        }
        List<WeacResolvedField> fields = new LinkedList<>();
        toMixIn.fields.forEach(m -> addOrOverrideField(resolveSingleField(m, source, sideClasses), fields));

        aClass.fields.forEach(m -> addOrOverrideField(resolveSingleField(m, source, sideClasses), fields));
        return fields;
    }

    private WeacResolvedField resolveSingleField(WeacPrecompiledField field, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        WeacResolvedField resolvedField = new WeacResolvedField();
        resolvedField.name = field.name;
        resolvedField.access = field.access;
        resolvedField.type = resolveType(field.type, source, sideClasses);
        resolvedField.defaultValue.addAll(resolveSingleExpression(field.defaultValue));
        return resolvedField;
    }

    private List<WeacResolvedMethod> resolveMethods(WeacResolvedClass resolvedClass, WeacPrecompiledClass aClass, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses, WeacMixedContentClass toMixIn) {
        List<WeacResolvedMethod> methods = new LinkedList<>();
        toMixIn.methods.forEach(m -> addOrOverride(resolveSingleMethod(m, source, sideClasses), methods));

        aClass.methods.forEach(m -> addOrOverride(resolveSingleMethod(m, source, sideClasses), methods));
        return methods;
    }

    private WeacResolvedMethod resolveSingleMethod(WeacPrecompiledMethod precompiledMethod, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        WeacResolvedMethod method = new WeacResolvedMethod();
        method.access = precompiledMethod.access;
        method.annotations.addAll(precompiledMethod.annotations);
        method.argumentNames.addAll(precompiledMethod.argumentNames);
        method.isAbstract = precompiledMethod.isAbstract;
        method.isConstructor = precompiledMethod.isConstructor;
        method.name = precompiledMethod.name;
        method.returnType = resolveType(precompiledMethod.returnType, source, sideClasses);
        precompiledMethod.argumentTypes.stream()
                .map(t -> resolveType(t, source, sideClasses))
                .forEach(method.argumentTypes::add);
        method.name = precompiledMethod.name;
        return method;
    }

    private WeacType resolveType(Identifier type, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        if(type.getId().equalsIgnoreCase("Void"))
            return WeacType.VOID_TYPE;
        WeacType intermediateType = new WeacType(type);
        String core = intermediateType.getCoreType().getIdentifier().getId();
        WeacPrecompiledClass typeClass = findClass(core, source, sideClasses);
        if(typeClass == null) {
            newError("Invalid type: "+type.getId(), -1);
        }
        return new WeacType(typeClass.fullName+(type.getId().substring(core.length())), true);
    }

    private void addOrOverrideField(WeacResolvedField toAdd, List<WeacResolvedField> fieldList) {
        Iterator<WeacResolvedField> iterator = fieldList.iterator();
        while (iterator.hasNext()) {
            WeacResolvedField existing = iterator.next();
            if(existing.name.equals(toAdd.name)) { // same name
                iterator.remove();
            }
        }
        fieldList.add(toAdd);
    }

    private void addOrOverride(WeacResolvedMethod toAdd, List<WeacResolvedMethod> methodList) {
        Iterator<WeacResolvedMethod> iterator = methodList.iterator();
        while (iterator.hasNext()) {
            WeacResolvedMethod existing = iterator.next();
            if(existing.name.equals(toAdd.name)) { // same name
                if(existing.argumentTypes.size() == toAdd.argumentTypes.size()) { // same count of arguments
                    for(int i = 0;i<existing.argumentTypes.size();i++) { // check if same argument types
                        WeacType toAddArgType = toAdd.argumentTypes.get(i);
                        WeacType existingArgType = existing.argumentTypes.get(i);
                        if(!toAddArgType.equals(existingArgType)) {
                            break; // not matching types, abord
                        }
                    }
                    iterator.remove();
                }
            }
        }
        methodList.add(toAdd);
    }

    private WeacMixedContentClass resolveMixins(WeacResolvedClass resolvedClass, List<WeacPrecompiledClass> mixins) {

        WeacMixedContentClass contentClass = new WeacMixedContentClass();

        for(WeacPrecompiledClass mixin : mixins) {
            // if some methods have the same name, last one should win
            contentClass.methods.addAll(mixin.methods);
            contentClass.fields.addAll(mixin.fields);
        }
        return contentClass;
    }

    private String getFullName(WeacPrecompiledClass aClass) {
        return (aClass.packageName == null || aClass.packageName.isEmpty()) ? aClass.name : aClass.packageName+"."+aClass.name;
    }

    private ClassParents getInterfaces(WeacPrecompiledClass aClass, List<String> interfacesImplemented, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        ClassParents parents = new ClassParents();

        List<String> interfaces = new ArrayList<>();
        interfaces.addAll(interfacesImplemented);

        if(!interfaces.contains(aClass.motherClass) && aClass.motherClass != null)
            interfaces.add(aClass.motherClass);

        WeacPrecompiledClass superclass = null;
        for(String inter : interfaces) {
            WeacPrecompiledClass clazz = findClass(inter, source, sideClasses);
            if(clazz == null) {
                newError("Class not found: "+inter, -1);
                return parents;
            }
            switch(clazz.classType) {
                case ANNOTATION:
                    // TODO
                    break;

                case ENUM:
                    newError("Cannot extend an enum", -1);
                    break;

                case OBJECT:
                    newError("Cannot extend an object", -1);
                    break;

                case STRUCT:
                    newError("Cannot extend a struct", -1);
                    break;

                case INTERFACE:
                    parents.addInterface(clazz);
                    break;

                case CLASS:
                    if(clazz.isMixin && !aClass.isMixin) {
                        parents.addMixin(clazz);
                        parents.addInterface(clazz);
                    } else if(superclass == null && !aClass.isMixin) {
                        superclass = clazz;
                    } else if(!aClass.isMixin) {
                        newError("Cannot extend multiple classes that are not mixins.", -1);
                    } else {
                        newError("Cannot inherit classes to mixins.", -1);
                    }
                    break;
            }
        }
        if(superclass == null) {
            superclass = new WeacPrecompiledClass();
            superclass.name = "WeacObject";
            superclass.packageName = "weac.lang";
            superclass.fullName = "weac.lang.WeacObject";
        }
        parents.setSuperclass(superclass);
        return parents;
    }

    private WeacPrecompiledClass findClass(String inter, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        WeacPseudoResolvedClass clazz = new WeacPseudoResolvedClass();

        String transformedName = transform(inter, source.imports);
        System.out.println(inter+" transformed to "+transformedName);
        // check in source if can be found
        for (WeacPrecompiledClass sourceClass : source.classes) {
            if(sourceClass.fullName.equals(transformedName)) {
                return sourceClass;
            }

            if(sourceClass.name.equals(inter)) {
                return sourceClass;
            }
        }

        for (WeacPrecompiledClass sideClass : sideClasses) {
            if(sideClass.fullName.equals(transformedName)) {
                return sideClass;
            }
        }


        // test for base classes, because they are not necessarily imported
        for (WeacPrecompiledClass sideClass : sideClasses) {
            System.out.println(">>>>"+sideClass.fullName);
            if(sideClass.fullName.equals("weac.lang."+inter)) {
                return sideClass;
            }

            if(sideClass.fullName.equals("java.lang."+inter)) {
                return sideClass;
            }
        }

        return null;
    }

    private String transform(String name, List<WeacImport> imports) {
        for(WeacImport imp : imports) {
            if(imp.usageName != null) {
                if (imp.usageName.equals(name)) {
                    return imp.importedType;
                } else if(imp.importedType.endsWith("."+name)) {
                    return imp.importedType;
                }
            }
        }
        return name;
    }

    private List<WeacResolvedEnumConstant> resolveEnums(List<WeacPrecompiledEnumConstant> enumConstants) {
        List<WeacResolvedEnumConstant> resolvedConstants = new ArrayList<>();
        for(WeacPrecompiledEnumConstant cst : enumConstants) {
            WeacResolvedEnumConstant resolved = new WeacResolvedEnumConstant();
            resolved.name = cst.name;

            cst.parameters.stream()
                    .map(this::resolveSingleExpression)
                    .forEach(resolved.parameters::add);

            resolvedConstants.add(resolved);
        }
        return resolvedConstants;
    }

    private List<WeacResolvedInsn> resolveSingleExpression(List<WeacPrecompiledInsn> precompiled) {
        List<WeacResolvedInsn> insns = new LinkedList<>();
        for(int i = 0;i<precompiled.size();i++) {
            WeacPrecompiledInsn precompiledInsn = precompiled.get(i);
            if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_NUMBER_CONSTANT) {
                WeacLoadNumberConstant cst = ((WeacLoadNumberConstant) precompiledInsn);
                String numberRepresentation = cst.getValue();
                insns.add(resolveNumber(numberRepresentation));
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_BOOLEAN_CONSTANT) {
                WeacLoadBooleanConstant cst = ((WeacLoadBooleanConstant) precompiledInsn);
                insns.add(new WeacLoadBooleanInsn(cst.getValue()));
            }
        }
        return insns;
    }

    private WeacResolvedInsn resolveNumber(String num) {
        if(isInteger(num)) {
            int base = getBase(num);
            if(base > 0) {
                int start = 0;
                if(num.length() > 2) {
                    if (isBaseCharacter(num.charAt(1))) {
                        if(num.contains("#")) { // custom base, start after '#' symbol
                            start = num.indexOf('#')+1;
                        } else {
                            start = 2; // skips "0a" where a is the base character
                        }
                    }
                }
                String actualNumber = num.substring(start);
                String type = null;
                long convertedNumber;
                char[] chars = actualNumber.toCharArray();
                if(!isDigit(chars[chars.length-1], base)) {
                    type = deduceType(chars[chars.length-1]);
                    actualNumber = actualNumber.substring(0, actualNumber.length()-1);
                }
                if(base == 10) {
                    convertedNumber = Long.parseLong(actualNumber);
                } else {
                    convertedNumber = 0L;
                    chars = actualNumber.toCharArray();
                    for(int i = 0;i<chars.length;i++) {
                        char c = chars[i];
                        int power = chars.length-i-1;
                        int digit;
                        if(c >= '0' && c <= '9') {
                            digit = c-'0';
                        } else {
                            if(base == 16) {
                                c = Character.toUpperCase(c);
                            }
                            digit = Arrays.binarySearch(WeacPreCompiler.extraDigits, c)+10;
                        }
                        convertedNumber += digit * Math.pow(base, power);
                        System.out.println(">> "+power+" / "+digit+" / "+convertedNumber+" / "+c);
                    }
                }
                if(type == null) {
                    // find type
                    type = "long";
                    if(convertedNumber <= Integer.MAX_VALUE) {
                        type = "int";
                    }
                    if(convertedNumber <= Short.MAX_VALUE) {
                        type = "short";
                    }
                    if(convertedNumber <= Byte.MAX_VALUE) {
                        type = "byte";
                    }
                }

                switch (type) {
                    case "long":
                        return new WeacLoadLongInsn(convertedNumber);

                    case "int":
                        return new WeacLoadIntInsn((int)convertedNumber);

                    case "short":
                        return new WeacLoadShortInsn((short)convertedNumber);

                    case "byte":
                        return new WeacLoadByteInsn((byte)convertedNumber);

                    case "float":
                        return new WeacLoadFloatInsn((float)convertedNumber);

                    case "double":
                        return new WeacLoadDoubleInsn((double)convertedNumber);
                }
            }
            newError("Invalid base: "+base, -1); // TODO: line
        } else {
            String type = null;
            char[] chars = num.toCharArray();
            if(!isDigit(chars[chars.length-1], 10)) {
                type = deduceType(chars[chars.length-1]);
                num = num.substring(0, num.length()-1);
            }

            BigDecimal decimal = new BigDecimal(num);
            if(type == null) {
                if(decimal.compareTo(new BigDecimal(Float.MAX_VALUE)) <= 0) {
                    return new WeacLoadFloatInsn(decimal.floatValue());
                } else {
                    return new WeacLoadDoubleInsn(decimal.doubleValue());
                }
            } else {
                switch (type) {
                    case "float":
                        return new WeacLoadFloatInsn(decimal.floatValue());

                    case "double":
                        return new WeacLoadDoubleInsn(decimal.doubleValue());
                }
            }
        }
        return null;
    }

    private String deduceType(char c) {
        switch (c) {
            case 'f': // float
            case 'F': // float
                return "float";

            case 'd': // double
            case 'D': // double
                return "double";

            case 'l': // long
            case 'L': // long
                return "long";

            case 's': // short
            case 'S': // short
                return "short";

            case 'b': // byte
            case 'B': // byte
                return "byte";
        }
        return "double";
    }

    private boolean isInteger(String num) {
        return !num.contains(".");
    }

    private boolean hasBase(String num) {
        if(num.length() > 2) {
            if(isBaseCharacter(num.charAt(1))) {
                int base = getBase(num.charAt(1));
                if (base == -10) { // custom base
                    base = Integer.parseInt(readBase(num.toCharArray(), 2));
                }
                return base != -1;
            }
        }
        return false;
    }

    private int getBase(String num) {
        if(num.length() > 2) {
            if(isBaseCharacter(num.charAt(1))) {
                int base = getBase(num.charAt(1));
                if(base == -10) { // custom base
                    base = Integer.parseInt(readBase(num.toCharArray(), 2));
                }
                return base;
            } else {
                return 10;
            }
        }
        return 10;
    }

    // TODO: copypasted from WeacPreCompiler
    private boolean isBaseCharacter(char c) {
        switch (c) {
            case 'x':
            case 'b':
            case 'o':
                return true;

            case 'c':
                return true;
        }
        return false;
    }

    private String readBase(char[] chars, int offset) {
        StringBuilder builder = new StringBuilder();
        for(int i = offset;i<chars.length;i++) {
            char c = chars[i];
            if(c == '#') {
                break;
            } else if(isDigit(c, 10)) {
                builder.append(c);
            } else {
                newError("Invalid base character: "+c, -1); // TODO: find correct line
                break;
            }
        }
        return builder.toString();
    }

    private boolean isDigit(char c, int base) {
        if (base == 10) {
            return Character.isDigit(c);
        } else if(base < 10) {
            return Character.isDigit(c) && (c-'0') < base;
        } else {
            // ((c-'A')+10 < base) checks if the character is a valid digit character
            return Character.isDigit(c) || ((c-'A')+10 < base && (c-'A')+10 > 0);
        }
    }

    private int getBase(char c) {
        switch (c) {
            case 'x':
                return 16;

            case 'b':
                return 2;

            case 'o':
                return 8;

            case 'c':
                return -10;
        }
        return -1;
    }

}
