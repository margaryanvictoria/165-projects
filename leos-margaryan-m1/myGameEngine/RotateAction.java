package myGameEngine;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;

public class RotateAction extends AbstractInputAction {
	
	private Node avN;  
	public RotateAction(Node n)  
	{   
		avN = n;  
	}     
	
	public void performAction(float time, Event e)  
	{ 
		
		
		if(e.getComponent().toString().compareTo("X Axis") == 0) {
			controllerActN(time, e);
		}
		else {
			//send move to keyboardAct
			keyboardActN(time,e);
			
		}
		
		
		
		  
	}

	private void controllerActN(float time, Event e) {
		//grab dolphin y axis
		Vector3 dolphinYAxis = avN.getLocalUpAxis();
		Angle rotDegree = Degreef.createFrom(-0.4f);
		//rotate dolphin on axis
		if(e.getValue() > 0.2f ) {
			
			//rotate dolphin
			avN.rotate(rotDegree, dolphinYAxis);
		}
		
		if(e.getValue() < -0.2f) {
			rotDegree = Degreef.createFrom(0.4f);
			
			//rotate dolphin
			avN.rotate(rotDegree, dolphinYAxis);
		}
	}

	private void keyboardActN(float time, Event e) {
		//grab dolphin y axis
		Vector3 dolphinYAxis = avN.getLocalUpAxis();
		Angle rotDegree = Degreef.createFrom(-0.4f);
		
		//rotate dolphin on axis
		if(e.getComponent().toString().compareTo("D") == 0) {
			//rotate dolphin
			avN.rotate(rotDegree, dolphinYAxis);
			
		}
		
		if(e.getComponent().toString().compareTo("A") == 0) {
			rotDegree = Degreef.createFrom(0.4f);
			
			//rotate dolphin
			avN.rotate(rotDegree, dolphinYAxis);
			
		}
		
	}   
	
	
}
