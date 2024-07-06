package io.redspace.ironsspellbooks.item.curios;

//public class SimpleAttributeCurio extends CurioBaseItem {
//
//    private final AttributeModifier attributeModifier;
//    Multimap<Attribute, AttributeModifier> attributeMap;
//
//    public SimpleAttributeCurio(Item.Properties properties, Attribute attribute, AttributeModifier attributeModifier) {
//        super(properties);
//        this.attributeModifier = attributeModifier;
//        attributeMap = HashMultimap.create();
//        attributeMap.put(attribute, this.attributeModifier);
//    }
//
//    @Override
//    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
//        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeBuilder = new ImmutableMultimap.Builder<>();
//        for (Attribute attribute : attributeMap.keySet()) {
//            attributeBuilder.put(attribute, new AttributeModifier(uuid, attributeModifier.getName(), attributeModifier.getAmount(), attributeModifier.getOperation()));
//        }
//        return attributeBuilder.build();
//    }
//
//}
