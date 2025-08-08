package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;


import net.minecraft.util.Mth;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;

import java.util.*;

public class TransformStack {
    private final Map<CoreGeoBone, Stack<Vector3f>> positionStack = new HashMap<>();
    private final Map<CoreGeoBone, Stack<Vector3f>> rotationStack = new HashMap<>();
    private final Set<CoreGeoBone> toReset = new HashSet<>();

    public void pushPosition(CoreGeoBone bone, Vector3f appendVec) {
        var stack = positionStack.getOrDefault(bone, new Stack<>());
        stack.push(appendVec);
        positionStack.put(bone, stack);
    }

    public void resetDirty() {
        toReset.forEach(bone -> {
            var snapshot = bone.getInitialSnapshot();
            bone.updatePosition(snapshot.getOffsetX(), snapshot.getOffsetY(), snapshot.getOffsetZ());
            bone.updateRotation(snapshot.getRotX(), snapshot.getRotY(), snapshot.getRotZ());
            bone.resetStateChanges();
        });
        toReset.clear();
    }

    public void pushPosition(CoreGeoBone bone, float x, float y, float z) {
        pushPosition(bone, new Vector3f(x, y, z));
    }

    public void pushRotation(CoreGeoBone bone, Vector3f appendVec) {
        var stack = rotationStack.getOrDefault(bone, new Stack<>());
        stack.push(appendVec);
        rotationStack.put(bone, stack);
    }

    public void pushRotation(CoreGeoBone bone, float x, float y, float z) {
        pushRotation(bone, new Vector3f(x, y, z));
    }

    public void pushRotationDegrees(CoreGeoBone bone, float x, float y, float z) {
        pushRotation(bone, new Vector3f(x * Mth.DEG_TO_RAD, y * Mth.DEG_TO_RAD, z * Mth.DEG_TO_RAD));
    }

    public void popStack() {
        positionStack.forEach((bone, stack) -> {
            toReset.add(bone);
            Vector3f position = new Vector3f(bone.getPosX(), bone.getPosY(), bone.getPosZ());
            stack.forEach(position::add);
            setPosImpl(bone, position);
        });
        rotationStack.forEach((bone, stack) -> {
            toReset.add(bone);
            Vector3f rotation = new Vector3f(bone.getRotX(), bone.getRotY(), bone.getRotZ());
            stack.forEach(rotation::add);
            setRotImpl(bone, rotation);
        });
        positionStack.clear();
        rotationStack.clear();
    }

    public void setRotImpl(CoreGeoBone bone, Vector3f vector3f) {
        bone.updateRotation(
                wrapRadians(vector3f.x()),
                wrapRadians(vector3f.y()),
                wrapRadians(vector3f.z()));
    }

    public void setPosImpl(CoreGeoBone bone, Vector3f vector3f) {
        bone.updatePosition(vector3f.x, vector3f.y, vector3f.z);
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
