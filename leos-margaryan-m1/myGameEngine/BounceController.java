package myGameEngine;

import java.util.ArrayList;

import ray.rage.scene.SceneNode;
import ray.rage.scene.controllers.AbstractController;

public class BounceController extends AbstractController{

		//variables for bouncing
		float bounceTime = 0.0f;
		boolean bounceUp = true;
		float bounceHeight = 0.0f;
		ArrayList <SceneNode> bouncing = new ArrayList<SceneNode>();
	
	
		
		
	public BounceController(){
		
	}
	
	
	public void update(float gTime) {
		bounceTime = Math.round(gTime/50.0f);
		
		
		if(bounceUp == true) {
			//bounce up		
			if( bounceHeight + 0.05f <= 1.0f) {
				bounceHeight += 0.05f;
				bounceUp = true;
				//move nodes up
				for(SceneNode n : bouncing) {
					
					n.moveUp(0.05f);
				}
			}
			else {
				bounceUp = false;
			}
			
			
		}
		else {
			//bounce down
			if( bounceHeight - 0.05f >= 0.0f) {
				bounceHeight -= 0.05f;
				bounceUp = false;
				//move nodes up
				for(SceneNode n : bouncing) {
					
					n.moveUp(-0.05f);
				}
			}
			else {
				bounceUp = true;
			}
			
		}
		
		
	}
	
	
	
	
	@Override
	protected void updateImpl(float gTime) {
		bounceTime = Math.round(gTime/50.0f);
		
		if(bounceUp == true) {
			//bounce up		
			if( bounceHeight + 0.05f <= 1.0f) {
				bounceHeight += 0.05f;
				bounceUp = true;
				//move nodes up
				for(SceneNode n : bouncing) {
					
					n.moveUp(0.05f);
				}
			}
			else {
				bounceUp = false;
			}
			
			
		}
		else {
			//bounce down
			if( bounceHeight - 0.05f >= 0.0f) {
				bounceHeight -= 0.05f;
				bounceUp = false;
				//move nodes up
				for(SceneNode n : bouncing) {
					
					n.moveUp(0.05f);
				}
			}
			else {
				bounceUp = true;
			}
			
		}

	}
	
	
	public void addNode(SceneNode N) {
		bouncing.add(N);
		System.out.println("added a node to bounce");
	}
	
	
	

}
