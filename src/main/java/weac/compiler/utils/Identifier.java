package weac.compiler.utils;

import org.jglr.flows.collection.VariableTopStack;
import weac.compiler.Keywords;
import weac.compiler.CompileUtils;
import weac.compiler.parser.Parser;

/**
 * A WeaC identifier, used for types, variable and method names
 */
public class Identifier {

    public static final char[] allowedCharacters = {'<', '>', '~', '[', ']', '_'};

    /**
     * The raw id
     */
    private final String id;

    /**
     * Used when having a problem to read the identifier
     */
    public static final Identifier INVALID = new Identifier("");

    public static final Identifier VOID = new Identifier("Void");
    private final boolean fullName;

    /**
     * Creates a new instance of {@link Identifier}
     * @param id
     *          The raw id
     */
    public Identifier(String id) {
        this(id, false);
    }

    public Identifier(String id, boolean fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return getId();
    }

    /**
     * Returns <code>true</code> if this identifier is a valid one. Returns <code>false</code> otherwise
     * @return
     *          <code>true</code> if this identifier is a valid one, <code>false</code> otherwise
     */
    public boolean isValid() {
        return isValid(id, fullName);
    }

    /**
     * Returns <code>true</code> if the identifier is a valid one. Returns <code>false</code> otherwise
     * @return
     *          <code>true</code> if the identifier is a valid one, <code>false</code> otherwise
     */
    public static boolean isValid(String potientialID) {
        return isValid(potientialID, false);
    }

    public static boolean isValid(String potientialID, boolean fullName) {
        for(Keywords w : Keywords.values()) {
           if(w.toString().toLowerCase().equals(potientialID)) {
               return false;
           }
        }
        if(potientialID.isEmpty())
            return false;
        if(!isIdentifierStart(potientialID.charAt(0))) {
            return false;
        }
        int unclosedAngle = 0;
        for(int i = 1;i<potientialID.length();i++) {
            char c = potientialID.charAt(i);
            if(c == '<') {
                unclosedAngle++;
            } else if(c == '>') {
                unclosedAngle--;
            }
            if(!isIdentifierPart(c, fullName)) {
                if(c == ',' || c == ' ') {
                    if(unclosedAngle <= 0) {
                        return false;
                    }
                } else {
                    String startString = potientialID.substring(0, i);
                    boolean isPotentialOperatorOverload = isValidOperatorOverloadStart(startString);
                    if(isPotentialOperatorOverload) {
                        String operator = CompileUtils.readOperator(potientialID.toCharArray(), i);
                        if(operator != null && potientialID.equals(startString+operator)) {
                            return checkCoherence(potientialID);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        return checkCoherence(potientialID);
    }

    /**
     * Check if given <code>potientialID</code> is coherent (ie special characters such as '[', ']', '<', '>', '*' are properly matched and at the end of the identifier)
     * @param potientialID
     * @return
     */
    private static boolean checkCoherence(String potientialID) {
        return checkSpecialAtEnd(potientialID) && checkClosed(potientialID);
    }

    private static boolean checkClosed(String potientialID) {
        // TODO: Error messages?
        VariableTopStack<Integer> unclosedBrackets = new VariableTopStack<>();
        unclosedBrackets.setCurrent(0);

        VariableTopStack<Integer> unbalancedAngleBrackets = new VariableTopStack<>();
        unbalancedAngleBrackets.setCurrent(0);
        for(int i = 0;i<potientialID.length();i++) {
            char c = potientialID.charAt(i);
            switch (c) {
                case '<':
                    unbalancedAngleBrackets.setCurrent(unbalancedAngleBrackets.getCurrent()+1);
                    unclosedBrackets.push().setCurrent(0);
                    break;

                case '>':
                    unbalancedAngleBrackets.setCurrent(unbalancedAngleBrackets.getCurrent()-1);
                    if(unbalancedAngleBrackets.getCurrent() < 0) {
                        return false;
                    }
                    unclosedBrackets.pop();
                    break;

                case '[':
                    unclosedBrackets.setCurrent(unclosedBrackets.getCurrent()+1);
                    unbalancedAngleBrackets.push().setCurrent(0);
                    break;

                case ']':
                    unclosedBrackets.setCurrent(unclosedBrackets.getCurrent()-1);
                    if(unclosedBrackets.getCurrent() < 0) {
                        return false;
                    }
                    unbalancedAngleBrackets.pop();
                    break;
            }
        }
        return unbalancedAngleBrackets.getLevel() == 0 && unclosedBrackets.getLevel() == 0 && unbalancedAngleBrackets.getCurrent() == 0 && unclosedBrackets.getCurrent() == 0;
    }

    private static boolean checkSpecialAtEnd(String potientialID) {
        int i = 0;
        for(;i<potientialID.length();i++) {
            char c = potientialID.charAt(i);
            if(!(Character.isJavaIdentifierPart(c) || c == '_' || c == '.' || c == '<' || c == '>' || c == ',' || c == ' ')) {
                break;
            }
        }
        for(;i<potientialID.length();i++) {
            char c = potientialID.charAt(i);
            if(Character.isJavaIdentifierPart(c)) {
                break;
            }
        }
        return i >= potientialID.length();
    }

    public static boolean isAllowedCharacter(char c, boolean fullName) {
        if(c == '.' && fullName)
            return true;
        for(char allowed : allowedCharacters) {
            if(c == allowed) {
                return true;
            }
        }
        return false;
    }

    public static Identifier read(Parser parser) {
        Identifier id = read(parser.getCharacters(), parser.getPosition());
        parser.forward(id.getId().length());
        return id;
    }

    /**
     * Reads an identifier from text
     * @param chars
     *              The source code characters
     * @param start
     *              The offset at which to start reading in the source code
     * @return
     *          The read identifier or {@link #INVALID}
     *
     * @see java.lang.Character#isJavaIdentifierPart(char)
     * @see java.lang.Character#isJavaIdentifierStart(char)
     */
    public static Identifier read(char[] chars, int start) {
        StringBuilder builder = new StringBuilder();
        if(!isIdentifierStart(chars[start])) {
            return INVALID;
        }
        builder.append(chars[start]);
        int unclosedAngle = 0;
        for(int i = start+1;i<chars.length;i++) {
            char c = chars[i];
            if(c == '<') {
                unclosedAngle++;
            } else if(c == '>') {
                unclosedAngle--;
            }
            if(!isIdentifierPart(c)) {
                if(c == ',') {
                    if(unclosedAngle <= 0) {
                        break;
                    }
                } else {
                    String startString = builder.toString();
                    boolean isPotentialOperatorOverload = isValidOperatorOverloadStart(startString);
                    if(isPotentialOperatorOverload) {
                        String operator = CompileUtils.readOperator(chars, i);
                        if (operator != null && !operator.isEmpty()) {
                            return new Identifier(startString+operator);
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            builder.append(c);
        }
        return new Identifier(builder.toString());
    }

    public static boolean isValidOperatorOverloadStart(String start) {
        return start.equals("operator") || start.equals("unary");
    }

    public static boolean isIdentifierStart(char c) {
        return c == '_' || Character.isJavaIdentifierStart(c);
    }

    public static boolean isIdentifierPart(char c) {
        return isIdentifierPart(c, false);
    }

    public static boolean isIdentifierPart(char c, boolean fullName) {
        return Character.isJavaIdentifierStart(c)  || isAllowedCharacter(c, fullName) || (c >= '0' && c <= '9');
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Identifier) {
            return ((Identifier) obj).id.equals(id);
        }
        return false;
    }

    /**
     * Returns the raw id
     * @return
     *          Raw ID
     */
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
