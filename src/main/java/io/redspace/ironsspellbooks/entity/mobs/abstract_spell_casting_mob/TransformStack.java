package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;


import org.joml.Vector3f;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;

import java.util.HashMap;
import java.util.Map;

public class TransformStack {
    private final Map<CoreGeoBone, Vector3f> positionStack = new HashMap<>();
    private final Map<CoreGeoBone, Vector3f> rotationStack = new HashMap<>();
    private boolean needsReset;

    public void pushPosition(CoreGeoBone bone, Vector3f appendVec) {
        var vec = positionStack.getOrDefault(bone, new Vector3f(0, 0, 0));
        vec.add(appendVec);
        positionStack.put(bone, vec);
    }

    public void pushPosition(CoreGeoBone bone, float x, float y, float z) {
        pushPosition(bone, new Vector3f(x, y, z));
    }

    public void overridePosition(CoreGeoBone bone, Vector3f newVec) {
        positionStack.put(bone, newVec);
    }

    public void pushRotation(CoreGeoBone bone, Vector3f appendVec) {
        var vec = rotationStack.getOrDefault(bone, new Vector3f(0, 0, 0));
        vec.add(appendVec);
        rotationStack.put(bone, vec);
    }

    public void pushRotation(CoreGeoBone bone, float x, float y, float z) {
        pushRotation(bone, new Vector3f(x, y, z));
    }

    public void pushRotationWithBase(CoreGeoBone bone, float x, float y, float z) {
        var base = new Vector3f(bone.getRotX(), bone.getRotY(), bone.getRotZ());
        base.add(x, y, z);
        // fixme: seems like 1.20 works differently with this
        pushRotation(bone, x, y, z);
    }

    public void overrideRotation(CoreGeoBone bone, Vector3f newVec) {
        rotationStack.put(bone, newVec);
    }

    public void popStack() {
        positionStack.forEach(this::setPosImpl);
        rotationStack.forEach(this::setRotImpl);
        positionStack.clear();
        rotationStack.clear();
    }

    public void setRotImpl(CoreGeoBone bone, Vector3f vector3f) {
        bone.setRotX(wrapRadians(vector3f.x()));
        bone.setRotY(wrapRadians(vector3f.y()));
        bone.setRotZ(wrapRadians(vector3f.z()));
    }

    public void setPosImpl(CoreGeoBone bone, Vector3f vector3f) {
        bone.setPosX(vector3f.x());
        bone.setPosY(vector3f.y());
        bone.setPosZ(vector3f.z());
    }

    public static float wrapRadians(float pValue) {
        float twoPi = 6.2831f;
        float pi = 3.14155f;
        float f = pValue % twoPi;
        if (f >= pi) {
            f -= twoPi;
        }

        if (f < -pi) {
            f += twoPi;
        }

        return f;
    }
}
