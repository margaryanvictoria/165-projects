package myGameEngine;

//import ray.rage.scene.SceneNode;
import ray.rml.*;
import ray.rage.scene.Node;
import ray.rage.scene.controllers.AbstractController;

public class CustomController extends AbstractController {
    private float defaultSpeedT = .005f;
    private float defaultSpeedS = .01f;
    private float defaultPhaseLength = 750.0f; //in ms
    private float time = 0.0f; //in ms
    //Abstract Controller gives us protected java.util.List<Node>	controlledNodesList
    private Vector3f yAxis = (Vector3f) Vector3f.createFrom(0.0f, 1.0f, 0.0f);
    //private bool translatePhase = false;
    private int phaseCounter = 0; //0-1 for translate, 2-3 for scale


    protected void updateImpl(float elapsedTimeMillis) {
        //update the time we've been in this phase
        time += elapsedTimeMillis;
        if (time > defaultPhaseLength) { //are we done with the phase
            defaultSpeedT = -defaultSpeedT; //flip the speed counter for the trasnaltion animation
            defaultSpeedS = -defaultSpeedS; //flip the speed counter for the scaling animation
            time = 0.0F; //reset the clock
            phaseCounter++;
            if(phaseCounter % 4 == 0) { phaseCounter = 0; }
        }
        //move vertically on the y axis
        if(phaseCounter == 0 || phaseCounter == 1) {
            Vector3 vertical = (Vector3) yAxis.mult(defaultSpeedT * elapsedTimeMillis);
            Vector3f nodePos;
            //Vector3 vertical = 
            //for (SceneNode node : this.controlledNodesList)
            for (Node node : this.controlledNodesList) {
                //float vertY = verti
                nodePos = (Vector3f) node.getWorldPosition();
                if(nodePos.y() + vertical.y() > 0f) { //if we're above the y axis
                    node.translate(vertical); //for all our nodes, go up & down
                }
            }
        } else if (phaseCounter == 2 || phaseCounter == 3) {
            float scaling = 1.0F + defaultSpeedS;
            for (Node node : this.controlledNodesList)
                node.scale(scaling, 1.0f, scaling);  //for all our nodes, scale lengthwise
        }
    }
}