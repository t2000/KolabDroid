package at.dasz.KolabDroid.glue;

public class DataFlavor {
	private Class<?> representationClass = null;
	private String mimeType = null;

	public DataFlavor(Class<?> representationClass, String humanPresentableName) {
		this.representationClass = representationClass;
	}

	public DataFlavor(String mimeType, String humanPresentableName) {
		this.mimeType = mimeType;
	}

	public String getMimeType() {
		return mimeType;
	}

	public Class<?> getRepresentationClass() {
		return representationClass;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null) return false;
	
		if (!o.getClass().equals(this.getClass())) return false;
		
		return this.mimeType.equalsIgnoreCase(((DataFlavor)o).mimeType);
	}
	
	@Override
	public int hashCode()
	{
		return this.mimeType.hashCode();
	}

}
