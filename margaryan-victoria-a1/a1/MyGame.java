package a1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import ray.rage.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;

// Import my engine extension files
import myGameEngine.*;
// These are to use a gamepad with rage
import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.input.*;
import ray.input.action.*;

//associate user actions with ALL keyboard controllers
import java.util.ArrayList;
import net.java.games.input.Controller;

import ray.rage.rendersystem.shader.*; //GpuShaderProgram was having issues so manually imported
import ray.rage.util.*; //BufferUtil having issues so manually imported
import java.nio.*; //FloatBuffer & IntBuffer were having issues so I imported this from csc-155
import ray.rage.asset.material.*; //So we can use Materials
import java.awt.geom.AffineTransform; //For skybox
import net.java.games.input.Rumbler; //for Rumbler
//import ray.rml.MathUtil; //for calculating distance from dolphin


public class MyGame extends VariableFrameRateGame {

	// to minimize variable allocation in update()
	GL4RenderSystem rs;
	float elapsTime = 0.0f;
	String elapsTimeStr, counterStr, dispStr;
	int elapsTimeSec, counter = 0;

    Controller gamepad = null;
    boolean rumbleFlag = false;
    //GamepadController gpCtrlr = new GamepadController(this, camera);

    Camera camera;
    SceneNode rootNode;
    SceneNode cameraNode;
    SceneNode dolphinN;
    boolean explosion = false,
            explosionPhase1 = false,
            explosionPhase2 = false,
            explosionPhase3 = false,
            explosionPhase4 = false,
            explosionPhase5 = false,
            explosionPhase6 = false,
            explosionPhase7 = false,
            explosionPhase8 = false,
            explosionPhase9 = false,
            explosionPhase10 = false;
    int explosionStartTime;
    SceneNode explodingPlanet;
    // To get controller inputs ------------
    private InputManager im;
    private Action  quitGameAction,
                    incrementCounterAction,
                    incAmtModAct,
                    moveForwardAction,
                    moveBackwardAction,
                    moveLeftAction,
                    moveRightAction,
                    yawLeft,
                    yawRight,
                    pitchDown,
                    pitchUp,
                    interactDolphin,
                    moveFBAction,
                    moveLRAction,
                    pitchUDAction,
                    yawLRAction;
    //private boolean showAxes = false;
    private ArrayList<SceneNode> newPlanets = new ArrayList<SceneNode>();
    private ArrayList<SceneNode> visitedPlanets = new ArrayList<SceneNode>();
    private int visitedPlanetsSize = 0;
    String visitedPlanetsString = "Visited: ";

    // Constructor, called in main(), will find components, print controls, then start game
    // ------------------------------------------------------------------------------------
    public MyGame() {
        super();

        try {
            FindComponents f = new FindComponents();
            f.listControllers();
        } catch (Exception e) {
            System.out.println("No controllers found!!!");
        }
        System.out.println("\n--------------------------------------------------");
		/* System.out.println("press T to render triangles");
		System.out.println("press L to render lines");
		System.out.println("press P to render points");
		System.out.println("press C to increment counter"); */
        System.out.println("press C or Button 3 (X) to increment counter");
        System.out.println("press V or Button 4 (Y) to change increment amount");
        System.out.println("press ESC or Button 2 (B) to quit");
        // Controls!
        System.out.println("press W or Left Joystick to move forward");
        System.out.println("press S or Left Joystick to move backward");
        System.out.println("press A or Left Joystick to move left");
        System.out.println("press D or Left Joystick to move right");
        System.out.println("press LEFT or Right Joystick to yaw left");
        System.out.println("press RIGHT or Right Joystick to yaw right");
        System.out.println("press UP or Right Joystick to pitch up");
        System.out.println("press DOWN or Right Joystick to pitch down");
        System.out.println("press SPACE or Button 1 (A) to hop on a nearby dolphin");
        System.out.println("--------------------------------------------------");
    }

    // Create our game ------------------------------------------------------------------
    public static void main(String[] args) {
        Game game = new MyGame();
        try {
            game.startup();
            System.out.println("\n"); //I like my spacing, clearly.........
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }
	
    // Setting up our game window -------------------------------------------------------
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
        //Found how to set the Window Title in ray/rage/rendersystem/RenderWindow.html
        rs.getRenderWindow().setTitle("Assignment 1 - Victoria Margaryan");
	}

    // Setting up our camera --------------------------------------------------------------
    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        rootNode = sm.getRootSceneNode();
        camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE); //made this a global var
        rw.getViewport(0).setCamera(camera);
		
		camera.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
		camera.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
		camera.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));
		
		camera.setPo((Vector3f)Vector3f.createFrom(-0.5f, 0f, 6.0f));
        //camera.setPo((Vector3f)Vector3f.createFrom(0f, 0f, 0f));
        
        cameraNode = rootNode.createChildSceneNode(camera.getName() + "Node");
        cameraNode.attachObject(camera);
        camera.setMode('c');
    }
	
    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {
        setupInputs();

        /* Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES);

        SceneNode dolphinN = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
        dolphinN.moveBackward(2.0f);
        dolphinN.attachObject(dolphinE);

        sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));
		
		Light plight = sm.createLight("testLamp1", Light.Type.POINT);
		plight.setAmbient(new Color(.3f, .3f, .3f));
        plight.setDiffuse(new Color(.7f, .7f, .7f));
		plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(5f);
		
		SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);

        RotationController rc = new RotationController(Vector3f.createUnitVectorY(), .02f);
        rc.addNode(dolphinN);
        sm.addController(rc); */

        // Need these to add textures
        TextureManager tm = eng.getTextureManager();
        RenderSystem rs = sm.getRenderSystem();
        TextureState state = (TextureState) rs.createRenderState(RenderState.Type.TEXTURE);

        // Dolphin --
        Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES); //set it to show the triangle faces, not just lines or points
        dolphinN = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
        //dolphinN.moveBackward(2.0f); //offset the node's position by 2
        dolphinN.attachObject(dolphinE); //add our dolphin obj to our dolphin node
        Angle degrees180 = Degreef.createFrom(180.0f);
        dolphinN.yaw(degrees180); //rotate the dolphin 180 degrees on x axis
        dolphinN.setLocalPosition(0.0f, 0.0f, 4.0f);

        // Manually adding a texture to our dolphin
        Texture redTexture = tm.getAssetByPath("redDolphin.jpg"); //get the red texture we made
        state.setTexture(redTexture); //set it to a texture state
        dolphinE.setRenderState(state); //set that texture state to our dolphin

        // make manual objects – in this case a pyramid
        ManualObject pyr = makePyramid(eng, sm); //using our makePyramid method we imported
        SceneNode pyrN = sm.getRootSceneNode().createChildSceneNode("PyrNode"); //add it to the scene
        pyrN.scale(0.7f, 0.9f, 0.7f); //make the node smaller (and whatever is in it)
        pyrN.moveBackward(2.0f); //i put this here to shift it backward a bit (cause the backfaces are not coded)
        pyrN.attachObject(pyr); //attach our pyramid to our node

        Texture iceTex = eng.getTextureManager().getAssetByPath("ice.jpg");
        TextureState iceTextureState = (TextureState) rs.createRenderState(RenderState.Type.TEXTURE);
        iceTextureState.setTexture(iceTex);
        pyr.setRenderState(iceTextureState);

        
        // Earth 1 --
        Entity earthE1 = sm.createEntity("myEarth1", "earth.obj");
        earthE1.setPrimitive(Primitive.TRIANGLES);
        SceneNode earthN1 = sm.getRootSceneNode().createChildSceneNode(earthE1.getName() + "Node");
        earthN1.attachObject(earthE1); //attaching our earth obj to our earth node
        earthN1.setLocalPosition(-2.0f, 2.0f, -4.0f); //offset the earth's position from the node's origin
        earthN1.setLocalScale(0.2f, 0.2f, 0.2f); //make the earth really small
        newPlanets.add(earthN1);

        Texture greenTexture = tm.getAssetByPath("green.jpeg"); //get the ice texture we imported
        TextureState greenTextureState = (TextureState) rs.createRenderState(RenderState.Type.TEXTURE);
        greenTextureState.setTexture(greenTexture); //set it to a texture state
        earthE1.setRenderState(greenTextureState); //set that texture state to our earth

        // Earth 2 --
        Entity earthE2 = sm.createEntity("myEarth2", "earth.obj");
        earthE2.setPrimitive(Primitive.TRIANGLES);
        SceneNode earthN2 = sm.getRootSceneNode().createChildSceneNode(earthE2.getName() + "Node");
        earthN2.attachObject(earthE2); //attaching our earth obj to our earth node
        earthN2.setLocalPosition(-10.0f, 0.0f, -4.0f); //offset the earth's position from the node's origin
        earthN2.setLocalScale(0.2f, 0.2f, 0.2f); //make the earth really small
        newPlanets.add(earthN2);

        // Earth 3 --
        Entity earthE3 = sm.createEntity("myEarth3", "earth.obj");
        earthE3.setPrimitive(Primitive.TRIANGLES);
        SceneNode earthN3 = sm.getRootSceneNode().createChildSceneNode(earthE3.getName() + "Node");
        earthN3.attachObject(earthE3); //attaching our earth obj to our earth node
        earthN3.setLocalPosition(-2.0f, 1.0f, -8.0f); //offset the earth's position from the node's origin
        earthN3.setLocalScale(0.2f, 0.2f, 0.2f); //make the earth really small
        newPlanets.add(earthN3);

        // Earth 4 --
        Entity earthE4 = sm.createEntity("myEarth4", "earth.obj");
        earthE4.setPrimitive(Primitive.TRIANGLES);
        SceneNode earthN4 = sm.getRootSceneNode().createChildSceneNode(earthE4.getName() + "Node");
        earthN4.attachObject(earthE4); //attaching our earth obj to our earth node
        earthN4.setLocalPosition(4.0f, 1.5f, -10.0f); //offset the earth's position from the node's origin
        earthN4.setLocalScale(0.2f, 0.2f, 0.2f); //make the earth really small
        newPlanets.add(earthN4);

        // create a rotation controller and rotate by .02 each frame
        RotationController rc = new RotationController(Vector3f.createUnitVectorY(), .02f); //speed it rotates
        rc.addNode(earthN1); //rotate our earth node
        rc.addNode(earthN2);
        rc.addNode(earthN3);
        rc.addNode(pyrN); //rotate our pyramid node

        sm.addController(rc); //add our rotation controller to the scene so it will update each frame Update

        // Colored Axes
        ManualObject lineX = makeLineX(eng, sm);
        lineX.setPrimitive(Primitive.LINES);
        ManualObject lineY = makeLineY(eng, sm);
        lineY.setPrimitive(Primitive.LINES);
        ManualObject lineZ = makeLineZ(eng, sm);
        lineZ.setPrimitive(Primitive.LINES);
        SceneNode axesN = sm.getRootSceneNode().createChildSceneNode("Axes Node");
        axesN.scale(5f, 5f, 5f); //make really big axes
        axesN.attachObject(lineX);
        axesN.attachObject(lineY);
        axesN.attachObject(lineZ);

        // set up lights
        sm.getAmbientLight().setIntensity(new Color(.3f, .3f, .3f)); //ambient
        Light plight = sm.createLight("testLamp1", Light.Type.POINT); //point light
        plight.setAmbient(new Color(.1f, .1f, .1f));
        plight.setDiffuse(new Color(0.8f, 0.8f, 0.8f));
        plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(20f);

        SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode"); //make light node
        plightNode.attachObject(plight); //add our light obj to our light node
        plightNode.setLocalPosition(1.0f, 1.0f, 5.0f); //offset the light from the light node's origin
        
        //System.out.println(newPlanets.toString());
        //visitedPlanetsString += " " + rs.getRenderWindow().getHeight();
        //System.out.println(rs.getRenderWindow().getLocationTop());

        // Cube map --
        Configuration conf = eng.getConfiguration();
        conf.load("assets/config/myGame.properties");
        //TextureManager textureMgr = getEngine().getTextureManager();

        // Had to manually go into the asset folder and change this conf value/use new properties file
        // File: assets\config\rage.properties
        // Line 18: "assets.skyboxes.path=assets/skyboxes/oga/galaxy/red/dense/"
        // Change it to just be "assets.skyboxes.path=assets/skyboxes/" in the myGame.properties file
        tm.setBaseDirectoryPath(conf.valueOf("assets.skyboxes.path")); //change to skybox texture directory
        Texture front = tm.getAssetByPath("front.jpeg"); //zn - had to flip these
        Texture back = tm.getAssetByPath("back.jpeg"); //zp - had to flip these
        Texture left = tm.getAssetByPath("left.jpeg"); //xn
        Texture right = tm.getAssetByPath("right.jpeg"); //xp
        Texture top = tm.getAssetByPath("top.jpeg"); //yp
        Texture bottom = tm.getAssetByPath("bottom.jpeg"); //yn
        tm.setBaseDirectoryPath(conf.valueOf("assets.textures.path")); //change back to normal texture directory
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
    }

    // Update() will update every frame -------------------------------------------------------
    @Override   
    protected void update(Engine engine) {
		// build and set HUD
		rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		counterStr = Integer.toString(counter);
		dispStr = "Time = " + elapsTimeStr + "   Keyboard hits = " + counterStr
                    + "   Visited = " + visitedPlanetsSize;
		rs.setHUD(dispStr, 15, 15);

        //rs.setHUD2(visitedPlanetsString, 15, (rs.getRenderWindow().getHeight()/4));
        //rs.setHUD2(visitedPlanetsString, 15, (int) ((rs.getRenderWindow().getHeight())*.75));
        checkIfPlanetCollision();

        if(explosion) {
            //SceneNode planet = (getEngine().getSceneManager().getSceneNode(explodingPlanet.getName()));
            explosionAnimation();
        }

        // Tell the input manager to process the inputs
        im.update(elapsTime);
	}

    // Set up our controls ---------------------------------------------------------------------
    protected void setupInputs() {
        im = new GenericInputManager(); //this was a global InputManager var
        ArrayList<Controller> controllers = im.getControllers(); //get all our controllers

        // Keyboard controls -------------
        //String kbName = im.getKeyboardName();

        // build some action objects for doing things in response to user input
        // These are global Action vars, we pass in the game object
        quitGameAction = new QuitGameAction(this);
        incAmtModAct = new IncrementAmountModifierAction(this);
        incrementCounterAction = new IncrementCounterAction(this, (IncrementAmountModifierAction) incAmtModAct);
        moveForwardAction = new MoveForwardAction(this, camera);
        moveBackwardAction = new MoveBackwardAction(this, camera);
        moveLeftAction = new MoveLeftAction(this, camera);
        moveRightAction = new MoveRightAction(this, camera);
        yawLeft = new YawLeft(this, camera);
        yawRight = new YawRight(this, camera);
        pitchDown = new PitchDown(this, camera);
        pitchUp = new PitchUp(this, camera);
        interactDolphin = new InteractDolphin(this, camera);
        moveFBAction = new MoveFBAction(this, camera);
        moveLRAction = new MoveLRAction(this, camera);
        pitchUDAction = new PitchUDAction(this, camera);
        yawLRAction = new YawLRAction(this, camera);

        // attach the action objects to keyboard controllers
        for (Controller c : controllers) {
            if (c.getType() == Controller.Type.KEYBOARD) {
                //ESC = quit game
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.ESCAPE,
                    quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
                // C = increment counter
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.C,
                    incrementCounterAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
                // V = change increment counter
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.V,
                    incAmtModAct, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
                // W = move forward
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.W,
                    moveForwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // S = move backward
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.S,
                    moveBackwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // A = move left
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.A,
                    moveLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // D = move right
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.D,
                    moveRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // Left Arrow = yaw left
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.LEFT,
                    yawLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // Right Arrow = yaw right
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.RIGHT,
                    yawRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // Up Arrow = pitch up
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.UP,
                    pitchUp, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // Down Arrow = pitch down
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.DOWN,
                    pitchDown, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // Space bar = interact with a dolphin if nearby
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.SPACE,
                    interactDolphin, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            } else if (c.getType() == Controller.Type.GAMEPAD) {
                gamepad = c;
                //Remember that inputComponent starts at index 0
                // Button 2 (B) = quit game
                im.associateAction(c, net.java.games.input.Component.Identifier.Button._1, 
                    quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
                // Button 3 (X) = increment counter
                im.associateAction(c, net.java.games.input.Component.Identifier.Button._2,
                    incrementCounterAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
                // Button 4 (Y) = change increment counter
                im.associateAction(c, net.java.games.input.Component.Identifier.Button._3,
                    incAmtModAct, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
                // left joystick (W & S) = move forward & backward
                im.associateAction(c, net.java.games.input.Component.Identifier.Axis.Y,
                    moveFBAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // left joystick (A & D) = move left & right
                im.associateAction(c, net.java.games.input.Component.Identifier.Axis.X,
                    moveLRAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // right joystick (UP & DOWN) = pitch up & down
                im.associateAction(c, net.java.games.input.Component.Identifier.Axis.RY,
                    pitchUDAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // right joystick (LEFT & RIGHT) = yaw left & right
                im.associateAction(c, net.java.games.input.Component.Identifier.Axis.RX,
                    yawLRAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // Button 1 (A) = interact with dolphin if nearby
                im.associateAction(c, net.java.games.input.Component.Identifier.Button._0,
                    interactDolphin, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            }
        }
        
        /* // Gamepad controls ----------
        if (im.getFirstGamepadName() != null) {
            String gpName = im.getFirstGamepadName();
            // Button 9 = quit game
            im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._9, 
                quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            // Button 3 = increment counter
            im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._3,
                incrementCounterAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
        } else {
            System.out.println("No controller found . . .");
        } */

    }

    // For immediate control results, use this for testing more than actual mapping pls -----
    @Override
    public void keyPressed(KeyEvent e) {
        //Entity dolphin = getEngine().getSceneManager().getEntity("myDolphin");
        //System.out.println("User pressed: " + e.getKeyChar());
        /* switch (e.getKeyCode()) {
            case KeyEvent.VK_L:
                dolphin.setPrimitive(Primitive.LINES);
                break;
            case KeyEvent.VK_T:
                dolphin.setPrimitive(Primitive.TRIANGLES);
                break;
            case KeyEvent.VK_P:
                dolphin.setPrimitive(Primitive.POINTS);
                break;
			case KeyEvent.VK_C:
				counter++;
				break;
            case KeyEvent.VK_N:
				showAxes = true;
				break;
        } */
        super.keyPressed(e);
    }

    // Object creation methods --------------------------------------------------------
    public ManualObject makePyramid(Engine eng, SceneManager sm) throws IOException {
        ManualObject pyr = sm.createManualObject("Pyramid");
        ManualObjectSection pyrSec = pyr.createManualSection("PyramidSection");
        pyr.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
        float[] vertices = new float[]
            {   -1.0f, 0.0f, 1.0f,      1.0f, 0.0f, 1.0f,       0.0f, 1.0f, 0.0f, //front
                1.0f, 0.0f, 1.0f,       1.0f, 0.0f, -1.0f,      0.0f, 1.0f, 0.0f, //right
                1.0f, 0.0f, -1.0f,     -1.0f, 0.0f, -1.0f,      0.0f, 1.0f, 0.0f, //back
                -1.0f, 0.0f, -1.0f,    -1.0f, 0.0f, 1.0f,       0.0f, 1.0f, 0.0f, //left
            //-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
            //1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f //RR
                1.0f, 0.0f, 1.0f,       -1.0f, 0.0f, 1.0f,      0.0f, -1.0f, 0.0f, //front bottom
                1.0f, 0.0f, -1.0f,      1.0f, 0.0f, 1.0f,       0.0f, -1.0f, 0.0f, //right bottom
                -1.0f, 0.0f, -1.0f,      1.0f, 0.0f, -1.0f,     0.0f, -1.0f, 0.0f, //back bottom
                -1.0f, 0.0f, 1.0f,       -1.0f, 0.0f, -1.0f,    0.0f, -1.0f, 0.0f, //left bottom
            };
        float[] texcoords = new float[]
            { /* 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            //0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
            //1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
            1.0f, 0.0f, 0.0f, 0.0f, 0.5f, 1.0f,
            1.0f, 0.0f, 0.0f, 0.0f, 0.5f, 1.0f,
            1.0f, 0.0f, 0.0f, 0.0f, 0.5f, 1.0f,
            1.0f, 0.0f, 0.0f, 0.0f, 0.5f, 1.0f */
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f
            };
        float[] normals = new float[]
            {
            0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, //front
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, //right
            0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, //back
            -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, //left
            0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, //front bottom
            1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, //right bottom
            0.0f, -1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, -1.0f, -1.0f, //back bottom
            -1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f //left bottom
            /* 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
            0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f,
            //0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
            //0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f
            0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
            0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f */
            };

        int[] indices = new int[] { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23 };
        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
        FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
        pyrSec.setVertexBuffer(vertBuf);
        pyrSec.setTextureCoordsBuffer(texBuf);
        pyrSec.setNormalsBuffer(normBuf);
        pyrSec.setIndexBuffer(indexBuf);
        //Texture tex = eng.getTextureManager().getAssetByPath("chain-fence.jpeg");
        //TextureState texState = (TextureState)sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        //texState.setTexture(tex);
        //pyr.setRenderState(texState);
        Material matDefault = sm.getMaterialManager().getAssetByPath("default.mtl");
        pyrSec.setMaterial(matDefault);
        FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
        pyr.setDataSource(DataSource.INDEX_BUFFER);
        pyr.setRenderState(faceState);

        RenderSystem rs = sm.getRenderSystem();
        ZBufferState zstate = (ZBufferState) rs.createRenderState(RenderState.Type.ZBUFFER);
        zstate.setTestEnabled(true);
        pyr.setRenderState(zstate);

        return pyr;
    }

    public ManualObject makeLineX(Engine eng, SceneManager sm) throws IOException {
        ManualObject line = sm.createManualObject("Line");
        ManualObjectSection linSec = line.createManualSection("LineSection");
        line.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float[] vertices1 = new float[]
            { 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f };
        float[] texcoords1 = new float[]
            { 0.0f, 0.0f, 1.0f, 1.0f };
        int[] indices1 = new int[] { 0, 1 };

        FloatBuffer vertBuf1 = BufferUtil.directFloatBuffer(vertices1);
        FloatBuffer texBuf1 = BufferUtil.directFloatBuffer(texcoords1);
        IntBuffer indexBuf1 = BufferUtil.directIntBuffer(indices1);
        linSec.setVertexBuffer(vertBuf1);
        linSec.setTextureCoordsBuffer(texBuf1);
        linSec.setIndexBuffer(indexBuf1);

        Material matRed = sm.getMaterialManager().getAssetByPath("default1.mtl");
        matRed.setEmissive(Color.RED);
        Texture redLineTexture = eng.getTextureManager().getAssetByPath("red.jpeg");
        TextureState redLineTextureState = (TextureState)sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        redLineTextureState.setTexture(redLineTexture);
        linSec.setRenderState(redLineTextureState);
        linSec.setMaterial(matRed);
        //FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.LINE);
        //lin.setDataSource(DataSource.INDEX_BUFFER);
        //lin.setRenderState(texState);
        //lin.setRenderState(faceState);
        return line;
    }

    public ManualObject makeLineY(Engine eng, SceneManager sm) throws IOException {
        ManualObject lineY = sm.createManualObject("LineY");
        ManualObjectSection linYSec = lineY.createManualSection("LineYSection");
        lineY.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float[] vertices2 = new float[]
            { 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f };
        float[] texcoords2 = new float[]
            { 0.0f, 0.0f, 1.0f, 1.0f };
        int[] indices2 = new int[] { 0, 1 };

        FloatBuffer vertBuf2 = BufferUtil.directFloatBuffer(vertices2);
        FloatBuffer texBuf2 = BufferUtil.directFloatBuffer(texcoords2);
        IntBuffer indexBuf2 = BufferUtil.directIntBuffer(indices2);
        linYSec.setVertexBuffer(vertBuf2);
        linYSec.setTextureCoordsBuffer(texBuf2);
        linYSec.setIndexBuffer(indexBuf2);

        Material matGreen = sm.getMaterialManager().getAssetByPath("default2.mtl");
        matGreen.setEmissive(Color.GREEN);
        Texture greenLineTexture = eng.getTextureManager().getAssetByPath("green.jpeg");
        TextureState greenLineTextureState = (TextureState)sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        greenLineTextureState.setTexture(greenLineTexture);
        linYSec.setRenderState(greenLineTextureState);
        linYSec.setMaterial(matGreen);

        return lineY;
    }

    public ManualObject makeLineZ(Engine eng, SceneManager sm) throws IOException {
        ManualObject lineZ = sm.createManualObject("LineZ");
        ManualObjectSection linZSec = lineZ.createManualSection("LineZSection");
        lineZ.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float[] vertices3 = new float[]
            { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f }; //just need two vertices
        float[] texcoords3 = new float[]
            { 0.0f, 0.0f, 1.0f, 1.0f };  //just need two texture coords
        int[] indices3 = new int[] { 0, 1 };  //only hvae 2 verts

        FloatBuffer vertBuf3 = BufferUtil.directFloatBuffer(vertices3);
        FloatBuffer texBuf3 = BufferUtil.directFloatBuffer(texcoords3);
        IntBuffer indexBuf3 = BufferUtil.directIntBuffer(indices3);
        linZSec.setVertexBuffer(vertBuf3);
        linZSec.setTextureCoordsBuffer(texBuf3);
        linZSec.setIndexBuffer(indexBuf3);

        Material matBlue = sm.getMaterialManager().getAssetByPath("default3.mtl");
        matBlue.setEmissive(Color.BLUE);
        Texture blueLineTexture = eng.getTextureManager().getAssetByPath("blue.jpeg");
        TextureState blueLineTextureState = (TextureState)sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        blueLineTextureState.setTexture(blueLineTexture);
        linZSec.setRenderState(blueLineTextureState);
        linZSec.setMaterial(matBlue);
        
        return lineZ;
    }

    // myGameEngine methods -----------------------------------------------------------
    //should be protected, but you have the stuff in a different package so...
    public void incrementCounter(int amt) {
        counter += amt;
    }

    public void updateCamera(Camera c) {
        camera = c;
    }

    public void sanityNodeCheck(SceneNode rn, SceneNode cn, SceneNode dn) {
        rootNode = rn;
        cameraNode = cn;
        dolphinN = dn;
    }

    public void checkIfPlanetCollision() {
        Vector3f cp = camera.getPo();
        float tempX, tempY, tempZ;
        for (SceneNode sn : newPlanets) {
            tempX = sn.getWorldPosition().x() - cp.x(); //check if any planets are close enough on the x axis
            if (-.5f <= tempX && tempX <= .5f) {
                tempY = sn.getWorldPosition().y() - cp.y();
                tempZ = sn.getWorldPosition().z() - cp.z();                
                if (-.5f <= tempY && tempY <= .5f && -.5f <= tempZ && tempZ <= .5f) {
                    visitedPlanet(sn);
                    break;
                }
            }
        }
    }

    public void visitedPlanet(SceneNode sn) {
        System.out.println("User visited planet node: " + sn.getName());
    
        newPlanets.remove(sn);
        visitedPlanetsString += sn.getName() + ", ";
        visitedPlanets.add(sn);
        visitedPlanetsSize++;

        //initiate rumble???
        //file:///C:/javagaming/jinput/javadoc/net/java/games/input/Rumbler.html
        if(gamepad != null){
            rumbleFlag = true;
        }

        explosion = true;
        explosionPhase1 = true;
        explosionStartTime = elapsTimeSec;
        explodingPlanet = sn;        
    }

    public void explosionAnimation() {
        if(explosionPhase10 && elapsTimeSec == (explosionStartTime+10)) {
            explodingPlanet.scale(0.01f, 0.01f, 0.01f);
            explosion = false;
            explosionPhase10 = false;
        } else if(explosionPhase9 && elapsTimeSec == (explosionStartTime+9)) {
            explodingPlanet.scale(.8f, .8f, .8f);
            if(rumbleFlag) {
                rumbleFlag = false;
            }
            explosionPhase10 = true;
            explosionPhase9 = false;
        } else if(explosionPhase8 && elapsTimeSec == (explosionStartTime+8)) {
            explodingPlanet.scale(.8f, .8f, .8f);
            explosionPhase9 = true;
            explosionPhase8 = false;
        } else if(explosionPhase7 && elapsTimeSec == (explosionStartTime+7)) {
            //System.out.println("expStTi: " + explosionStartTime + " elap: " + elapsTimeSec);
            explodingPlanet.scale(.8f, .8f, .8f);
            explosionPhase8 = true;
            explosionPhase7 = false;
        } else if(explosionPhase6 && elapsTimeSec == (explosionStartTime+6)) {
            //System.out.println("expStTi: " + explosionStartTime + " elap: " + elapsTimeSec);
            explodingPlanet.scale(.8f, .8f, .8f);
            explosionPhase7 = true;
            explosionPhase6 = false;
        } else if(explosionPhase5 && elapsTimeSec == (explosionStartTime+5)) {
            //System.out.println("expStTi: " + explosionStartTime + " elap: " + elapsTimeSec);
            explodingPlanet.scale(.8f, .8f, .8f);
            explosionPhase6 = true;
            explosionPhase5 = false;
        } else if(explosionPhase4 && elapsTimeSec == (explosionStartTime+4)) {
            explodingPlanet.scale(.8f, .8f, .8f);
            explosionPhase5 = true;
            explosionPhase4 = false;
        } else if(explosionPhase3 && elapsTimeSec == (explosionStartTime+3)) {
            //System.out.println("expStTi: " + explosionStartTime + " elap: " + elapsTimeSec);
            explodingPlanet.scale(.8f, .8f, .8f);
            explosionPhase4 = true;
            explosionPhase3 = false;
        } else if(explosionPhase2 && elapsTimeSec == (explosionStartTime+2)) {
            //System.out.println("expStTi: " + explosionStartTime + " elap: " + elapsTimeSec);
            explodingPlanet.scale(.8f, .8f, .8f);
            explosionPhase3 = true;
            explosionPhase2 = false;
        } else if(explosionPhase1 && elapsTimeSec == (explosionStartTime+1)) {
            //System.out.println("expStTi: " + explosionStartTime + " elap: " + elapsTimeSec);
            explodingPlanet.scale(.8f, .8f, .8f);
            if (rumbleFlag) {
                //gpCtrlr.rumble(.25f);
                Rumbler[] arr = gamepad.getRumblers();
                //System.out.println("[" + arr + "]");
                if (arr.length >= 1) {
                    arr[0].rumble(10f);
                }
                System.out.println("Rumblers found: " + arr);
            }
            explosionPhase2 = true;
            explosionPhase1 = false;
        }
    }

    public Vector3f dolphinTetherCheck(Vector3f vec) {
        //if out of bounds, return to dolphin
        /*Vector3f camPos = (Vector3f) camera.getPo();
        Vector3f dolPos = (Vector3f) dolphinN.getWorldPosition();
        float xDif = camPos.x() - dolPos.x();
        float yDif = camPos.y() - dolPos.y();
        float zDif = camPos.z() - dolPos.z();
        //float[] axes = new float[] { xDif, yDif, zDif };
        int distance = 1 + (int) Math.sqrt(Math.pow(xDif, 2) + Math.pow(yDif, 2) + Math.pow(zDif, 2));
        System.out.println("distance: " + distance + "    " + xDif + " " + yDif + " " + zDif);
        //System.out.println("cam: " + camPos.x() + " " + camPos.y() + " " + camPos.z());
        //System.out.println("dol: " + dolPos.x() + " " + dolPos.y() + " " + dolPos.z());
        //return true;
        if ((distance) < 21) {
            return true;
        }
        return false;*/

        //clamp function in the docs
        //Vector3f camPos = (Vector3f) camera.getPo();
        Vector3f dolPos = (Vector3f) dolphinN.getWorldPosition();

        float xD = checkWithin(vec.x(), dolPos.x(), 10.0f);
        float yD = checkWithin(vec.y(), dolPos.y(), 10.0f);
        float zD = checkWithin(vec.z(), dolPos.z(), 10.0f);
        //System.out.println(" " + xD + " " + yD + " " + zD);
        return ((Vector3f)Vector3f.createFrom(xD, yD, zD));
        //return true;
    }

    public float checkWithin(float target, float home, float bounds) {
        if(target < (home-bounds) || target > (home+bounds)) {
            float minDiff = Math.abs(home-bounds-target);
            float maxDiff = Math.abs(home+bounds-target);
            if (minDiff>maxDiff) {
                return (home+bounds);
            }
            return (home-bounds);
        }
        return target;
    }

    /* public void moveForwardMethod() {
        Camera c = camera;
        Vector3f v = c.getFd();
        Vector3f p = c.getPo();
        Vector3f p1 = (Vector3f) Vector3f.createFrom(0.5f*v.x(), 0.5f*v.y(), 0.5f*v.z());
        Vector3f p2 = (Vector3f) p.add((Vector3)p1);
        c.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
        camera = c;
    } */

}

