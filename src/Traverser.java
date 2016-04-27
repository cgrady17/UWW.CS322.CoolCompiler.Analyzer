import java.util.Enumeration;

/**
 * Implementation of our AST Traverser used to traverse the ASTs of
 * the provided objects.
 */
public class Traverser {
    private Classes classes;


    public Traverser(Classes classes) {
        this.classes = classes;
    }

    public void traverse(program program) {
        for (Enumeration e = classes.getElements(); e.hasMoreElements()) {
            class_ class_ = (class_)e.nextElement();

            SymbolTable symbolTable = new SymbolTable();
            symbolTable.enterScope();

            Features classFeatures = class_.features;

            for (Enumeration features = classFeatures.getElements(); features.hasMoreElements()) {
                Feature feature = (Feature)features.nextElement();

                FeatureType featureType = FeatureType.valueOf(feature.getClass().getSimpleName());
                switch(featureType) {
                    case attr:
                        serviceAttribute(feature, symbolTable, program.classTable);
                        break;

                    case method:

                        break;

                }
            }
        }
    }

    private void serviceAttribute(Feature feature, SymbolTable symbolTable, ClassTable classTable) {
        if (symbolTable.lookup(((attr)feature).name) != null) {
            // TODO: Throw Semantic Error
            classTable.semantError();
        }

        symbolTable.addId(((attr) feature).name, ((attr)feature).type_decl);

    }
}

enum FeatureType {
    attr,
    method
}
