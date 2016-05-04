import java.util.Enumeration;

public class InheritanceTree {
    private program program;

    public InheritanceTree(program program) {
        this.program = program;
    }

    void build() {
        buildRoot(TreeConstants.Object_.toString());
    }

    private void buildRoot(String rootNode) {
        class_ rootClass = program.classTable.classByName.get(rootNode);

        if (program.classTable.childClasses.get(rootNode).isEmpty()) return;

        for (String childClass : program.classTable.childClasses.get(rootNode)) {
            buildChildren(childClass);
        }
    }

    private void buildChildren(String childClassName) {
        class_ thisClass = program.classTable.classByName.get(childClassName);

        if (thisClass == null) return;

        for (Enumeration e = thisClass.features.getElements(); e.hasMoreElements();) {

        }
    }
}
