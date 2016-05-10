class C {
	a : Int;
	b : Bool;
	init(x : Int, y : Bool) : C {
           {
		a <- x;
		b <- y;
		self;
           }
	};
};

Class Main {

    c : C;
    math : MathClass;
    node : Node;


	main() : Object {{

      c.init(5,true);

      math.addNumbers(1,2);
      math.subtractNumbers(10,4);
      math.squareNumber(9);
      math.negateNumber(15);

      node.strset("this is a test");

	}};
};

class MathClass {

    num : Int;

    addNumbers(num1: Int, num2 : Int) : Int {
        {
        num <- num1 + num2;
        num;
        }
    };

    subtractNumbers(num1: Int, num2 : Int) : Int {
        {
        num <- num1 - num2;
        num;
        }
     };

    squareNumber(num1: Int) : Int {
        {
        num <- num1 * num1;
        num;
        }
    };

    negateNumber(num1 : Int) : Int {
        {
        num <- 0 - num1;
        num;
        }
    };
};

class Node inherits IO{
    str : String;
    node : Node;
    strset(istr:String):Object{
        str<-istr
    };

    add(inode:Node):Object{
        node<-inode
    };

    getstr():String{
        str
    };

    getnext():Node{
        node
    };
};