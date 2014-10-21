package test.MemoryManagement;


import junit.framework.Assert;

import org.junit.Test;

import MemoryManagement.Clock;
import MemoryManagement.PageTableEntry;

/**
 * 
 * @author Johann Mantler
 *
 */
public class ClockTest {

	/**
	 * Testet execute() nur mit einem Listenelement.
	 */
	@Test
	public final void testExecute_1() {
		//set up
		Clock clock = new Clock();
		
		PageTableEntry entry1 = new PageTableEntry();
		entry1.setReferenced(true);
		clock.insert(1, 1, entry1, 1);
		
		//exercise
		int[] result = clock.execute();
		
		//verify outcome
		Assert.assertTrue( result[0] == 1 );	//SeitenNr
		Assert.assertTrue( result[1] == -1 );	//RahmenNr
		Assert.assertTrue( result[2] == 1 );	//Addresse der Plattenzuordnungstabelle

		result = clock.execute(); //result muss jetzt null sein
		Assert.assertNull(result);
	}
	
	
	@Test
	public final void testExecute_2() {
		//set up
		Clock clock = new Clock();
		
		PageTableEntry entry1 = new PageTableEntry();
		entry1.setReferenced(true);
		clock.insert(1, 1, entry1, 1);
		
		PageTableEntry entry2 = new PageTableEntry();
		entry2.setReferenced(true);
		clock.insert(2, 2, entry2, 2);
		
		PageTableEntry entry3 = new PageTableEntry();
		entry3.setReferenced(true);
		clock.insert(3, 3, entry3, 3);
		
		
		PageTableEntry entry4 = new PageTableEntry();
		entry4.setReferenced(true);
		clock.insert(4, 4, entry4, 4);
		
		PageTableEntry entry5 = new PageTableEntry();
		entry5.setReferenced(true);
		clock.insert(5, 5, entry5, 5);
		
		
		//exercise
		int[] result = clock.execute(); //Jetzt wird die ganze Ringliste vom Cursor durchlaufen..
										//und von allen Einttraegen die referenced auf false gesetzt.
		
		//verify outcome
		Assert.assertTrue( result[0] == 1 );	//SeitenNr
		Assert.assertTrue( result[1] == -1 );	//RahmenNr
		Assert.assertTrue( result[2] == 1 );	//Addresse der Plattenzuordnungstabelle

		Assert.assertFalse(entry5.isReferenced()); //Beweis: alle referenced sind false!
		
		 
		entry2.setReferenced(true); //damit execute entry3 zurueckliefert
		
		result = clock.execute();
		Assert.assertTrue( result[0] == 3 );	//SeitenNr
		Assert.assertTrue( result[1] == -1 );	//RahmenNr
		Assert.assertTrue( result[2] == 3 );	//Addresse der Plattenzuordnungstabelle

		Assert.assertFalse(entry2.isReferenced());
		
	}

}
