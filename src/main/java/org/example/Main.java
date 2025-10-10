package org.example;

import com.jme3.anim.AnimComposer;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.*;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.terrain.Terrain;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

import java.nio.FloatBuffer;
import java.util.*;

public class Main extends SimpleApplication {
    public static Main instance; // static reference for networking thread

    private BulletAppState bulletAppState;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left, right, forward, backward;

    public static Hashtable<String, Node> playerEntities = new Hashtable<>();
    public static Hashtable<String, String> playerData = new Hashtable<>();
    public static Spatial hand;
    private static Vector3f handOffset;
    private static Node handNode;
    private static Node playerView;
    // strong structure for entitydata:
    // {playerName : Yrotation § playerX § playerY § playerZ}

    public static String serverAdress;
    public static String clientID;
    public static boolean useInstancing;

    //declare materials
    public static Material semiMat1;
    public static Material semiMat2;
    public static Material semiMat3;
    public static Material semiMat4;
    public static Material semiMat5;
    public static Material semiMat6;
    public static Material semiMat7;
    public static Material semiMat8;
    public static Material semiMat9;
    public static ArrayList<Material> matList = new ArrayList<Material>();


    public static void main(String[] args) throws Exception {
        //player mode list


        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter IP Adress of Server:");
        serverAdress = scanner.nextLine();
        while(true) {
            System.out.println("Enter Username:");
            clientID = scanner.nextLine();
            if(clientID.contains("$") || clientID.contains("§") || clientID.contains("&")){
                System.out.println("Invalid Usrename, cannot contain Symbols: [§, $, &]");
            }
            else {
                break;
            }
        }
        System.out.println("Use Instancing? 'no' for no, otherwise: yes");
        String s = scanner.nextLine();
        useInstancing = !s.equalsIgnoreCase("no");




        NetworkThread client = new NetworkThread(serverAdress, 8080);
        client.connect();



        Main app = new Main();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1920, 1080); // set to your monitor resolution
        settings.setFullscreen(true);      // true = fullscreen, false = windowed
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

        // Called by networking thread
        public Vector3f getPlayerPosition() {
            if (player == null) {
                return new Vector3f(0,0,0); // or skip sending
            }
            return player.getPhysicsLocation().clone(); // clone to avoid threading issues
        }

        public float getPlayerRotationY() {
            if (player == null) {
                return 0f;
            }

            Quaternion camRot = cam.getRotation();
            float[] angles = camRot.toAngles(null); // returns [X, Y, Z] in radians
            float yaw = angles[1]; // Yaw around Y-axis in radians
            float yawDegrees = yaw * FastMath.RAD_TO_DEG;
            return yawDegrees;
        }
        public float getPlayerRotationX() {
            if (player == null) {
                return 0f;
            }

            Quaternion camRot = cam.getRotation();
            float[] angles = camRot.toAngles(null); // returns [X, Y, Z] in radians
            float pitch = angles[0]; // Yaw around Y-axis in radians
            float pitchDegrees = pitch * FastMath.RAD_TO_DEG;
            return pitchDegrees;
        }


    public int getTextureIndex(String username){
        int sum = 0;
        for (char c : username.toCharArray()){
            sum += (int) c;
        }

        int firstDigit = Integer.toString(sum).charAt(0) - '0';
        return firstDigit;
    }

    public static Vector3f getRandomPointOnMesh(Mesh mesh) {
        VertexBuffer posBuffer = mesh.getBuffer(VertexBuffer.Type.Position);
        FloatBuffer fb = (FloatBuffer) posBuffer.getDataReadOnly();
        int vertCount = mesh.getVertexCount();
        int i = FastMath.nextRandomInt(0, vertCount - 1);
        int index = i * 3;

        float x = fb.get(index);
        float y = fb.get(index + 1);
        float z = fb.get(index + 2);

        return new Vector3f(x, y, z);
    }







    @Override
    public void simpleInitApp() {
        instance = this;

        // Physics
        bulletAppState = new BulletAppState();
        //bulletAppState.setDebugEnabled(true);

        stateManager.attach(bulletAppState);

        //test data for testing entity creation
        playerData.put("heimat0729", "130§5§2§5");

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



        //create materials
        semiMat1 = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");
        semiMat2 = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");
        semiMat3 = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");
        semiMat4 = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");
        semiMat5 = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");
        semiMat6 = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");
        semiMat7 = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");
        semiMat8 = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");
        semiMat9 = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");
        semiMat1.setTexture("BaseColorMap", semibot_1);
        semiMat2.setTexture("BaseColorMap", semibot_2);
        semiMat3.setTexture("BaseColorMap", semibot_3);
        semiMat4.setTexture("BaseColorMap", semibot_4);
        semiMat5.setTexture("BaseColorMap", semibot_5);
        semiMat6.setTexture("BaseColorMap", semibot_6);
        semiMat7.setTexture("BaseColorMap", semibot_7);
        semiMat8.setTexture("BaseColorMap", semibot_8);
        semiMat9.setTexture("BaseColorMap", semibot_9);
        matList.add(semiMat1);
        matList.add(semiMat2);
        matList.add(semiMat3);
        matList.add(semiMat4);
        matList.add(semiMat5);
        matList.add(semiMat6);
        matList.add(semiMat7);
        matList.add(semiMat8);
        matList.add(semiMat9);
        for (int i=0; i<9; i++){
            matList.get(i).setParam("Roughness", 0.5f);
            matList.get(i).setParam("Metallic", 0f);
        }



        Spatial sky = SkyFactory.createSky(getAssetManager(), "textures/sky/sky_25_2k.png", SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);
        //viewPort.setBackgroundColor(ColorRGBA.fromRGBA255(64, 223, 255, 255));

        // Create a directional light (like the sun)
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
        rootNode.addLight(sun);

        Spatial probeHolder = this.getAssetManager().loadModel("textures/sky/Sky_Cloudy.j3o");
        LightProbe probe = (LightProbe)probeHolder.getLocalLightList().get(0);
        probe.setPosition(Vector3f.ZERO);

        probeHolder.removeLight(probe);
        rootNode.addLight(probe);

        final int SHADOWMAP_SIZE=1024;
        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
        dlsr.setLight(sun);
        viewPort.addProcessor(dlsr);


        Texture ground_tex = assetManager.loadTexture("textures/grass_online.jpg");
        Texture groundMR_tex = assetManager.loadTexture("textures/karambit/karambitTex_1_RoughnessMetallic.png");
        ground_tex.setWrap(Texture.WrapMode.Repeat);
        Material groundMat = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        groundMat.setTexture("Tex1",ground_tex);
        groundMat.setFloat("Tex1Scale", 64f);
        //groundMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        HillHeightMap heightmap = null;
        try {
            heightmap = new HillHeightMap(1025, 1000, 50, 100, (byte) 3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        heightmap.load();
        Spatial ground = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
        ground.setMaterial(groundMat);
        ground.setLocalScale(1f);
        ground.setLocalTranslation(0, -1f, 0);
        RigidBodyControl groundPhys = new RigidBodyControl(0.0f); // static
        ground.addControl(groundPhys);
        ground.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(ground);
        bulletAppState.getPhysicsSpace().add(groundPhys);

        // Hand
        playerView = new Node("PlayerView");
        playerView.setLocalTranslation(new Vector3f(0, 1.5f, 0)); // camera height relative to player
        rootNode.attachChild(playerView);

        Spatial hand = assetManager.loadModel("models/player_hand_anim.glb");
        handOffset = new Vector3f(0.5f, -0.5f, -.75f);
        cam.setFrustumPerspective(45, (float) cam.getWidth() / cam.getHeight(), 0.01f, 1000f);
        hand.setLocalScale(0.2f); // scale to match scene
        Material handMat = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");
        handMat.setColor("BaseColor", ColorRGBA.Red);
        handMat.setParam("Roughness", 0.5f);
        handMat.setParam("Metallic", 0f);
        rootNode.attachChild(hand);
        handNode = new Node("HandNode");
        handNode.attachChild(hand);
        hand.setLocalTranslation(handOffset);
        playerView.attachChild(handNode);
        hand.setMaterial(handMat);

        AnimComposer composer = null;
        for (Spatial s : ((Node) hand).getChildren()) {
            composer = s.getControl(AnimComposer.class);
            if (composer != null) {
                System.out.println("Found AnimComposer on: " + s.getName());
                break;
            }
        }
        if (composer == null) {
            System.out.println("No AnimComposer found in any child node.");
        } else {
            System.out.println("Animations: " + composer.getAnimClipsNames());
        }
        //composer.setCurrentAction("idle ", AnimComposer.DEFAULT_LAYER, true);



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
        playerView.setLocalTranslation(player.getPhysicsLocation().add(0, 1.5f, 0)); // camera height
        handNode.setLocalRotation(RotationUtil.fromDegrees(-getPlayerRotationX(), getPlayerRotationY()+180, 0));

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

        Enumeration<String> playerNames = playerData.keys();
        while (playerNames.hasMoreElements()) {
            String PName = playerNames.nextElement();
            if(playerEntities.containsKey(PName)){
               String[] playerArray = playerData.get(PName).split("§");
               //playerEntities.get(PName).getControl(RigidBodyControl.class).setPhysicsLocation(new Vector3f(Float.parseFloat(playerArray[1]), Float.parseFloat(playerArray[2]), Float.parseFloat(playerArray[3])));
                playerEntities.get(PName).setLocalTranslation(new Vector3f(Float.parseFloat(playerArray[1]), Float.parseFloat(playerArray[2]), Float.parseFloat(playerArray[3])));

                playerEntities.get(PName).setLocalRotation(RotationUtil.fromDegrees(0f, Float.parseFloat(playerArray[0]), 0f));
                //playerEntities.get(PName).getControl(RigidBodyControl.class).setPhysicsRotation(RotationUtil.fromDegrees(0f, Float.parseFloat(playerArray[0]), 0f));
            }
            else{
                Spatial newP = assetManager.loadModel("models/semibot.obj");
                Node newPnode = new Node(PName);
                newPnode.attachChild(newP);
                CapsuleCollisionShape newMC = new CapsuleCollisionShape(1f,2f);
                RigidBodyControl newMCTRL = new RigidBodyControl(newMC, 0f);
                newMCTRL.setKinematic(true);
                newPnode.addControl(newMCTRL);
                bulletAppState.getPhysicsSpace().add(newMCTRL);
                newP.setMaterial(matList.get(getTextureIndex(PName) - 1));
                newPnode.setLocalScale(1.25f);
                TextUtil.addNameTag(newPnode, PName, assetManager);
                newP.setShadowMode(RenderQueue.ShadowMode.Cast);
                rootNode.attachChild(newPnode);
                playerEntities.put(PName, newPnode);
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        // cleanup logic here
        System.out.println("Window closed. Stopping code.");
        System.exit(0); // guarantees JVM exit
    }
}
