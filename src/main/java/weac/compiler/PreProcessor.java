package weac.compiler;

import weac.compiler.utils.SourceCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

// h0I I'm temmie!
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
public class PreProcessor extends CompilePhase<SourceCode, SourceCode> {

    private final Map<String, String> compilerDefinitions;
    private final Stack<Boolean> conditions;

    public PreProcessor() {
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
    @Override
    public SourceCode process(SourceCode source) {
        conditions.clear();
        compilerDefinitions.clear();
        conditions.push(true);
        String[] lines = source.getContent().split("\n");
        StringBuilder builder = new StringBuilder();
        int lineIndex = 0;
        for(String l : lines) {
            l = trimStartingSpace(l);
            if(l.startsWith("#")) { // probably a precompile command
                String command = l.substring(1);
                if(!processCommand(command)) {
                    newError("Unknown precompile command "+command, lineIndex);
                }
                if(conditions.peek() && (command.startsWith("target") || command.startsWith("version"))) {
                    builder.append(command).append('\n');
                }
            } else if(conditions.peek()) {
                builder.append(replaceDefined(l, compilerDefinitions));
                builder.append("\n");
            }
            lineIndex++;
        }
        return new SourceCode(source.getFileName(), builder.toString());
    }

    private String replaceDefined(String l, Map<String, String> compilerDefinitions) {
        for(Map.Entry<String, String> entry : compilerDefinitions.entrySet()) {
            if(entry != null) {
                l = l.replace(entry.getKey(), entry.getValue());
            }
        }
        return l;
    }

    @Override
    public Class<SourceCode> getInputClass() {
        return SourceCode.class;
    }

    @Override
    public Class<SourceCode> getOutputClass() {
        return SourceCode.class;
    }

    /**
     * Processes a command line.
     * @param command
     *              The command line
     * @return
     *          Returns true if it was a valid command, false if not
     */
    private boolean processCommand(String command) {
        command = command.replace("\r", "");
        int end = command.indexOf(' ');
        if(end < 0)
            end = command.indexOf('\r');
        if(end < 0)
            end = command.indexOf('\n');
        if(end < 0)
            end = command.length();
        String actualCommand = command.substring(0, end);
        switch (actualCommand) {
            case "ifdef":
                if(conditions.peek()) {
                String valueToCheck = command.replace(actualCommand+" ", "");
                boolean result = compilerDefinitions.containsKey(valueToCheck);
                conditions.push(result);
            }
            break;

            case "else": if(!conditions.peek()) {
                conditions.push(!conditions.pop());
            }
                break;

            case "ifndef":
                if(conditions.peek()) {
                    String valueToCheck = command.replace(actualCommand+" ", "");
                    boolean result = compilerDefinitions.containsKey(valueToCheck);
                    conditions.push(!result);
                }
            break;

            case "define": if(conditions.peek()) {
                String[] arg = command.replace(actualCommand+" ", "").split(" ");
                String name = arg[0];
                String val;
                if(arg.length > 1) {
                    val = String.join(" ", arg).replace(name+" ", "");
                } else {
                    val = "1";
                }
                val = val.replace("\r", "").replace("\n", "");
                compilerDefinitions.put(name, val);
            }
            break;

            case "end": conditions.pop();
            break;

            case "target":
            case "version":
                if(conditions.peek()) {
                    return true;
                }
                break;

            default:
                return false;
        }
        return true;
    }

}
