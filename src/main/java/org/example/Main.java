import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Plane;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

import java.util.ArrayList;

public class Main extends SimpleApplication {

    private BulletAppState bulletAppState;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left, right, forward, backward;


    //player mode list
    private ArrayList<Spatial> playerEntities = new ArrayList<Spatial>();

    public static void main(String[] args) {
        Main app = new Main();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1920, 1080); // set to your monitor resolution
        settings.setFullscreen(true);      // true = fullscreen, false = windowed
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Physics
        bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(true);

        stateManager.attach(bulletAppState);

        //initialize textures
        Texture semibot_1 = assetManager.loadTexture("textures/semibot/semibot_01.png");
        Texture semibot_2 = assetManager.loadTexture("textures/semibot/semibot_02.png");
        Texture semibot_3 = assetManager.loadTexture("textures/semibot/semibot_03.png");
        Texture semibot_4 = assetManager.loadTexture("textures/semibot/semibot_04.png");
        Texture semibot_5 = assetManager.loadTexture("textures/semibot/semibot_05.png");
        Texture semibot_6 = assetManager.loadTexture("textures/semibot/semibot_06.png");
        Texture semibot_7 = assetManager.loadTexture("textures/semibot/semibot_07.png");
        Texture semibot_8 = assetManager.loadTexture("textures/semibot/semibot_08.png");
        Texture semibot_9 = assetManager.loadTexture("textures/semibot/semibot_09.png");

        Texture ground_tex = assetManager.loadTexture("textures/grass_online.jpg");
        ground_tex.setWrap(Texture.WrapMode.Repeat);

        //create materials
        Material semiMat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Material semiMat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Material semiMat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Material semiMat4 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Material semiMat5 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Material semiMat6 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Material semiMat7 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Material semiMat8 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Material semiMat9 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        semiMat1.setTexture("ColorMap", semibot_1);
        semiMat2.setTexture("ColorMap", semibot_2);
        semiMat3.setTexture("ColorMap", semibot_3);
        semiMat4.setTexture("ColorMap", semibot_4);
        semiMat5.setTexture("ColorMap", semibot_5);
        semiMat6.setTexture("ColorMap", semibot_6);
        semiMat7.setTexture("ColorMap", semibot_7);
        semiMat8.setTexture("ColorMap", semibot_8);
        semiMat9.setTexture("ColorMap", semibot_9);

        Material groundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        groundMat.setTexture("ColorMap", ground_tex);

        //rootNode.attachChild(SkyFactory.createSky(getAssetManager(), "textures/sky/sky_25_2k.png", SkyFactory.EnvMapType.EquirectMap));
        viewPort.setBackgroundColor(ColorRGBA.fromRGBA255(64, 223, 255, 255));

        // Ground
        Box groundBox = new Box(50, 0.1f, 50);
        groundBox.scaleTextureCoordinates(new Vector2f(10, 10));
        Geometry ground = new Geometry("Ground", groundBox);
        ground.setMaterial(groundMat);
        ground.setLocalTranslation(0, -0.1f, 0);
        RigidBodyControl groundPhys = new RigidBodyControl(0.0f); // static
        ground.addControl(groundPhys);
        rootNode.attachChild(ground);
        bulletAppState.getPhysicsSpace().add(groundPhys);

        // Load model
        Spatial model = assetManager.loadModel("models/semibot.obj");
        CapsuleCollisionShape modelcoll = new CapsuleCollisionShape(1f,2f);
        RigidBodyControl modelcontroll = new RigidBodyControl(modelcoll, 0);
        model.addControl(modelcontroll);
        bulletAppState.getPhysicsSpace().add(modelcontroll);
        model.setMaterial(semiMat1);
        model.getControl(RigidBodyControl.class).setPhysicsLocation(new Vector3f(0,2,5));
        model.setLocalScale(1.25f);
        rootNode.attachChild(model);

        //instantly remove the first model, comment to show
        model.removeFromParent();
        bulletAppState.getPhysicsSpace().remove(model.getControl(RigidBodyControl.class));


        // Character (first-person)
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(1f, 2f);
        player = new CharacterControl(capsule, 0.05f);
        player.setPhysicsLocation(new Vector3f(0, 2, 0));
        bulletAppState.getPhysicsSpace().add(player);

        // Camera
        flyCam.setMoveSpeed(1f);
        cam.setLocation(player.getPhysicsLocation());

        // Input
        initKeys();
    }

    private void initKeys() {
        inputManager.addMapping("Left",     new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right",    new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Forward",  new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump",     new KeyTrigger(KeyInput.KEY_SPACE));

        inputManager.addListener(actionListener,
                "Left", "Right", "Forward", "Backward", "Jump");
    }

    private ActionListener actionListener = new ActionListener() {
        public void onAction(String binding, boolean isPressed, float tpf) {
            switch (binding) {
                case "Left":     left = isPressed; break;
                case "Right":    right = isPressed; break;
                case "Forward":  forward = isPressed; break;
                case "Backward": backward = isPressed; break;
                case "Jump":
                    if (isPressed) {
                        player.jump();
                    }
                    break;
            }
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        walkDirection.set(0, 0, 0);
        if (left) walkDirection.addLocal(cam.getLeft());
        if (right) walkDirection.addLocal(cam.getLeft().negate());
        if (forward) walkDirection.addLocal(cam.getDirection());
        if (backward) walkDirection.addLocal(cam.getDirection().negate());

        walkDirection.y = 0; // keep horizontal
        player.setWalkDirection(walkDirection.mult(0.25f));
        player.setJumpSpeed(12f); // lower jump
        player.setFallSpeed(30f);
        player.setGravity(30f);
        cam.setLocation(player.getPhysicsLocation().add(0, 1.5f, 0));
    }
}
