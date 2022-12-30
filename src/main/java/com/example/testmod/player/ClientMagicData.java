package com.example.testmod.player;

import com.example.testmod.capabilities.magic.data.PlayerCooldowns;
import com.example.testmod.spells.SpellType;
import net.minecraft.client.Minecraft;

public class ClientMagicData {
    static {
        ClientMagicData.playerCooldowns = new PlayerCooldowns();
        //ClientMagicData.playerCooldowns.setTickBuffer(-2);
    }

    private static int playerMana;
    private static PlayerCooldowns playerCooldowns;

    public static void setMana(int playerMana) {
        ClientMagicData.playerMana = playerMana;
    }

    public static PlayerCooldowns getCooldowns() {
        return ClientMagicData.playerCooldowns;
    }

    public static float getCooldownPercent(SpellType spellType) {
        return playerCooldowns.getCooldownPercent(spellType);
    }

    public static int getPlayerMana() {
        return playerMana;
    }

    public static SpinAttackType lastSpinAttack = SpinAttackType.RIPTIDE;

    public enum SpinAttackType {
        RIPTIDE,
        FIRE
    }

    //
    // Temporary BS to animate cast time bar
    //

    private static boolean tempAnimate;
    private static float castCompletionPercent;
    public static float getCastCompletionPercent(){
        return castCompletionPercent;
    }

    public static void setCastCompletionPercent(float percent){
        castCompletionPercent = percent;
    }
    public static void tempStartAnimation(){
        castCompletionPercent=0f;
        tempAnimate=true;
    }
    public static void progressAnimation(){
        //this will reach 100% in 5 seconds
        if(!tempAnimate)
            return;
        castCompletionPercent += Minecraft.getInstance().getDeltaFrameTime()/20/5;
        if(castCompletionPercent>=1f){
            castCompletionPercent=1f;
            tempAnimate = false;
        }
    }
}