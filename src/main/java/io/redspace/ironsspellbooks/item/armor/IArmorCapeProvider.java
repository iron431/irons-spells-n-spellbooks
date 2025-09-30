package io.redspace.ironsspellbooks.item.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public interface IArmorCapeProvider {
    ResourceLocation getCapeResourceLocation();

    class CapeData {
        public double xCloakO;
        public double yCloakO;
        public double zCloakO;
        public double xCloak;
        public double yCloak;
        public double zCloak;
        public float bob;
        public float oBob;
        public int lastTick;
        public void moveCloak(LivingEntity livingEntity) {
            this.oBob = this.bob;
            float f;
            if (livingEntity.onGround() && !livingEntity.isDeadOrDying()) {
                f = (float) Math.min(0.1, livingEntity.getDeltaMovement().horizontalDistance());
            } else {
                f = 0.0F;
            }
            this.bob = this.bob + (f - this.bob) * 0.4F;

            this.xCloakO = this.xCloak;
            this.yCloakO = this.yCloak;
            this.zCloakO = this.zCloak;
            double d0 = livingEntity.getX() - this.xCloak;
            double d1 = livingEntity.getY() - this.yCloak;
            double d2 = livingEntity.getZ() - this.zCloak;
            double d3 = 10.0;
            if (d0 > 10.0) {
                this.xCloak = livingEntity.getX();
                this.xCloakO = this.xCloak;
            }

            if (d2 > 10.0) {
                this.zCloak = livingEntity.getZ();
                this.zCloakO = this.zCloak;
            }

            if (d1 > 10.0) {
                this.yCloak = livingEntity.getY();
                this.yCloakO = this.yCloak;
            }

            if (d0 < -10.0) {
                this.xCloak = livingEntity.getX();
                this.xCloakO = this.xCloak;
            }

            if (d2 < -10.0) {
                this.zCloak = livingEntity.getZ();
                this.zCloakO = this.zCloak;
            }

            if (d1 < -10.0) {
                this.yCloak = livingEntity.getY();
                this.yCloakO = this.yCloak;
            }

            this.xCloak += d0 * 0.25;
            this.zCloak += d2 * 0.25;
            this.yCloak += d1 * 0.25;
        }
    }
}
