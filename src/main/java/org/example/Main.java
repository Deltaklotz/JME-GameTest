import com.jme3.app.SimpleApplication;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.material.Material;
import com.jme3.texture.Texture;

public class Main extends SimpleApplication {
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Spatial model = assetManager.loadModel("models/semibot.obj");


        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("textures/semibot_01.png");
        mat.setTexture("ColorMap", tex);
        model.setMaterial(mat);
        //mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);


        rootNode.attachChild(model);
    }
}
