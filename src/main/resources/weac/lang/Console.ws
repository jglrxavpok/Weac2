package weac.lang

import java.lang.Object as JObject

object Console {

    Void writeLine(String line) {
        System.out.println(line);
    }

    Void writeLine(Int i) {
        System.out.println(i);
    }

    Void writeLine(Byte b) {
        System.out.println(b);
    }

    Void writeLine(Boolean b) {
        System.out.println(b);
    }

    Void writeLine(Float f) {
        System.out.println(f);
    }

    Void writeLine(Double d) {
        System.out.println(d);
    }

    Void writeLine(Char c) {
        System.out.println(c);
    }

    Void writeLine(Long l) {
        System.out.println(l);
    }

    Void writeLine(Short s) {
        System.out.println(s);
    }

    Void writeLine(JObject o) {
        System.out.println(o);
    }
}