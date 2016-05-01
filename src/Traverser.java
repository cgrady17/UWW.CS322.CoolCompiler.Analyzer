import java.util.Enumeration;

/**
 * Implementation of our AST Traverser used to traverse the ASTs of
 * the provided objects.
 */
class Traverser {
    private Classes classes;

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
    	// traverse through each class
        for (Enumeration e = classes.getElements(); e.hasMoreElements();) {
            class_ currentClass_ = (class_)e.nextElement();

            //TODO: Should methods and attributes be in the same SymbolTable?
            SymbolTable objectsTable = new SymbolTable();
            SymbolTable methodsTable = new SymbolTable(); 
            objectsTable.enterScope();

            Features classFeatures = currentClass_.features;

            // traverse through all features of this class
            for (Enumeration features = classFeatures.getElements(); features.hasMoreElements();) {
                Feature feature = (Feature)features.nextElement();

                FeatureType featureType = FeatureType.valueOf(feature.getClass().getSimpleName());
                switch(featureType) {
                    case attr:
                        serviceAttribute((attr)feature, objectsTable, program.classTable, currentClass_);
                        break;

                    case method:
                        serviceMethod((method)feature, methodsTable, program.classTable, currentClass_);
                        break;

                }
            }
        }
    }

    /**
     * Inspects the specified method and, if necessary, inserts it into the methods
     * symbol table and traverses it.
     * @param method The method being serviced.
     * @param methodsTable The symbol table to which the attribute may be added
     * @param classTable The global ClassTable.
     * @param currentClass_ The current class_ of which this method is a member.
     */
    private void serviceMethod(method method, SymbolTable methodsTable, ClassTable classTable, class_ currentClass_) {
        // We first have to check if the method is already in
        // the methods symbol table. If it is, we throw a semantic error
    	AbstractSymbol methodName = ((method)method).name;
    	if ( methodsTable.lookup( methodName ) != null ) {
			classTable.semantError( currentClass_.getFilename(), method ).println( "Method " + methodName.toString() + " was already defined." );
		}
    	
    	// We need to add the methods to the methodsTable
    	methodsTable.addId(((method)method).name, ((method)method).return_type);
    	
        // We now want to traverse the method
        // TODO: Traverse the method
    }

    /**
     * Inspects the specified attribute and, if necessary, inserts it into the objects
     * table and traverses its expression.
     * @param attribute The attribute being serviced.
     * @param objectsTable The symbol table to which the attribute may be added.
     * @param classTable The global ClassTable.
     * @param class_ The current class_ of which this attribute is a Feature.
     */
    private void serviceAttribute(attr attribute, SymbolTable objectsTable, ClassTable classTable, class_ class_) {
        // We first have to check if the attribute is already in the
        // objects table. If it is, we throw a semantic error
    	AbstractSymbol attrName = ((attr)attribute).name;
        if ( objectsTable.lookup( attrName ) != null ) { 
            classTable.semantError( class_.getFilename(), attribute).println( "Attribute " + attrName.toString() + " has already been defined." );
        }

        // We now want to add the attribute to the objects table
        objectsTable.addId(((attr) attribute).name, ((attr)attribute).type_decl);

        // We now need to traverse the expression
        // TODO: Traverse the expression.
        traverse( ((attr)attribute).init, objectsTable, class_ );
    }

    /** Expression traversing
     * Fills in types and scoping for an expression
     * @param Expression The expression being traversed
     * @param objectsTable The symbol table to which the attribute may be added.
     * @param class_ The current class_ of which this attribute is a Feature.
     */
    public void traverse( Expression expression, SymbolTable objectsTable, class_ class_ ) {
    	
    	// Determine the type of expression
    	ExpressionType expressionType = ExpressionType.valueOf( expression.getClass().getSimpleName() );
        System.out.println( "Expression Type: " + expressionType.toString() );
        
    	switch( expressionType ) {
            case assign:
            	// Traverse assignment to find type
            	Expression assignment = ( (assign)expression ).expr;
            	
            	// Temp to help understand assign
            	expression.dump_with_types(System.out,0);
            	
                traverse( assignment, objectsTable, class_ );
                
                expression.set_type( assignment.get_type() );
                break;

            case static_dispatch:
                // What is static dispatch?
            	//TODO
                break;
                
            case dispatch:
            	// What is dispatch?
            	//TODO
            	break;
            	
            case cond:
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
            	// a loop should have its own scope
            	objectsTable.enterScope();
            	// a loop has a predicate expression and a body expression. We need to traverse these.
            	traverse( ((loop)expression).pred, objectsTable, class_ );
            	traverse( ((loop)expression).body, objectsTable, class_ );
            	// exit scope
            	objectsTable.exitScope();
            	break;
            	
            case typcase:
            	//TODO
            	break;
            	
            case block:
            	//TODO
            	break;
            	
            case let:
            	//TODO: let should have its own scope, initial expression, and body expression
            	objectsTable.enterScope();

            	// add to object table
            	objectsTable.addId( ((let)expression).identifier, ((let)expression).type_decl );
            	
            	// traverse its initial expression and body
            	traverse( ((let)expression).init, objectsTable, class_ );
            	traverse( ((let)expression).body, objectsTable, class_ );

            	// set type
            	
            	// exit scope
            	objectsTable.exitScope();
            	break;
            	
            case plus:
            	expression.set_type( TreeConstants.Int );
            	traverse( ((plus)expression).e1, objectsTable, class_ );
            	traverse( ((plus)expression).e2, objectsTable, class_ );
            	break;
            	
            case sub:
            	expression.set_type( TreeConstants.Int );
            	traverse( ((sub)expression).e1, objectsTable, class_ );
            	traverse( ((sub)expression).e2, objectsTable, class_ );
            	break;
            	
            case mul:
            	expression.set_type( TreeConstants.Int );
            	traverse( ((mul)expression).e1, objectsTable, class_ );
            	traverse( ((mul)expression).e2, objectsTable, class_ );
            	break;
            	
            case divide:
            	expression.set_type( TreeConstants.Int );
            	traverse( ((divide)expression).e1, objectsTable, class_ );
            	traverse( ((divide)expression).e2, objectsTable, class_ );
            	break;
            	
            case neg:
            	expression.set_type( TreeConstants.Int );
            	traverse( ((neg)expression).e1, objectsTable, class_ );
            	break;
            	
            case lt:
            	expression.set_type( TreeConstants.Bool );
            	traverse( ((lt)expression).e1, objectsTable, class_ );
            	traverse( ((lt)expression).e2, objectsTable, class_ );
            	break;
            	
            case eq:
            	expression.set_type( TreeConstants.Bool );
            	traverse( ((eq)expression).e1, objectsTable, class_ );
            	traverse( ((eq)expression).e2, objectsTable, class_ );
            	break;
            	
            case leq:
            	expression.set_type( TreeConstants.Bool );
            	traverse( ((leq)expression).e1, objectsTable, class_ );
            	traverse( ((leq)expression).e2, objectsTable, class_ );
            	break;
            	
            case comp:
            	// compare?
            	expression.set_type( TreeConstants.Bool );
            	traverse( ((comp)expression).e1, objectsTable, class_ );
            	break;
            	
            case int_const:
            	//TODO token
            	expression.set_type( TreeConstants.Int );
            	break;
            	
            case bool_const:
            	//TODO token
            	expression.set_type( TreeConstants.Bool );
            	break;
            	
            case string_const:
            	//TODO token
            	expression.set_type( TreeConstants.Str );
            	break;
            	
            case new_:
            	//TODO type_name
            	break;
            	
            case isvoid:
            	//TODO e1
            	expression.set_type( TreeConstants.Bool );
            	break;
            	
            case no_expr:
            	//TODO is this saying there is nothing in the expression?
            	break;
            	
            case object:
            	//TODO
            	break;
            	
        }
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

