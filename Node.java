/**
 * Node - one coach of the "train": holds a Student and a link to the next node.
 */
public class Node {
    Student data;
    Node next;

    public Node(Student data) {
        this.data = data;
        this.next = null;   // NULL until linked
    }
}
