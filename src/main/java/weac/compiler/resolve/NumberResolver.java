package weac.compiler.resolve;

import weac.compiler.CompileUtils;
import weac.compiler.precompile.PreCompiler;
import weac.compiler.resolve.insn.*;

import java.math.BigDecimal;
import java.util.Arrays;

public class NumberResolver extends CompileUtils {

    public ResolvedInsn resolve(String num) {
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
                            digit = Arrays.binarySearch(PreCompiler.extraDigits, c)+10;
                        }
                        convertedNumber += digit * Math.pow(base, power);
                    }
                }
                if(type == null) {
                    // find type
                    type = "long";
                    if(convertedNumber <= Integer.MAX_VALUE) {
                        type = "int";
                    }
                }

                switch (type) {
                    case "long":
                        return new LoadLongInsn(convertedNumber);

                    case "int":
                        return new LoadIntInsn((int)convertedNumber);

                    case "short":
                        return new LoadShortInsn((short)convertedNumber);

                    case "byte":
                        return new LoadByteInsn((byte)convertedNumber);

                    case "float":
                        return new LoadFloatInsn((float)convertedNumber);

                    case "double":
                        return new LoadDoubleInsn((double)convertedNumber);
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
                    return new LoadFloatInsn(decimal.floatValue());
                } else {
                    return new LoadDoubleInsn(decimal.doubleValue());
                }
            } else {
                switch (type) {
                    case "float":
                        return new LoadFloatInsn(decimal.floatValue());

                    case "double":
                        return new LoadDoubleInsn(decimal.doubleValue());
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

    // TODO: copypasted from PreCompiler
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
