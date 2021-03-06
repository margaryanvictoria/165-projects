package myGameEngine;

import ray.input.InputManager;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class Camera3Pcontroller {
	private Camera camera;//the camera being controlled  
	private SceneNode cameraN;//the node the camera is attached to  
	private SceneNode target;//the target the camera looks at  
	private float cameraAzimuth;//rotation of camera around Y axis  
	private float cameraElevation;//elevation of camera above target  
	private float radias;//distance between camera and target  
	private Vector3 targetPos;//target�s position in the world  
	private Vector3 worldUpVec;
	
	
	public Camera3Pcontroller(Camera cam, SceneNode camN,SceneNode targ, String controllerName, InputManager im)  { 
		camera = cam;    
		cameraN = camN;    
		target = targ;    
		cameraAzimuth = 225.0f;// start from BEHIND and ABOVE the target    
		cameraElevation = 20.0f;// elevation is in degrees    
		radias = 2.0f;    
		worldUpVec = Vector3f.createFrom(0.0f, 1.0f, 0.0f);    
		setupInput(im, controllerName);    
		updateCameraPosition();
	}
	
	
	public void updateCameraPosition()   { 
		double theta = Math.toRadians(cameraAzimuth);// rot around target    
		double phi = Math.toRadians(cameraElevation);// altitude angle    
		double x = radias * Math.cos(phi) * Math.sin(theta);    
		double y = radias * Math.sin(phi);    
		double z = radias * Math.cos(phi) * Math.cos(theta);    
		cameraN.setLocalPosition(Vector3f.createFrom ((float)x, (float)y, (float)z).add(target.getWorldPosition()));    
		cameraN.lookAt(target, worldUpVec);     
	}
	
	 private void setupInput(InputManager im, String cn)  { 
		 Action orbitAAction = new OrbitAroundAction();
		 Action orbitEAction = new OrbitElevationAction();
		 Action orbitRAction = new OrbitRadiasAction();
		 
		 im.associateAction(cn, net.java.games.input.Component.Identifier.Axis.RX,  orbitAAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		 //  similar input set up for OrbitRadiasAction, OrbitElevationAction
		 im.associateAction(cn, net.java.games.input.Component.Identifier.Axis.RY,  orbitEAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		 
		 im.associateAction(cn, net.java.games.input.Component.Identifier.Button._0,  orbitRAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		 im.associateAction(cn, net.java.games.input.Component.Identifier.Button._1,  orbitRAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	 }
	
	 
	 private class OrbitAroundAction extends AbstractInputAction  
	 { 
		 // Moves the camera around the target (changes camera azimuth).    
		 public void performAction(float time, net.java.games.input.Event evt)    
		 {
			 float rotAmount;      
			 if (evt.getValue() < -0.2)
			 { 
				 rotAmount=-0.4f * evt.getValue(); 
			}          
			 else          
			 { 
				 if (evt.getValue() > 0.2)    
				 { 
					 rotAmount= -0.4f * evt.getValue(); 
				 }          
				 else          
				 { 
					 rotAmount=0.0f ; 
				}      
			}       
			 cameraAzimuth += rotAmount;      
			 cameraAzimuth = cameraAzimuth % 360;  
			 updateCameraPosition();  
		}  //  similar for OrbitRadiasAction, OrbitElevationAction} 
		  
		 
	 }
	 
	 private class OrbitElevationAction extends AbstractInputAction  
	 { 
		 // Moves the camera around the target (changes camera azimuth).    
		 public void performAction(float time, net.java.games.input.Event evt)    
		 {
			 float rotAmount = 0.0f;
			 
			 if(cameraElevation >= 1.0f && cameraElevation <= 30.0f) {
				 if (evt.getValue() < -0.2)
				 { 
					 rotAmount=-0.4f; 
				}          
				 else          
				 { 
					 if (evt.getValue() > 0.2)    
					 { 
						 rotAmount=0.4f; 
					 }          
					 else          
					 { 
						 rotAmount=0.0f; 
					}      
				}

			 }
			 
			 if( (cameraElevation + rotAmount) >= 1.0f && (cameraElevation + rotAmount) <= 30.0f ) {
				 cameraElevation += rotAmount; 
			 }
			 
			 updateCameraPosition();  
		}
		  
		 
	 }

	 private class OrbitRadiasAction extends AbstractInputAction  
	 { 
		 // Moves the camera around the target (changes camera azimuth).    
		 public void performAction(float time, net.java.games.input.Event evt)    
		 {
			 float zoomAmount = 0.0f;
			 if(radias >= 1.0f && radias <= 3.0f) {
				 if (evt.getComponent().toString().compareTo("Button 0") == 0)
				 { 
					  zoomAmount = -0.1f;
				}          
				 else          
				 { 
					 if (evt.getComponent().toString().compareTo("Button 1") ==0)    
					 { 
						 zoomAmount = 0.1f;
					 }          
					 else          
					 { 
						 zoomAmount=0.0f; 
					}      
				}

			 }
			 
			 if( (radias + zoomAmount) >= 1.0f && (radias + zoomAmount) <= 3.0f ) {
				 radias += zoomAmount; 
			 }
			 
			 updateCameraPosition();  
		}
		  
		 
	 }
	 
	 
	 
	 
	 
}
