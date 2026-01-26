package io.redspace.ironsspellbooks.patreon.statue;

import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import net.minecraft.resources.ResourceLocation;

public record StatueTextureHolder(PatreonPermissions permissions, ResourceLocation textureLocation, PlayerStatueModelType modelType) {

}
