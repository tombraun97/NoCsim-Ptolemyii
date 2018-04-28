package myactors;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

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


@SuppressWarnings("serial")
public class TaskTest extends TypedAtomicActor {
	
	protected Time firetime;
	
	protected Parameter _taskid;
	protected int taskid;
	public double[] delayTimes = new double[10];
	protected String[] token;
	StringParameter fileToRead;
	protected String _file;
	
	
	protected Parameter _Xdes;
	protected Parameter _Ydes;
	protected Parameter _Xstart;
	protected Parameter _Ystart;
	protected Parameter _data;
	protected Parameter _priority;
	protected Parameter _flits;
	
	//protected Parameter _startdelay;
	
	protected IntToken Xdes;
	protected IntToken Ydes;
	protected IntToken data;
	protected IntToken priority;
	protected IntToken flits;
	protected IntToken startTime;
	
	protected double startdel_double;
	
	protected TypedIOPort output;
	protected TypedIOPort input;
	
	
	protected int outcount = 0;
	protected int credits = 5;
	//output type
	public String[] labels = new String[] {"Xdes", "Ydes", "startTime", "data", "priority", "flits"};
	public Type[] types = new Type[6];
	
	public TaskTest(CompositeEntity container, String name) throws               
    NameDuplicationException, IllegalActionException  {

	super(container, name);
	fileToRead = new StringParameter(this, "file");
	fileToRead.setExpression("testparam.txt"); 
	for (int i = 0; i < 6; i++) {
		types[i] = BaseType.INT;
	}
	RecordType Type1 = new RecordType(labels, types);
	
	output = new TypedIOPort(this, "OutputTask", false, true);
	input = new TypedIOPort(this, "InputCredits", true, false);
	StringAttribute inCard = new StringAttribute(input,
            "_cardinal");
	inCard.setExpression("EAST");
	
	_taskid = new Parameter(this, "taskid");
	_Xdes = new Parameter(this, "Xdes");
	_Ydes = new Parameter(this, "Ydes");
	_data = new Parameter(this, "data");
	_priority = new Parameter(this, "priority");
	_flits = new Parameter(this, "flits");
	
	//_startdelay = new Parameter(this, "Start Delay");
	
	//initial values
	_taskid.setExpression("0");
    _Xdes.setExpression("0"); 
    _Ydes.setExpression("0"); 
    _data.setExpression("0");
    _priority.setExpression("1"); 
    _flits.setExpression("0"); 
    
    //_startdelay.setExpression("0.0");
	
    output.setTypeEquals(Type1);

	}
	
	public void initialize() throws IllegalActionException{
		taskid = ((IntToken) _taskid.getToken()).intValue();
		_file = fileToRead.stringValue();
		try {
			FileReader f = new FileReader(_file);
			BufferedReader br = new BufferedReader(f);
			String line;
			while((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line);
				int Taskid = Integer.parseInt(st.nextToken());
				double delay = Integer.parseInt(st.nextToken());
				
				delayTimes[Taskid] = delay;
				
			}
			br.close();
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		outcount = ((IntToken) _flits.getToken()).intValue();
		
		startdel_double = delayTimes[taskid];
		Xdes = ((IntToken) _Xdes.getToken());
	    Ydes = ((IntToken) _Ydes.getToken());
	    data = ((IntToken) _data.getToken());
	    priority = ((IntToken) _priority.getToken());
	    flits = ((IntToken) _flits.getToken());
	    double temp = startdel_double;
	    //conversion from double to IntToken
	    int temp2 = (int) temp;
	    IntToken temp3 = new IntToken(temp2);
	    startTime = temp3;
	    
		Time currenttime = getDirector().getModelTime();
		getDirector().fireAt(this, currenttime.add(startdel_double));
		
		firetime = currenttime.add(startdel_double);
		
		
	    
	}
	
	public void fire() throws IllegalActionException{
		super.fire();
		
		Time currenttime = getDirector().getModelTime();
		
		if(currenttime.getDoubleValue() >= firetime.getDoubleValue()) {
			firetime = currenttime.add(1.0);
			if (outcount > 0) {
				getDirector().fireAt(this, firetime);
				sendtokenout(Xdes, Ydes, startTime, data, priority, flits);
			}
			
		}
		if(input.hasToken(0)) {
			input.get(0);
			credits += 1;
		}
		
	}
	
	public void sendtokenout(IntToken Xdes, IntToken Ydes, IntToken startTime, IntToken data, IntToken priority, IntToken flits) throws IllegalActionException {
		String[] labels = {"Xdes", "Ydes", "startTime", "data", "priority", "flits"};
		Token[] values = {Xdes, Ydes, startTime, data, priority, flits};
		
		RecordToken OutputToken = new RecordToken(labels, values);
		if (credits > 0) {
			output.send(0, OutputToken);
			credits -= 1;
			outcount -= 1;
		}
	}
	public void pruneDependencies() {
		super.pruneDependencies();
		
		removeDependency(input, output);
	}
	
	
	
	
	
}
