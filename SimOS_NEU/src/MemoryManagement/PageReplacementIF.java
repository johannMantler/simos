package MemoryManagement;

public interface PageReplacementIF {
	
	void insert(int allocateTableAddr,int pageTableAddr, PageTableEntry entry, int pageNr);
	int[] execute();
}
