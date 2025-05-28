package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class IceSpiderNavigation extends GroundPathNavigation {

    public IceSpiderNavigation(Mob pMob, Level pLevel) {
        super(pMob, pLevel);
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    protected void trimPath() {
        if (path == null || path.getNextNodeIndex() >= path.nodes.size()) {
            return;
        }

        try {
            var baseY = mob.getY();
            var maxStepUp = mob.maxUpStep();
            var finalNode = path.getEndNode().asVec3();
            // if the target position has a next elevation change greater than our step-up abilities, attempt to shortcut the path to utilize our climbing abilities
            // do this by finding the most direct node
            if (finalNode.y - baseY > maxStepUp) {
                Vec3 directionVector = finalNode.subtract(mob.position());
                for (int i = path.getNextNodeIndex(); i < path.nodes.size(); i++) {
                    var node = path.getNode(i).asVec3();
                    if (finalNode.subtract(node).dot(directionVector) > 0.8
                            && isTraversable(node, finalNode)) {
                        var inbetweenNodes = new ArrayList<Node>();
                        for (int j = i + 1; j < path.nodes.size() - 1; j++) {
                            inbetweenNodes.add(path.nodes.get(j));
                        }
                        path.nodes.removeAll(inbetweenNodes);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.error(e.getMessage());
            //cancel navigation
            this.path = null;
        }
        super.trimPath();
    }
//fixme: this is duplicated from notidioticnav
    protected boolean isTraversable(Vec3 pos1, Vec3 pos2) {
        Vec3 step = pos2.subtract(pos1);
        double distance = step.length();
        step = step.scale(1 / distance); // normalize
        for (int i = 0; i < distance; i++) {
            BlockPos currentPos = BlockPos.containing(pos1.add(step.scale(i)));
            if (mob.getType().isBlockDangerous(level.getBlockState(currentPos))) {
                return false; // block is dangerous to traverse through (ie fire)
            } else if (!level.getBlockState(currentPos.below()).isFaceSturdy(level, currentPos.below(), Direction.UP)) {
                return false; // block does not support upwards (ie is pitfall or no collision)
            }
        }
        return true;
    }
}
