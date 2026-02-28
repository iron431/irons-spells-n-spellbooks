package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.Base64;
import java.util.List;

public class UnchainedBookItem extends ReadableLoreItem {
    private static Component ONE = Component.literal(
            translate("52585A6C626942306147397A5A53423361473867626D56325A584967644768766457646F644342306147567463325673646D567A4948527649474A6C494731685A326C6A5957787365534270626D4E736157356C5A4342695A576468626942306279426D5A5756734948526F5A576C7949484E7761584A7064484D67596D55676448566E5A32566B49485276643246795A484D676447686C494746756232316862486B754945396D4948526F62334E6C4948646F6279426B615751734948526F5A586B675932393162475167623235736553426D5A57567349474567646D6C7A59325679595777675A484A6C59575167596D566E61573467644738676332566C6343426D636D39744948526F5A576C7949474A76626D567A494746755A43427664585167615735306279426C646D56796553426A636D563261574E6C4947396D4948526F5A576C7949474A765A476C6C6379343D")
    ).withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt")));
    private static Component TWO = Component.literal(
            translate("543235736553423062323867624746305A53426B615751676447686C655342795A5746736158706C4948526F595851675A585A6C636E6B676332393162434276626942306147463049474A68644852735A575A705A57786B49484E315A47526C626D783549477876633351675A6E4A6C5A534233615778734F79423061475670636942796157646F644342306279427361585A6C494768685A4342695A57567549474E7359576C745A575167596E6B676447686C4947526C62576C6E62325175")
    ).withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt")));
    public static WrittenBookContent CONTENTS = new WrittenBookContent(Filterable.passThrough(""), "???", 0, List.of(
            Filterable.passThrough(ONE),
            Filterable.passThrough(TWO)
    ), true);

    public UnchainedBookItem(Properties pProperties) {
        super(IronsSpellbooks.id("textures/entity/lectern/unchained_book.png"), pProperties);
    }

    public static String translate(String str) {
        var sb = new StringBuilder();
        for (int i = 0; i < str.length(); i += 2) {
            var s = str.substring(i, i + 2);
            var j = Integer.parseInt(s, 16);
            sb.append((char) j);
        }
        var b = Base64.getDecoder().decode(sb.toString());
        return new String(b);
    }
}

