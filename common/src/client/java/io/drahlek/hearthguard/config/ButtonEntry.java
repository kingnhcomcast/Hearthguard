package io.drahlek.hearthguard.config;


import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ButtonEntry extends AbstractConfigListEntry<Void> {
    private final Button button;
    private boolean isSelectAll = true;

    public ButtonEntry(Component text, boolean isSelectAll, boolean canEdit, Consumer<Boolean> onPress) {
        super(Component.empty(), false);
        this.isSelectAll = isSelectAll;

        this.button = Button.builder(text, btn -> {
            // Toggle the state
            this.isSelectAll = !this.isSelectAll;
            // Run the logic passed from the config screen
            onPress.accept(!this.isSelectAll); // Passing the state just set
            // Update the button label
            btn.setMessage(Component.literal(this.isSelectAll ? "Select All" : "Deselect All"));
        }).bounds(0, 0, 150, 20).build();
        this.button.active = canEdit;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float tickDelta) {
        // 1. Fix the width to match the standard boolean toggle (150px)
        int fixedWidth = 100;
        this.button.setWidth(fixedWidth + 14);

        // 2. Position it at the far right of the entry row
        // This is the exact math BooleanListEntry uses for its main toggle
        int rightAlignedX = x + entryWidth - fixedWidth;

        this.button.setX(x + entryWidth - fixedWidth - 50 );
        this.button.setY(y);

        // 3. Render the button
        this.button.render(graphics, mouseX, mouseY, tickDelta);
    }

    // Required for the button to register hover/interaction in 1.21.11
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.button.isMouseOver(mouseX, mouseY);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.singletonList(this.button);
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return Collections.singletonList(this.button);
    }

    @Override public Void getValue() { return null; }
    @Override public Optional<Void> getDefaultValue() { return Optional.empty(); }
    @Override public void save() { }
}
