// SOURCE FILE (.ws)
package my.package
import weac.lang.Math
import foo.Bar as Foobar

// Line comment
/*
    Multiline comment
*/
class Parent /* full name: my.package.Parent */ {

	private MyStruct field1;
	private MyStruct field2;

	Parent() {
		field1 = new MyStruct((Byte)(Math.sin()*100), "Text");
		field2 = new MyStruct;
		field2.flag = 0b;
	}
}

class Child > Parent + MyInterface {

	Child() {
		super();
	}
}

struct MyStruct /* full name: my.package.MyStruct */ {
	Byte flag;
	String text = "Default value";
}
