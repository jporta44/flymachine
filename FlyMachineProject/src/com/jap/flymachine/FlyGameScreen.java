package com.jap.flymachine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw.DebugDrawModes;
import com.badlogic.gdx.utils.Array;

public class FlyGameScreen implements Screen {
    
    final Fly game;
    boolean debugMode = false;
    boolean useAccelerometer = false;
    
    Music music;
    public DebugDrawerCustom debugDrawer = null;
    public PerspectiveCamera cam;
    public Model buildingModel;
    private Model groundModel;
    private Model shipModel;
    private ModelInstance ground;
    public ModelInstance ship;
    
    public ModelBatch modelBatch;
    public Environment environment;
    public CameraInputController camController;
    ModelBuilder modelBuilder = new ModelBuilder();
    BlendingAttribute blending;
    
    btCollisionConfiguration collisionConfiguration;
    btCollisionDispatcher dispatcher;
    btBroadphaseInterface broadphase;
    btConstraintSolver solver;
    btDynamicsWorld collisionWorld;
    //Vector3 gravity = new Vector3(0, -9.8f, 0);
    Vector3 gravity = new Vector3(0, 0.0f, 0);
    Vector3 tempVector = new Vector3();
    
    public Array<ModelInstance> instances = new Array<ModelInstance>();
    //Array<Model> models = new Array<Model>();
    Array<btDefaultMotionState> motionStates = new Array<btDefaultMotionState>();
    Array<btRigidBodyConstructionInfo> bodyInfos = new Array<btRigidBodyConstructionInfo>();
    Array<btCollisionShape> shapes = new Array<btCollisionShape>();
    Array<btRigidBody> bodies = new Array<btRigidBody>(); 
    //private MyContactListener contactCB;  
    btRigidBody shipBody;
    btRigidBody groundBody;

    int speed = 25;
    int movementSpeed = 50;
    int buildingSpawnNumber = 10;
    
    float shipWidth = 7.5f;
    float shipHeight = 2.5f;
    float shipDepth = 5f;
    float shipStartX = 20f;
    float shipStartY = 0.1f;
    
    float buildingWidth = 5f;
    float buildingDepth = 5f;
    float buildingMaxHeight = 20f;
    float buildingXFactor = 100f;
    
    float cameraXDistance = 40f;
    float lastCameraX = cameraXDistance;
    float camFar = 300f;
    float camNear = 0.1f;
    float camHeight = shipStartY + 4.0f;

    float universeDepth = 25f;
    float universeHeight = buildingMaxHeight;
    float deltaMoveX = 0.0f;
    
    boolean collision = false;
    boolean moveLeft = false;
    boolean moveRight = false;
    boolean moveUp = false;
    boolean moveDown = false;    
    
    public class MyContactListener extends ContactListener {
        
        
        @Override
        public void onContactStarted(int userValue0, int userValue1) {
            if (userValue0 == 1 || userValue1 == 1) {
                System.out.println("***********CONTACT**********");
                collision = true;
                ///Stop Moving
                deltaMoveX = 0;
                //Disable Ship Simulation
                shipBody.forceActivationState(5);
                //Stop music
                music.stop();
                Gdx.graphics.setContinuousRendering(false);
                // TODO Remove Ship (replace with crash or something else)
                // instances.removeIndex(1);
                // motionStates.removeIndex(1);
                Gdx.graphics.requestRendering();

            }            
        }
    }
    
    public class MyInputProcessor implements InputProcessor {
        @Override
        public boolean keyDown (int keycode) {
            switch (keycode) {
                case Keys.LEFT:
                    moveLeft = true;
                    break;
                case Keys.RIGHT: 
                    moveRight = true;
                    break;
                case Keys.UP:
                    moveDown = true;
                    break;
                case Keys.DOWN: 
                    moveUp = true;
                    break;
                case Keys.W:
                    speed++;
                    break;   
                case Keys.S:
                    speed--;
                    break;   
                case Keys.BACK:
                    game.setScreen(new MainMenuScreen(game));
                    dispose();
                    break;
            }
            return true;
        }

        @Override
        public boolean keyUp (int keycode) {
            switch (keycode) {
                case Keys.LEFT:
                    moveLeft = false;
                    break;
                case Keys.RIGHT: 
                    moveRight = false;
                    break;
                case Keys.UP:
                    moveDown = false;
                    break;
                case Keys.DOWN: 
                    moveUp = false;
                    break;
            }
            return true;
        }

        @Override
        public boolean keyTyped (char character) {
           return false;
        }

        @Override
        public boolean touchDown (int x, int y, int pointer, int button) {
           return false;
        }

        @Override
        public boolean touchUp (int x, int y, int pointer, int button) {
           return false;
        }

        @Override
        public boolean touchDragged (int x, int y, int pointer) {
           return false;
        }

        @Override
        public boolean scrolled (int amount) {
           return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }
     }    

    public FlyGameScreen(final Fly game) {
        this.game = game;
        Gdx.graphics.setContinuousRendering(true);
        Bullet.init();
        new MyContactListener();
        
        MyInputProcessor inputProcessor = new MyInputProcessor();
        Gdx.input.setInputProcessor(inputProcessor);
        Gdx.input.setCatchBackKey(true);
        
        music = Gdx.audio.newMusic(Gdx.files.internal("MeMata.mp3"));
        
        music.setLooping(true);
        music.play();
        
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(lastCameraX, camHeight, 0f);
        //cam.position.set(30, 1f, 30);
        cam.lookAt(0, 0, 0);
        cam.near = camNear;
        cam.far = camFar;
        cam.update();
        
        //camController = new CameraInputController(cam);
        //TODO Can use InputMultiplexer  to add this back
        //Gdx.input.setInputProcessor(camController);
        
        modelBatch = new ModelBatch();
        
        blending = new BlendingAttribute(1.0f);
        //Make models transparent in debug mode
        if (debugMode) {
            blending.opacity = 0.45f;
        } 
        
        // Create some basic models
        groundModel = modelBuilder.createRect(
                4*universeDepth, 0f, -universeDepth, 
                -8*universeDepth, 0f, -universeDepth, 
                -8*universeDepth, 0f, universeDepth, 
                4*universeDepth, 0f, universeDepth, 
                0, 1, 0, 
                new Material(ColorAttribute.createDiffuse(Color.GREEN), ColorAttribute.createSpecular(Color.WHITE), FloatAttribute.createShininess(16f), blending),
                Usage.Position | Usage.Normal);
        
        
        //models.add(groundModel);
        
        shipModel = modelBuilder.createBox(shipWidth, shipHeight, shipDepth,
                new Material(ColorAttribute.createDiffuse(Color.RED), blending), Usage.Position | Usage.Normal);
        //models.add(shipModel);   
        //models.add(buildingModel);        
        
        broadphase = new btDbvtBroadphase();
        // Set up the collision configuration and dispatcher
        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        
        // The actual physics solver
        solver = new btSequentialImpulseConstraintSolver();
        
        // The world.
        collisionWorld = new btDiscreteDynamicsWorld(dispatcher,broadphase,solver,collisionConfiguration);
        collisionWorld.setGravity(gravity);
        if (debugMode) {
            collisionWorld.setDebugDrawer(debugDrawer = new DebugDrawerCustom());
            debugDrawer.lineRenderer.setProjectionMatrix(cam.combined);
            debugDrawer.setDebugMode(DebugDrawModes.DBG_DrawWireframe);
        }
        
        btCollisionShape groundShape = new btBoxShape(new Vector3(universeDepth*8, 0, universeDepth));
        shapes.add(groundShape);
        //Si le seteo masa se rompe todo (cambia de altura la nave, no se porque)
        btRigidBodyConstructionInfo groundInfo = new btRigidBodyConstructionInfo(0.0f, null, groundShape, Vector3.Zero);
        //FOR BOUNCING
        //groundInfo.setRestitution(1.0f);
        bodyInfos.add(groundInfo);
        
        btCollisionShape shipShape = new btBoxShape(new Vector3(shipWidth/2, shipHeight/2, shipDepth/2));
        shapes.add(shipShape);
        btRigidBodyConstructionInfo shipInfo = new btRigidBodyConstructionInfo(1.0f, null, shipShape, Vector3.Zero);
        bodyInfos.add(shipInfo);   
        
        // Create the ground
        ground = new ModelInstance(groundModel);
        instances.add(ground);        
        btDefaultMotionState groundMotionState = new btDefaultMotionState();
        groundMotionState.setWorldTransform(ground.transform);
        motionStates.add(groundMotionState);
        btRigidBody groundBody = new btRigidBody(groundInfo);
        this.groundBody = groundBody;
        groundBody.setUserValue(0);
        groundBody.setMotionState(groundMotionState);
        //Disable Deactivation
        groundBody.setActivationState(4);
        bodies.add(groundBody);
        collisionWorld.addRigidBody(groundBody);    
        
//        ObjLoader loader = new ObjLoader();
//        Model model = loader.loadModel(Gdx.files.internal("ship.obj")); 
       
        ship = new ModelInstance(shipModel);
        instances.add(ship);
//        ship.transform.scl(3.0f);
//        ship.transform.rotate(new Vector3(0, 1, 0), -90);
//        ship.transform.translate(0, 10 , 0);
//        ship.calculateTransforms();
        ship.transform.trn(shipStartX, (shipHeight/2)+ shipStartY , 0);
        btDefaultMotionState shipMotionState = new btDefaultMotionState();
        shipMotionState.setWorldTransform(ship.transform);
        motionStates.add(shipMotionState);
        btRigidBody shipBody = new btRigidBody(shipInfo);
        this.shipBody = shipBody;
        shipBody.setUserValue(1);
        shipBody.setMotionState(shipMotionState);
        //Disable Deactivation
        shipBody.setActivationState(4);
        bodies.add(shipBody);
        collisionWorld.addRigidBody(shipBody);    

        spawnBuilding();
    }
    
    private Vector3 getPosition (btRigidBody body) {
        Matrix4 trans = new Matrix4();
        body.getMotionState().getWorldTransform(trans);
        Vector3 position = new Vector3();
        trans.getTranslation(position);
        return position;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        
        if (collision) {
            game.batch.begin();
            game.font.draw(game.batch, "Chocaste!!! Toca la pantalla para empezar de nuevo", 10, 470);
            game.batch.end();
            if (Gdx.input.isTouched()) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        }
        
        float accelX = Gdx.input.getAccelerometerX();
        float accelY = Gdx.input.getAccelerometerY();
        float accelZ = Gdx.input.getAccelerometerZ();
        
        //Simple control 
        if (useAccelerometer) {
            if (accelY > 2.0) {
                moveRight = true;
                moveLeft = false;
            } else if (accelY < -2.0) {
                moveRight = false;
                moveLeft = true;
            } else {
                moveLeft = false;
                moveRight = false;
            }
            
            if (accelX > 8.0) {
                moveUp = true;
                moveDown = false;
            } else if (accelX < 5.0 && accelX > 0) {
                moveUp = false;
                moveDown = true;
            } else {
                moveUp = false;
                moveDown = false;
            }        
        }
 //       System.out.println("X: " + accelX + ", Y: " +accelY + " Z: " + accelZ);

        //***** START MOVE CONTROL*****//
        if (!collision) {
            float deltaMoveZ = movementSpeed * Gdx.graphics.getDeltaTime();
            float deltaMoveY = movementSpeed * Gdx.graphics.getDeltaTime();
            if (moveLeft) {
                cam.position.z += deltaMoveZ;
                float currentZ = getPosition(shipBody).z;
                float maxZ = universeDepth - shipDepth/2; 
                if (currentZ + deltaMoveZ < maxZ) {
                    shipBody.translate(new Vector3(0,0,deltaMoveZ));
                } else {
                    shipBody.translate(new Vector3(0,0,maxZ-currentZ));
                }
            }
            if (moveRight) {
                cam.position.z -= deltaMoveZ;
                float currentZ = getPosition(shipBody).z;
                float minZ = -(universeDepth - shipDepth/2); 
                if (currentZ - deltaMoveZ > minZ) {
                    shipBody.translate(new Vector3(0,0,-deltaMoveZ));
                } else {
                    shipBody.translate(new Vector3(0,0, minZ+(-currentZ)));
                }
            }  
            if (moveUp) {
                cam.position.y += deltaMoveY;
                float currentY = getPosition(shipBody).y;
                float maxY = universeHeight - shipHeight/2;
                if (currentY + deltaMoveY < maxY) {
                    shipBody.translate(new Vector3(0,deltaMoveY,0));
                } else {
                    shipBody.translate(new Vector3(0,maxY-currentY,0));
                }                
                
            }
            if (moveDown) {
                cam.position.y -= deltaMoveY;
                shipBody.translate(new Vector3(0,-deltaMoveY,0));
                //NO NEED TO CORRECT DOWN (Ship already crashed)
            }    
            
            //***** START CAMERA CORRECTION *****//
            if(cam.position.z < -(universeDepth - shipDepth/2)) {
                cam.position.z = -(universeDepth - shipDepth/2);
            }
            
            if(cam.position.z > (universeDepth - shipDepth/2)) {
                cam.position.z = (universeDepth - shipDepth/2);
            }
            
            if(cam.position.y < camHeight) {
                cam.position.y = camHeight;
            }
            
            if(cam.position.y > (universeHeight + camHeight - shipHeight)) {
                cam.position.y = (universeHeight  + camHeight - shipHeight);
            }
            //***** END CAMERA CORRECTION *****//
        }
        //***** END MOVE CONTROL*****//

        //camController.update();

        if (!collision) {
            deltaMoveX = (float)(speed * Gdx.graphics.getDeltaTime());
        } 
        
        //Update Camera Position
        cam.position.x -= deltaMoveX;
        cam.update();
        if (debugMode) {
            debugDrawer.lineRenderer.setProjectionMatrix(cam.combined);
        }
        
        //Move Ground & Ship
        shipBody.translate(new Vector3(-deltaMoveX, 0, 0));
        groundBody.translate(new Vector3(-deltaMoveX, 0, 0));
        //WORKAROUND: Ground won't move if it has no mass, so We have to move the Model instance
        ground.transform.translate(-deltaMoveX, 0, 0);
        
        //Physics
        ((btDynamicsWorld)collisionWorld).stepSimulation(Gdx.graphics.getDeltaTime(), 55);
        if (debugMode) {
            debugDrawer.begin();
            collisionWorld.debugDrawWorld();
            debugDrawer.end();
        }
        //This loop updates Models based on Physics Bodies' motionState
        //DO NOT include Ground (index 0), as it is moved on it's own (workaround for fail to move due to no mass)
        
       
        int c = motionStates.size;
        //System.out.println("*********ARRAY SIZE: "+  c + " **********" );  
        for (int i = 1; i < c; i++) {
            motionStates.get(i).getWorldTransform(instances.get(i).transform);
        }       
        
        for (int i = 2; i < c; i++) {
            Matrix4 trans = new Matrix4();
            bodies.get(i).getMotionState().getWorldTransform(trans);
            Vector3 position = new Vector3();
            trans.getTranslation(position);
            if (position.y < 0) {
                //System.out.println("Body #"+i +" Y: " + position.y + " ***");  
            }
        }           
            
        modelBatch.begin(cam);
            //Spawn new Buildings if needed
            if (Math.abs(cam.position.x - lastCameraX) > buildingXFactor) {
                lastCameraX = cam.position.x;
                if (instances.size > buildingSpawnNumber *2 + 2) {
                    purgeBuildings();
                }
                spawnBuilding();
            }
            modelBatch.render(instances, environment);
        modelBatch.end();
//      Matrix4 trans = new Matrix4();
//      bodies.get(2).getMotionState().getWorldTransform(trans);
//      Vector3 position = new Vector3();
//      trans.getTranslation(position);
//      System.out.println(position.x);        
    }

    private void purgeBuildings() {
        for (int i = 2; i<buildingSpawnNumber + 2; i++) {
            collisionWorld.removeRigidBody(bodies.get(i));
            bodies.get(i).setMotionState(null);
            bodyInfos.get(i).setCollisionShape(null);
            //motionStates.get(i).setWorldTransform(null);
            //bodies.get(i).setActivationState(5);
            //shapes.get(i).dispose();
            shapes.removeIndex(i);  
            motionStates.removeIndex(i);  
            bodyInfos.removeIndex(i); 
            bodies.removeIndex(i);
            instances.removeIndex(i);
            //motionStates.get(i).dispose();
            //motionStates.get(i).setWorldTransform(null);
        }
    }

    private void spawnBuilding() {
        for (int i = 0; i < buildingSpawnNumber; i++) {
            float randomX = MathUtils.random(lastCameraX-cameraXDistance-buildingXFactor, lastCameraX-cameraXDistance-(2*buildingXFactor));
            float randomZ = MathUtils.random(-(universeDepth-buildingDepth/2), (universeDepth-buildingDepth/2));
            float randomHeightScale = MathUtils.random(0.1f, 1f);
            buildingModel = modelBuilder.createBox(buildingWidth, buildingMaxHeight*randomHeightScale, buildingDepth,
                    new Material(ColorAttribute.createDiffuse(Color.BLUE), blending), Usage.Position | Usage.Normal);
            ModelInstance building = new ModelInstance(buildingModel);
            instances.add(building);
            //building.transform.scale(1f, randomHeightScale, 1f);
            //building.transform.translate(randomX, buildingMaxHeight/2 , randomZ);
            building.transform.trn(randomX, 0 , randomZ);
            //building.calculateTransforms();
            
            btCollisionShape buildingShape = new btBoxShape(new Vector3(buildingWidth/2, (randomHeightScale * buildingMaxHeight)/2, buildingDepth/2));
            shapes.add(buildingShape);
            btRigidBodyConstructionInfo buildingInfo = new btRigidBodyConstructionInfo(1.0f, null, buildingShape, Vector3.Zero);
            //buildingInfo.setRestitution(1.0f);
            bodyInfos.add(buildingInfo);              
            btDefaultMotionState buildingMotionState = new btDefaultMotionState();
            buildingMotionState.setWorldTransform(building.transform);
            motionStates.add(buildingMotionState);
            btRigidBody buildingBody = new btRigidBody(buildingInfo);
            buildingBody.setMotionState(buildingMotionState);
            bodies.add(buildingBody);
            collisionWorld.addRigidBody(buildingBody);          
        }
     }
    
    @Override
    public void dispose() {
        music.dispose();
//        collisionWorld.dispose();
//        solver.dispose();
//        broadphase.dispose();
//        dispatcher.dispose();
//        collisionConfiguration.dispose();
//        
//        for (btRigidBody body : bodies)
//            body.dispose();
//        bodies.clear();
//        for (btDefaultMotionState motionState : motionStates)
//            motionState.dispose();
//        motionStates.clear();
//        for (btCollisionShape shape : shapes)
//            shape.dispose();
//        shapes.clear();
//        for (btRigidBodyConstructionInfo info : bodyInfos)
//            info.dispose();
//        bodyInfos.clear();
//        
//        modelBatch.dispose();
//        instances.clear();
    }

    @Override
    public void resume() {
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }



    @Override
    public void show() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub
        
    }
}
