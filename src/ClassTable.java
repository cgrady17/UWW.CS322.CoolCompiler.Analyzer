import sun.reflect.generics.tree.Tree;

import javax.swing.plaf.metal.MetalIconFactory;
import java.io.PrintStream;
import java.util.*;

/** This class may be used to contain the semantic information such as
 * the inheritance graph.  You may use it or not as you like: it is only
 * here to provide a container for the supplied methods.  */
class ClassTable {
    private int semantErrors;
    private PrintStream errorStream;
	Classes basicClasses;
	LinkedHashMap<String, class_> classByName;
	LinkedHashMap<String, List<String>> childClasses;

    /** Creates data structures representing basic Cool classes (Object,
     * IO, Int, Bool, String).  Please note: as is this method does not
     * do anything useful; you will need to edit it to make if do what
     * you want.
     * */
    private void installBasicClasses() {
	AbstractSymbol filename 
	    = AbstractTable.stringtable.addString("<basic class>");
	
	// The following demonstrates how to create dummy parse trees to
	// refer to basic Cool classes.  There's no need for method
	// bodies -- these are already built into the runtime system.

	// IMPORTANT: The results of the following expressions are
	// stored in local variables.  You will want to do something
	// with those variables at the end of this method to make this
	// code meaningful.

	// The Object class has no parent class. Its methods are
	//        cool_abort() : Object    aborts the program
	//        type_name() : Str        returns a string representation 
	//                                 of class name
	//        copy() : SELF_TYPE       returns a copy of the object

	class_ Object_class = 
	    new class_(0, 
		       TreeConstants.Object_, 
		       TreeConstants.No_class,
		       new Features(0)
			   .appendElement(new method(0, 
					      TreeConstants.cool_abort, 
					      new Formals(0), 
					      TreeConstants.Object_, 
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.type_name,
					      new Formals(0),
					      TreeConstants.Str,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.copy,
					      new Formals(0),
					      TreeConstants.SELF_TYPE,
					      new no_expr(0))),
		       filename);
	
	// The IO class inherits from Object. Its methods are
	//        out_string(Str) : SELF_TYPE  writes a string to the output
	//        out_int(Int) : SELF_TYPE      "    an int    "  "     "
	//        in_string() : Str            reads a string from the input
	//        in_int() : Int                "   an int     "  "     "

	class_ IO_class = 
	    new class_(0,
		       TreeConstants.IO,
		       TreeConstants.Object_,
		       new Features(0)
			   .appendElement(new method(0,
					      TreeConstants.out_string,
					      new Formals(0)
						  .appendElement(new formal(0,
								     TreeConstants.arg,
								     TreeConstants.Str)),
					      TreeConstants.SELF_TYPE,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.out_int,
					      new Formals(0)
						  .appendElement(new formal(0,
								     TreeConstants.arg,
								     TreeConstants.Int)),
					      TreeConstants.SELF_TYPE,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.in_string,
					      new Formals(0),
					      TreeConstants.Str,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.in_int,
					      new Formals(0),
					      TreeConstants.Int,
					      new no_expr(0))),
		       filename);

	// The Int class has no methods and only a single attribute, the
	// "val" for the integer.

	class_ Int_class = 
	    new class_(0,
		       TreeConstants.Int,
		       TreeConstants.Object_,
		       new Features(0)
			   .appendElement(new attr(0,
					    TreeConstants.val,
					    TreeConstants.prim_slot,
					    new no_expr(0))),
		       filename);

	// Bool also has only the "val" slot.
	class_ Bool_class = 
	    new class_(0,
		       TreeConstants.Bool,
		       TreeConstants.Object_,
		       new Features(0)
			   .appendElement(new attr(0,
					    TreeConstants.val,
					    TreeConstants.prim_slot,
					    new no_expr(0))),
		       filename);

	// The class Str has a number of slots and operations:
	//       val                              the length of the string
	//       str_field                        the string itself
	//       length() : Int                   returns length of the string
	//       concat(arg: Str) : Str           performs string concatenation
	//       substr(arg: Int, arg2: Int): Str substring selection

	class_ Str_class =
	    new class_(0,
		       TreeConstants.Str,
		       TreeConstants.Object_,
		       new Features(0)
			   .appendElement(new attr(0,
					    TreeConstants.val,
					    TreeConstants.Int,
					    new no_expr(0)))
			   .appendElement(new attr(0,
					    TreeConstants.str_field,
					    TreeConstants.prim_slot,
					    new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.length,
					      new Formals(0),
					      TreeConstants.Int,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.concat,
					      new Formals(0)
						  .appendElement(new formal(0,
								     TreeConstants.arg, 
								     TreeConstants.Str)),
					      TreeConstants.Str,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.substr,
					      new Formals(0)
						  .appendElement(new formal(0,
								     TreeConstants.arg,
								     TreeConstants.Int))
						  .appendElement(new formal(0,
								     TreeConstants.arg2,
								     TreeConstants.Int)),
					      TreeConstants.Str,
					      new no_expr(0))),
		       filename);


		// Create an array of the previously instantiated class_s
		class_[] class_s = new class_[] {
				Object_class,
				IO_class,
				Bool_class,
				Str_class,
				Int_class
		};

		// Loop through each class_ and append to our basic classes <code>Classes</code>
		for (class_ class_ : class_s) basicClasses.appendElement(class_);

		// Add all basic classes and their corresponding names to the <code>LinkedHashMap</code>
		classByName.put(TreeConstants.Object_.toString(), Object_class);
		classByName.put(TreeConstants.IO.toString(), IO_class);
		classByName.put(TreeConstants.Bool.toString(), Bool_class);
		classByName.put(TreeConstants.Str.toString(), Str_class);
		classByName.put(TreeConstants.Int.toString(), Int_class);


		// We need to instantiate our list of classes and their children
		// to the basic classes. We'll then actually build out this list later
		childClasses.put(TreeConstants.Object_.toString(), new ArrayList<String>());
		childClasses.put(TreeConstants.IO.toString(), new ArrayList<String>());
		childClasses.put(TreeConstants.Bool.toString(), new ArrayList<String>());
		childClasses.put(TreeConstants.Str.toString(), new ArrayList<String>());
		childClasses.put(TreeConstants.Int.toString(), new ArrayList<String>());
	}
	


    public ClassTable(Classes cls) {
		semantErrors = 0;
		errorStream = System.err;

		basicClasses = new Classes(0);
		classByName = new LinkedHashMap<>();
		childClasses = new LinkedHashMap<>();

		installBasicClasses();

        fillChildClasses(cls);
    }

    /** Prints line number and file name of the given class.
     *
     * Also increments semantic error count.
     *
     * @param c the class
     * @return a print stream to which the rest of the error message is
     * to be printed.
     *
     * */
    public PrintStream semantError(class_ c) {
	return semantError(c.getFilename(), c);
    }

    /** Prints the file name and the line number of the given tree node.
     *
     * Also increments semantic error count.
     *
     * @param filename the file name
     * @param t the tree node
     * @return a print stream to which the rest of the error message is
     * to be printed.
     *
     * */
    public PrintStream semantError(AbstractSymbol filename, TreeNode t) {
	errorStream.print(filename + ":" + t.getLineNumber() + ": ");
	return semantError();
    }

    /** Increments semantic error count and returns the print stream for
     * error messages.
     *
     * @return a print stream to which the error message is
     * to be printed.
     *
     * */
    public PrintStream semantError() {
	semantErrors++;
	return errorStream;
    }

    /** Returns true if there are any static semantic errors. */
    public boolean errors() {
	return semantErrors != 0;
    }

	private void fillChildClasses(Classes classes) {
		// Add all existing classes, other than Object, to Object's list of children
		// At this time, it would only be the basic classes
		for (String className : childClasses.keySet()) {
			if (Objects.equals(className, TreeConstants.Object_.toString())) continue;

			childClasses.get(TreeConstants.Object_.toString()).add(className);
		}

		for (Enumeration e = classes.getElements(); e.hasMoreElements();) {
			class_ currentClass_ = ((class_) e.nextElement());

			if (classByName.containsKey(currentClass_.name.toString())) {
				this.semantError(currentClass_).println("Class " + currentClass_.name.toString() + " has already been defined.");
				continue;
			}

			classByName.put(currentClass_.name.toString(), currentClass_);

			String parentClass = currentClass_.parent.toString();
			if (!childClasses.containsKey(parentClass)) {
				childClasses.put(parentClass, new ArrayList<String>());
			}

			childClasses.get(parentClass).add(currentClass_.name.toString());
		}

        for (String parentClass : childClasses.keySet()) {
            if (!classByName.containsKey(parentClass)) {
                for (String childClass : childClasses.get(parentClass)) {
                    this.semantError(classByName.get(childClass)).println("Class " + childClass + " is inheriting from undefined class, " + parentClass + ".");
                }

                childClasses.remove(parentClass);
            }
        }

        if (!childClasses.get(TreeConstants.Str.toString()).isEmpty()) {
            String errorString = "Class(s) ";
            for (String childClassName : childClasses.get(TreeConstants.Str.toString())) {
                errorString += childClassName + ", ";
            }
            errorString = errorString.substring(0, errorString.length() - 2);
            errorString += " inherits from primitive String.";
            this.semantError(classByName.get(TreeConstants.Str.toString())).println(errorString);
        }

        if (!childClasses.get(TreeConstants.Int.toString()).isEmpty()) {
            String errorString = "Class(s) ";
            for (String childClassName : childClasses.get(TreeConstants.Int.toString())) {
                errorString += childClassName + ", ";
            }
            errorString = errorString.substring(0, errorString.length() - 2);
            errorString += " inherits from primitive Int.";
            this.semantError(classByName.get(TreeConstants.Int.toString())).println(errorString);
        }

        if (!childClasses.get(TreeConstants.Bool.toString()).isEmpty()) {
            String errorString = "Class(s) ";
            for (String childClassName : childClasses.get(TreeConstants.Bool.toString())) {
                errorString += childClassName + ", ";
            }
            errorString = errorString.substring(0, errorString.length() - 2);
            errorString += " inherits from primitive Boolean.";
            this.semantError(classByName.get(TreeConstants.Bool.toString())).println(errorString);
        }
	}
}
