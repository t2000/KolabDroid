package at.dasz.KolabDroid.Sync;

import javax.mail.Message;

public class SyncContext
{
	private Object localItem = null;
	private CacheEntry cacheEntry = null;
	private Message message = null;

	public Object getLocalItem()
	{
		return localItem;
	}
	public void setLocalItem(Object localItem)
	{
		this.localItem = localItem;
	}
	public CacheEntry getCacheEntry()
	{
		return cacheEntry;
	}
	public void setCacheEntry(CacheEntry cacheEntry)
	{
		this.cacheEntry = cacheEntry;
	}
	public Message getMessage()
	{
		return message;
	}
	public void setMessage(Message message)
	{
		this.message = message;
	}
}
