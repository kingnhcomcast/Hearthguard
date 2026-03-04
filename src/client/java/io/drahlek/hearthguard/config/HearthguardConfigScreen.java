package io.drahlek.hearthguard.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jspecify.annotations.NonNull;

//TODO add 'reset default' button
public class HearthguardConfigScreen extends Screen {
    private static final int LEFT_MARGIN = 40;
    private static final int RIGHT_MARGIN = 40;
    private static final int TOP_MARGIN = 11;
    private static final int BOTTOM_MARGIN = 11;
    private static final int SPACING = 7;
    private static final int WIDGET_HEIGHT = 20;
    private static final int WIDGET_WIDTH = 150;
    private static final int TOP_OFFSET = 31;
    private final Screen parent;
    private MobList mobList;

    public HearthguardConfigScreen(Screen parent) {
        super(Component.literal("Mob Toggles"));
        this.parent = parent;
    }

    private int calculateTop(int index) {
        return (SPACING + WIDGET_HEIGHT) * (index - 1);
    }

    @Override
    protected void init() {
        HearthguardConfig config = HearthguardConfig.getInstance();

        initHeader();
        initWhiteListButton(config);
        initRangeSlider(config);
        initSlowSpeedSlider(config);
        initFastSpeedSlider(config);
        initMobList();
        initSelectAllCheckbox();
        initDone();
    }

    private void initFastSpeedSlider(HearthguardConfig config) {
        NumberSliderButton slider = new NumberSliderButton(
                "Flee Fast Speed",
                this.width / 2,
                calculateTop(3),
                WIDGET_WIDTH,
                WIDGET_HEIGHT,
                config.fleeFastSpeed,
                0.1,   // min
                2.0,  // max
                false,
                0.1,
                newValue -> {
                    config.fleeFastSpeed = newValue;
                }
        );

        this.addRenderableWidget(slider);
    }

    private void initSlowSpeedSlider(HearthguardConfig config) {
        NumberSliderButton slider = new NumberSliderButton(
                "Flee Slow Speed",
                LEFT_MARGIN,
                calculateTop(3),
                WIDGET_WIDTH,
                WIDGET_HEIGHT,
                config.fleeSlowSpeed,
                0.1,   // min
                2.0,  // max
                false,
                0.1,
                newValue -> {
                    config.fleeSlowSpeed = newValue;
                }
        );

        this.addRenderableWidget(slider);
    }

    private void initRangeSlider(HearthguardConfig config) {
        NumberSliderButton slider = new NumberSliderButton(
                "Range",
                this.width / 2,
                calculateTop(2),
                WIDGET_WIDTH,
                WIDGET_HEIGHT,
                config.range,   // initial value
                3,   // min
                32,  // max
                true,
                1.0,
                newValue -> {
                    config.range = (int) newValue;
                }
        );

        this.addRenderableWidget(slider);
    }

    private void initHeader() {
        String text = "HearthGuard Options";
        int width = this.font.width(text);

        this.addRenderableWidget(
                new StringWidget(
                        (this.width / 2) - (width / 2),
                        TOP_MARGIN,
                        width,
                        9,
                        Component.literal(text),
                        this.font
                )
        );
    }

    private void initSelectAllCheckbox() {
        this.addRenderableWidget(
                new SelectAllWidget(LEFT_MARGIN, calculateTop(4), WIDGET_WIDTH, WIDGET_HEIGHT)
        );
    }

    private void initDone() {
        this.addRenderableWidget(
                Button.builder(Component.literal("Done"), b -> {
                            HearthguardConfig.getInstance().save();
                            minecraft.setScreen(parent);
                        })
                        .bounds(this.width / 2 - 50, this.height - 30, 100, 20)
                        .build()
        );
    }

    private void initMobList() {
        int x = LEFT_MARGIN;
        int y = calculateTop(5);
        int width = this.width - LEFT_MARGIN - RIGHT_MARGIN;
        int height = this.height - BOTTOM_MARGIN - calculateTop(6);//  140;

        mobList = new MobList(
                this.minecraft,
                x,     //x
                y,     //y
                5,     //width
                20     //height
        );

        // Loop over all entity types in the registry
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type.getCategory() != MobCategory.MONSTER) continue;

            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);

            String idString = id.toString();
            boolean selected = HearthguardConfig.getInstance().mobs.contains(idString);

            mobList.addMob(idString, selected);
        }

        // Ensure entries are properly positioned
        mobList.updateSizeAndPosition(width, height, x, y);

        this.addRenderableWidget(mobList);
    }

    private void initWhiteListButton(HearthguardConfig config) {
        // CycleButton for WHITELIST / BLACKLIST
        this.addRenderableWidget(
                new CycleButton.Builder<>(
                        mode -> Component.literal(mode == HearthguardConfig.Mode.WHITELIST ? "Whitelist" : "Blacklist"),
                        config.modeEnum
                )
                        .withValues(HearthguardConfig.Mode.values())
                        .create( //x, y, width, height
                                LEFT_MARGIN, // this.width / 2 - 75,
                                calculateTop(2), //TOP_MARGIN + SPACING,
                                WIDGET_WIDTH,
                                20,
                                Component.literal("Mode"),
                                (button, value) -> {
                                    config.modeEnum = value;
                                    config.mode = value.name();
                                }
                        )
        );
    }

    @Override
    public void render(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick); // automatically calls proper background
    }

    // =========================
    // Mob List
    // =========================
    private static class MobList extends AbstractSelectionList<MobList.MobEntry> {

        public MobList(Minecraft minecraft, int x, int y, int width, int height) {
            super(minecraft, x, y, width, height);
        }

        @Override
        public void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput) {
            // No custom narration needed
        }

        @Override
        protected int scrollBarX() {
            return this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 12;
        }

        // Convenience method
        public void addMob(String name, boolean selected) {
            this.addEntry(new MobEntry(name, selected));
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
            double mouseX = event.x();
            double mouseY = event.y();

            // Find the entry under the mouse
            MobEntry entry = this.getEntryAtPosition(mouseX, mouseY);
            if (entry != null) {
                entry.selected = !entry.selected;

                HearthguardConfig config = HearthguardConfig.getInstance();
                if (entry.selected) config.mobs.add(entry.displayName);
                else config.mobs.remove(entry.displayName);

                // return true; // event handled
            }

            // Otherwise, let super handle scrolling etc.
            return super.mouseClicked(event, consumed);
        }

        public boolean areAllSelected() {
            for (MobEntry entry : this.children()) {
                if (!entry.selected) return false;
            }
            return !this.children().isEmpty();
        }

        public void setAllSelected(boolean value) {
            HearthguardConfig config = HearthguardConfig.getInstance();

            for (MobEntry entry : this.children()) {
                entry.selected = value;

                if (value) config.mobs.add(entry.displayName);
                else config.mobs.remove(entry.displayName);
            }
        }

        // =========================
        // Single Entry
        // =========================
        private static class MobEntry extends Entry<MobEntry> {
            private final String displayName;
            private boolean selected;

            public MobEntry(String displayName, boolean selected) {
                this.displayName = displayName;
                this.selected = selected;
                this.setHeight(24); // Ensure height matches the list's default
            }


            @Override
            public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
                int checkboxSize = 12;
                int padding = 4;

                // Draw the checkbox (left side)
                int checkboxX = getContentX();
                int checkboxY = getContentY() + (getContentHeight() - checkboxSize) / 2;

                // Draw checkbox background
                guiGraphics.fill(
                        checkboxX, checkboxY,
                        checkboxX + checkboxSize, checkboxY + checkboxSize,
                        0xFF555555 // dark gray border
                );

                // Fill the checkbox if selected
                if (selected) {
                    guiGraphics.fill(
                            checkboxX + 2, checkboxY + 2,
                            checkboxX + checkboxSize - 2, checkboxY + checkboxSize - 2,
                            0xFF00FF00 // green
                    );
                }

                // Draw the mob name to the right of the checkbox
                guiGraphics.drawString(
                        Minecraft.getInstance().font,
                        displayName,
                        checkboxX + checkboxSize + padding,
                        getContentY() + (getContentHeight() - Minecraft.getInstance().font.lineHeight) / 2,
                        0xFFFFFFFF // white text
                );
            }


        }
    }

    private class SelectAllWidget extends Button {
        private final int x;
        private final int y;

        public SelectAllWidget(int x, int y, int width, int height) {
            super(
                    x,
                    y,
                    width,
                    height,
                    Component.literal(""), // text handled in renderWidget
                    button -> {            // onPress
                        if (mobList != null) {
                            boolean allSelected = mobList.areAllSelected();
                            mobList.setAllSelected(!allSelected);
                        }
                    },
                    button -> Component.literal("") // CreateNarration: just empty
            );
            this.x = x;
            this.y = y;
        }

        @Override
        public void renderContents(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            // Draw the checkbox
            boolean allSelected = mobList != null && mobList.areAllSelected();

            int checkboxSize = 12;
            int padding = 4;

            int checkboxX = this.x;
            int checkboxY = this.y + (this.height - checkboxSize) / 2;

            // border
            graphics.fill(
                    checkboxX, checkboxY,
                    checkboxX + checkboxSize, checkboxY + checkboxSize,
                    0xFF555555
            );

            // fill if selected
            if (allSelected) {
                graphics.fill(
                        checkboxX + 2, checkboxY + 2,
                        checkboxX + checkboxSize - 2, checkboxY + checkboxSize - 2,
                        0xFF00FF00
                );
            }

            // label matching list style
            String label = allSelected ? "Deselect All" : "Select All";
            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(label),
                    checkboxX + checkboxSize + padding,
                    this.y + (this.height - Minecraft.getInstance().font.lineHeight) / 2,
                    0xFFFFFFFF
            );
        }
    }
}