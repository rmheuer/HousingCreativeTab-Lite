package xyz.busterbrown1218.housingcreativetab;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public final class HousingCreativeTabClient implements ClientModInitializer {
  public static final Set<Item> ALLOWED_1_8_9_ITEMS = new HashSet<>();
  public static final HashMap<Identifier, Pair<Identifier, Integer>> DATA_VALUES = new HashMap<>();
  public static boolean hideItems = false;
  public static boolean hasSetHideItems = false;

  @Override
  public void onInitializeClient() {
    Gson gson = new Gson();
    loadItems(loadJson(gson, "/assets/housingcreativetab/items.json", JsonArray.class));
    loadDataValues(loadJson(gson, "/assets/housingcreativetab/data_values.json", JsonObject.class));
  }

  private <T> T loadJson(Gson gson, String path, Class<T> type) {
    try (InputStream stream = getClass().getResourceAsStream(path)) {
      if (stream == null) {
        throw new FileNotFoundException(path);
      }
      return gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), type);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load required data from " + path, e);
    }
  }

  private void loadItems(JsonArray jsonArray) {
    for (JsonElement element : jsonArray) {
      Identifier id = Identifier.parse(element.getAsString());

      if (BuiltInRegistries.ITEM.containsKey(id)) {
        ALLOWED_1_8_9_ITEMS.add(BuiltInRegistries.ITEM.getValue(id));
      }
    }
  }

  private void loadDataValues(JsonObject jsonObject) {
    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
      JsonObject value = (JsonObject) entry.getValue();
      DATA_VALUES.put(
          Identifier.parse(entry.getKey()),
          new Pair<>(
              Identifier.parse(value.get("identifier").getAsString()),
              value.get("data_value").getAsInt()));
    }
  }
}
