import weac.compiler.utils.Identifier;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestIdentifiers {

    @Test
    public void nestedGenericType() {
        assertTrue("Identifier 'List<Either<OtherType, AnotherType>, SomethingElse, WowAgainSomethingElse<A<B<C>>>>' must be valid", new Identifier("List<Either<OtherType, AnotherType>, SomethingElse, WowAgainSomethingElse<A<B<C>>>>").isValid());
    }

    @Test
    public void genericType() {
        assertTrue("Identifier 'SomeType<OtherType>' must be valid", new Identifier("SomeType<OtherType>").isValid());
    }

    @Test
    public void multipleGenericParameters() {
        assertTrue("Identifier 'SomeType<OtherType, AnotherType>' must be valid", new Identifier("SomeType<OtherType, AnotherType>").isValid());
    }

    @Test
    public void simpleID() {
        assertTrue("Identifier 'SomeType' must be valid", new Identifier("SomeType").isValid());
    }

    @Test
    public void arrayID() {
        assertTrue("Identifier 'SomeType[]' must be valid", new Identifier("SomeType[]").isValid());
    }

    @Test
    public void unaryOperatorOverload() {
        assertTrue("Identifier 'unary++' must be valid", new Identifier("unary++").isValid());
    }

    @Test
    public void binaryOperatorOverload() {
        assertTrue("Identifier 'operator*' must be valid", new Identifier("operator*").isValid());
    }

    @Test
    public void iDontCareAboutTheActualName() {
        assertTrue("Identifier '_' must be valid", new Identifier("_").isValid());
    }

    @Test
    public void pointerID() {
        assertTrue("Identifier 'SomeType~' must be valid", new Identifier("SomeType~").isValid());
    }

    @Test
    public void testCoherence() {
        assertFalse("Identifier 'Some*Type' must not be valid", new Identifier("Some*Type").isValid());
        assertFalse("Identifier 'SomeType*<' must not be valid", new Identifier("SomeType*<").isValid());
        assertFalse("Identifier 'SomeType]' must not be valid", new Identifier("SomeType]").isValid());
        assertFalse("Identifier 'SomeType][' must not be valid", new Identifier("SomeType][").isValid());
        assertFalse("Identifier 'SomeType<]' must not be valid", new Identifier("SomeType<]").isValid());
    }
}
