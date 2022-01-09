package com.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.json.game.JsonAgents;
import com.json.game.JsonInfo;
import com.json.game.JsonPokemons;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

public class JsonUtilities {

    public static JsonModel getJsonModelFromFile(String fileName) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(fileName));
        JsonModel data = gson.fromJson(reader, JsonModel.class);
        return data;
    }

    public static JsonModel getJsonModelFromString(String str) {
        Gson gson = new Gson();
        JsonModel data = gson.fromJson(str, JsonModel.class);
        return data;
    }

    public static JsonPokemons getJsonPokemons(String str) {
        Gson gson = new Gson();
        JsonPokemons data = gson.fromJson(str, JsonPokemons.class);
        return data;
    }

    public static JsonAgents getJsonAgents(String str) {
        Gson gson = new Gson();
        JsonAgents data = gson.fromJson(str, JsonAgents.class);
        return data;
    }

    public static JsonInfo getJsonInfo(String str) {
        Gson gson = new Gson();
        try {
            JsonInfo data = gson.fromJson(str, JsonInfo.class);
            return data;
        } catch (Exception e) {
            return null;
        }

    }


}
