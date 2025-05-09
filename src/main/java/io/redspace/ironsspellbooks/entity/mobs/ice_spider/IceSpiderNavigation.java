package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.NotIdioticNavigation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class IceSpiderNavigation extends NotIdioticNavigation {

    public IceSpiderNavigation(Mob pMob, Level pLevel) {
        super(pMob, pLevel);
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
//    @Override
//    protected void trimPath() {
//        if (path == null || path.getNextNodeIndex() >= path.nodes.size()) {
//            return;
//        }
//
//        try {
//            var baseY = mob.getY();
//            var maxStepUp = mob.maxUpStep();
//            var finalNode = path.getEndNode().asVec3();
//            // the idea is that, since we can climb, we want to take a direct path that the node evaluator would normally avoid due to verticality
//            // has line of sight to target node
//            if (level.clip(new ClipContext(mob.position().add(0, 0.75, 0), finalNode.add(0, 0.75, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).getType() == HitResult.Type.MISS) {
//                Vec3 nearestLocalMaximum = null;
//                int lastImportantIndex = -1;
//                for (int i = path.getNextNodeIndex(); i < path.nodes.size(); i++) {
//                    var node = path.getNode(i).asVec3();
//                    if (node.y - baseY > maxStepUp) {
//                        if (nearestLocalMaximum == null) {
//                            nearestLocalMaximum = node;
//                        } else {
//                            Vec3 delta = node.subtract(nearestLocalMaximum);
//                            if (Math.abs(delta.dot(new Vec3(0, 1, 0))) > .75) {
//                                // this node has a highly correlated upward trajectory from the last "important" node, thus we consider it for the peak
//                                //validate whether skipping to this node is traversable
//                                Vec3 pos = mob.position();
//                                Vec3 horizontalTrajectory = Utils.moveToRelativeGroundLevel(mob.level, node.subtract(pos).multiply(1, 0, 1).add(pos), 2);
//                                Vec3 intersection = level.clip(new ClipContext(pos.add(0, 0.75, 0), horizontalTrajectory, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).getLocation();
//                                // is base of cliff close horiztonally to top of cliff
//                                if (node.subtract(intersection).horizontalDistanceSqr() < 2 * 2 * mob.getScale() * mob.getScale()) {
//                                    // is base of cliff approachable
//                                    if (canMoveDirectly(pos, horizontalTrajectory)) {
//                                        nearestLocalMaximum = node;
//                                        lastImportantIndex = i;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                if (lastImportantIndex > 0) {
//                    ArrayList<Node> toRemove = new ArrayList<>();
//                    for (int i = 0; i < lastImportantIndex; i++) {
//                        toRemove.add(path.getNode(i));
//                    }
//                    path.nodes.removeAll(toRemove);
//                    if (debugparticles) {
//                        for (Node node : toRemove) {
//                            MagicManager.spawnParticles(level, ParticleHelper.FIRE_EMITTER, node.x, node.y + .1, node.z, 2, 0, 0, 0, 0.0, true);
//                        }
//                    }
//                    return; // stop other trimming
//                }
//            }
//        } catch (Exception e) {
//            IronsSpellbooks.LOGGER.error(e.getMessage());
//            //cancel navigation
//            this.path = null;
//        }
//        super.trimPath();
//    }
}
