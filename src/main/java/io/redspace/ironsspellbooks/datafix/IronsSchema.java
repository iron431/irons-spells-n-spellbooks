package io.redspace.ironsspellbooks.datafix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Map;
import java.util.function.Supplier;

public class IronsSchema extends NamespacedSchema {

    public IronsSchema(int pVersionKey, Schema pParent) {
        super(pVersionKey, pParent);
    }
}