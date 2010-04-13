package corinis.util.xml;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ElementAdapter implements Iterable<Element> {

	private Node start;
	
	public ElementAdapter(Node start)
	{
		this.start = start;
	}
	
	public Iterator<Element> iterator() {
		// TODO Auto-generated method stub
		return new ElementIterator(start);
	}

	private class ElementIterator implements Iterator<Element>
	{
		Node cur;
		Node next;

		public ElementIterator(Node start) {
			next = start;
			// make sure next is an Element
			while (next != null && !(next instanceof Element))
				next = next.getNextSibling();
		}

		public boolean hasNext() {
			return next != null;
		}

		public Element next() {
			cur = next;
			if (cur != null)
			{
				// make sure next is an Element
				do
				{
					next = next.getNextSibling();
				}
				while (next != null && !(next instanceof Element));
			}
			return (Element)cur;
		}

		public void remove() {
			// ignore
		}		
	}
}
