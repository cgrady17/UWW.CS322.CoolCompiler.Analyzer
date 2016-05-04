import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Implementation of our AST Traverser used to traverse the ASTs of
 * the provided objects.
 */
class Traverser {
    private Classes classes;
    private program program;

    /**
     * Initializes a new instance of <code>Traverser</code> with the specified
     * Classes container.
     * @param classes Container object for a collection of Classes.
     */
    Traverser(Classes classes) {
        this.classes = classes;
    }

    /**
     * Traverses the AST and builds out the respective symbol tables, performing
     * scoping and type checking.
     * @param program The program running as the analyzer with requisite members.
     */
    void traverse(program program) {
        this.program = program;

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
                        serviceMethod((method)feature, objectsTable, methodsTable, program.classTable, currentClass_);
                        break;

                }
            }

            
            // Insert the now filled methods table into the methods collection
            program.methodsByObject.put(currentClass_.name.toString(), methodsTable);
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
    	System.out.println( "Traversing method expression in " + method.name + " at line " + method.getLineNumber() );
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
    	AbstractSymbol attrName = ((attr)attribute).name;
        if ( objectsTable.lookup( attrName ) != null ) { 
            program.classTable.semantError(class_.getFilename(), attribute).println( "Attribute " + attrName.toString() + " has already been defined." );
        }

        // We now want to add the attribute to the objects table
        objectsTable.addId(((attr) attribute).name, ((attr)attribute).type_decl);

        // We now need to traverse the expression
        System.out.println( "Traversing attr expression in " + attribute.name + " at line " + attribute.getLineNumber() );
        attribute.dump(System.out, 0);
        traverse(attribute.init, objectsTable, class_ );
    }

    /** Expression traversing
     * Fills in types and scoping for an expression
     * @param Expression The expression being traversed
     * @param objectsTable The symbol table to which the attribute may be added.
     * @param class_ The current class_ of which this attribute is a Feature.
     */
    private void traverse(Expression expression, SymbolTable objectsTable, class_ class_) {

    	// Determine the type of expression
    	ExpressionType expressionType = ExpressionType.valueOf( expression.getClass().getSimpleName() );
        System.out.println( "Expression Type: " + expressionType.toString() );

    	switch( expressionType ) {
            case assign:
            	/** Variables - name: Symbol, body: Expression **/
            	// Traverse assignment to find type
            	Expression assignment = ( (assign)expression ).expr;
            	
                traverse( assignment, objectsTable, class_ );
                
                expression.set_type( assignment.get_type() );
                
                break;

            case static_dispatch:
            	/** Variables - expr: Expression, type_name: Symbol, name: Symbol, actual: Expression **/
                
            	// What is static dispatch?
            	//TODO
                break;
                
            case dispatch:
            	/** Variables - expr: Expression, name: Symbol, actual: Expression **/
            	
            	//TODO
            	break;
            	
            case cond:
            	/** Variables - pred: Expression, then_exp: Expression, else_exp: Expression **/
            	
            	// A condition has a predicate, then expression, and else expression
            	Expression if_ = ( (cond)expression ).pred;
            	Expression then_ = ( (cond)expression ).then_exp;
            	Expression else_ = ( (cond)expression ).else_exp;
            	
            	// Temp to help understand cond
            	expression.dump_with_types(System.out,0);
            	
            	// We need to traverse through each of these
            	traverse( if_ , objectsTable, class_ );
            	traverse( then_ , objectsTable, class_ );
            	traverse( else_ , objectsTable, class_ );
            	// If-else statements should go into a new scope right?
            	//TODO
            	break;
            	
            case loop:
            	/** Variables - pred: Expression, body: Expression **/
            	
            	loop loopExpr = (loop)expression;
            	// a loop should have its own scope
            	objectsTable.enterScope();
            	// a loop has a predicate expression and a body expression. We need to traverse these.
            	traverse( loopExpr.pred, objectsTable, class_ );
            	traverse( loopExpr.body, objectsTable, class_ );
            	// exit scope
            	objectsTable.exitScope();
            	break;
            	
            case typcase:
            	/** Variables - expr: Expression, cases: Cases **/
            	
            	typcase caseExpr = (typcase)expression;
            	caseExpr.dump_with_types(System.out,0);
            	
            	// traverse expression
            	traverse(caseExpr.expr, objectsTable, class_);
	
	            // traverse through each case
	            for ( Enumeration elements = caseExpr.cases.getElements(); elements.hasMoreElements(); ){
	                objectsTable.enterScope();
	                
	                branch elementCase = (branch) elements.nextElement();
	                objectsTable.addId( elementCase.name, elementCase.type_decl );
	                traverse(elementCase.expr, objectsTable, class_);
	                
	                objectsTable.exitScope();
	            }
	            
            	break;
            	
            case block:
            	/** Variables - body: Expression **/
            	
            	objectsTable.enterScope();
            	// Traverse the block of code
            	for (Enumeration expressions = ((block)expression).body.getElements(); expressions.hasMoreElements();) {
                    Expression blockExpression = (Expression) expressions.nextElement();
                    traverse( blockExpression, objectsTable, class_ );
                    expression.set_type( blockExpression.get_type() );
            	}
            	objectsTable.exitScope();
            	break;
            	
            case let:
            	/** Variables - identifier: Symbol, type_decl: Symbol, inti: Expression, body: Expression **/
            	
            	let letExpr = (let)expression;
            	//TODO: let should have its own scope, initial expression, and body expression
            	objectsTable.enterScope();

            	// add to object table
            	objectsTable.addId( letExpr.identifier, letExpr.type_decl );
            	
            	// traverse its initial expression and body
            	traverse( letExpr.init, objectsTable, class_ );
            	traverse( letExpr.body, objectsTable, class_ );

            	// set type
            	
            	// exit scope
            	objectsTable.exitScope();
            	break;
            	
            case plus:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Int );
            	traverse( ((plus)expression).e1, objectsTable, class_ );
            	traverse( ((plus)expression).e2, objectsTable, class_ );
            	break;
            	
            case sub:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Int );
            	traverse( ((sub)expression).e1, objectsTable, class_ );
            	traverse( ((sub)expression).e2, objectsTable, class_ );
            	break;
            	
            case mul:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Int );
            	traverse( ((mul)expression).e1, objectsTable, class_ );
            	traverse( ((mul)expression).e2, objectsTable, class_ );
            	break;
            	
            case divide:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Int );
            	traverse( ((divide)expression).e1, objectsTable, class_ );
            	traverse( ((divide)expression).e2, objectsTable, class_ );
            	break;
            	
            case neg:
            	/** Variables - e1: Expression **/
            	
            	expression.set_type( TreeConstants.Int );
            	traverse( ((neg)expression).e1, objectsTable, class_ );
            	break;
            	
            case lt:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Bool );
            	traverse( ((lt)expression).e1, objectsTable, class_ );
            	traverse( ((lt)expression).e2, objectsTable, class_ );
            	break;
            	
            case eq:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Bool );
            	traverse( ((eq)expression).e1, objectsTable, class_ );
            	traverse( ((eq)expression).e2, objectsTable, class_ );
            	break;
            	
            case leq:
            	/** Variables - e1: Expression, e2: Expression **/
            	
            	expression.set_type( TreeConstants.Bool );
            	traverse( ((leq)expression).e1, objectsTable, class_ );
            	traverse( ((leq)expression).e2, objectsTable, class_ );
            	break;
            	
            case comp:
            	/** Variables - e1: Expression **/
            	
            	// compare?
            	expression.set_type( TreeConstants.Bool );
            	traverse( ((comp)expression).e1, objectsTable, class_ );
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

            	expression.set_type( TreeConstants.Bool );
            	traverse( ((isvoid)expression).e1, objectsTable, class_);
            	break;
            	
            case no_expr:
            	/** Variables - **/
            	//TODO is this saying there is nothing in the expression?
            	break;
            	
            case object:
            	/** Variables - name: Symbol **/
            	
            	//TODO
            	//Find the type of this object
            	if( objectsTable.lookup( ((object)expression).name ) != null )
					expression.set_type( (AbstractSymbol) objectsTable.lookup( ((object)expression).name ) );
            	else if( ((object)expression).name == TreeConstants.self )
            		expression.set_type( TreeConstants.SELF_TYPE );
            	else
            		expression.set_type( TreeConstants.Object_ );
            	
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
        System.out.println( methodDef );
        // Add the current Method's return type to the Method's table
        // This should, then, be the very last item added to the list
        methodsTable.get(methodName).add(method.return_type);

        // Traverse the method's expression
        traverse(method.expr, objectsTable, class_);

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

