package weac.compiler.resolve;

import weac.compiler.targets.jvm.JVMWeacTypes;
import weac.compiler.utils.WeacType;

import java.util.HashMap;
import java.util.Map;

public class VariableMap {

    private final Map<String, Integer> localIDs;
    private final Map<String, WeacType> localTypes;
    private final Map<Integer, String> localNames;
    private final Map<String, WeacType> fieldTypes;
    private int localIndex;

    public VariableMap() {
        localTypes = new HashMap<>();
        localIDs = new HashMap<>();
        localNames = new HashMap<>();
        fieldTypes = new HashMap<>();
    }

    public void registerField(String name, WeacType type) {
        fieldTypes.put(name, type);
    }

    public int registerLocal(String name, WeacType type) {
        int index = getCurrentLocalIndex();
        localIDs.put(name, index);
        localNames.put(index, name);
        localTypes.put(name, type);
        if(type.equals(JVMWeacTypes.LONG_TYPE) || type.equals(JVMWeacTypes.DOUBLE_TYPE)) {
            localIndex++;
        }
        localIndex++;
        return index;
    }

    public int getCurrentLocalIndex() {
        return localIndex;
    }

    public boolean localExists(String name) {
        return getLocalIndex(name) != -1;
    }

    public boolean fieldExists(String name) {
        return fieldTypes.containsKey(name);
    }

    public String getLocalName(int index) {
        return localNames.getOrDefault(index, "$INVALID ID$");
    }

    public WeacType getLocalType(String name) {
        return localTypes.getOrDefault(name, JVMWeacTypes.VOID_TYPE);
    }

    public WeacType getFieldType(String name) {
        return fieldTypes.getOrDefault(name, JVMWeacTypes.VOID_TYPE);
    }

    public int getLocalIndex(String name) {
        return localIDs.getOrDefault(name, -1);
    }

    public boolean localExists(int i) {
        return localNames.containsKey(i);
    }

    public Map<String, WeacType> getRegistredFields() {
        return fieldTypes;
    }

    public Map<Integer, String> getLocalNames() {
        return localNames;
    }

    public void setLocalType(String name, WeacType type) {
        if(!localTypes.containsKey(name))
            throw new IllegalArgumentException("Local variable "+name+" does not exist");
        localTypes.put(name, type);
    }
}
