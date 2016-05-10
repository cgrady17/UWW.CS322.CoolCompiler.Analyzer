import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Implementation of our AST Traverser used to traverse the ASTs of
 * the provided objects.
 */
class Traverser {
	static final boolean debugging = false;
    private Classes classes;
    private program program;
    private ClassType classType;

    /**
     * Initializes a new instance of <code>Traverser</code> with the specified
     * Classes container.
     * @param classes Container object for a collection of Classes.
     */
    Traverser(Classes classes, ClassType classType) {
        this.classes = classes;
        this.classType = classType;
    }

    /**
     * Traverses the AST and builds out the respective symbol tables, performing
     * scoping and type checking.
     * @param origProgram The program running as the analyzer with requisite members.
     */
    void traverse(program origProgram) {
        this.program = origProgram;

        if ( classType == ClassType.Complex ) {
            // start building inheritance tree
            InheritanceTree tree = new InheritanceTree(this.program);
            tree.startBuild();
        }

        // traverse through each class
        for (Enumeration e = classes.getElements(); e.hasMoreElements();) {
            class_ currentClass_ = (class_)e.nextElement();

            
            //TODO: Should methods and attributes be in the same SymbolTable?
            SymbolTable objectsTable = new SymbolTable();
            objectsTable.enterScope();

            // Instantiate an empty <code>LinkedHashMap</code> for the current class
            LinkedHashMap<String, List<AbstractSymbol>> methodsTable = new LinkedHashMap<>();

            // traverse through all features of this class
            for (Enumeration features = currentClass_.features.getElements(); features.hasMoreElements();) {
                Feature feature = (Feature)features.nextElement();

                FeatureType featureType = FeatureType.valueOf(feature.getClass().getSimpleName());
                switch(featureType) {
                    case attr:
                        serviceAttribute((attr)feature, objectsTable, currentClass_);
                        break;

                    case method:
                        serviceMethod((method)feature, objectsTable, methodsTable, origProgram.classTable, currentClass_);
                        break;

                }
            }

            
            // Insert the now filled methods table into the methods collection
            origProgram.methodsByObject.put(currentClass_.name.toString(), methodsTable);
        }

        if ( classType == ClassType.Complex ) {
            // Check for Main errors
            // we need to check if the program has a main class and main method
            int mainClassCount = 0;
            int mainMethodCount = 0;
            boolean mainClassHasMainMethod = false;
            for (Enumeration e = classes.getElements(); e.hasMoreElements(); ) {
                class_ currentClass_ = (class_) e.nextElement();

                // Get Main classes count
                if (currentClass_.name.equals(TreeConstants.Main)) {
                    mainClassCount++;
                }

                // Check for main() methods
                for (Enumeration features = currentClass_.features.getElements(); features.hasMoreElements(); ) {
                    Feature feature = (Feature) features.nextElement();
                    if (feature instanceof method) {
                        // if method = main()
                        if (((method) feature).name.equals(TreeConstants.main_meth)) {
                            mainMethodCount++;
                            // if main method is inside main class
                            if (currentClass_.name.equals(TreeConstants.Main)) {
                                mainClassHasMainMethod = true;
                            } else {
                                origProgram.classTable.semantError(currentClass_.filename, currentClass_).println("Main class needs a main method");
                            }
                        }
                    }
                }
            }

            // Call main class errors
            if (mainClassCount == 0)
                origProgram.classTable.semantError().println("Couldnt find a Main class");
            else if (mainClassCount > 1)
                origProgram.classTable.semantError().println("Can only have one Main class");

            // Call main method errors
            if (mainMethodCount == 0)
                origProgram.classTable.semantError().println("Couldnt find a main() method");
            else if (mainMethodCount > 1)
                origProgram.classTable.semantError().println("Too may definitions for main() method");
            
            if( !mainClassHasMainMethod )
            	origProgram.classTable.semantError().println("main() method is not inside Main class");
        }
    }

    /**
     * Inspects the specified method and, if necessary, inserts it into the methods
     * symbol table and traverses it.
     * @param method The method being serviced.
     * @param objectsTable The symbol table to which the attribute may be added.
     * @param methodsTable The symbol table for Methods.
     * @param classTable The global ClassTable.
     * @param currentClass_ The current class_ of which this method is a member.
     */
    private void serviceMethod(method method, SymbolTable objectsTable, LinkedHashMap<String, List<AbstractSymbol>> methodsTable, ClassTable classTable, class_ currentClass_) {
        // We first have to check if the method is already in
        // the methods symbol table. If it is, we throw a semantic error
    	if (methodsTable.containsKey(method.name.toString())) {
			classTable.semantError( currentClass_.getFilename(), method ).println( "Method " + method.name.toString() + " was already defined." );
		}
    	
        // We now want to traverse the method
    	if( classType == ClassType.Complex && debugging ){
    		System.out.println( "Traversing method expression in " + method.name + " at line " + method.getLineNumber() );
    	}
        traverse(method, objectsTable, methodsTable, currentClass_);
    }

    /**
     * Inspects the specified attribute and, if necessary, inserts it into the objects
     * table and traverses its expression.
     * @param attribute The attribute being serviced.
     * @param objectsTable The symbol table to which the attribute may be added.
     * @param class_ The current class_ of which this attribute is a Feature.
     */
    private void serviceAttribute(attr attribute, SymbolTable objectsTable, class_ class_) {
        // We first have to check if the attribute is already in the
        // objects table. If it is, we throw a semantic error
    	AbstractSymbol attrName = attribute.name;
        if ( objectsTable.lookup( attrName ) != null ) {
            program.classTable.semantError(class_.getFilename(), attribute).println( "Attribute " + attrName.toString() + " has already been defined." );
        }

        // We now want to add the attribute to the objects table
        objectsTable.addId(attribute.name, attribute.type_decl);

        // We now need to traverse the expression
        if( classType == ClassType.Complex && debugging ){
        	System.out.println( "Traversing attr expression in " + attribute.name + " at line " + attribute.getLineNumber() );
        }
        //attribute.dump(System.out, 0);
        traverse(attribute.init, objectsTable, class_);
    }

    /** Expression traversing
     * Fills in types and scoping for an expression
     * @param Expression The expression being traversed
     * @param objectsTable The symbol table to which the attribute may be added.
     */
    private void traverse(Expression expression, SymbolTable objectsTable, class_ currentClass ) {

    	// Determine the type of expression
    	ExpressionType expressionType = ExpressionType.valueOf( expression.getClass().getSimpleName() );
    	if( classType == ClassType.Complex && debugging ){
    		System.out.println( "Expression Type: " + expressionType.toString() );
    	}

    	switch( expressionType ) {
            case assign:
            	/** Variables - name: Symbol, expr: Expression **/
            	// Traverse assignment to find type
            	Expression assignment = ( (assign)expression ).expr;
                traverse( assignment, objectsTable, currentClass);
                
                expression.set_type( assignment.get_type() );
                
                // check if it was declared
                if( objectsTable.lookup( ((assign)expression).name ) == null ){
                	program.classTable.semantError().println( "Undefined assignment at line " + expression.lineNumber );
                }else{// type check
	                if( assignment.get_type() != objectsTable.lookup( ((assign)expression).name ) ){
	                	program.classTable.semantError().println( "Type mismatch at line " + expression.lineNumber );
	                }
                }
                
                
                break;

            case static_dispatch:
            	/** Variables - expr: Expression, type_name: Symbol, name: Symbol, actual: Expressions **/
                /** <expr>.<id>(<expr>,...,<expr>)
                	The other forms of dispatch are:
					<id>(<expr>,...,<expr>)
					<expr>@<type>.id(<expr>,...,<expr>)
					We need to do several checkings here such as:
					 Checking if the method exists
					 Checking the formals(amount and type)
					 Check if the types conform
                 */
            	static_dispatch stat_dispatch = (static_dispatch)expression;
            	traverse( stat_dispatch.expr, objectsTable, currentClass);
            	List<AbstractSymbol> stat_parameterTypes = new ArrayList<>();
            	
            	for (int i = 0; i < stat_dispatch.actual.getLength(); i++) {
            		traverse( (Expression) stat_dispatch.actual.getNth(i), objectsTable, currentClass );
            		stat_parameterTypes.add( ((Expression)stat_dispatch.actual.getNth(i)).get_type() );
            	}
            	
            	if( classType == ClassType.Complex && debugging ){
            		stat_dispatch.dump_with_types(System.out, 0);
	            	
	            	System.out.println("Dispatch Method Name: " + stat_dispatch.name + " Type: " + stat_dispatch.get_type());
	            	System.out.println("Dispatch Expression Id: " + stat_dispatch.expr.toString() + " Type: " + stat_dispatch.expr.get_type());
	            	System.out.println("Dispatch Formals Expressions: " + stat_dispatch.actual.toString());
            	}
				AbstractSymbol stat_className = stat_dispatch.expr.get_type();
            	
            	//LinkedHashMap<String, LinkedHashMap<String, List<AbstractSymbol>>>
            	ArrayList<AbstractSymbol> stat_actualMethodTypes = null;
            	try{
            		stat_actualMethodTypes = (ArrayList<AbstractSymbol>) program.methodsByObject.get(stat_className.toString()).get(stat_dispatch.name.toString());
            	}catch( NullPointerException error ){
            		program.classTable.semantError().println("Error in method " + stat_dispatch.name + " at " + expression.lineNumber );
            		return;
            	}
            	
            	// does this method exist?
            	if( (!program.methodsByObject.get(stat_className.toString()).containsKey(stat_dispatch.name.toString())) ){
            		program.classTable.semantError().println("Undefined method " + stat_dispatch.name + " at " + expression.lineNumber );
            	}else{
            		
            		// check the amount and types of formals, (-1 because last is the return type)
            		if( stat_actualMethodTypes.size()-1 != stat_parameterTypes.size() ){
            			program.classTable.semantError().println("The method " + stat_dispatch.name + " at " + expression.lineNumber + " has the wrong amount of arguments" );
            		}else{
            			
	            		for (int i = 0; i < stat_actualMethodTypes.size()-1; i++) {
	                		if( stat_actualMethodTypes.get(i) != stat_parameterTypes.get(i) ){
	                			
	                			program.classTable.semantError().println("Method call " + stat_dispatch.name + " at " + expression.lineNumber + " has the wrong type of arguments" );
	                		}
	                	}
            		}
            	}
            	
            	try{
            		if (stat_actualMethodTypes.get(stat_actualMethodTypes.size() - 1) == TreeConstants.SELF_TYPE) {
            			stat_dispatch.set_type( stat_dispatch.expr.get_type() );
                    }else{
                    	stat_dispatch.set_type(stat_actualMethodTypes.get(stat_actualMethodTypes.size() - 1));
                    }
            	}catch( NullPointerException error ){}

                break;
                
            case dispatch:
            	/** Variables - expr: Expression, name: Symbol, actual: Expressions **/
            	/** <expr>.<id>(<expr>,...,<expr>)
	            	The other forms of dispatch are:
					<id>(<expr>,...,<expr>)
					<expr>@<type>.id(<expr>,...,<expr>)
					We need to do several checkings here such as:
					 Checking if the method exists
					 Checking the formals(amount and type)
					 Check if the types conform
            	*/
            	dispatch dispatch = (dispatch)expression;
            	// traverse the identifier
            	traverse( dispatch.expr, objectsTable, currentClass);
            	// traverse through each formal parameter, record each type
            	List<AbstractSymbol> parameterTypes = new ArrayList<AbstractSymbol>();
            	for (int i = 0; i < dispatch.actual.getLength(); i++) {
            		traverse( (Expression) dispatch.actual.getNth(i), objectsTable, currentClass );
            		parameterTypes.add( ((Expression)dispatch.actual.getNth(i)).get_type() );
            	}
            	
            	if( classType == ClassType.Complex && debugging ){
	            	dispatch.dump_with_types(System.out, 0);
	            	
	            	System.out.println("Dispatch Method Name: " + dispatch.name + " Type: " + dispatch.get_type());
	            	System.out.println("Dispatch Expression Id: " + dispatch.expr.toString() + " Type: " + dispatch.expr.get_type());
	            	System.out.println("Dispatch Formals Expressions: " + dispatch.actual.toString());
            	}
				AbstractSymbol className = dispatch.expr.get_type();
            	
            	//LinkedHashMap<String, LinkedHashMap<String, List<AbstractSymbol>>>
            	ArrayList<AbstractSymbol> actualMethodTypes = null;
            	try{
            		actualMethodTypes = (ArrayList<AbstractSymbol>) program.methodsByObject.get(className.toString()).get(dispatch.name.toString());
            	}catch( NullPointerException error ){
            		program.classTable.semantError().println("Error in method " + dispatch.name + " at " + expression.lineNumber );
            		return;
            	}
            	
            	// does this method exist?
            	if( (!program.methodsByObject.get(className.toString()).containsKey(dispatch.name.toString())) ){
            		program.classTable.semantError().println("Undefined method " + dispatch.name + " at " + expression.lineNumber );
            	}else{
            		
            		// check the amount and types of formals, (-1 because last is the return type)
            		if( actualMethodTypes.size()-1 != parameterTypes.size() ){
            			program.classTable.semantError().println("The method " + dispatch.name + " at " + expression.lineNumber + " has the wrong amount of arguments" );
            		}else{
            			
	            		for (int i = 0; i < actualMethodTypes.size()-1; i++) {
	                		if( actualMethodTypes.get(i) != parameterTypes.get(i) ){
	                			
	                			program.classTable.semantError().println("Method call " + dispatch.name + " at " + expression.lineNumber + " has the wrong type of arguments" );
	                		}
	                	}
            		}
            	}
            	/** "If f has return type B and B is a class name, then the static type of the dispatch is B. Otherwise, if f has return type SELF_TYPE, then the static type of the dispatch is A." **/
            	
            	try{
            		if (actualMethodTypes.get(actualMethodTypes.size() - 1) == TreeConstants.SELF_TYPE) {
            			dispatch.set_type( dispatch.expr.get_type() );
                    }else{
                    	dispatch.set_type(actualMethodTypes.get(actualMethodTypes.size() - 1));
                    }
            	}catch( NullPointerException error ){}

            	break;
            	
            case cond:
            	/** Variables - pred: Expression, then_exp: Expression, else_exp: Expression **/
            	
            	// A condition has a predicate, then expression, and else expression
            	Expression if_ = ( (cond)expression ).pred;
            	Expression then_ = ( (cond)expression ).then_exp;
            	Expression else_ = ( (cond)expression ).else_exp;
            	
            	// Temp to help understand cond
            	if( classType == ClassType.Complex && debugging )
            		expression.dump_with_types(System.out,0);
            	
            	// We need to traverse through each of these
            	traverse( if_ , objectsTable, currentClass );
            	traverse( then_ , objectsTable, currentClass );
            	traverse( else_ , objectsTable, currentClass );
            	
            	if( if_.get_type() != TreeConstants.Bool )
            		program.classTable.semantError().println( "'If statement' does not return a Boolean at line " + expression.lineNumber );
            	
            	
            	break;
            	
            case loop:
            	/** Variables - pred: Expression, body: Expression **/
            	
            	loop loopExpr = (loop)expression;
            	// a loop should have its own scope?
            	//objectsTable.enterScope();
            	// a loop has a predicate expression and a body expression. We need to traverse these.
            	traverse( loopExpr.pred, objectsTable, currentClass );
            	traverse( loopExpr.body, objectsTable, currentClass );
            	// exit scope?
            	//objectsTable.exitScope();
            	break;
            	
            case typcase:
            	/** Variables - expr: Expression, cases: Cases **/
            	
            	typcase caseExpr = (typcase)expression;
            	if( classType == ClassType.Complex && debugging )
            		caseExpr.dump_with_types(System.out,0);
            	
            	// traverse expression
            	traverse(caseExpr.expr, objectsTable, currentClass );
	
	            // traverse through each case
	            for ( Enumeration elements = caseExpr.cases.getElements(); elements.hasMoreElements(); ){
	                objectsTable.enterScope();
	                
	                branch elementCase = (branch) elements.nextElement();
	                objectsTable.addId( elementCase.name, elementCase.type_decl );
	                traverse(elementCase.expr, objectsTable, currentClass );
	                
	                objectsTable.exitScope();
	            }
	            
            	break;
            	
            case block:
            	/** Variables - body: Expressions **/
            	
            
            	// Traverse the block of code
            	for (Enumeration expressions = ((block)expression).body.getElements(); expressions.hasMoreElements();) {
                    Expression blockExpression = (Expression) expressions.nextElement();
                    traverse( blockExpression, objectsTable, currentClass );
                    expression.set_type( blockExpression.get_type() );
            	}
            	
            	break;
            	
            case let:
            	/** Variables - identifier: Symbol, type_decl: Symbol, inti: Expression, body: Expression **/
            	
            	let letExpr = (let)expression;
            	//let should have its own scope, initial expression, and body expression
            	objectsTable.enterScope();

            	// add to object table
            	objectsTable.addId( letExpr.identifier, letExpr.type_decl );
            	
            	// traverse its initial expression and body
            	traverse( letExpr.init, objectsTable, currentClass );
            	traverse( letExpr.body, objectsTable, currentClass );

            	// set type
            	
            	// exit scope
            	objectsTable.exitScope();
            	break;
            	
            case plus:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Int );
            	traverse( ((plus)expression).e1, objectsTable, currentClass );
            	traverse( ((plus)expression).e2, objectsTable, currentClass );
            	if( ((plus)expression).e1.get_type() != TreeConstants.Int || ((plus)expression).e2.get_type() != TreeConstants.Int )
            		program.classTable.semantError().println( "Expression at line " + expression.lineNumber + " is not an Int" );
            	break;
            	
            case sub:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Int );
            	traverse( ((sub)expression).e1, objectsTable, currentClass );
            	traverse( ((sub)expression).e2, objectsTable, currentClass );
            	if( ((sub)expression).e1.get_type() != TreeConstants.Int || ((sub)expression).e2.get_type() != TreeConstants.Int )
            		program.classTable.semantError().println( "Expression at line " + expression.lineNumber + " is not an Int" );
            	break;
            	
            case mul:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Int );
            	traverse( ((mul)expression).e1, objectsTable, currentClass );
            	traverse( ((mul)expression).e2, objectsTable, currentClass );
            	if( ((mul)expression).e1.get_type() != TreeConstants.Int || ((mul)expression).e2.get_type() != TreeConstants.Int )
            		program.classTable.semantError().println( "Expression at line " + expression.lineNumber + " is not an Int" );
            	break;
            	
            case divide:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Int );
            	traverse( ((divide)expression).e1, objectsTable, currentClass );
            	traverse( ((divide)expression).e2, objectsTable, currentClass );
            	if( ((divide)expression).e1.get_type() != TreeConstants.Int || ((divide)expression).e2.get_type() != TreeConstants.Int )
            		program.classTable.semantError().println( "Expression at line " + expression.lineNumber + " is not an Int" );
            	break;
            	
            case neg:
            	/** Variables - e1: Expression **/
            	
            	expression.set_type( TreeConstants.Int );
            	traverse( ((neg)expression).e1, objectsTable, currentClass );
            	if( ((neg)expression).e1.get_type() != TreeConstants.Int )
            		program.classTable.semantError().println( "Expression at line " + expression.lineNumber + " is not an Int" );
            	break;
            	
            case lt:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Bool );
            	traverse( ((lt)expression).e1, objectsTable, currentClass );
            	traverse( ((lt)expression).e2, objectsTable, currentClass );
            	if( ((lt)expression).e1.get_type() != TreeConstants.Int || ((lt)expression).e2.get_type() != TreeConstants.Int )
            		program.classTable.semantError().println( "Expression at line " + expression.lineNumber + " is not an Int" );
            	
            	break;
            	
            case eq:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Bool );
            	traverse( ((eq)expression).e1, objectsTable, currentClass );
            	traverse( ((eq)expression).e2, objectsTable, currentClass );
            	if( ((eq)expression).e1.get_type() != ((eq)expression).e2.get_type() )
            		program.classTable.semantError().println( "Comparison mismatch at line " + expression.lineNumber );
            	break;
            	
            case leq:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Bool );
            	traverse( ((leq)expression).e1, objectsTable, currentClass );
            	traverse( ((leq)expression).e2, objectsTable, currentClass );
            	if( ((leq)expression).e1.get_type() != TreeConstants.Int || ((leq)expression).e2.get_type() != TreeConstants.Int )
            		program.classTable.semantError().println( "Expression at line " + expression.lineNumber + " is not an Int" );
            	break;
            	
            case comp:
            	/** Variables - e1: Expression **/
            	
            	traverse( ((comp)expression).e1, objectsTable, currentClass );
            	if( ((comp)expression).e1.get_type() != TreeConstants.Bool )
            		program.classTable.semantError().println( "Comparison at line " + expression.lineNumber + " is not a Boolean" );
            	expression.set_type( TreeConstants.Bool );
            	break;
            	
            case int_const:
            	/** Variables - token: Symbol **/
            	
            	//TODO token
            	expression.set_type( TreeConstants.Int );
            	break;
            	
            case bool_const:
            	/** Variables - val: Boolean **/
            	
            	//TODO token
            	expression.set_type( TreeConstants.Bool );
            	break;
            	
            case string_const:
            	/** Variables - token: Symbol **/
            	
            	//TODO token
            	expression.set_type( TreeConstants.Str );
            	break;
            	
            case new_:
            	/** Variables - type_name: Symbol **/

            	expression.set_type( ((new_)expression).type_name );
            	break;
            	
            case isvoid:
            	/** Variables - e1: Expression **/
            	traverse( ((isvoid)expression).e1, objectsTable, currentClass );
            	expression.set_type( TreeConstants.Bool );
            	break;
            	
            case no_expr:
            	/** Variables - **/
            	//TODO is this saying there is nothing in the expression?
            	break;
            	
            case object:
            	/** Variables - name: Symbol **/

            	//Find the type of this object
            	if( objectsTable.lookup( ((object)expression).name ) != null ){
					expression.set_type( (AbstractSymbol) objectsTable.lookup( ((object)expression).name ) );
            	}else if( ((object)expression).name == TreeConstants.self ){
            		expression.set_type( TreeConstants.SELF_TYPE );
    			}else{
            		program.classTable.semantError().println( "Undeclared object at line " + expression.lineNumber );
            		expression.set_type( TreeConstants.Object_ );
            	}
            	
            	break;
            	
        }
    }

    /**
     * Traverses the provided Method and it's parameters.
     * @param method The Method being traversed.
     * @param objectsTable The symbol table to which the Method may be added.
     * @param class_ The current class_ of which this Method is a Feature.
     */
    private void traverse(method method, SymbolTable objectsTable, LinkedHashMap<String, List<AbstractSymbol>> methodsTable, class_ class_) {
        // Enter the table's scope
        objectsTable.enterScope();

        // Get the method's name, we'll also use it several times
        String methodName = method.name.toString();

        // Create an empty AbstractSymbol collection for the Method if it
        // doesn't already exist in the collection
        // We'll be adding all of the Method's parameters to the collection
        if (!methodsTable.containsKey(methodName)) {
            methodsTable.put(methodName, new ArrayList<AbstractSymbol>());
        }

        // Enumerate over the formals in the Method
        // We want to add each formal (i.e. parameter) to the
        // collection of <code>AbstractSymbol</code> for this Method
        
        String methodDef = "Method Def: " + method.name +"( ";
        for (Enumeration e = method.formals.getElements(); e.hasMoreElements();) {
            // Get the current formal from the enumeration
            formal currFormal = ((formal)e.nextElement());

            // Check to see whether this formal has already been defined
            if (objectsTable.probe(currFormal.name) != null) {
                // Formal is already in the objects table
                program.classTable.semantError(class_.filename, currFormal).println("Parameter " + currFormal.name.toString() + " has already been defined.");
            }
            
            methodDef += currFormal.name + " : " + currFormal.type_decl + ", ";
            
            // Add the current formal to the objects collection
            objectsTable.addId(currFormal.name, currFormal.type_decl);

            // Add the current Formal's type to the Method's table
            methodsTable.get(methodName).add(currFormal.type_decl);
        }
        methodDef = methodDef.substring(0, methodDef.length() -2 ).concat(" )"); 
        if( classType == ClassType.Complex && debugging ){
        	System.out.println( methodDef );
        }
        // Add the current Method's return type to the Method's table
        // This should, then, be the very last item added to the list
        methodsTable.get(methodName).add(method.return_type);

        // Traverse the method's expression
        traverse(method.expr, objectsTable, class_ );

        // Exit the symbol table's scope
        objectsTable.exitScope();
    }
}

enum FeatureType {
    attr,
    method
}

enum ExpressionType {
    assign, static_dispatch, dispatch, cond, loop, typcase, block, let, plus, sub,
    mul, divide, neg, lt, eq, leq, comp, int_const, bool_const, string_const, new_,
    isvoid, no_expr, object
}

