package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;

import com.mojang.math.Vector3f;
import software.bernie.geckolib3.core.processor.IBone;

import java.util.HashMap;
import java.util.Map;

public class TransformStack {
    private final Map<IBone, Vector3f> positionStack = new HashMap<>();
    private final Map<IBone, Vector3f> rotationStack = new HashMap<>();

    public void pushPosition(IBone bone, Vector3f appendVec) {
        var vec = positionStack.getOrDefault(bone, new Vector3f(0, 0, 0));
        vec.add(appendVec);
        positionStack.put(bone, vec);
    }

    public void pushPosition(IBone bone, float x, float y, float z) {
        pushPosition(bone, new Vector3f(x, y, z));
    }

    public void overridePosition(IBone bone, Vector3f newVec) {
        positionStack.put(bone, newVec);
    }

    public void pushRotation(IBone bone, Vector3f appendVec) {
        var vec = rotationStack.getOrDefault(bone, new Vector3f(0, 0, 0));
        vec.add(appendVec);
        rotationStack.put(bone, vec);
    }

    public void pushRotation(IBone bone, float x, float y, float z) {
        pushRotation(bone, new Vector3f(x, y, z));
    }

    public void pushRotationWithBase(IBone bone, float x, float y, float z) {
        var base = new Vector3f(bone.getRotationX(), bone.getRotationY(), bone.getRotationZ());
        base.add(x, y, z);
        pushRotation(bone, base);
    }

    public void overrideRotation(IBone bone, Vector3f newVec) {
        rotationStack.put(bone, newVec);
    }

    public void popStack() {
        positionStack.forEach(this::setPosImpl);
        rotationStack.forEach(this::setRotImpl);
        positionStack.clear();
        rotationStack.clear();
    }

    public void setRotImpl(IBone bone, Vector3f vector3f) {
        bone.setRotationX(wrapRadians(vector3f.x()));
        bone.setRotationY(wrapRadians(vector3f.y()));
        bone.setRotationZ(wrapRadians(vector3f.z()));
    }

    public void setPosImpl(IBone bone, Vector3f vector3f) {
        bone.setPositionX(vector3f.x());
        bone.setPositionY(vector3f.y());
        bone.setPositionZ(vector3f.z());
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
