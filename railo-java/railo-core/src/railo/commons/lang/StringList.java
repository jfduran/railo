package railo.commons.lang;

/**
 * a Simple single direction string list
 */
public final class StringList {
	


	private final Entry root=new Entry(null,Entry.NUL);
	private Entry curr;
	private int count=0;
	
	/**
	 * constructor of the class
	 */
	public StringList() {
		curr=root;
	}
	
	/**
	 * constructor of the class
	 * @param str String Element
	 */
	public StringList(String str) {
		root.next=new Entry(str,Entry.NUL);
		curr=root.next;
		count=1;
	}
	
	/**
	 * constructor of the class, initalize with 2 values
	 * @param str1
	 * @param str2
	 */
	public StringList(String str1, String str2) {
		this(str1);
		add(str2);
	}

	/**
	 * @return returns if List has a next Element
	 */
	public boolean hasNext() {
		return curr.next!=null;
	}

	/**
	 * @return returns if List has a next Element
	 */
	public boolean hasNextNext() {
		return curr.next!=null && curr.next.next!=null;
	}

	/**
	 * @return returns next element in the list
	 */
	public String next() {
		curr=curr.next;
		return curr.data;
	}
	public char delimeter() {
		return curr.delimeter;
	}
	
	/**
	 * @return returns current element in the list
	 */
	public String current() {
		return curr.data;
	}

	/**
	 * reset the String List
	 * @return 
	 */
	public StringList reset() {
		curr=root;
        return this;
	}
	
	/**
	 * @return returns the size of the list
	 */
	public int size() {
	    return count;
	}
	
	
	/**
	 * adds a element to the list
	 * @param str String Element to add 
	 */
	public void add(String str) {
		curr.next=new Entry(str,Entry.NUL);
		curr=curr.next;
		count++;
	}
	public void add(String str, char delimeter) {
		curr.next=new Entry(str,delimeter);
		curr=curr.next;
		count++;
	}
	
	private class Entry {
		private static final char NUL=(char)0;
		private Entry next;
		private String data;
		private char delimeter;
		private Entry(String data, char delimeter) {
			this.data=data;
			this.delimeter=delimeter;
		}
	}
}