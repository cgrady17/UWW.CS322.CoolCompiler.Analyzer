import java.util.Enumeration;

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
    public void startBuild() {
        System.out.println("About to build Object's tree...");
        buildRoot(TreeConstants.Object_.toString());
    }

    /**
     * Initiates the primary operations with the specified class
     * as the "root" class.
     * @param rootNode The class that will be considered root.
     */
    private void buildRoot(String rootNode) {
        // Get the class_ object for this class
        class_ rootClass = program.classTable.classByName.get(rootNode);

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

        // Enumerate over each of the Features of this class
        for (Enumeration e = thisClass.features.getElements(); e.hasMoreElements(); ) {

        }
    }
}
