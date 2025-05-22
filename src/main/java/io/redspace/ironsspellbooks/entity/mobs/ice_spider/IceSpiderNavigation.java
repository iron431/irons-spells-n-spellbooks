package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.NotIdioticNavigation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class IceSpiderNavigation extends NotIdioticNavigation {

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
}
