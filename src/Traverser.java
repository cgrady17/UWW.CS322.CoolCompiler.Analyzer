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

    public void traverse() {
        for (Enumeration e = classes.getElements(); e.hasMoreElements()) {
            class_ class_ = (class_)e.nextElement();
        }
    }
}
