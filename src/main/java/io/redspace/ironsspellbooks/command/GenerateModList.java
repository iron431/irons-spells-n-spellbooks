package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class GenerateModList {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.generate_mod_list.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        if (pDispatcher.getRoot().getChild("modlist") == null) {
            pDispatcher.register(Commands.literal("modlist").requires((p_138819_) -> {
                return p_138819_.hasPermission(2);
            }).executes((commandContext) -> {
                return generateModList(commandContext.getSource());
            }));
        }else{
            IronsSpellbooks.LOGGER.debug("modlist already loaded.. skipping");
        }
    }

    private static int generateModList(CommandSourceStack source) throws CommandSyntaxException {
        var sb = new StringBuilder();

        sb.append("mod_id");
        sb.append(",");
        sb.append("mod_name");
        sb.append(",");
        sb.append("mod_version");
        sb.append(",");
        sb.append("mod_file");
        sb.append(",");
        sb.append("mod_url");
        sb.append(",");
        sb.append("display_url");
        sb.append(",");
        sb.append("issue_tracker_url");
        sb.append("\n");

        ModList.get().getMods().forEach(iModInfo -> {
            sb.append(iModInfo.getModId());
            sb.append(",");
            sb.append(iModInfo.getDisplayName());
            sb.append(",");
            sb.append(iModInfo.getVersion());
            sb.append(",");
            sb.append(iModInfo.getOwningFile().getFile().getFileName());
            sb.append(",");
            iModInfo.getModURL().ifPresent(sb::append);
            sb.append(",");
            iModInfo.getConfig().getConfigElement("displayURL").ifPresent(sb::append);
            sb.append(",");
            iModInfo.getOwningFile().getConfig().getConfigElement("issueTrackerURL").ifPresent(sb::append);
            sb.append("\n");
        });

        try {
            var file = new File("modlist.txt");
            var writer = new BufferedWriter(new FileWriter(file));
            writer.write(sb.toString());
            writer.close();

            Component component = Component.literal(file.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle((style) -> {
                return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()));
            });

            source.sendSuccess(() -> Component.translatable("commands.irons_spellbooks.generate_mod_list.success", component), true);

        } catch (Exception e) {
            IronsSpellbooks.LOGGER.info(e.getMessage());
            throw ERROR_FAILED.create();
        }
        return 1;
    }
}
