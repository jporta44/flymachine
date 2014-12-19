package com.jap.flymachine;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class FlyModelInstance extends ModelInstance {
    
    Vector3 scale;
    BoundingBox originalBounds = new BoundingBox();
    BoundingBox bounds = new BoundingBox();

    public FlyModelInstance(Model model, Vector3 scale) {
        super(model);
        this.scale = scale;
        this.calculateBoundingBox(originalBounds);
        originalBounds.getMin().scl(scale);
        originalBounds.getMax().scl(scale);
    }
    
    public void updateBounds () {
        Vector3 minPosition = new Vector3();
        Vector3 maxPosition = new Vector3();
        this.transform.getTranslation(minPosition);
        this.transform.getTranslation(maxPosition);
        bounds.set(minPosition.add(originalBounds.getMin()), maxPosition.add(originalBounds.getMax()));
    }
    
    public BoundingBox getBounds() {
        return bounds;
    }

}
