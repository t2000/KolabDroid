package corinis.util.xml;

import org.w3c.dom.Node;

public class ChildNodeAdapter extends NodeAdapter {
	public ChildNodeAdapter(Node parent) {
		super(parent != null ? parent.getFirstChild() : null);
	}

}
