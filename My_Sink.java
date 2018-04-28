package myactors;

import ptolemy.actor.*;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.IntToken;
import ptolemy.data.type.Type;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;


@SuppressWarnings("serial")
public class My_Sink extends TypedAtomicActor{
	public int P_levels = 10;
	
	protected int[] currentflits = new int[P_levels];
	protected Time finishtime;
	protected int[] tasksource = new int[] {0, 0};
	protected int[] dataid = new int[P_levels];
	protected TypedIOPort input;
	public String[] labels = new String[] {"Xdes", "Ydes", "startTime", "data", "priority", "flits"};
	public Type[] types = new Type[6];
	
	public int t = 0;
	
	public My_Sink(CompositeEntity container, String name) throws               
    NameDuplicationException, IllegalActionException  {

		super(container, name);
	
		for (int i = 0; i < 6; i++) {
			types[i] = BaseType.INT;
		}
		for (int i = 0; i < P_levels; i++) {
			currentflits[i] = 0;
		}
		RecordType Type1 = new RecordType(labels, types);
	
		input = new TypedIOPort(this, "flitsIN", true, false);
		
		input.setTypeEquals(Type1);
	}
	public void preinitialize() throws IllegalActionException{
		PrintWriter writer;
		try {
			writer = new PrintWriter("Times.txt");
			writer.append("");
		    writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	public void fire() throws IllegalActionException{
	
		if (input.hasToken(0)) {
			RecordToken In = (RecordToken) input.get(0);
			int priority = ((IntToken) In.get("priority")).intValue();
			int flits = ((IntToken) In.get("flits")).intValue();
			int start = ((IntToken) In.get("startTime")).intValue();
			int data = ((IntToken) In.get("data")).intValue();
			if (currentflits[priority-1] < 1) {		
				currentflits[priority-1] = flits-1;
				dataid[priority-1] = data;
				
				t = 1;
	
			}
			else {
				currentflits[priority-1] -= 1;
				if (t != 0 && currentflits[priority-1] == 0) {
					finishtime = getDirector().getModelTime();
					int GAValue = (int) finishtime.getDoubleValue();
					GAValue -= start;
					//System.out.println(dataid[priority-1]+ ":" + GAValue);
					try {
						WriteStringToFile(dataid[priority-1]+ ":"+GAValue);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
			//finishtime = getDirector().getModelTime();
			
			//System.out.println(currentflits[1]);
			//System.out.println(finishtime);
		}
	}
	public void WriteStringToFile(String out) throws IOException{
		//PrintWriter pw = null;
		//FileWriter fw = null;
		BufferedWriter writer = new BufferedWriter(new FileWriter("Times.txt", true));
		
	    writer.append(out);
	    writer.append("\r\n");
	    writer.close();
	    
	    
		
	}
}	
