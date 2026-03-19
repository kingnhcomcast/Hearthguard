package io.drahlek.hearthguard.config.screen;

import io.drahlek.hearthguard.config.HearthguardConfig;
import io.drahlek.hearthguard.util.MobRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class HearthguardConfigScreen extends Screen {
    private static final String CATEGORY_GENERAL = "__general__";
    private static final DecimalFormat SPEED_FORMAT = new DecimalFormat("0.00");

    private final Screen parent;
    private final Function<String, String> modNameLookup;

    private final HearthguardConfig config;
    private final Set<String> selectedMobs;

    private Map<String, List<EntityType<?>>> mobsByModId;
    private List<String> categoryKeys;
    private final Map<String, String> categoryLabels = new HashMap<>();
    private String selectedCategoryKey = CATEGORY_GENERAL;

    private ConfigList list;
    private CycleButton<String> categoryButton;
    private Button selectAllButton;
    private Button doneButton;
    private Button cancelButton;

    private int rangeValue;
    private int dropChanceValue;
    private HearthguardConfig.Mode modeValue;
    private double slowSpeedValue;
    private double fastSpeedValue;

    private EditBox slowSpeedField;
    private EditBox fastSpeedField;

    public HearthguardConfigScreen(Screen parent, Function<String, String> modNameLookup) {
        super(Component.literal("HearthGuard Options"));
        this.parent = parent;
        this.modNameLookup = modNameLookup;
        this.config = HearthguardConfig.getInstance();
        this.selectedMobs = new HashSet<>(config.getMobs());
        this.modeValue = config.getModeEnum() != null ? config.getModeEnum() : HearthguardConfig.Mode.WHITELIST;
        this.rangeValue = config.getRange();
        this.dropChanceValue = config.getDropItemChance();
        this.slowSpeedValue = config.getFleeSlowSpeed();
        this.fastSpeedValue = config.getFleeFastSpeed();
    }

    @Override
    protected void init() {
        ensureCategories();

        int listTop = 64;
        int listBottom = this.height - 40;
        int listHeight = listBottom - listTop;
        this.list = new ConfigList(this.minecraft, this.width, listHeight, listTop, 24);
        this.addWidget(this.list);

        this.categoryButton = CycleButton.builder(value -> Component.literal(categoryLabels.getOrDefault(value, value)), selectedCategoryKey)
                .withValues(categoryKeys)
                .create(this.width / 2 - 110, 28, 220, 20, Component.literal("Category"),
                        (btn, value) -> {
                            selectedCategoryKey = value;
                            rebuildList();
                        });
        this.addRenderableWidget(this.categoryButton);

        this.selectAllButton = Button.builder(Component.literal("Select All"), btn -> toggleSelectAll())
                .bounds(this.width / 2 - 75, 52, 150, 20)
                .build();
        this.addRenderableWidget(this.selectAllButton);

        this.doneButton = Button.builder(Component.literal("Done"), btn -> saveAndClose())
                .bounds(this.width / 2 - 154, this.height - 28, 150, 20)
                .build();
        this.addRenderableWidget(this.doneButton);

        this.cancelButton = Button.builder(Component.literal("Cancel"), btn -> onClose())
                .bounds(this.width / 2 + 4, this.height - 28, 150, 20)
                .build();
        this.addRenderableWidget(this.cancelButton);

        rebuildList();
    }

    private void ensureCategories() {
        if (mobsByModId != null) {
            return;
        }

        mobsByModId = new HashMap<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type.getCategory() != MobCategory.MONSTER) {
                continue;
            }
            if (MobRules.isBossMob(type)) {
                continue;
            }

            Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (key == null) {
                continue;
            }

            mobsByModId.computeIfAbsent(key.getNamespace(), ignored -> new ArrayList<>()).add(type);
        }

        for (List<EntityType<?>> list : mobsByModId.values()) {
            list.sort(Comparator.comparing(type -> type.getDescription().getString()));
        }

        categoryKeys = new ArrayList<>();
        categoryLabels.clear();
        categoryKeys.add(CATEGORY_GENERAL);
        categoryLabels.put(CATEGORY_GENERAL, "General");

        List<String> modIds = new ArrayList<>(mobsByModId.keySet());
        modIds.sort(Comparator.comparing(this::resolveModName, String.CASE_INSENSITIVE_ORDER));
        for (String modId : modIds) {
            categoryKeys.add(modId);
            categoryLabels.put(modId, resolveModName(modId));
        }
    }

    private String resolveModName(String modId) {
        if (modNameLookup == null) {
            return modId;
        }
        String name = modNameLookup.apply(modId);
        return name == null || name.isBlank() ? modId : name;
    }

    private void rebuildList() {
        this.list.clearEntriesInternal();

        if (CATEGORY_GENERAL.equals(selectedCategoryKey)) {
            selectAllButton.visible = false;
            buildGeneralEntries();
        } else {
            selectAllButton.visible = true;
            buildMobEntries(selectedCategoryKey);
            updateSelectAllLabel();
        }
    }

    private void buildGeneralEntries() {
        IntSlider rangeSlider = new IntSlider(0, 0, 200, 20, Component.literal("Range"), 3, 32, rangeValue,
                value -> rangeValue = value,
                value -> Component.literal("Range: " + value));

        IntSlider dropChanceSlider = new IntSlider(0, 0, 200, 20, Component.literal("Drop Item Chance"), 0, 100, dropChanceValue,
                value -> dropChanceValue = value,
                value -> Component.literal("Drop Item Chance: " + value + "%"));

        CycleButton<HearthguardConfig.Mode> modeButton = CycleButton.builder(mode -> Component.literal(mode.name()), modeValue)
                .withValues(HearthguardConfig.Mode.values())
                .create(0, 0, 200, 20, Component.literal("Mode"),
                        (btn, value) -> modeValue = value);

        this.slowSpeedField = new EditBox(this.font, 0, 0, 80, 20, Component.literal("Flee Slow Speed"));
        this.slowSpeedField.setValue(SPEED_FORMAT.format(slowSpeedValue));
        this.slowSpeedField.setResponder(value -> updateSpeedValue(value, true));

        this.fastSpeedField = new EditBox(this.font, 0, 0, 80, 20, Component.literal("Flee Fast Speed"));
        this.fastSpeedField.setValue(SPEED_FORMAT.format(fastSpeedValue));
        this.fastSpeedField.setResponder(value -> updateSpeedValue(value, false));

        list.addEntryInternal(new ConfigList.WidgetEntry(rangeSlider));
        list.addEntryInternal(new ConfigList.WidgetEntry(dropChanceSlider));
        list.addEntryInternal(new ConfigList.WidgetEntry(modeButton));
        list.addEntryInternal(new ConfigList.LabeledEditBoxEntry(Component.literal("Flee Slow Speed"), slowSpeedField, this.font));
        list.addEntryInternal(new ConfigList.LabeledEditBoxEntry(Component.literal("Flee Fast Speed"), fastSpeedField, this.font));
    }

    private void buildMobEntries(String modId) {
        List<EntityType<?>> mobs = mobsByModId.getOrDefault(modId, List.of());
        if (mobs.isEmpty()) {
            list.addEntryInternal(new ConfigList.LabelEntry(Component.literal("No mobs found."), this.font));
            return;
        }

        for (EntityType<?> type : mobs) {
            Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (key == null) {
                continue;
            }
            String mobId = key.toString();
            String displayName = type.getDescription().getString();
            boolean selected = selectedMobs.contains(mobId);

            Button toggle = Button.builder(mobToggleLabel(displayName, selected), btn -> {
                        boolean nowSelected = !selectedMobs.contains(mobId);
                        if (nowSelected) {
                            selectedMobs.add(mobId);
                        } else {
                            selectedMobs.remove(mobId);
                        }
                        btn.setMessage(mobToggleLabel(displayName, nowSelected));
                        updateSelectAllLabel();
                    })
                    .bounds(0, 0, 200, 20)
                    .build();

            list.addEntryInternal(new ConfigList.WidgetEntry(toggle));
        }
    }

    private static Component mobToggleLabel(String name, boolean selected) {
        return Component.literal((selected ? "[x] " : "[ ] ") + name);
    }

    private void updateSpeedValue(String value, boolean slow) {
        Double parsed = parseDouble(value);
        if (parsed == null) {
            return;
        }
        double clamped = Mth.clamp(parsed, 0.1, 2.0);
        if (slow) {
            slowSpeedValue = clamped;
        } else {
            fastSpeedValue = clamped;
        }
    }

    private void toggleSelectAll() {
        if (CATEGORY_GENERAL.equals(selectedCategoryKey)) {
            return;
        }
        List<EntityType<?>> mobs = mobsByModId.getOrDefault(selectedCategoryKey, List.of());
        if (mobs.isEmpty()) {
            return;
        }
        boolean allSelected = mobs.stream()
                .map(type -> BuiltInRegistries.ENTITY_TYPE.getKey(type))
                .filter(id -> id != null)
                .map(Identifier::toString)
                .allMatch(selectedMobs::contains);

        for (EntityType<?> type : mobs) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (id == null) {
                continue;
            }
            if (allSelected) {
                selectedMobs.remove(id.toString());
            } else {
                selectedMobs.add(id.toString());
            }
        }

        rebuildList();
    }

    private void updateSelectAllLabel() {
        if (selectAllButton == null || CATEGORY_GENERAL.equals(selectedCategoryKey)) {
            return;
        }
        List<EntityType<?>> mobs = mobsByModId.getOrDefault(selectedCategoryKey, List.of());
        boolean allSelected = !mobs.isEmpty() && mobs.stream()
                .map(type -> BuiltInRegistries.ENTITY_TYPE.getKey(type))
                .filter(id -> id != null)
                .map(Identifier::toString)
                .allMatch(selectedMobs::contains);

        selectAllButton.active = !mobs.isEmpty();
        selectAllButton.setMessage(Component.literal(allSelected ? "Deselect All" : "Select All"));
    }

    private void saveAndClose() {
        config.setRange(rangeValue);
        config.setDropItemChance(dropChanceValue);
        config.setModeEnum(modeValue != null ? modeValue : HearthguardConfig.Mode.WHITELIST);
        config.setFleeSlowSpeed(slowSpeedValue);
        config.setFleeFastSpeed(fastSpeedValue);

        config.getMobs().clear();
        config.getMobs().addAll(selectedMobs);
        config.save();

        this.minecraft.setScreen(parent);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.list.render(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
    }

    private static Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static final class IntSlider extends net.minecraft.client.gui.components.AbstractSliderButton {
        private final int min;
        private final int max;
        private final java.util.function.IntConsumer onChange;
        private final java.util.function.IntFunction<Component> messageBuilder;

        private IntSlider(int x, int y, int width, int height, Component message, int min, int max, int current,
                          java.util.function.IntConsumer onChange,
                          java.util.function.IntFunction<Component> messageBuilder) {
            super(x, y, width, height, message, valueFromInt(current, min, max));
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            this.messageBuilder = messageBuilder;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(messageBuilder.apply(getValue()));
        }

        @Override
        protected void applyValue() {
            onChange.accept(getValue());
        }

        private int getValue() {
            return intFromValue(this.value, min, max);
        }

        private static double valueFromInt(int value, int min, int max) {
            if (max == min) {
                return 0.0;
            }
            return (double) (value - min) / (double) (max - min);
        }

        private static int intFromValue(double sliderValue, int min, int max) {
            return Mth.clamp((int) Math.round(sliderValue * (max - min) + min), min, max);
        }
    }

    private static final class ConfigList extends ContainerObjectSelectionList<ConfigList.Entry> {
        private ConfigList(Minecraft minecraft, int width, int height, int y0, int itemHeight) {
            super(minecraft, width, height, y0, itemHeight);
        }

        private void clearEntriesInternal() {
            super.clearEntries();
        }

        private void addEntryInternal(Entry entry) {
            super.addEntry(entry);
        }

        @Override
        protected int scrollBarX() {
            return this.getRowRight() + 6 + 2;
        }

        @Override
        public int getRowWidth() {
            return 320;
        }

        private abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        }

        private static final class WidgetEntry extends Entry {
            private final AbstractWidget widget;

            private WidgetEntry(AbstractWidget widget) {
                this.widget = widget;
            }

            @Override
            public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
                int width = Math.min(getContentWidth(), 320);
                int left = getContentX() + (getContentWidth() - width) / 2;
                widget.setX(left);
                widget.setY(getContentY());
                widget.setWidth(width);
                widget.render(graphics, mouseX, mouseY, partialTick);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(widget);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(widget);
            }
        }

        private static final class LabeledEditBoxEntry extends Entry {
            private final Component label;
            private final EditBox box;
            private final net.minecraft.client.gui.Font font;

            private LabeledEditBoxEntry(Component label, EditBox box, net.minecraft.client.gui.Font font) {
                this.label = label;
                this.box = box;
                this.font = font;
            }

            @Override
            public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
                int labelX = getContentX() + 4;
                int labelY = getContentY() + 6;
                graphics.drawString(font, label, labelX, labelY, 0xFFFFFF, false);

                int boxWidth = 80;
                int boxX = getContentRight() - boxWidth - 4;
                box.setX(boxX);
                box.setY(getContentY());
                box.setWidth(boxWidth);
                box.render(graphics, mouseX, mouseY, partialTick);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(box);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(box);
            }
        }

        private static final class LabelEntry extends Entry {
            private final Component text;
            private final net.minecraft.client.gui.Font font;

            private LabelEntry(Component text, net.minecraft.client.gui.Font font) {
                this.text = text;
                this.font = font;
            }

            @Override
            public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
                graphics.drawCenteredString(font, text, getContentXMiddle(), getContentY() + 6, 0xAAAAAA);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of();
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of();
            }
        }
    }
}
