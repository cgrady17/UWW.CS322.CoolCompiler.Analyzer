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

        buildChildren();
    }

    private void buildChildren() {


    }
}
