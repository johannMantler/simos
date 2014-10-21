package Hardware;

import java.util.ArrayList;


public class RegisterSet {
	
    private String programCounter;
    private String register1;
    private String register2;

    private SysConsole console;
    private ArrayList<String> stack;
    
    //neu:
    private int pageTableAddr; //Speicheranfangsadresse der Seitentabelle
    private int allocateTableAddr;
    
    public final int getAllocateTableAddr() {
		return allocateTableAddr;
	}

	public final void setAllocateTableAddr(int allocateTableAddr) {
		this.allocateTableAddr = allocateTableAddr;
	}

	/** Creates a new instance of Register */
    public RegisterSet() {
      stack = new ArrayList<String>();
    }
    
    public final int getPageTableAddr() {
		return pageTableAddr;
	}

	public final void setPageTableAddr(int pageTableAddr) {
		this.pageTableAddr = pageTableAddr;
	}

	public String getProgramCounter() {
        return programCounter;
    }
    
    public void setProgramCounter(String virtualAddr) {
        programCounter = virtualAddr;
    }
    
    public String getRegister1() {
        return register1;
    }

    public void setRegister1(String value) {
        register1 = value;
    }

    public String getRegister2() {
        return register2;
    }

    public void setRegister2(String value) {
        register2 = value;
    }    

    public SysConsole getConsole() {
      return console;
    }
    
    public void setConsole( SysConsole console ) {
      this.console = console;
    }
    
}

