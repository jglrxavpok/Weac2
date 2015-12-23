package org.jglr.weac.parse;

import org.jglr.weac.WeacCompilePhase;

import java.util.ArrayList;

public class WeacParser extends WeacCompilePhase {

    public WeacParser() {

    }

    public WeacParsedSource parseSource(String source) {
        WeacParsedSource parsedSource = new WeacParsedSource();
        parsedSource.imports = new ArrayList<>();
        parsedSource.classes = new ArrayList<>();
        source = removeComments(source);
        analyseHeader(parsedSource, source);
        parsedSource.sourceCode = source;
        return parsedSource;
    }

    private void analyseHeader(WeacParsedSource parsedSource, String source) {
        String[] lines = source.split("\n");
        int lineIndex = 0;
        int globalIndex = 0;
        for(String fullLine : lines) {
            String l = trimStartingSpace(fullLine).replace("\r", "");
            int end = l.indexOf(' ');
            if(end < 0)
                end = l.length();
            String command = l.substring(0, end);
            switch (command) {
                case "class":
                case "enum":
                case "struct":
                case "interface":
                    readClass(parsedSource, source.substring(globalIndex, source.indexOf("}", globalIndex)+1));
                    break;

                case "package":
                    String arg = trimStartingSpace(l.replaceFirst("package", ""));
                    String packageName = readUntilSpace(arg);
                    if(parsedSource.packageName != null) {
                        newError("Cannot set package name twice", lineIndex);
                    } else {
                        parsedSource.packageName = packageName;
                    }
                    break;

                case "import":
                    readImport(parsedSource, l, lineIndex);
                    break;

                default:
                    break;
            }
            lineIndex++;
            globalIndex+=fullLine.length();
        }
    }

    private void readImport(WeacParsedSource parsedSource, String line, int lineIndex) {
        String toImport = trimStartingSpace(line.replace("import", "")).replace("  ", "");
        String[] parts = toImport.split(" ");
        WeacParsedImport parsedImport = new WeacParsedImport();
        parsedImport.importedType = parts[0];
        if(parts.length > 2) {
            if(parts[1].equals("as")) {
                parsedImport.usageName = parts[2];
            } else {
                newError("Import statement not undertstood, should follow: 'import <name>' or 'import <name> as <usageName>''", lineIndex);
            }
        }
        parsedSource.imports.add(parsedImport);
    }

    private String readUntilSpace(String arg) {
        int end = arg.indexOf(' ');
        if(end < 0)
            end = arg.length();
        return arg.substring(0, end);
    }

    private void readClass(WeacParsedSource parsedSource, String source) {
        System.err.println(">>class "+source); // TODO
    }

    private String removeComments(String source) {
        char[] chars = source.toCharArray();
        StringBuilder builder = new StringBuilder();
        boolean inString = false;
        boolean inQuote = false;
        for(int i = 0;i<chars.length;i++) {
            boolean shouldAppend = true;
            char c = chars[i];
            char next = (i < chars.length-1) ? chars[i+1] : '\0';
            if(c == '/' && !inString) {
                if(next == '/') {
                    shouldAppend = false;
                    while(chars[i] != '\n') {
                        i++;
                    }
                } else if(next == '*') {
                    shouldAppend = false;
                    while(i < chars.length-1 && (chars[i] != '/' || chars[i-1] != '*')) {
                        i++;
                    }
                }
            }

            if(c == '\'' && !inString) {
                if(i != 0 && chars[i-1] != '\\') {
                    inQuote = !inQuote;
                }
            }

            if(c == '"' && !inQuote) {
                if(i != 0 && chars[i-1] != '\\') {
                    inString = !inString;
                }
            }

            if(shouldAppend)
                builder.append(c);
        }
        return builder.toString();
    }

    private String trimStartingSpace(String l) {
        while(l.startsWith(" ")) {
            l = l.substring(1);
        }
        while(l.startsWith("\t")) {
            l = l.substring(1);
        }
        return l;
    }
}
