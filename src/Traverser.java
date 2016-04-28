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
}

enum FeatureType {
    attr,
    method
}
