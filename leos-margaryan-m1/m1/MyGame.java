package m1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;

import myGameEngine.BounceController;
import myGameEngine.Camera3PKB;
import myGameEngine.Camera3Pcontroller;
import myGameEngine.MoveForwardAction;
import myGameEngine.RotateAction;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Version;
import net.java.games.input.Component;


import ray.rage.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.states.*;
import ray.rage.asset.material.Material;
import ray.rage.asset.texture.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import ray.rage.util.BufferUtil;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.input.*;
import ray.input.action.*;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

import java.awt.geom.AffineTransform; //For skybox
import ray.rage.util.Configuration; //for configuration
//import ray.rage.rendersystem.states.TextureState.WrapMode;



public class MyGame extends VariableFrameRateGame {
	private Camera3Pcontroller orbitController2;
	private Camera3PKB orbitController1;
	private Action moveFwdActD1;// other action classes as needed
	private Action rotateActionD1;
	
	//camera nodes
	SceneNode cameraN;
	
	// to minimize variable allocation in update()
	GL4RenderSystem rs;
	float elapsTime = 0.0f;
	String elapsTimeStr, counterStr, dispStr;
	int elapsTimeSec, counter = 0;
	
	
	//add extra classes from myGameEngine
	private InputManager im;
	

    public MyGame() {
        super();
    }

    public static void main(String[] args) {
        Game game = new MyGame();
        try {
			game.startup();
			System.out.println("\n");
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }
	
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);

		rs.getRenderWindow().setTitle("Milestone 1 - Michael Leos & Victoria Margaryan");
	}

    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
    	SceneNode rootNode = sm.getRootSceneNode();
    	RenderSystem rs = sm.getRenderSystem();
    	//set up cam 1
    	Camera camera = sm.createCamera("MainCamera1",Projection.PERSPECTIVE);    
    	rw.getViewport(0).setCamera(camera);    
    	cameraN = rootNode.createChildSceneNode("MainCamera1Node");     
    	cameraN.attachObject(camera);    
    	camera.setMode('n');    
    	camera.getFrustum().setFarClipDistance(1000.0f);
        
    }
	
    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {
		im = new GenericInputManager();
		
    	//set up player 1 dolphin
    	Entity dolphin1E = sm.createEntity("dolphin1", "dolphinHighPoly.obj");    
    	dolphin1E.setPrimitive(Primitive.TRIANGLES);    
    	SceneNode dolphin1N = sm.getRootSceneNode().createChildSceneNode("dolphin1Node");
    	dolphin1N.moveUp(1.0f);
    	dolphin1N.attachObject(dolphin1E);    // earth avatar for player in the bottom window
    	
    	
    	//set up player 2 dolphin
    	Entity dolphin2E = sm.createEntity("dolphin2", "dolphinHighPoly.obj");    
    	dolphin2E.setPrimitive(Primitive.TRIANGLES);    
    	SceneNode dolphin2N = sm.getRootSceneNode().createChildSceneNode("dolphin2Node");
    	dolphin2N.attachObject(dolphin2E);    
    	dolphin2N.setLocalPosition(-1.0f, 1.0f, 0.0f);   
    	
    	//change dolphin 2 to red
    	Material matX = sm.getMaterialManager().getAssetByPath("defaultRed.mtl");
    	matX.setEmissive(Color.red);
    	dolphin2E.setMaterial(matX);
		
		//Terrain manipulation
		Tessellation tessE = sm.createTessellation("tessE", 7);
		tessE.setSubdivisions(0f); //set this to 0 because was seeing cracks in the map
		SceneNode tessN = sm.getRootSceneNode().createChildSceneNode("TessN");
		tessN.attachObject(tessE);
		tessN.scale(50, 300, 50);
		tessE.setHeightMap(this.getEngine(), "ice_neg.jpg");
		tessE.setTexture(this.getEngine(), "green.jpeg");
		tessE.setNormalMap(this.getEngine(), "ice_normal.jpg");
		//set tiling & texture state wrap to REPEAT?
    	
        
        //add ambient white light to scene
        sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));
		
		Light plight = sm.createLight("testLamp1", Light.Type.POINT);
		plight.setAmbient(new Color(.3f, .3f, .3f));
        plight.setDiffuse(new Color(.7f, .7f, .7f));
		plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(15f);
		
		SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);


		// Cube map -------
        Configuration conf = eng.getConfiguration();
        conf.load("assets/config/myGame.properties");
        eng.getTextureManager().setBaseDirectoryPath(conf.valueOf("assets.skyboxes.path")); //change to skybox texture directory
        Texture front = eng.getTextureManager().getAssetByPath("front.jpg"); //zn - had to flip these
        Texture back = eng.getTextureManager().getAssetByPath("back.jpg"); //zp - had to flip these
        Texture left = eng.getTextureManager().getAssetByPath("left.jpg"); //xn
        Texture right = eng.getTextureManager().getAssetByPath("right.jpg"); //xp
        Texture top = eng.getTextureManager().getAssetByPath("top.jpg"); //yp
        Texture bottom = eng.getTextureManager().getAssetByPath("bottom.jpg"); //yn
        eng.getTextureManager().setBaseDirectoryPath(conf.valueOf("assets.textures.path")); //change back to normal texture directory
        // cubemap textures must be flipped up-side-down to face inward;
        // all textures must have the same dimensions,
        // so any image’s height will do
        AffineTransform xform = new AffineTransform();
        xform.translate(0, front.getImage().getHeight());
        xform.scale(1d, -1d);
        front.transform(xform);
        back.transform(xform);
        left.transform(xform);
        right.transform(xform);
        top.transform(xform);
        bottom.transform(xform);
        SkyBox sb = sm.createSkyBox("mySkyBox");
        sb.setTexture(front, SkyBox.Face.FRONT);
        sb.setTexture(back, SkyBox.Face.BACK);
        sb.setTexture(left, SkyBox.Face.LEFT);
        sb.setTexture(right, SkyBox.Face.RIGHT);
        sb.setTexture(top, SkyBox.Face.TOP);
        sb.setTexture(bottom, SkyBox.Face.BOTTOM);  
		sm.setActiveSkyBox(sb);
		
        setupInputs(sm);
        setupOrbitCamera(eng, sm);
        
    }

    @Override
    protected void update(Engine engine) {
		// build and set HUD
    	rs = (GL4RenderSystem) engine.getRenderSystem();    
    	elapsTime += engine.getElapsedTimeMillis();    
    	elapsTimeSec = Math.round(elapsTime/1000.0f);    
    	elapsTimeStr = Integer.toString(elapsTimeSec);    
    	dispStr = "Game Time = " + elapsTimeStr + " Score = --";    
    	
    	rs.setHUD(dispStr, 20, 15);
    	
		// tell the input manager to process the inputs    
    	im.update(elapsTime);
    	//set up cam 1 for keyboard
    	orbitController1.updateCameraPosition();
		
	}
    
    protected void setupInputs(SceneManager sm) throws IOException{
    	
    	im = new GenericInputManager();
    	
    	SceneNode dolphin1N = getEngine().getSceneManager().getSceneNode("dolphin1Node");
    	//grab names of keyboard and game pad
    	ArrayList<Controller> controllers = im.getControllers();
    	
    	
    	moveFwdActD1 = new MoveForwardAction(dolphin1N, this);
    	
    	rotateActionD1 = new RotateAction(dolphin1N);
    	
    	
    	//Attach controls to all keyboards and Gamepads
    	for(Controller c : controllers) {
    		if(c.getType() == Controller.Type.KEYBOARD) {
    			System.out.println(c.getName());
    			//movement
    			   
    			im.associateAction(c,net.java.games.input.Component.Identifier.Key.W,moveFwdActD1, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c,net.java.games.input.Component.Identifier.Key.S,moveFwdActD1, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); 
    			
    			
    			im.associateAction(c,net.java.games.input.Component.Identifier.Key.D,rotateActionD1, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c,net.java.games.input.Component.Identifier.Key.A,rotateActionD1, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			
    	    	
    			
    		}
    		
    		else if(c.getType() == Controller.Type.GAMEPAD) {
    			System.out.println(c.getName());
    			
    			
    			im.associateAction(c,net.java.games.input.Component.Identifier.Axis.Y,moveFwdActD1, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c,net.java.games.input.Component.Identifier.Axis.X,rotateActionD1, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			
    			
    		}
    		
    	}
    	
    	
    }
    
	public void updateVerticalPosition() {
		SceneNode dolphin1N = this.getEngine().getSceneManager().getSceneNode("dolphin1Node");
		SceneNode tessN = this.getEngine().getSceneManager().getSceneNode("TessN");
		Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));
		
		// Figure out Avatar's position relative to plane
		Vector3 worldAvatarPosition = dolphin1N.getWorldPosition();
		Vector3 localAvatarPosition = dolphin1N.getLocalPosition();

		// use avatar World coordinates to get coordinates for height
		Vector3 newAvatarPosition = Vector3f.createFrom(
			// Keep the X coordinate
			localAvatarPosition.x(),
			// The Y coordinate is the varying height
			tessE.getWorldHeight(worldAvatarPosition.x(), worldAvatarPosition.z()) +.5f,
			//Keep the Z coordinate
			localAvatarPosition.z()
		);
		// use avatar Local coordinates to set position, including height
		dolphin1N.setLocalPosition(newAvatarPosition);
	}
    
    protected void setupOrbitCamera(Engine eng, SceneManager sm)  {
    	
    	String gpName = im.getFirstGamepadName();
    	
    	SceneNode dolphin1N = sm.getSceneNode("dolphin1Node");    
    	SceneNode camera1N = sm.getSceneNode("MainCamera1Node");    
    	Camera camera1 = sm.getCamera("MainCamera1");    
    	//String kbName = im.getKeyboardName();
    	orbitController1 = new Camera3PKB(camera1, camera1N, dolphin1N, im);
    	
    	if(gpName != null) {
    		/* SceneNode dolphin2N = sm.getSceneNode("dolphin2Node");    
        	SceneNode camera2N = sm.getSceneNode("MainCamera2Node");    
        	Camera camera2 = sm.getCamera("MainCamera2");        
        	orbitController2 = new Camera3Pcontroller(camera2, camera2N, dolphin2N, gpName, im); */
    	}
	
    } 
    
}
