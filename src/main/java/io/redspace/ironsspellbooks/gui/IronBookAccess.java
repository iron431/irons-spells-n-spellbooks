package io.redspace.ironsspellbooks.gui;

import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.List;

public record IronBookAccess(List<Component> pages) implements BookViewScreen.BookAccess {

    @Override
    public int getPageCount() {
        return this.pages.size();
    }

    @Override
    public FormattedText getPageRaw(int pIndex) {
        return this.pages.get(pIndex);
    }

    public FormattedText getPage(int page) {
        return page >= 0 && page < this.getPageCount() ? this.pages.get(page) : FormattedText.EMPTY;
    }


}
