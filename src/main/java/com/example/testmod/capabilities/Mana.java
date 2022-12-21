package com.example.testmod.capabilities;

import net.minecraft.client.player.LocalPlayer;

import java.sql.Ref;

import static com.example.testmod.registries.AttributeRegistry.MAX_MANA;

public class Mana {
    private double mana;
    private LocalPlayer owner;
    public Mana(LocalPlayer player){
        owner = player;
        mana = 69;
    }


    public double getMana(){
        return mana;
    }
    public void addMana(double amount){
        mana+=amount;
        clampMana();
    }
    public void removeMana(double amount){
        addMana(-amount);
    }
    public void addPercentMana(double percent){
        addMana(percent*owner.getAttributeValue(MAX_MANA.get()));
    }
    private void clampMana(){
        double maxMana = owner.getAttributeValue(MAX_MANA.get());
        mana = mana<0||mana>maxMana?mana<0?0:maxMana:mana;

    }

}
