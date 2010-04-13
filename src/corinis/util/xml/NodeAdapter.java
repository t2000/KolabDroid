package corinis.util.xml;

import java.util.Iterator;

import org.w3c.dom.Node;

public class NodeAdapter implements Iterable<Node> {
	private Node start;
	
	public NodeAdapter(Node start)
	{
		this.start = start;
	}
	
	
	public Iterator<Node> iterator() {
		// TODO Auto-generated method stub
		return new NodeIterator(start);
	}

	private class NodeIterator implements Iterator<Node>
	{
		Node cur;
		Node next;

		public NodeIterator(Node start) {
			next = start;
		}

		public boolean hasNext() {
			return next != null;
		}

		public Node next() {
			cur = next;
			if (cur != null)
				next = cur.getNextSibling();
			return (Node)cur;
		}

		public void remove() {
			// ignore
		}		
	}

}
