package io.redspace.ironsspellbooks.item.spell_books;

//public class SimpleAttributeSpellBook extends SpellBook {
//    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
//
//    public SimpleAttributeSpellBook(int spellSlots, SpellRarity rarity, Attribute attribute, double value) {
//        this(spellSlots, rarity, createMultimap(attribute, new AttributeModifier(UUID.fromString("667ad88f-901d-4691-b2a2-3664e42026d3"), "Weapon modifier", value, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)));
//    }
//
//    public SimpleAttributeSpellBook(int spellSlots, SpellRarity rarity, Multimap<Attribute, AttributeModifier> defaultModifiers) {
//        super(spellSlots, rarity);
//        this.defaultModifiers = defaultModifiers;
//    }
//
//    public SimpleAttributeSpellBook(int spellSlots, SpellRarity rarity, Multimap<Attribute, AttributeModifier> defaultModifiers, Item.Properties properties) {
//        super(spellSlots, rarity, properties);
//        this.defaultModifiers = defaultModifiers;
//    }
//
//    @Override
//    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
//        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeBuilder = new ImmutableMultimap.Builder<>();
//        for (Attribute attribute : defaultModifiers.keySet()) {
//            var modifiers = defaultModifiers.get(attribute);
//            for (AttributeModifier attributeModifier : modifiers) {
//                attributeBuilder.put(attribute, new AttributeModifier(uuid, attributeModifier.getName(), attributeModifier.getAmount(), attributeModifier.getOperation()));
//            }
//        }
//        return attributeBuilder.build();
//    }
//
//    private static Multimap<Attribute, AttributeModifier> createMultimap(Attribute attribute, AttributeModifier modifier) {
//        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
//        map.put(attribute, modifier);
//        return map;
//    }
//}
