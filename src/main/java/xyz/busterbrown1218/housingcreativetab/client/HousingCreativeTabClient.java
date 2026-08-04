package xyz.busterbrown1218.housingcreativetab.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import xyz.busterbrown1218.housingcreativetab.HousingCreativeTab;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HousingCreativeTabClient implements ClientModInitializer {
    public static final Set<Item> ALLOWED_1_8_9_ITEMS = new HashSet<>();
    public static final HashMap<Identifier, Pair<Identifier, Integer>> DATA_VALUES = new HashMap<>();
    public static boolean hideItems = false;
    public static boolean hasSetHideItems = false;
    private static final Gson gson = new Gson();

    @Override
    public void onInitializeClient() {
        ItemCommands.registerCommands();
        loadItems(gson.fromJson(new InputStreamReader(HousingCreativeTab.class.getResourceAsStream("/assets/housingcreativetab/items.json"), StandardCharsets.UTF_8), JsonArray.class));
        loadDataValues(gson.fromJson(new InputStreamReader(HousingCreativeTab.class.getResourceAsStream("/assets/housingcreativetab/data_values.json"), StandardCharsets.UTF_8), JsonObject.class));
    }

    public void loadItems(JsonArray jsonArray) {
        for (JsonElement element : jsonArray) {
            Identifier id = Identifier.of(element.getAsString());

            if (Registries.ITEM.containsId(id)) {
                ALLOWED_1_8_9_ITEMS.add(Registries.ITEM.get(id));
            }
        }
    }

    public void loadDataValues(JsonObject jsonObject) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonObject value = (JsonObject) entry.getValue();
            DATA_VALUES.put(Identifier.of(entry.getKey()), new Pair<>(Identifier.of(value.get("identifier").getAsString()), value.get("data_value").getAsInt()));
        }
    }
}
