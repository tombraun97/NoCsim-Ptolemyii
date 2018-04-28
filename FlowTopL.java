package myactors;


import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;

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
public class FlowTopL extends TypedAtomicActor {
	public int enablesend = 1;
	public int _cycleCount = 0;

	public int P_levels = 10;
	
	protected Time firetime;
	protected Time readytime;
	protected Time[] eastheaderTime =  new Time[P_levels];
	protected Time[] southheaderTime =  new Time[P_levels];
	protected Time[] nextEheaderTime =  new Time[P_levels];
	protected Time[] nextSheaderTime =  new Time[P_levels];
	
	
	
	public int until;
		
	protected TypedIOPort outputDataE;
	protected TypedIOPort outputCredE;
	protected TypedIOPort inputDataE;	
	protected TypedIOPort inputCredE;
	
	protected TypedIOPort outputDataS;
	protected TypedIOPort outputCredS;
	protected TypedIOPort inputDataS;	
	protected TypedIOPort inputCredS;
	
	protected TypedIOPort _debug;
	//-----------------injection port----------------------//
	protected TypedIOPort _injectport;
	protected TypedIOPort _injectcredits;
	
	protected Time[] IheaderTime =  new Time[P_levels];
	protected Time[] nextIheaderTime =  new Time[P_levels];
	protected RecordToken[][] bufferI;
	protected int[] buffposI =  new int[P_levels];
	protected int[] outposI =  new int[P_levels];
	protected int[][] currentheaderI = new int[P_levels][2];
	protected int[][] nextheaderI = new int[P_levels][2];
	protected int[] noI = new int[P_levels];
	//------------------------------------------------------//
	public int outselect = 0;
	
	protected Parameter _Xcoord;
	protected Parameter _Ycoord;
	protected int Xcoord;
	protected int Ycoord;
	//--------------------------------
	//set up buffers for inputs
	protected RecordToken[][] bufferE;
	protected RecordToken[][] bufferS;
	
	protected int[] buffposE =  new int[P_levels];
	protected int[] buffposS=  new int[P_levels];
	
	protected int[] outposE =  new int[P_levels];
	protected int[] outposS =  new int[P_levels];
	
	
	//{dir, flits}
	protected int[][] currentheaderE = new int[P_levels][2];
	protected int[][] currentheaderS = new int[P_levels][2];
											
	protected int[][] nextheaderE = new int[P_levels][2];
	protected int[][] nextheaderS = new int[P_levels][2];
	
	protected int[] creditsE = new int[P_levels];
	protected int[] creditsS = new int[P_levels];
	
	protected int[] noE = new int[P_levels];
	protected int[] noS = new int[P_levels];
	
	
	//---------------------------------

	protected int priorityE = 1;
	protected int priorityS = 1;
	//------------------------------------
	public String[] labels = new String[] {"Xdes", "Ydes", "startTime", "data", "priority", "flits"};
	public Type[] types = new Type[6];
	//------------------------------------
	public FlowTopL(CompositeEntity container, String name) throws               
	      NameDuplicationException, IllegalActionException  {
	
		super(container, name);
		//set up type for the input and output to expect
		for (int i = 0; i < 6; i++) {
			types[i] = BaseType.INT;
		}
		for (int i = 0; i < P_levels; i++) {
			for(int j = 0; j < 2; j++) {
				currentheaderI[i][j] = 0;
				nextheaderI[i][j] = 0;
				currentheaderE[i][j] = 0;
				nextheaderE[i][j] = 0;
				currentheaderS[i][j] = 0;
				nextheaderS[i][j] = 0;
			}
		}
		//initialise values
		for (int i = 0; i<P_levels; i++) {
			creditsE[i] = 5;
			creditsS[i] = 5;
			buffposE[i] = 0;
			buffposS[i]= 0;
			outposE[i] = 0;
			outposS[i] = 0;
			noE[i] = 0;
			noS[i] = 0;
			IheaderTime[i] = getDirector().getModelTime().add(20.0);
			eastheaderTime[i] = getDirector().getModelTime().add(20.0);
			southheaderTime[i] = getDirector().getModelTime().add(20.0);
			
		}
		RecordType Type1 = new RecordType(labels, types); //basic type of accepted flit
		//set up buffers
		
		bufferE = new RecordToken[P_levels][5];
		bufferS = new RecordToken[P_levels][5];
		bufferI = new RecordToken[P_levels][5];

		//setup ports
		outputDataE = new TypedIOPort(this, "OutputDataEast", false, true);
		outputCredE = new TypedIOPort(this, "OutputCredEast", false, true);
		
		inputDataE = new TypedIOPort(this, "InputDataEast", true, false);
		inputCredE = new TypedIOPort(this, "InputCredEast", true, false);
		
		outputDataS = new TypedIOPort(this, "OutputDataSouth", false, true);
		outputCredS = new TypedIOPort(this, "OutputCredSouth", false, true);
		
		inputDataS = new TypedIOPort(this, "InputDataSouth", true, false);
		inputCredS = new TypedIOPort(this, "InputCredSouth", true, false);
		
		_debug = new TypedIOPort(this, "Debug", false, true);
		_injectport = new TypedIOPort(this, "injectport", true, false);
		_injectcredits = new TypedIOPort(this, "injectcredits", false, true);
		
		
		_Xcoord = new Parameter(this, "Xcoord");
		_Ycoord = new Parameter(this, "Ycoord");
	    _Xcoord.setExpression("0"); // initial value
	    _Ycoord.setExpression("0"); // initial value
	    
	    /*----Setting up the port directions and look of the actor----*/
	    StringAttribute outdECardinal = new StringAttribute(outputDataE,
                "_cardinal");
	    StringAttribute outcECardinal = new StringAttribute(outputCredE,
                "_cardinal");
	    StringAttribute indECardinal = new StringAttribute(inputDataE,
                "_cardinal");
	    StringAttribute incECardinal = new StringAttribute(inputCredE,
                "_cardinal");
	    outdECardinal.setExpression("EAST");
	    outcECardinal.setExpression("EAST");
	    indECardinal.setExpression("EAST");
	    incECardinal.setExpression("EAST");
	    
	    StringAttribute outdSCardinal = new StringAttribute(outputDataS,
                "_cardinal");
	    StringAttribute outcSCardinal = new StringAttribute(outputCredS,
                "_cardinal");
	    StringAttribute indSCardinal = new StringAttribute(inputDataS,
                "_cardinal");
	    StringAttribute incSCardinal = new StringAttribute(inputCredS,
                "_cardinal");
	    outdSCardinal.setExpression("SOUTH");
	    outcSCardinal.setExpression("SOUTH");
	    indSCardinal.setExpression("SOUTH");
	    incSCardinal.setExpression("SOUTH");
	    
	    StringAttribute _debugCardinal = new StringAttribute(_debug,
                "_cardinal");
	    _debugCardinal.setExpression("WEST");
	    
	    StringAttribute _injectCardinal = new StringAttribute(_injectport,
                "_cardinal");
	    _injectCardinal.setExpression("NORTH");
	    StringAttribute _inject2Cardinal = new StringAttribute(_injectcredits,
                "_cardinal");
	    _inject2Cardinal.setExpression("NORTH");
        /*--------------------------------------------------------------*/
        outputDataE.setTypeEquals(Type1);       
        inputDataE.setTypeEquals(Type1); 

        inputCredE.setTypeEquals(BaseType.INT);
        outputCredE.setTypeEquals(BaseType.INT);
        
        outputDataS.setTypeEquals(Type1);        
        inputDataS.setTypeEquals(Type1); 

        inputCredS.setTypeEquals(BaseType.INT);
        outputCredS.setTypeEquals(BaseType.INT);
        
        _debug.setTypeEquals(Type1);
        _injectport.setTypeEquals(Type1);
        _injectcredits.setTypeEquals(BaseType.INT);
        
	}
	public void initialize() throws IllegalActionException{
		until = 0;
		firetime = getDirector().getModelTime();
		//eastheaderTime = getDirector().getModelTime();
		//southheaderTime = getDirector().getModelTime();
	}
	
	public void fire() throws IllegalActionException{
		super.fire();
		_cycleCount+=1;
		//System.out.println(_cycleCount);
		
		//record current time of fire
		Time currenttime = getDirector().getModelTime();
		//System.out.println(currenttime.getDoubleValue());
		//System.out.println(firetime.getDoubleValue());
		//check to see if the fire was expected and enable or disable output respectively
		if(currenttime.getDoubleValue() >= firetime.getDoubleValue()) {
			//_cycleCount+=1;
			firetime = currenttime.add(1.0);
			getDirector().fireAt(this, firetime);
			//enablesend = 0;
		/*---algorithm to decide which values get priority to go out first----*/
			outselect();
			enablesend = 1;
			
			
		/*------------------------------------------------------------------*/	
		}
		else {
			enablesend = 0;
		}
		
		for (int i = 0; i < P_levels; i++) {
			if (currentheaderE[i][1] == 0) {
				if (nextheaderE[i][1] > 0) {
					currentheaderE[i][0] = nextheaderE[i][0];
					currentheaderE[i][1] = nextheaderE[i][1];
					eastheaderTime[i] = nextEheaderTime[i];
					nextheaderE[i][1] = 0;
				}
			}
			if (currentheaderS[i][1] == 0) {
				if (nextheaderS[i][1] > 0) {
					currentheaderS[i][0] = nextheaderS[i][0];
					currentheaderS[i][1] = nextheaderS[i][1];
					southheaderTime[i] = nextSheaderTime[i];
					nextheaderS[i][1] = 0;
				}
			}
			if (currentheaderI[i][1] == 0) {
				if (nextheaderI[i][1] > 0) {
					currentheaderI[i][0] = nextheaderI[i][0];
					currentheaderI[i][1] = nextheaderI[i][1];
					IheaderTime[i] = nextIheaderTime[i];
					nextheaderI[i][1] = 0;
				}
			}
			if (buffposE[i] > 4) {
				buffposE[i] = 0;
			}
			if (buffposS[i] > 4) {
				buffposS[i] = 0;
			}
			if (buffposI[i] > 4) {
				buffposI[i] = 0;
			}
			if (outposE[i] > 4) {
				outposE[i] = 0;
			}
			if (outposS[i] > 4) {
				outposS[i] = 0;
			}
			if (outposI[i] > 4) {
				outposI[i] = 0;
			}
		}
		//get the router's current location		
		Xcoord = ((IntToken) _Xcoord.getToken()).intValue();
		Ycoord = ((IntToken) _Ycoord.getToken()).intValue();

		
//---------check for injected task--------------------------------------//		
		if (_injectport.hasToken(0)) {			
			//check to see if expecting header flit then configure the port respectively
			RecordToken In_I = (RecordToken) _injectport.get(0);
			int priorityI = ((IntToken) In_I.get("priority")).intValue();
			//if expecting header read it
			if (noI[priorityI-1] < 1) {
				Time headerTime = getDirector().getModelTime();
				int flitsI = ((IntToken) In_I.get("flits")).intValue();
				
				int XdesI = ((IntToken) In_I.get("Xdes")).intValue();
				int YdesI = ((IntToken) In_I.get("Ydes")).intValue();
				int dirI = xymap(Xcoord, Ycoord, XdesI, YdesI);
				noI[priorityI-1] = flitsI;
				if (currentheaderI[(priorityI-1)][1] != 0) {					
					nextheaderI[(priorityI-1)][0] = dirI;
					nextheaderI[(priorityI-1)][1] = flitsI;
					nextIheaderTime[(priorityI-1)] = headerTime;
				}
				else {
					currentheaderI[(priorityI-1)][0] = dirI;
					currentheaderI[(priorityI-1)][1] = flitsI;
					IheaderTime[(priorityI-1)] = headerTime;				
				}
				bufferI[priorityI-1][buffposI[priorityI-1]] = In_I;
				buffposI[priorityI-1] += 1;
				noI[priorityI-1] -= 1;
				if (buffposI[priorityI-1] > 4) {
					buffposI[priorityI-1] = 0;
				}
			}
			//if still flits to come handle them in the same way as current setup
			else if (noI[priorityI-1] > 0) {
				noI[priorityI-1] -= 1;				
				bufferI[priorityI-1][buffposI[priorityI-1]] = In_I;
				buffposI[priorityI-1] += 1;
				if (buffposI[priorityI-1] > 4) {
					buffposI[priorityI-1] = 0;
				}							
			}							
		}
//-------------Check EAST port for data-----------------------------------------//
		
		if (inputDataE.hasToken(0)) {				
			//check to see if expecting header flit then configure the port respectively
			RecordToken In_E = (RecordToken) inputDataE.get(0);
			int priorityE = ((IntToken) In_E.get("priority")).intValue();
			//if expecting header read it
			if (noE[priorityE-1] < 1) {
				Time headerTime = getDirector().getModelTime();
				int flitsE = ((IntToken) In_E.get("flits")).intValue();
				
				int XdesE = ((IntToken) In_E.get("Xdes")).intValue();
				int YdesE = ((IntToken) In_E.get("Ydes")).intValue();
				int dirE = xymap(Xcoord, Ycoord, XdesE, YdesE);
				noE[priorityE-1] = flitsE;
				if (currentheaderE[(priorityE-1)][1] != 0) {					
					nextheaderE[(priorityE-1)][0] = dirE;
					nextheaderE[(priorityE-1)][1] = flitsE;
					nextEheaderTime[(priorityE-1)] = headerTime;
				}
				else {
					currentheaderE[(priorityE-1)][0] = dirE;
					currentheaderE[(priorityE-1)][1] = flitsE;
					eastheaderTime[(priorityE-1)] = headerTime;				
				}
				bufferE[priorityE-1][buffposE[priorityE-1]] = In_E;
				buffposE[priorityE-1] += 1;
				noE[priorityE-1] -= 1;
				if (buffposE[priorityE-1] > 4) {
					buffposE[priorityE-1] = 0;
				}
			}
			//if still flits to come handle them in the same way as current setup
			else if (noE[priorityE-1] > 0) {
				noE[priorityE-1] -= 1;				
				bufferE[priorityE-1][buffposE[priorityE-1]] = In_E;
				buffposE[priorityE-1] += 1;
				if (buffposE[priorityE-1] > 4) {
					buffposE[priorityE-1] = 0;
				}							
			}							
		}
//----------------check south port for data-----------------------------------------//		
		//if there's a token, take it
		if (inputDataS.hasToken(0)) {
			//store to buffer depending on priority
			RecordToken In_S = (RecordToken) inputDataS.get(0);
			int priorityS = ((IntToken) In_S.get("priority")).intValue();
			
			if (noS[priorityS-1] < 1) {			
				Time headerTime = getDirector().getModelTime();	
				int flitsS = ((IntToken) In_S.get("flits")).intValue();
				
				int XdesS = ((IntToken) In_S.get("Xdes")).intValue();
				int YdesS = ((IntToken) In_S.get("Ydes")).intValue();
				int dirS = xymap(Xcoord, Ycoord, XdesS, YdesS);
				noS[priorityS-1] = flitsS;
				if (currentheaderS[(priorityS-1)][1] != 0) {					
					nextheaderS[(priorityS-1)][0] = dirS;
					nextheaderS[(priorityS-1)][1] = flitsS;
					nextSheaderTime[(priorityS-1)] = headerTime;
				}
				else {
					currentheaderS[(priorityS-1)][0] = dirS;
					currentheaderS[(priorityS-1)][1] = flitsS;
					southheaderTime[(priorityS-1)] = headerTime;
				}
				bufferS[priorityS-1][buffposS[priorityS-1]] = In_S;
				buffposS[priorityS-1] += 1;
				noS[priorityS-1] -= 1;
				if (buffposS[priorityS-1] > 4) {
					buffposS[priorityS-1] = 0;
				}
			}
			else if (noS[priorityS-1] > 0) {
				noS[priorityS-1] -= 1;
				bufferS[priorityS-1][buffposS[priorityS-1]] = In_S;
				buffposS[priorityS-1] += 1;
				if (buffposS[priorityS-1] > 4) {
					buffposS[priorityS-1] = 0;
				}							
			}			
		}
		//check for credits and type
		if(inputCredE.hasToken(0)) {		
			int type = ((IntToken)inputCredE.get(0)).intValue();			
			creditsE[type-1] += 1;
		}
		if(inputCredS.hasToken(0)) {
			
			int type = ((IntToken)inputCredS.get(0)).intValue();
			creditsS[type-1] += 1;
		}		
	}

	public void output(int direction, int priority, String buffer) throws IllegalActionException {
		
		int loc = (priority - 1);
		if (buffer == "INJEC") {
			if (outposI[loc] > 4) {
				outposI[loc] = 0;
			}
			IntToken cred = new IntToken(priority);
			
			if (direction == 1 && bufferI[loc][outposI[loc]] != null) {
				outputDataE.send(0, bufferI[loc][outposI[loc]]);
				bufferI[loc][outposI[loc]] = null;
				outposI[loc] += 1;
				_injectcredits.send(0, cred);
				creditsE[loc] -= 1;
				currentheaderI[loc][1] -= 1;
			}
			if (direction == 2 && bufferI[loc][outposI[loc]] != null) {
				outputDataS.send(0, bufferI[loc][outposI[loc]]);
				bufferI[loc][outposI[loc]] = null;
				outposI[loc] += 1;
				_injectcredits.send(0, cred);
				creditsS[loc] -= 1;
				currentheaderI[loc][1] -= 1;
			}
			if (direction == 3 && bufferI[loc][outposI[loc]] != null) {
				_debug.send(0, bufferI[loc][outposI[loc]]);
				bufferI[loc][outposI[loc]] = null;
				outposI[loc] += 1;
				_injectcredits.send(0, cred);
	
				currentheaderI[loc][1] -= 1;
			}
		}
		else if (buffer == "EAST") {
			if (outposE[loc] > 4) {
				outposE[loc] = 0;
			}
			IntToken cred = new IntToken(priority);
			
			if (direction == 1 && bufferE[loc][outposE[loc]] != null) {
				outputDataE.send(0, bufferE[loc][outposE[loc]]);
				bufferE[loc][outposE[loc]] = null;
				outposE[loc] += 1;
				outputCredE.send(0, cred);
				creditsE[loc] -= 1;
				currentheaderE[loc][1] -= 1;
			}
			if (direction == 2 && bufferE[loc][outposE[loc]] != null) {
				outputDataS.send(0, bufferE[loc][outposE[loc]]);
				bufferE[loc][outposE[loc]] = null;
				outposE[loc] += 1;
				outputCredE.send(0, cred);
				creditsS[loc] -= 1;
				currentheaderE[loc][1] -= 1;
			}
			if (direction == 3 && bufferE[loc][outposE[loc]] != null) {
				_debug.send(0, bufferE[loc][outposE[loc]]);
				bufferE[loc][outposE[loc]] = null;
				outposE[loc] += 1;
				outputCredE.send(0, cred);
	
				currentheaderE[loc][1] -= 1;
			}
		}
		else if (buffer == "SOUTH") {
			if (outposS[loc] > 4) {
				outposS[loc] = 0;
			}
			IntToken cred = new IntToken(priority);
			if (direction == 1 && bufferS[loc][outposS[loc]] != null) {
				outputDataE.send(0, bufferS[loc][outposS[loc]]);
				bufferS[loc][outposS[loc]] = null;
				outposS[loc] += 1;
				outputCredS.send(0, cred);
				creditsE[loc] -= 1;
				currentheaderS[loc][1] -= 1;
			}
			if (direction == 2 && bufferS[loc][outposS[loc]] != null) {
				outputDataS.send(0, bufferS[loc][outposS[loc]]);
				bufferS[loc][outposS[loc]] = null;
				outposS[loc] += 1;
				outputCredS.send(0, cred);
				creditsS[loc] -= 1;
				currentheaderS[loc][1] -= 1;
			}
			if (direction == 3 && bufferS[loc][outposS[loc]] != null) {
				_debug.send(0, bufferS[loc][outposS[loc]]);
				bufferS[loc][outposS[loc]] = null;
				outposS[loc] += 1;
				outputCredS.send(0, cred);
	
				currentheaderS[loc][1] -= 1;
			}

		}
	}

	public void outselect() throws IllegalActionException {
								//	 direction = 1, direction = 2, direction = 4;
								// { {bestS,bestE,bestI}, {bestS,bestE,bestI}, {bestS,bestE,bestI} } 
		int[][] _t = new int[][] {{0,0,0}, {0,0,0}, {0,0,0}};
		
		//loop through current values waiting in each buffer to find the highest priority
		for (int i = P_levels-1; i >= 0; i--) {
			//find highest in injection port
			if (currentheaderI[i][1] > 0) {
				if (currentheaderI[i][0] == 1 && creditsE[i] > 0) {
					if(i+1 >= _t[0][2]) {
						_t[0][2] = i+1;
					}
				}
				if (currentheaderI[i][0] == 2  && creditsS[i] > 0) {
					if(i+1 >= _t[1][2]) {
						_t[1][2] = i+1;
					}
				}
				if (currentheaderI[i][0] == 4) {
					if(i+1 >= _t[2][2]) {
						_t[2][2] = i+1;
					}
				}
			}
			//find highest priority in east buffer
			if (currentheaderE[i][1] > 0) {
				if (currentheaderE[i][0] == 1 && creditsE[i] > 0) {
					if(i+1 >= _t[0][1]) {
						_t[0][1] = i+1;
					}
				}
				if (currentheaderE[i][0] == 2  && creditsS[i] > 0) {
					if(i+1 >= _t[1][1]) {
						_t[1][1] = i+1;
					}
				}
				if (currentheaderE[i][0] == 4) {
					if(i+1 >= _t[2][1]) {
						_t[2][1] = i+1;
					}
				}
			}
			//find highest priority in south buffer
			if (currentheaderS[i][1] > 0) {
				if (currentheaderS[i][0] == 1 && creditsE[i] > 0) {
					if(i+1 > _t[0][0]) {
						_t[0][0] = i+1;
					}
				}
				if (currentheaderS[i][0] == 2 && creditsS[i] > 0) {
					if(i+1 >= _t[1][0]) {
						_t[1][0] = i+1;
					}
				}
				if (currentheaderS[i][0] == 4 ) {
					if(i+1 >= _t[2][0]) {
						_t[2][0] = i+1;
					}
				}
			}
		}
		//System.out.println(Arrays.deepToString(_t));
		//for each direction
		for (int i = 2; i >= 0; i--) {
			//System.out.println(i);
			if (_t[i][0] > 0 || _t[i][1] > 0 || _t[i][2] > 0) {
				int max = maxarray(_t[i]);
				//System.out.println(max);
				if (max == 2) {
					output(i+1, _t[i][2], "INJEC");
				}
				else if (max == 1) {
					output(i+1, _t[i][1], "EAST");
				}
				else {
					output(i+1, _t[i][0], "SOUTH");
				}
			}
		}
	}
	//return index of max value in array
	public int maxarray(int[] array) {
		int largest = array[0], index = 0;
		for (int i = 1; i < array.length; i++) {
		  if ( array[i] > largest ) {
		      largest = array[i];
		      index = i;
		   }
		  else if (array[i] == largest) {
			  index = i+10;
		  }
		}
		return index;
	}
	//map xy algorithm
	public int xymap(int Cx, int Cy, int Dx, int Dy) {
		int NORTH = 0;
		int EAST = 1;
		int SOUTH = 2;
		int WEST = 3;
		int current = 4;		
		if (Dx > Cx) {
			return EAST;
		}
		if (Dx < Cx){
			return WEST;
		}
		else {
			if (Dy < Cy) {
				return SOUTH;
			}
			if (Dy > Cy) {
				return NORTH;
			}
			else {
				return current;
			}
		}
	}
	public void pruneDependencies() {
		super.pruneDependencies();
		removeDependency(inputDataE, outputDataE);
		removeDependency(inputDataE, outputCredE);
		removeDependency(inputDataE, inputCredE);
		removeDependency(inputCredE, outputDataE);
		removeDependency(inputCredE, outputCredE);
		
		removeDependency(inputDataS, outputDataS);
		removeDependency(inputDataS, outputCredS);
		removeDependency(inputDataS, inputCredS);	
		removeDependency(inputCredS, outputDataS);
		removeDependency(inputCredS, outputCredS);
		
		removeDependency(inputDataS, outputDataE);
		removeDependency(inputDataS, outputCredE);
		removeDependency(inputDataS, inputCredE);	
		removeDependency(inputCredS, outputDataE);
		removeDependency(inputCredS, outputCredE);
	}
}
