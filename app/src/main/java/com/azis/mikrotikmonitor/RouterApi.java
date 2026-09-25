package com.azis.mikrotikmonitor;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.net.ssl.*;

public class RouterApi {
    private final RouterConfig cfg;
    private final Handler main = new Handler(Looper.getMainLooper());

    public RouterApi(RouterConfig c) { cfg = c; }

    private HttpsURLConnection conn(String path, String method) throws Exception {
        URL u = new URL(cfg.baseUrl() + "/" + path);
        HttpsURLConnection c = (HttpsURLConnection) u.openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(7000);
        c.setReadTimeout(10000);
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("Content-Type", "application/json");
        String basic = android.util.Base64.encodeToString(
            (cfg.user + ":" + cfg.pass).getBytes(StandardCharsets.UTF_8),
            android.util.Base64.NO_WRAP);
        c.setRequestProperty("Authorization", "Basic " + basic);
        if (cfg.insecure) trustAll(c);
        return c;
    }

    private void trustAll(HttpsURLConnection c) throws Exception {
        TrustManager[] t = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers(){return new java.security.cert.X509Certificate[0];}
            public void checkClientTrusted(java.security.cert.X509Certificate[] x,String s){}
            public void checkServerTrusted(java.security.cert.X509Certificate[] x,String s){}
        }};
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, t, new java.security.SecureRandom());
        c.setSSLSocketFactory(sc.getSocketFactory());
        c.setHostnameVerifier((h, s) -> true);
    }

    private String request(String path, String method, String body) throws Exception {
        HttpsURLConnection c = conn(path, method);
        if (body != null) {
            c.setDoOutput(true);
            try(OutputStream os = c.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
        int code = c.getResponseCode();
        InputStream is = code >= 400 ? c.getErrorStream() : c.getInputStream();
        StringBuilder b = new StringBuilder();
        if (is != null) {
            try(BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
                String line; while((line=r.readLine())!=null) b.append(line);
            }
        }
        if (code >= 400) throw new IOException("HTTP " + code + ": " + b);
        return b.toString();
    }

    public JSONArray get(String path) throws Exception {
        String s = request(path, "GET", null);
        return s.isEmpty() ? new JSONArray() : new JSONArray(s);
    }

    public JSONObject patch(String path, JSONObject body) throws Exception {
        String s = request(path, "PATCH", body.toString());
        return s.isEmpty() ? new JSONObject() : new JSONObject(s);
    }

    public void delete(String path) throws Exception { request(path, "DELETE", null); }

    public JSONObject resource() throws Exception {
        JSONArray a = get("system/resource");
        return a.length() > 0 ? a.getJSONObject(0) : new JSONObject();
    }

    public JSONArray secrets() throws Exception { return get("ppp/secret"); }
    public JSONArray active() throws Exception { return get("ppp/active"); }
    public JSONArray interfaces() throws Exception { return get("interface"); }

    public String secretId(String name) throws Exception {
        JSONArray a = get("ppp/secret?name=" + URLEncoder.encode(name, "UTF-8"));
        return a.length() > 0 ? a.getJSONObject(0).optString(".id") : "";
    }

    public void setSecretDisabled(String name, boolean disabled) throws Exception {
        String id = secretId(name);
        if (id.isEmpty()) throw new IOException("User tidak ditemukan");
        JSONObject b = new JSONObject().put("disabled", disabled ? "yes" : "no");
        patch("ppp/secret/" + URLEncoder.encode(id, "UTF-8"), b);
    }

    public void kick(String name) throws Exception {
        JSONArray a = get("ppp/active?name=" + URLEncoder.encode(name, "UTF-8"));
        for (int i=0;i<a.length();i++) {
            String id=a.getJSONObject(i).optString(".id");
            if(!id.isEmpty()) delete("ppp/active/" + URLEncoder.encode(id, "UTF-8"));
        }
    }

    public void setInterface(String name, boolean disabled) throws Exception {
        JSONObject b = new JSONObject().put("disabled", disabled ? "yes" : "no");
        patch("interface/" + URLEncoder.encode(name, "UTF-8"), b);
    }


    public JSONArray profiles() throws Exception { return get("ppp/profile"); }

    public JSONObject createSecret(String name, String password, String profile) throws Exception {
        JSONObject b = new JSONObject()
            .put("name", name)
            .put("password", password)
            .put("service", "pppoe")
            .put("profile", profile);
        String s = request("ppp/secret", "PUT", b.toString());
        return s.isEmpty() ? new JSONObject() : new JSONObject(s);
    }


    public JSONArray activeByName(String name) throws Exception {
        return get("ppp/active?name=" + URLEncoder.encode(name, "UTF-8"));
    }

    public JSONArray interfaceByNames(String names) throws Exception {
        return get("interface");
    }

    public JSONObject interfaceByName(String name) throws Exception {
        JSONArray a = get("interface?name=" + URLEncoder.encode(name, "UTF-8"));
        return a.length() > 0 ? a.getJSONObject(0) : new JSONObject();
    }

    public void test() throws Exception { resource(); }
}
