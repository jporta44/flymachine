package com.jap.flymachine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class FlyGameScreen implements Screen {
    
    final Fly game;
    boolean debugMode = false;
    boolean useAccelerometer = false;
    boolean freeFlightEnabled = false;
    
    Music music;
    public PerspectiveCamera cam;
    public Model buildingModel;
    private Model groundModel;
    private Model shipModel;
    private FlyModelInstance ground;
    public FlyModelInstance ship;
    
    public ModelBatch modelBatch;
    public Environment environment;
    public CameraInputController camController;
    ModelBuilder modelBuilder = new ModelBuilder();
    BlendingAttribute blending;
    
    public SpriteBatch batch;
    public BitmapFont font;
    ShapeRenderer sr = new ShapeRenderer();
    
    //Vector3 gravity = new Vector3(0, -9.8f, 0);
    Vector3 gravity = new Vector3(0, 0.0f, 0);
    Vector3 tempV1 = new Vector3();
    Vector3 tempV2 = new Vector3();
    
    public Array<FlyModelInstance> instances = new Array<FlyModelInstance>();
    //Array<Model> models = new Array<Model>();
    //private MyContactListener contactCB;  

    int speed = 25;
    int movementSpeed = 50;
    int buildingSpawnNumber = 100;
    
    float shipModelScale = 2.0f;
    float shipWidth = 7.5f;
    float shipHeight = 2.5f;
    float shipDepth = 5f;
    float shipStartX = 30f;
    float shipStartY = 0.1f;
    
    float buildingWidth = 5f;
    float buildingDepth = 5f;
    float buildingMaxHeight = 30f;
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
                case Keys.R:
                    game.setScreen(new MainMenuScreen(game));
                    dispose();
                case Keys.D:
                    if (debugMode) {
                        debugMode = false;
                    } else {
                        debugMode = true;
                    }
                    break;  
                case Keys.F:
                    if (freeFlightEnabled) {
                        freeFlightEnabled = false;
                    } else {
                        freeFlightEnabled = true;
                    }
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
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setScale(2.0f);
        Gdx.graphics.setContinuousRendering(true);

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
        
        Texture buildingTexture = new Texture(Gdx.files.internal("building.png"), Format.RGB888, false);
        
        buildingModel = modelBuilder.createBox(buildingWidth, buildingMaxHeight, buildingDepth,
                new Material(blending, TextureAttribute.createDiffuse(buildingTexture)), Usage.Position | Usage.Normal);        
        
        //models.add(groundModel);
        
        shipModel = modelBuilder.createBox(shipWidth, shipHeight, shipDepth,
                new Material(ColorAttribute.createDiffuse(Color.RED), blending), Usage.Position | Usage.Normal);
        //models.add(shipModel);   
        //models.add(buildingModel);        
  
        
        // Create the ground
        ground = new FlyModelInstance(groundModel, new Vector3(1.0f, 1.0f, 1.0f));
        instances.add(ground);        
        
        ObjLoader loader = new ObjLoader();
        Model model = loader.loadModel(Gdx.files.internal("ship.obj")); 

       
        ship = new FlyModelInstance(model, new Vector3(shipModelScale, shipModelScale, shipModelScale));

        //ship = new FlyModelInstance(shipModel);
        instances.add(ship);
        ship.transform.rotate(Vector3.Y, -90).scale(shipModelScale, shipModelScale, shipModelScale);
        ship.calculateTransforms();
        //update Ship dimension Values
        BoundingBox box = new BoundingBox();
        ship.calculateBoundingBox(box);
        shipWidth = Math.abs(box.min.x - box.max.x)* shipModelScale; 
        shipDepth = Math.abs(box.min.z - box.max.z) * shipModelScale; 
        shipHeight = Math.abs(box.min.y - box.max.y) * shipModelScale; 
        
        ship.transform.trn(shipStartX, (shipHeight/2)+ shipStartY , 0);
        ship.updateBounds();
        spawnBuilding();
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        checkCollision();
        if (collision) {
            batch.begin();
            font.draw(batch, "Chocaste!!! Toca la pantalla para empezar de nuevo", 10, Gdx.graphics.getHeight() - 10);
            batch.end();
            if (Gdx.input.isTouched()) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }            
        } else {
            batch.begin();
            font.draw(batch, "Current Speed: " + speed, 10, Gdx.graphics.getHeight() - 10);
            batch.end();
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
            ship.transform.getTranslation(tempV1);
            if (moveLeft) {
                cam.position.z += deltaMoveZ;
                float currentZ = tempV1.z;
                float maxZ = universeDepth - shipDepth/2; 
                if (currentZ + deltaMoveZ < maxZ || freeFlightEnabled) {
                    ship.transform.trn(0,0,deltaMoveZ);
                } else {
                    ship.transform.trn(0,0,maxZ-currentZ);
                }
            }
            if (moveRight) {
                cam.position.z -= deltaMoveZ;
                float currentZ = tempV1.z;
                float minZ = -(universeDepth - shipDepth/2); 
                if (currentZ - deltaMoveZ > minZ || freeFlightEnabled) {
                    ship.transform.trn(0,0,-deltaMoveZ);
                } else {
                    ship.transform.trn(0,0, minZ+(-currentZ));
                }
            }  
            if (moveUp) {
                cam.position.y += deltaMoveY;
                float currentY = tempV1.y;
                float maxY = universeHeight - shipHeight/2;
                if (currentY + deltaMoveY < maxY || freeFlightEnabled) {
                    ship.transform.trn(0,deltaMoveY,0);
                } else {
                    ship.transform.trn(0,maxY-currentY,0);
                }                
                
            }
            if (moveDown) {
                cam.position.y -= deltaMoveY;
                float currentY = tempV1.y;
                float minY = shipHeight/2;
                if (currentY - deltaMoveY > minY || freeFlightEnabled) {
                    ship.transform.trn(0,-deltaMoveY,0);
                } else {
                    ship.transform.trn(0,minY+(-currentY),0);
                }                  
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

        
        //Move Ground & Ship
        ship.transform.trn(-deltaMoveX,0,0);
        ship.updateBounds();
        ground.transform.trn(-deltaMoveX,0,0);
        
        //This loop updates Models based on Physics Bodies' motionState
        //DO NOT include Ground (index 0), as it is moved on it's own (workaround for fail to move due to no mass)
        
         
            
        modelBatch.begin(cam);
            //Spawn new Buildings if needed
            if (Math.abs(cam.position.x - lastCameraX) > buildingXFactor) {
                lastCameraX = cam.position.x;
                if (instances.size > buildingSpawnNumber * 3 + 2) {
                    purgeBuildings();
                }
                spawnBuilding();
            }
            modelBatch.render(instances, environment);
        modelBatch.end();
        if (debugMode) {
            float width, height, depth, startX, startY, startZ;
            sr.setProjectionMatrix(cam.combined);
            sr.begin(ShapeType.Line);
            sr.setColor(Color.RED);
            for (int i = 1; i < instances.size; i++) {
                BoundingBox box = instances.get(i).bounds;
                Vector3 temp1 = box.min;
                Vector3 temp2 = box.max;
                width = temp2.x - temp1.x;
                height = temp2.y - temp1.y;
                depth = temp2.z - temp1.z;
                startX = temp1.x;
                startY = temp1.y;
                startZ = temp1.z+depth;
                sr.box(startX, startY, startZ,width,height,depth);
            }
            
            sr.end();
            
        }
//      Matrix4 trans = new Matrix4();
//      bodies.get(2).getMotionState().getWorldTransform(trans);
//      Vector3 position = new Vector3();
//      trans.getTranslation(position);
//      System.out.println(position.x);        
    }

    private void checkCollision() {
        for (int i =2; i<instances.size; i++) {
            BoundingBox shipBox = ship.getBounds();
            BoundingBox instanceBox = instances.get(i).getBounds();
            if (shipBox.intersects(instanceBox)) {
                collision = true;
                //Stop moving
                deltaMoveX = 0;
                //Stop music
                music.stop();
                Gdx.graphics.setContinuousRendering(false);
                // TODO Play CRASH ANIMATION
                Gdx.graphics.requestRendering();                
                return;
            }
        }
    }

    private void purgeBuildings() {
        for (int i = 2; i<buildingSpawnNumber + 2; i++) {
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
            FlyModelInstance building = new FlyModelInstance(buildingModel, new Vector3(1.0f, randomHeightScale, 1.0f));
            instances.add(building);
            building.transform.scale(1f, randomHeightScale, 1f);
            building.transform.trn(randomX, buildingMaxHeight/2 *  randomHeightScale, randomZ);
            building.updateBounds();
        }
     }
    
    @Override
    public void dispose() {
//        music.dispose();
//        batch.dispose();
//        font.dispose();
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
