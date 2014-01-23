package memoryManager;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 20/11/2013
 * Time: 00:21
 * To change this template use File | Settings | File Templates.
 */
public class ManagedMemoryPage
{
	public ManagedMemoryPage(byte[] rawData)
	{
		_rawData = rawData;
	}

	public void get() {_references++;}
	public void release() {_references--;}

	public byte[] data() {return _rawData;}
	public void setData(byte[] data)
	{
		_hasBeenChanged = true;
		System.arraycopy(data, 0, _rawData, 0, DiskPage.MAX_PAGE_SIZE);
	}

	public int references() {return _references;}

	public boolean fullyReleased() {return _references == 0;}

	public boolean hasBeenChanged() {return _hasBeenChanged;}

	private byte[] _rawData;
	private int _references = 0;
	private boolean _hasBeenChanged = false;
}
