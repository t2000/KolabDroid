package at.dasz.KolabDroid.Sync;

/**
 * Application-specific exception.
 */
public class SyncException extends Exception
{
	private static final long	serialVersionUID	= -4204603757536693989L;

	public SyncException(String item, String message)
	{
		super(message);
		_Item = item;
	}

	public SyncException(String item, String message, Exception inner)
	{
		super(message, inner);
		_Item = item;
	}

	public String get_Item()
	{
		return _Item;
	}

	private String	_Item;
}
