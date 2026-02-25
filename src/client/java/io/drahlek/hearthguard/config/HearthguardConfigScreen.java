package io.drahlek.hearthguard.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class HearthguardConfigScreen extends Screen {

    private MobList mobList;
    private final Screen parent;

    public HearthguardConfigScreen(Screen parent) {
        super(Component.literal("Mob Toggles"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        HearthguardConfig config = HearthguardConfig.getInstance();

       // initWhiteListButton(config);

        initMobList();

        initDone();
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
        int listTop = 40;
        int listHeight = this.height - 80;
        int entryHeight = 24;

        mobList = new MobList(
                this.minecraft,
                this.width,
                listHeight,
                listTop,
                entryHeight
        );

        // Loop over all entity types in the registry
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type.getCategory() != MobCategory.MONSTER) continue;

            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (id == null) continue;

            String idString = id.toString();
            boolean selected = HearthguardConfig.getInstance().mobs.contains(idString);

            mobList.addMob(idString, selected);
        }

        // Ensure entries are properly positioned
        mobList.updateSizeAndPosition(this.width, listHeight, listTop, 0);

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
                        .create(
                                this.width / 2 - 75, this.height / 2 - 10,
                                150, 20,
                                Component.literal("Mode"),
                                (button, value) -> {
                                    config.modeEnum = value;
                                    config.mode = value.name();
                                }
                        )
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick); // automatically calls proper background
    }

    // =========================
    // Mob List
    // =========================
    private class MobList extends AbstractSelectionList<MobList.MobEntry> {

        public MobList(Minecraft minecraft, int width, int height, int top, int entryHeight) {
            super(minecraft, width, height, top, entryHeight);
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
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

        // =========================
        // Single Entry
        // =========================
        private class MobEntry extends Entry<MobEntry> {
            private final String displayName;
            private boolean selected;

            public MobEntry(String displayName, boolean selected) {
                this.displayName = displayName;
                this.selected = selected;
                this.setHeight(24); // Ensure height matches the list's default
            }


            @Override
            public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
                // Draw a red rectangle for each entry
//                guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x80FF0000);
//
                guiGraphics.drawString(Minecraft.getInstance().font, displayName,
                        getContentX(), getContentY(), 0xFFFFFFFF);


                // Draw a light gray background for the entry
                guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x80000000);

                // Draw the mob name, inset by a few pixels
//                guiGraphics.drawString(
//                        Minecraft.getInstance().font,
//                        displayName,
//                        getX() + 4, // small padding from left
//                        getY() + (getHeight() - Minecraft.getInstance().font.lineHeight) / 2, // vertically centered
//                        0xFFFFFF
//                );

                // Draw selection highlight
                if (selected) {
                    guiGraphics.fill(
                            getX() + 1,
                            getY() + 1,
                            getX() + getWidth() - 1,
                            getY() + getHeight() - 1,
                            0x8000FF00 // translucent green
                    );
                }
            }

           // @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (isMouseOver(mouseX, mouseY)) {
                    selected = !selected;

                    String id = displayName;
                    HearthguardConfig config = HearthguardConfig.getInstance();
                    if (selected) {
                        config.mobs.add(id);
                    } else {
                        config.mobs.remove(id);
                    }
                    return true;
                }
                return false;
            }
        }
    }
}