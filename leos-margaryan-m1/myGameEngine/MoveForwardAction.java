package myGameEngine;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;
import m1.MyGame;

public class MoveForwardAction extends AbstractInputAction {
	
	private Node avN;
	private MyGame myGame;

	public MoveForwardAction(Node n, MyGame g)  
	{   
		avN = n;
		myGame = g;
	}     
	
	public void performAction(float time, Event e)  
	{ 
		
		
		if(e.getComponent().toString().compareTo("Y Axis") == 0) {
			controllerActN(time, e);
		}
		else {
			//send move to keyboardAct
			keyboardActN(time,e);
			
		}
		  
	}

	private void controllerActN(float time, Event e) {
		
		if(e.getValue() > 0.1f || e.getValue() < -0.1f) {
			avN.moveForward(-0.05f* e.getValue());
			myGame.updateVerticalPosition();
		}
		
	}

	private void keyboardActN(float time, Event e) {
		
		if(e.getComponent().toString().compareTo("W") == 0) {
			avN.moveForward(0.05f);
			myGame.updateVerticalPosition();
		}
		
		if(e.getComponent().toString().compareTo("S") == 0) {
			avN.moveForward(-0.05f);
			myGame.updateVerticalPosition();
		}
		
	}   
	
	
}
