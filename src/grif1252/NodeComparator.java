package grif1252;

import java.util.Comparator;

public class NodeComparator<T> implements Comparator<Node<T>>
{

	@Override
	public int compare(Node<T> o1, Node<T> o2)
	{
		if(o1.fn() < o2.fn())
			return 1;
		else if(o1.fn() > o2.fn())
			return -1;
		else 
			return 0;
	}
}