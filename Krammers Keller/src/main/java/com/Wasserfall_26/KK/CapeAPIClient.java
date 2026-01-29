package com.Wasserfall_26.KK;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CapeAPIClient {

    private static final CapeAPIClient INSTANCE = new CapeAPIClient();


    private static final String API_BASE_URL = "https://cape-api-server.onrender.com";

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final ConcurrentHashMap<String, Boolean> capeCache = new ConcurrentHashMap<>();
    private final Set<String> pendingChecks = new HashSet<>();

    private long lastHeartbeat = 0;
    private static final long HEARTBEAT_INTERVAL = 5 * 60 * 1000;


    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    public static CapeAPIClient getInstance() {
        return INSTANCE;
    }

    public void registerSelf() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (lastHeartbeat != 0 && now - lastHeartbeat < HEARTBEAT_INTERVAL) {
            return;
        }

        lastHeartbeat = now;

        String uuid = mc.thePlayer.getUniqueID().toString();
        String username = mc.thePlayer.getName();

        executor.submit(() -> {
            try {
                registerUser(uuid, username);
                capeCache.put(uuid, true);
            } catch (Exception e) {
                //Ignore
            }
        });
    }


    public void checkPlayerHasMod(String uuid) {
        if (capeCache.containsKey(uuid)) return;

        synchronized (pendingChecks) {
            if (pendingChecks.contains(uuid)) return;
            pendingChecks.add(uuid);
        }

        executor.submit(() -> {
            try {
                boolean hasMod = checkUser(uuid);
                capeCache.put(uuid, hasMod);
            } catch (Exception e) {
                capeCache.put(uuid, false);
            } finally {
                synchronized (pendingChecks) {
                    pendingChecks.remove(uuid);
                }
            }
        });
    }


    public boolean shouldHaveCape(String uuid) {
        return capeCache.getOrDefault(uuid, false);
    }


    private void registerUser(String uuid, String username) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                registerUserInternal(uuid, username);
                return;
            } catch (Exception e) {
                lastException = e;

                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new Exception("Registration interrupted");
                    }
                }
            }
        }


        throw lastException != null ? lastException : new Exception("Registration failed after " + MAX_RETRIES + " attempts");
    }

    private void registerUserInternal(String uuid, String username) throws Exception {
        URL url = new URL(API_BASE_URL + "/api/register");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);


        String jsonBody = String.format(
                "{\"uuid\":\"%s\",\"username\":\"%s\"}",
                uuid, username
        );

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            throw new Exception("HTTP Error: " + responseCode);
        }

        conn.disconnect();
    }


    private boolean checkUser(String uuid) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return checkUserInternal(uuid);
            } catch (Exception e) {
                lastException = e;


                if (!e.getMessage().contains("503")) {
                    return false;
                }

                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }

        return false;
    }

    private boolean checkUserInternal(String uuid) throws Exception {
        URL url = new URL(API_BASE_URL + "/api/check/" + uuid);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP Error: " + responseCode);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }


            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(response.toString()).getAsJsonObject();

            return json.has("hasMod") && json.get("hasMod").getAsBoolean();
        } finally {
            conn.disconnect();
        }
    }


    public void clearCache() {
        capeCache.clear();
        synchronized (pendingChecks) {
            pendingChecks.clear();
        }
    }


    public void shutdown() {
        executor.shutdown();
    }
}