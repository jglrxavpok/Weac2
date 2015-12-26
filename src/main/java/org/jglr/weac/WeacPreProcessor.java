package org.jglr.weac;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Preprocess a source code.<br/>
 * The available preprocessing commands are:
 * <ul>
 *     <li><code>#ifndef &lt var &gt</code>: Keep following code if the <code>var</code> is not defined</li>
 *     <li><code>#ifdef &lt var &gt</code>: Keep following code if the <code>var</code> is defined</li>
 *     <li><code>#define &lt var &gt</code>: Defines <code>var</code> and gives it the value 1</li>
 *     <li><code>#define &lt var &gt &lt value &gt</code>: Defines <code>var</code> and gives it the value described by <code>value</code></li>
 * </ul>
 */
public class WeacPreProcessor extends WeacCompilePhase {

    private final Map<String, String> compilerDefinitions;
    private final Stack<Boolean> conditions;

    public WeacPreProcessor() {
        compilerDefinitions = new HashMap<>();
        conditions = new Stack<>();
    }

    /**
     * Preprocesses the source code
     * @param source
     *              The source code
     * @return
     *              The preprocessed source code
     */
    public String preprocess(String source) {
        conditions.clear();
        compilerDefinitions.clear();
        conditions.push(true);
        String[] lines = source.split("\n");
        StringBuilder builder = new StringBuilder();
        int lineIndex = 0;
        for(String l : lines) {
            l = trimStartingSpace(l);
            if(l.startsWith("#")) { // probably a precompile command
                String command = l.substring(1);
                if(!processCommand(command)) {
                    newError("Unknown precompile command "+command, lineIndex);
                }
            } else if(conditions.peek()) {
                builder.append(l);
                builder.append("\n");
            }
            lineIndex++;
        }
        return builder.toString();
    }

    /**
     * Processes a command line.
     * @param command
     *              The command line
     * @return
     *          Returns true if it was a valid command, false if not
     */
    private boolean processCommand(String command) {
        int end = command.indexOf(' ');
        if(end < 0)
            end = command.indexOf('\r');
        if(end < 0)
            end = command.indexOf('\n');
        if(end < 0)
            end = command.length();
        String actualCommand = command.substring(0, end);
        switch (actualCommand) {// todo
            case "ifdef": if(conditions.peek()) {
                String valueToCheck = command.replace(command+" ", "");
                boolean result = compilerDefinitions.containsKey(valueToCheck);
                conditions.push(result);
            }
            break;

            case "else": if(!conditions.peek()) {
                conditions.push(!conditions.pop());
            }
                break;

            case "ifndef": if(conditions.peek()) {
                String valueToCheck = command.replace(command+" ", "");
                boolean result = compilerDefinitions.containsKey(valueToCheck);
                conditions.push(!result);
            }
            break;

            case "define": if(conditions.peek()) {
                String[] arg = command.replace(command+" ", "").split(" ");
                String name = arg[0];
                String val;
                if(arg.length > 1) {
                    val = arg[1];
                } else {
                    val = "1";
                }
                compilerDefinitions.put(name, val);
            }
            break;

            case "end": conditions.pop();
            break;

            default:
                return false;
        }
        return true;
    }

}
