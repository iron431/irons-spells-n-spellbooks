package io.redspace.ironsspellbooks.datafix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.ItemStackTagFix;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ItemStackScrollFix extends ItemStackTagFix {

    public ItemStackScrollFix(Schema pOutputSchema) {
        super(pOutputSchema, "ItemStackScrollFix", (p_216678_) -> {
            return p_216678_.equals("irons_spellbooks:scroll");
        });
    }

    @Override
    protected <T> @NotNull Dynamic<T> fixItemStackTag(Dynamic<T> dynamic1) {
        Optional<? extends Dynamic<?>> optional = dynamic1.get("type").result();
        if (optional.isPresent()) {
//            Dynamic<?> dynamic = optional.get();
//            Optional<String> optional1 = dynamic.get("Name").asString().result();
//            if (optional1.isPresent()) {
//                String s = optional1.get();
//                s = s.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
//                dynamic = dynamic.set("Name", dynamic.createString(s));
//            }

            return dynamic1;//.set("display", dynamic);
        } else {
            return dynamic1;
        }
    }
}