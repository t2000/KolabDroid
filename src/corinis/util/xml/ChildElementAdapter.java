package corinis.util.xml;

import org.w3c.dom.Node;

public class ChildElementAdapter extends ElementAdapter {

	public ChildElementAdapter(Node parent) {
		super(parent != null ? parent.getFirstChild(): null);
	}

}
