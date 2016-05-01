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
        for (Enumeration e = classes.getElements(); e.hasMoreElements();) {
            class_ currentClass_ = (class_)e.nextElement();

            SymbolTable objectsTable = new SymbolTable();
            objectsTable.enterScope();

            Features classFeatures = currentClass_.features;

            for (Enumeration features = classFeatures.getElements(); features.hasMoreElements();) {
                Feature feature = (Feature)features.nextElement();

                FeatureType featureType = FeatureType.valueOf(feature.getClass().getSimpleName());
                switch(featureType) {
                    case attr:
                        serviceAttribute((attr)feature, objectsTable, program.classTable, currentClass_);
                        break;

                    case method:
                        serviceMethod((method)feature, objectsTable, program.classTable, currentClass_);
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
        // TODO: Implement check and error throwing

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
        if (objectsTable.lookup(((attr)attribute).name) != null) { 
            classTable.semantError(class_.getFilename(), attribute).println("Attribute " + ((attr) attribute).name.toString() + " has already been defined.");
        }

        // We now want to add the attribute to the objects table
        objectsTable.addId(((attr) attribute).name, ((attr)attribute).type_decl);

        // We now need to traverse the expression
        // TODO: Traverse the expression
    }

    /** Expression traversing
     * Fills in types and scoping for an expression
     * @param Expression The expression being traversed
     * @param objectsTable The symbol table to which the attribute may be added.
     * @param methodsTable The symbol table to which the attribute may be added
     * @param class_ The current class_ of which this attribute is a Feature.
     */
    public void traverse( Expression expression, SymbolTable objectsTable, SymbolTable methodsTable, class_ class_ ) {
    	
    	// Determine the type of expression
    	ExpressionType expressionType = ExpressionType.valueOf(expression.getClass().getSimpleName());
        
    	switch( expressionType ) {
            case assign:
            	// Traverse assignment to find type
            	Expression assignment = ( ( assign )expression ).expr;
            	
            	// Temp to help understand assign
            	expression.dump_with_types(System.out,0);
            	
                traverse( assignment, objectsTable, methodsTable, class_ );
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
            	traverse( if_ , objectsTable, methodsTable, class_ );
            	traverse( then_ , objectsTable, methodsTable, class_ );
            	traverse( else_ , objectsTable, methodsTable, class_ );
            	// If-else statements should go into a new scope
            	//TODO
            	break;
            	
            case loop:
            	//TODO
            	objectsTable.enterScope();
            	//TODO a loop should have its own scope
            	objectsTable.exitScope();
            	break;
            	
            case typcase:
            	//TODO
            	break;
            	
            case block:
            	//TODO
            	break;
            	
            case let:
            	//TODO
            	objectsTable.enterScope();
            	//TODO let has its own scope
            	objectsTable.exitScope();
            	break;
            	
            case plus:
            	//TODO
            	expression.set_type( TreeConstants.Int );
            	break;
            	
            case sub:
            	//TODO
            	expression.set_type( TreeConstants.Int );
            	break;
            	
            case mul:
            	//TODO
            	expression.set_type( TreeConstants.Int );
            	break;
            	
            case divide:
            	//TODO
            	expression.set_type( TreeConstants.Int );
            	break;
            	
            case neg:
            	//TODO
            	expression.set_type( TreeConstants.Int );
            	break;
            	
            case lt:
            	//TODO
            	expression.set_type( TreeConstants.Bool );
            	break;
            	
            case eq:
            	//TODO
            	expression.set_type( TreeConstants.Bool );
            	break;
            	
            case leq:
            	//TODO
            	expression.set_type( TreeConstants.Bool );
            	break;
            	
            case comp:
            	//TODO
            	expression.set_type( TreeConstants.Bool );
            	break;
            	
            case int_const:
            	//TODO
            	expression.set_type( TreeConstants.Int );
            	break;
            	
            case bool_const:
            	//TODO
            	expression.set_type( TreeConstants.Bool );
            	break;
            	
            case string_const:
            	//TODO
            	expression.set_type( TreeConstants.Str );
            	break;
            	
            case new_:
            	//TODO
            	break;
            	
            case isvoid:
            	//TODO
            	expression.set_type( TreeConstants.Bool );
            	break;
            	
            case no_expr:
            	//TODO
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

