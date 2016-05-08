import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Handles building an inheritance tree and performing
 * checks on inheritance for classes.
 */
public class InheritanceTree {
    private program program;

    /**
     * Instantiates a new instance of <code>InheritanceTree</code> with
     * the specified <code>program</code>.
     * @param program The program that contains necessary data.
     */
    public InheritanceTree(program program) {
        this.program = program;
    }

    /**
     * Initiates the primrary operations of this <code>InheritanceTree</code>.
     */
    void startBuild() {
        System.out.println("About to build Object's tree...");
        buildRoot(TreeConstants.Object_.toString());
    }

    /**
     * Initiates the primary operations with the specified class
     * as the "root" class.
     * @param rootNode The class that will be considered root.
     */
    private void buildRoot(String rootNode) {
        // Cancel out if we couldn't find any children for this class
        if (program.classTable.childClasses.get(rootNode).isEmpty()) return;

        // Loop through each of this class_'s children and perform operations
        for (String childClass : program.classTable.childClasses.get(rootNode)) {
            buildChildren(childClass);
        }
    }

    /**
     * Performs primary operations against the specified class and its children.
     * @param childClassName The class against which to perform operations.
     */
    private void buildChildren(String childClassName) {
        // Get the class_ object for the specified class name
        class_ thisClass = program.classTable.classByName.get(childClassName);

        // Cancel out if we couldn't find this class_
        if (thisClass == null) return;

        String parentName = program.classTable.getParentsName(childClassName);

        SymbolTable parentTable = program.objects.get(parentName);

        SymbolTable thisClassObjects = program.objects.get(childClassName);

        LinkedHashMap<String, List<AbstractSymbol>> classMethods = program.methodsByObject.get(childClassName);
        LinkedHashMap<String, List<AbstractSymbol>> parentMethods = program.methodsByObject.get(parentName);

        if (parentTable != null) {
            // Enumerate over each of the Features of this class
            for (Enumeration e = thisClass.features.getElements(); e.hasMoreElements(); ) {
                Feature thisFeature = (Feature) e.nextElement();

                FeatureType featureType = FeatureType.valueOf(thisFeature.getClass().getSimpleName());
                switch (featureType) {
                    case attr:
                        if (parentTable.lookup(((attr) thisFeature).name) != null) {
                            program.classTable.semantError(thisClass.filename, thisFeature).println("Attribute " + ((attr) thisFeature).name.toString() + " is already defined and cannot be overriden.");

                            // TODO: Remove from this Class' object symbol table
                        }
                        break;
                    case method:
                        method thisMethod = (method) thisFeature;
                        if (parentMethods != null && parentMethods.containsKey(thisMethod.name.toString())) {
                            if (classMethods.get(thisMethod.name.toString()).size() != parentMethods.get(thisMethod.name.toString()).size()) {
                                program.classTable.semantError(thisClass.filename, thisMethod).println("Method " + thisMethod.name.toString() + " has an invalid number of parameters.");
                            } else {
                                for (int i = 0; i < classMethods.get(thisMethod.name.toString()).size(); i++) {
                                    if (classMethods.get(thisMethod.name.toString()).get(i) != parentMethods.get(thisMethod.name.toString()).get(i)) {
                                        program.classTable.semantError(thisClass.filename, thisMethod).println("Type mismatch of parameters on inherited method " + thisMethod.name.toString() + ".");

                                        classMethods.remove(thisMethod.name.toString());
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                }
            }

            // TODO: Copy classObjects to parentObjects (or vice versa)

            if (parentMethods != null) {
                for (String methodName : parentMethods.keySet()) {
                    if (!classMethods.containsKey(methodName))
                        classMethods.put(methodName, parentMethods.get(methodName));
                }
            }
        }

        if (program.classTable.childClasses.get(childClassName) != null) {
            for (String childsName : program.classTable.childClasses.get(childClassName)) {
                buildChildren(childsName);
            }
        }
    }
}
