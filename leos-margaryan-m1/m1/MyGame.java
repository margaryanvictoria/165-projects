package m1;

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
import java.util.Random;
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
                    moveForwardAction,
                    moveBackwardAction,
                    moveLeftAction,
                    moveRightAction,
                    yawLeft,
                    yawRight,
                    interactDolphin,
                    interactDolphin2,
                    moveFBAction,
                    moveLRAction,
                    yawLRAction;
    //private boolean showAxes = false;
    private ArrayList<SceneNode> newPlanets = new ArrayList<SceneNode>();
    private ArrayList<SceneNode> visitedPlanets = new ArrayList<SceneNode>();
    private int visitedPlanetsSize = 0;
    private ArrayList<SceneNode> visitedPlanets2 = new ArrayList<SceneNode>();
    private int visitedPlanets2Size = 0;

    String visitedPlanetsString = "Visited: ";

    //A2 additions ---
    private Camera3Pcontroller orbitController, orbitController2;
    SceneManager sm;
    private boolean printX = false, printY = false, printZ = false;

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
        System.out.println("press ESC or Button 2 (B) to quit");
        // Controls!
        System.out.println("press W or Left Joystick to move dolphin forward");
        System.out.println("press S or Left Joystick to move dolphin backward");
        System.out.println("press A or Left Joystick to move dolphin left");
        System.out.println("press D or Left Joystick to move dolphin right");
        System.out.println("press Q or Left Bumper to yaw dolphin left");
        System.out.println("press E or Right Bumper to yaw dolphin right");
        System.out.println("press UP or Right Joystick to pitch camera up");
        System.out.println("press DOWN or Right Joystick to pitch camera down");
		System.out.println("press LEFT or Right Joystick to yaw camera left");
        System.out.println("press RIGHT or Right Joystick to yaw camera right");
		System.out.println("press Z or Left Trigger to zoom camera in");
        System.out.println("press X or Right Trigger to zoom camera out");
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
        rs.getRenderWindow().setTitle("Assignment 2 - Michael Leos & Victoria Margaryan");
	}

    // Setting up multiple viewports
    protected void setupWindowViewports(RenderWindow rw) {
        rw.addKeyListener(this);

        Viewport topViewport = rw.getViewport(0);
        topViewport.setDimensions(.51f, .01f, .99f, .49f); // B,L,W,H
        topViewport.setClearColor(new Color(1.0f, .7f, .7f));

        Viewport botViewport = rw.createViewport(.01f, .01f, .99f, .49f);
        botViewport.setClearColor(new Color(.5f, 1.0f, .5f));
    }

    // Setting up our camera --------------------------------------------------------------------
    // We get the root node, create a camera, set Viewport 0 to the camera, set the position
    // Then create a camera node, attach it to the root (parent) node, and attach the camera to it
    // -------------------------------------------------------------------------------------------
    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        this.sm = sm;
        rootNode = sm.getRootSceneNode();
        camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE); //made this a global var
        rw.getViewport(0).setCamera(camera);
		
		/* camera.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
		camera.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
		camera.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));
		
		camera.setPo((Vector3f)Vector3f.createFrom(-0.5f, 0.5f, 6.0f)); */
        //camera.setPo((Vector3f)Vector3f.createFrom(0f, 0f, 0f));
        
        cameraNode = rootNode.createChildSceneNode(camera.getName() + "Node");
        cameraNode.attachObject(camera);
        camera.setMode('n');
        camera.getFrustum().setFarClipDistance(1000.0f);

        Camera camera2 = sm.createCamera("MainCamera2", Projection.PERSPECTIVE);
        rw.getViewport(1).setCamera(camera2);
        SceneNode camera2N = rootNode.createChildSceneNode("MainCamera2Node");
        camera2N.attachObject(camera2);
        camera2.setMode('n');
        camera2.getFrustum().setFarClipDistance(1000.0f);
    }
	
    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {
        im = new GenericInputManager(); //this was a global InputManager var
        //setupInputs();

        // Dolphin 1 ------------
        Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES); //set it to show the triangle faces, not just lines or points
        dolphinN = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
        //dolphinN.moveBackward(2.0f); //offset the node's position by 2
        dolphinN.attachObject(dolphinE); //add our dolphin obj to our dolphin node
        dolphinN.yaw(Degreef.createFrom(180.0f)); //rotate the dolphin 180 degrees on x axis
        dolphinN.setLocalPosition(0.0f, 0.0f, 4.0f);

        // Manually adding a texture to our dolphin
        TextureManager tm = eng.getTextureManager();
        Texture redTexture = tm.getAssetByPath("redDolphin.jpg"); //get the red texture we made
        TextureState state = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        state.setTexture(redTexture); //set it to a texture state
        dolphinE.setRenderState(state); //set that texture state to our dolphin

        //Dolphin 2 ------------------------
        Entity dolphin2E = sm.createEntity("myDolphin2", "dolphinHighPoly.obj");
        dolphin2E.setPrimitive(Primitive.TRIANGLES);
        SceneNode dolphin2N = sm.getRootSceneNode().createChildSceneNode(dolphin2E.getName() + "Node");
        dolphin2N.attachObject(dolphin2E);
        dolphin2N.yaw(Degreef.createFrom(180.0f));
        dolphin2N.setLocalPosition(-1.0f, 0.0f, 0.0f);
        //dolphin2N.setLocalScale(0.2f, 0.2f, 0.2f);

        // Set up inputs & orbit cams
        setupOrbitCameras(eng, sm);
        setupInputs(sm);


        // make manual objects – in this case a pyramid
        ManualObject pyr = makePyramid(eng, sm); //using our makePyramid method we imported
        SceneNode pyrN = sm.getRootSceneNode().createChildSceneNode("PyrNode"); //add it to the scene
        pyrN.scale(0.7f, 0.9f, 0.7f); //make the node smaller (and whatever is in it)
        pyrN.moveBackward(2.0f); //i put this here to shift it backward a bit (cause the backfaces are not coded)
        pyrN.attachObject(pyr); //attach our pyramid to our node

        Texture iceTex = eng.getTextureManager().getAssetByPath("ice.jpg");
        TextureState iceTextureState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        iceTextureState.setTexture(iceTex);
        pyr.setRenderState(iceTextureState);

        // Plane --------------------------------
        ManualObject plane = makePlane(eng, sm);
        SceneNode planeN = sm.getRootSceneNode().createChildSceneNode("PlaneNode");
        planeN.scale(50.0f, 50.0f, 50.0f);
        planeN.attachObject(plane);
        planeN.setLocalPosition(0.0f, -1.0f, 0.0f); //for sanity sake

        // Choose randomization of planets
        Random r = new Random();
        Vector3f e1pos = (Vector3f) Vector3f.createFrom(r.nextInt(40)-20.0f, (float) r.nextInt(1), r.nextInt(40) - 20.0f);
        Vector3f e2pos = (Vector3f) Vector3f.createFrom(r.nextInt(40)-20.0f, (float) r.nextInt(1), r.nextInt(40) - 20.0f);
        Vector3f e3pos = (Vector3f) Vector3f.createFrom(r.nextInt(40)-20.0f, (float) r.nextInt(1), r.nextInt(40) - 20.0f);
        Vector3f e4pos = (Vector3f) Vector3f.createFrom(r.nextInt(40)-20.0f, (float) r.nextInt(1), r.nextInt(40) - 20.0f);

        // Earth 1 --
        Entity earthE1 = sm.createEntity("myEarth1", "earth.obj");
        earthE1.setPrimitive(Primitive.TRIANGLES);
        SceneNode earthN1 = sm.getRootSceneNode().createChildSceneNode(earthE1.getName() + "Node");
        earthN1.attachObject(earthE1); //attaching our earth obj to our earth node
        earthN1.setLocalPosition(e1pos); //offset the earth's position from the node's origin
        earthN1.setLocalScale(0.4f, 0.4f, 0.4f); //make the earth really small
        newPlanets.add(earthN1);

        Texture greenTexture = tm.getAssetByPath("green.jpeg"); //get the ice texture we imported
        TextureState greenTextureState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        greenTextureState.setTexture(greenTexture); //set it to a texture state
        earthE1.setRenderState(greenTextureState); //set that texture state to our earth

        // Earth 2 --
        Entity earthE2 = sm.createEntity("myEarth2", "earth.obj");
        earthE2.setPrimitive(Primitive.TRIANGLES);
        SceneNode earthN2 = sm.getRootSceneNode().createChildSceneNode(earthE2.getName() + "Node");
        earthN2.attachObject(earthE2); //attaching our earth obj to our earth node
        earthN2.setLocalPosition(e2pos); //offset the earth's position from the node's origin
        earthN2.setLocalScale(0.4f, 0.4f, 0.4f); //make the earth really small
        newPlanets.add(earthN2);

        // Earth 3 --
        Entity earthE3 = sm.createEntity("myEarth3", "earth.obj");
        earthE3.setPrimitive(Primitive.TRIANGLES);
        SceneNode earthN3 = sm.getRootSceneNode().createChildSceneNode(earthE3.getName() + "Node");
        earthN3.attachObject(earthE3); //attaching our earth obj to our earth node
        earthN3.setLocalPosition(e3pos); //offset the earth's position from the node's origin
        earthN3.setLocalScale(0.4f, 0.4f, 0.4f); //make the earth really small
        newPlanets.add(earthN3);

        // Earth 4 --
        Entity earthE4 = sm.createEntity("myEarth4", "earth.obj");
        earthE4.setPrimitive(Primitive.TRIANGLES);
        SceneNode earthN4 = sm.getRootSceneNode().createChildSceneNode(earthE4.getName() + "Node");
        earthN4.attachObject(earthE4); //attaching our earth obj to our earth node
        earthN4.setLocalPosition(e4pos); //offset the earth's position from the node's origin
        earthN4.setLocalScale(0.5f, 0.5f, 0.5f); //make the earth really small
        newPlanets.add(earthN4);

        // E4 Moon
        Entity moon = sm.createEntity("myMoon", "earth.obj");
        moon.setPrimitive(Primitive.TRIANGLES);
        SceneNode moonN = sm.getRootSceneNode().createChildSceneNode(moon.getName() + "Node");
        moonN.attachObject(moon);
        moonN.setLocalPosition(3.0f, 0.0f, 3.0f);
        moonN.setLocalScale(0.4f, 0.4f, 0.4f);
        earthN4.attachChild(moonN);

        //TextureManager tm = eng.getTextureManager();
        Texture moonTexture = tm.getAssetByPath("moon.jpeg"); //get the red texture we made
        TextureState moonState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        moonState.setTexture(moonTexture); //set it to a texture state
        moon.setRenderState(moonState); //set that texture state to our dolphin

        RotationController rcMoon = new RotationController(Vector3f.createUnitVectorY(), .01f);
        rcMoon.addNode(moonN);
        sm.addController(rcMoon);


        // create a rotation controller and rotate by .02 each frame
        RotationController rc = new RotationController(Vector3f.createUnitVectorY(), .02f); //speed it rotates
        rc.addNode(earthN1); //rotate our earth node
        rc.addNode(earthN2);
        rc.addNode(earthN3);
        rc.addNode(earthN4);
        rc.addNode(pyrN); //rotate our pyramid node

        sm.addController(rc); //add our rotation controller to the scene so it will update each frame Update

        /* // Colored Axes
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
        axesN.attachObject(lineZ); */

        // set up lights
        sm.getAmbientLight().setIntensity(new Color(.3f, .3f, .3f)); //ambient
        Light plight = sm.createLight("testLamp1", Light.Type.POINT); //point light
        plight.setAmbient(new Color(.1f, .1f, .1f));
        plight.setDiffuse(new Color(0.8f, 0.8f, 0.8f));
        plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(50f);

        SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode"); //make light node
        plightNode.attachObject(plight); //add our light obj to our light node
        plightNode.setLocalPosition(1.0f, 5.0f, 5.0f); //offset the light from the light node's origin
        
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

        //Scaling controller for first player's visited planets
        ScalingController sc = new ScalingController(.009f, 500);
        sm.addController(sc);

        CustomController cc = new CustomController();
        sm.addController(cc);
    }

    // Update() will update every frame -------------------------------------------------------
    @Override   
    protected void update(Engine engine) {
		// build and set HUD
		rs = (GL4RenderSystem) engine.getRenderSystem();
        RenderWindow rw = rs.getRenderWindow();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		//counterStr = Integer.toString(counter);

		dispStr = "D1 Time = " + elapsTimeStr + "   Visited = " + visitedPlanetsSize;
		rs.setHUD(dispStr, 15 + rw.getViewport(0).getActualLeft(), 15 + rw.getViewport(0).getActualBottom());

        dispStr = "D2 Time = " + elapsTimeStr + "   Visited = " + visitedPlanets2Size;
		rs.setHUD2(dispStr, 15 + rw.getViewport(1).getActualLeft(), 15 + rw.getViewport(1).getActualBottom());

        //rs.setHUD2(visitedPlanetsString, 15, (rs.getRenderWindow().getHeight()/4));
        //rs.setHUD2(visitedPlanetsString, 15, (int) ((rs.getRenderWindow().getHeight())*.75));
        checkIfPlanetCollision();

        if(explosion) {
            //SceneNode planet = (getEngine().getSceneManager().getSceneNode(explodingPlanet.getName()));
            explosionAnimation();
        }

        // Tell the input manager to process the inputs
        im.update(elapsTime);
        orbitController.updateCameraPosition();
        orbitController2.updateCameraPosition();
	}

    // Set up our controls ---------------------------------------------------------------------
    protected void setupInputs(SceneManager sm) {
        ArrayList<Controller> controllers = im.getControllers(); //get all our controllers

        SceneNode dolphinN = getEngine().getSceneManager().getSceneNode("myDolphinNode");
        SceneNode dolphin2N = getEngine().getSceneManager().getSceneNode("myDolphin2Node");

        // Actions ------
        quitGameAction = new QuitGameAction(this);

        moveForwardAction = new MoveForwardAction(dolphin2N);
        moveBackwardAction = new MoveBackwardAction(dolphin2N);
        moveLeftAction = new MoveLeftAction(dolphin2N);
        moveRightAction = new MoveRightAction(dolphin2N);
        yawLeft = new YawLeft(dolphin2N);
        yawRight = new YawRight(dolphin2N);

        interactDolphin = new InteractDolphin(dolphinN);
        interactDolphin2 = new InteractDolphin(dolphin2N);

        moveFBAction = new MoveFBAction(dolphinN);
        moveLRAction = new MoveLRAction(dolphinN);
        yawLRAction = new YawLRAction(dolphinN);

        // First player is GAMEPAD, Second player will handle Mouse & Keyboard
        for (Controller c : controllers) {
            if (c.getType() == Controller.Type.GAMEPAD) {
                // Button 2 (B) = quit game
                im.associateAction(c, net.java.games.input.Component.Identifier.Button._1, 
                    quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
                // left joystick (W & S) = move forward & backward
                im.associateAction(c, net.java.games.input.Component.Identifier.Axis.Y,
                    moveFBAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // left joystick (A & D) = move left & right
                im.associateAction(c, net.java.games.input.Component.Identifier.Axis.X,
                    moveLRAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // left right bumper (LEFT & RIGHT) = yaw left & right
                im.associateAction(c, net.java.games.input.Component.Identifier.Button._4,
                    yawLRAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(c, net.java.games.input.Component.Identifier.Button._5,
                    yawLRAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // Button 1 (A) = jump
                im.associateAction(c, net.java.games.input.Component.Identifier.Button._0,
                    interactDolphin, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

            //} else if (c.getType() == Controller.Type.MOUSE) {

            } else if (c.getType() == Controller.Type.KEYBOARD) {
                //ESC = quit game
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.ESCAPE,
                    quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
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
                // Q = yaw left
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.Q,
                    yawLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // E = yaw right
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.E,
                    yawRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                // Space bar = jump
                im.associateAction(c, net.java.games.input.Component.Identifier.Key.SPACE,
                    interactDolphin2, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            }
        }
    }

    protected void setupInputsOLD() {
        /*
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
        */
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
        } 
        */
    }

    // For immediate control results, use this for testing more than actual mapping pls -----
    @Override
    public void keyPressed(KeyEvent e) {
        /* switch (e.getKeyCode()) {
            case KeyEvent.VK_P:
                printX = true;
                printY = false;
                printZ = false;
                break;
            case KeyEvent.VK_O:
                printX = false;
                printY = true;
                printZ = false;
                break;
            case KeyEvent.VK_I:
                printX = false;
                printY = false;
                printZ = true;
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

    public ManualObject makePlane(Engine eng, SceneManager sm) throws IOException {
        ManualObject plane = sm.createManualObject("Plane");
        ManualObjectSection planeSec = plane.createManualSection("PlaneSection");
        plane.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float[] vertices4 = new float[]
            { -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
              1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, -1.0f, 0.0f, -1.0f }; //two triangles
        float[] texcoords4 = new float[]
            { 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
              1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f };  //just need two texture coords
        int[] indices4 = new int[] { 0,1,2,3,4,5 };  //only hvae 6 verts

        FloatBuffer vertBuf4 = BufferUtil.directFloatBuffer(vertices4);
        FloatBuffer texBuf4 = BufferUtil.directFloatBuffer(texcoords4);
        IntBuffer indexBuf4 = BufferUtil.directIntBuffer(indices4);
        planeSec.setVertexBuffer(vertBuf4);
        planeSec.setTextureCoordsBuffer(texBuf4);
        planeSec.setIndexBuffer(indexBuf4);
        //changing this to ice cause hexagon scares me
        Texture hexagonTexture = eng.getTextureManager().getAssetByPath("ice.jpg");
        TextureState hexagonTextureState = (TextureState)sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        hexagonTextureState.setTexture(hexagonTexture);
        planeSec.setRenderState(hexagonTextureState);
        
        return plane;
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
        SceneNode dolphinN = sm.getSceneNode("myDolphinNode");
        SceneNode dolphin2N = sm.getSceneNode("myDolphin2Node");
        Vector3f dp = (Vector3f) dolphinN.getWorldPosition();
        Vector3f d2p = (Vector3f) dolphin2N.getWorldPosition(); 
        float tempX, tempY, tempZ;
        float temp2X, temp2Y, temp2Z;
        Vector3 scale;
        
        for (SceneNode sn : newPlanets) {
        	scale = sn.getLocalScale();
        	
            //For debugging purposes with the first player
            if(printX) {
                System.out.println("dpx: "+dp.x()+"  \n sn: "+sn.getWorldPosition().x());
            } else if (printY) {
                System.out.println("dpy: "+dp.y()+"  \n sn: "+sn.getWorldPosition().y());
            } else if (printZ) {
                System.out.println("dpz: "+dp.z()+"  \n sn: "+sn.getWorldPosition().z());
            }
            
            tempX = sn.getWorldPosition().x() - dp.x();
            if ((-scale.x()*2 - 0.5f) <= tempX && tempX <= (scale.x()*2 + 0.5f)) {
                tempY = sn.getWorldPosition().y() - dp.y(); 
                if ((-scale.y()*2 - 0.5f) <= tempY && tempY <= (scale.y()*2 + 0.5f)) {
                	tempZ = sn.getWorldPosition().z() - dp.z();
                	if((-scale.z()*2 - 0.5f) <= tempZ && tempZ <= (scale.z()*2 + 0.5f)) {
                        System.out.println("\n\nYou visited..\n\n");
                		visitedPlanet(0, sn);
                        break;
                	}        
                }
            }
            temp2X = sn.getWorldPosition().x() - d2p.x();
            if ((-scale.x()*2 - 0.5f) <= temp2X && temp2X <= (scale.x()*2 + 0.5f)) {
                temp2Y = sn.getWorldPosition().y() - d2p.y(); 
                if ((-scale.y()*2 - 0.5f) <= temp2Y && temp2Y <= (scale.y()*2 + 0.5f)) {
                	temp2Z = sn.getWorldPosition().z() - d2p.z();
                	if((-scale.z()*2 - 0.5f) <= temp2Z && temp2Z <= (scale.z()*2 + 0.5f)) {
                        System.out.println("\n\nYou visited..\n\n");
                		visitedPlanet(1, sn);
                        break;
                	}        
                }
            }
        }
        /*
        //Vector3f cp = camera.getPo();
        //SceneNode dolphin2N = sm.getSceneNode("myDolphin2Node");
        SceneNode camera2N = sm.getSceneNode("MainCamera2Node");
        SceneNode cameraN = sm.getSceneNode("MainCameraNode");
        Vector3f dp = (Vector3f) cameraN.getWorldPosition();
        Vector3f d2p = (Vector3f) camera2N.getLocalPosition();
        float tempX, tempY, tempZ;
        float temp2X, temp2Y, temp2Z;
        for (SceneNode sn : newPlanets) {
            tempX = sn.getWorldPosition().x() - dp.x(); //check if any planets are close enough on the x axis
            temp2X = sn.getLocalPosition().x() - d2p.x();
            System.out.println("dp: "+dp.x()+" "+dp.y()+" "+dp.z()+" \n sn: "+sn.getWorldPosition().x()+" "+sn.getWorldPosition().y()+" "+sn.getWorldPosition().z());
            //System.out.println("\nd2p: "+d2p.x()+" "+d2p.y()+" "+d2p.z()+" \n sn: "+sn.getLocalPosition().x()+" "+sn.getLocalPosition().y()+" "+sn.getLocalPosition().z()+"\n\n");
            if (-.5f <= tempX && tempX <= .5f) {
                System.out.println("\n\nclose enough\n\n");
                System.out.println("dp: "+dp.x()+" "+dp.y()+" "+dp.z()+" \n sn: "+sn.getWorldPosition().x()+" "+sn.getWorldPosition().y()+" "+sn.getWorldPosition().z());
                tempY = sn.getWorldPosition().y() - dp.y();
                tempZ = sn.getWorldPosition().z() - dp.z();                
                //if (-.5f <= tempY && tempY <= .5f && -.5f <= tempZ && tempZ <= .5f) {
                if (-.5f <= tempZ && tempZ <= .5f) {
                    System.out.println("\n\n\nclose!\n\n\n");
                    visitedPlanet(1, sn);
                    break;
                }
            }
            if (-.5f <= temp2X && temp2X <= .5f) {
                temp2Y = sn.getWorldPosition().y() - d2p.y();
                temp2Z = sn.getWorldPosition().z() - d2p.z();                
                //if (-.5f <= temp2Y && temp2Y <= .5f && -.5f <= temp2Z && temp2Z <= .5f) {
                if (-.5f <= temp2Z && temp2Z <= .5f) {
                    visitedPlanet(2, sn);
                    break;
                }
            }
        }*/
    }

    public void visitedPlanet(int a, SceneNode sn) {
        System.out.println("User " + a +" visited planet node: " + sn.getName());
    
        newPlanets.remove(sn);
        //visitedPlanetsString += sn.getName() + ", ";
        if(a==0) {
            visitedPlanets.add(sn);
            visitedPlanetsSize++;
            sm.getController(2).addNode(sn);
        } else if (a==1) {
            visitedPlanets2.add(sn);
            visitedPlanets2Size++;
            sm.getController(3).addNode(sn);
        }
        //sm.updateControllers();
        /*
        //initiate rumble???
        //file:///C:/javagaming/jinput/javadoc/net/java/games/input/Rumbler.html
        if(gamepad != null){
            rumbleFlag = true;
        }

        explosion = true;
        explosionPhase1 = true;
        explosionStartTime = elapsTimeSec;
        explodingPlanet = sn; */        
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
                    //arr[0].rumble(10f);
                }
                System.out.println("Rumblers found: " + arr);
            }
            explosionPhase2 = true;
            explosionPhase1 = false;
        }
    }

    public Vector3f dolphinTetherCheck(Vector3f vec) {
        
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

    public void setupOrbitCameras(Engine eng, SceneManager sm) {
        /* SceneNode dolphinN = sm.getSceneNode("myDolphinNode");
        //SceneNode cameraN = sm.getSceneNode("MainCameraNode");
        //Camera camera = sm.getCamera("MainCamera");
        String controllerType = "keyboard";
        orbitController = new Camera3Pcontroller(camera, cameraNode, dolphinN, controllerType, im); */

        SceneNode dolphinN = sm.getSceneNode("myDolphinNode");
        SceneNode cameraN = sm.getSceneNode("MainCameraNode");
        Camera camera = sm.getCamera("MainCamera");
        //String gpName = im.getFirstGamepadName();
        String gpName = "gamepad";
        orbitController = new Camera3Pcontroller(camera, cameraN, dolphinN, gpName, im);

        SceneNode dolphin2N = sm.getSceneNode("myDolphin2Node");
        SceneNode camera2N = sm.getSceneNode("MainCamera2Node");
        Camera camera2 = sm.getCamera("MainCamera2");
        //String msName = im.getMouseName();
        String msName = "keyboard";
        orbitController2 = new Camera3Pcontroller(camera2, camera2N, dolphin2N, msName, im);
    }

}

