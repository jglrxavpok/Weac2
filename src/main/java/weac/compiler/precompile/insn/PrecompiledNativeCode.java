package weac.compiler.precompile.insn;

public class PrecompiledNativeCode extends PrecompiledInsn {
    private final String code;

    public PrecompiledNativeCode(String code) {
        super(NATIVE_CODE);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
