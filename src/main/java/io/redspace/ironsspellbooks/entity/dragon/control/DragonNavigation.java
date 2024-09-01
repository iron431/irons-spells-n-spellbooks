package io.redspace.ironsspellbooks.entity.dragon.control;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class DragonNavigation extends GroundPathNavigation {
    public DragonNavigation(Mob pMob, Level pLevel) {
        super(pMob, pLevel);
    }

    int lastPathEvaluatedIndex;

    @Override
    protected void trimPath() {
        super.trimPath();
        //try to optimize out excessive direction jitter by eliminating close-angled nodes
        ArrayList<Node> dumbNodes = new ArrayList<Node>();
        var lastImportantNode = path.getNode(path.getNextNodeIndex()).asVec3();
        for (int i = path.getNextNodeIndex() + 2; i < path.nodes.size(); i++) {
            var node1 = path.getNode(i - 1).asVec3();
            var node2 = path.getNode(i).asVec3();
            var delta1 = node1.subtract(lastImportantNode).normalize();
            var delta2 = node2.subtract(node1).normalize();
            if (delta1.dot(delta2) > .75) {
                dumbNodes.add(path.getNode(i - 1));
            } else {
                lastImportantNode = delta1;
            }
        }
        path.nodes.removeAll(dumbNodes);
    }

    @Override
    public void tick() {
        this.tick++;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }

        if (!this.isDone()) {
            if (this.canUpdatePath()) {
                this.followThePath();
            } else if (this.path != null && !this.path.isDone()) {
                Vec3 vec3 = this.getTempMobPos();
                Vec3 vec31 = this.path.getNextEntityPos(this.mob);
                if (vec3.y > vec31.y && !this.mob.onGround() && Mth.floor(vec3.x) == Mth.floor(vec31.x) && Mth.floor(vec3.z) == Mth.floor(vec31.z)) {
                    this.path.advance();
                }
            }

            DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
            if (!this.isDone()) {
                Vec3 veryNextPos = this.path.getNextEntityPos(this.mob);
                Vec3 betterPos = /*this.path.getEndNode().asVec3();*/veryNextPos;
                int posCount = 1;
                //Try to smooth path by averaging relatively similar upcoming nodes
                //Find farthest node within a margin of angle to move towards
                if (this.path.getNextNodeIndex() != lastPathEvaluatedIndex) {
                    lastPathEvaluatedIndex = this.path.getNextNodeIndex();
                    var path = this.getPath();
                    var nodes = path.nodes;
                    Vec3 trajectory = veryNextPos.subtract(mob.position()).normalize();
                    IronsSpellbooks.LOGGER.debug("DragonNavigation.tick.onNodeChanged: trajectory: {}", trajectory);
                    for (int i = path.getNextNodeIndex() + 1; i < nodes.size(); i++) {
                        var node = path.nodes.get(i);
                        Vec3 furtherNode = node.asVec3();
                        Vec3 delta = furtherNode.subtract(veryNextPos);
                        IronsSpellbooks.LOGGER.debug("i: {}\t delta: {}\t dot: {}", i, delta, delta.normalize().dot(trajectory));
                        if (delta.normalize().dot(trajectory) >= .25) {
                            betterPos = betterPos.add(furtherNode);
                            posCount++;
                        } else {
                            break;
                        }
                    }
                    betterPos = betterPos.scale(1f / posCount);
                    IronsSpellbooks.LOGGER.debug("default wanted pos: {} new wanted pos: {}", veryNextPos, betterPos);

                    IronsSpellbooks.LOGGER.debug("default rot: {}", (float) (Mth.atan2(veryNextPos.z - mob.getZ(), veryNextPos.x - mob.getX()) * 180.0F / (float) Math.PI) - 90.0F);
                    IronsSpellbooks.LOGGER.debug("new rot: {}", (float) (Mth.atan2(betterPos.z - mob.getZ(), betterPos.x - mob.getX()) * 180.0F / (float) Math.PI) - 90.0F);

                }

                this.mob.setYRot((float) (Mth.atan2(betterPos.z - mob.getZ(), betterPos.x - mob.getX()) * 180.0F / (float) Math.PI) - 90.0F);
                this.mob.getMoveControl().setWantedPosition(betterPos.x, this.getGroundY(betterPos), betterPos.z, this.speedModifier);
            }
        }
    }
}
