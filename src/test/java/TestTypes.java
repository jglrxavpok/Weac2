import org.jglrxavpok.weac.utils.Identifier;
import org.jglrxavpok.weac.utils.WeacType;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestTypes {

    @Test
    public void genericType() {
        WeacType type = new WeacType(WeacType.OBJECT_TYPE, new Identifier("List<String>"));
        assertTrue("Tested type ("+type+") must be valid.", type.isValid());
        assertEquals("List", type.getCoreType().toString());
        assertTrue(type.getGenericParameters().length == 1);
        assertEquals("String", type.getGenericParameters()[0].toString());
    }

    @Test
    public void arrayOfPointers() {
        WeacType type = new WeacType(WeacType.OBJECT_TYPE, new Identifier("String*[]"));
        assertTrue("Tested type ("+type+") must be valid.", type.isValid());
        assertEquals(type.getCoreType().toString(), "String");
        assertTrue(type.isArray());
        assertTrue(type.getArrayType().isPointer());
        assertEquals(type.getArrayType().getPointerType().toString(), "String");
    }

    @Test
    public void pointerOfArray() {
        WeacType type = new WeacType(WeacType.OBJECT_TYPE, new Identifier("String[]*"));
        assertTrue("Tested type ("+type+") must be valid.", type.isValid());
        assertEquals(type.getCoreType().toString(), "String");
        assertTrue(type.isPointer());
        assertTrue(type.getPointerType().isArray());
        assertEquals(type.getPointerType().getArrayType().toString(), "String");
    }

    @Test
    public void pointerType() {
        WeacType type = new WeacType(WeacType.OBJECT_TYPE, new Identifier("String*"));
        assertTrue("Tested type ("+type+") must be valid.", type.isValid());
        assertEquals(type.getCoreType().toString(), "String");
        assertTrue(type.isPointer());
    }

    @Test
    public void arrayType() {
        WeacType type = new WeacType(WeacType.OBJECT_TYPE, new Identifier("Char[]"));
        assertTrue("Tested type ("+type+") must be valid.", type.isValid());
        assertEquals(type.getCoreType().toString(), "Char");
        assertTrue(type.isArray());
    }

    @Test
    public void multipleGenericTypes() {
        WeacType type = new WeacType(WeacType.OBJECT_TYPE, new Identifier("Either<String, Integer>"));
        assertTrue("Tested type ("+type+") must be valid.", type.isValid());
        assertEquals(type.getCoreType().toString(), "Either");
        assertTrue(type.getGenericParameters().length == 2);
        assertEquals("String", type.getGenericParameters()[0].toString());
        assertEquals("Integer", type.getGenericParameters()[1].toString());
    }

}
