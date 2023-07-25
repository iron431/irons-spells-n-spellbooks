//package io.redspace.ironsspellbooks.mixin;
//
//import com.mojang.datafixers.DataFixerBuilder;
//import com.mojang.datafixers.schemas.Schema;
//import io.redspace.ironsspellbooks.datafix.IronsSchema;
//import io.redspace.ironsspellbooks.datafix.ItemStackScrollFix;
//import net.minecraft.util.datafix.DataFixers;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(DataFixers.class)
//public abstract class DataFixersMixin {
//    @Inject(method = "addFixers", at = @At("RETURN"))
//    private static void addCustomDataFixers(DataFixerBuilder dataFixerBuilder, CallbackInfo ci) {
//        Schema schema = dataFixerBuilder.addSchema(3109, IronsSchema::new);
//        dataFixerBuilder.addFixer(new ItemStackScrollFix(schema));
//    }
//}