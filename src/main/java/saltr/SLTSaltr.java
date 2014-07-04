/**
 * Copyright Teoken LLC. (c) 2013. All rights reserved.
 * Copying or usage of any piece of this source code without written notice from Teoken LLC is a major crime.
 * Այս կոդը Թեոկեն ՍՊԸ ընկերության սեփականությունն է:
 * Առանց գրավոր թույլտվության այս կոդի պատճենահանումը կամ օգտագործումը քրեական հանցագործություն է:
 */
package saltr;

import android.content.ContextWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.RequestParams;
import saltr.parser.game.SLTLevel;
import saltr.parser.game.SLTLevelPack;
import saltr.parser.response.SLTResponse;
import saltr.parser.response.SLTResponseAppData;
import saltr.parser.response.level.SLTResponseLevelData;
import saltr.repository.ISLTRepository;
import saltr.repository.SLTDummyRepository;
import saltr.repository.SLTMobileRepository;
import saltr.status.*;

import java.text.MessageFormat;
import java.util.*;

public class SLTSaltr {
    private static SLTSaltr saltr;

    protected Gson gson;

    protected String socialId;
    protected String deviceId;
    protected boolean connected;
    protected String clientKey;
    protected String saltrUserId;
    protected boolean isLoading;

    protected ISLTRepository repository;

    protected Map<String, SLTFeature> activeFeatures;
    protected Map<String, SLTFeature> developerFeatures;
    protected List<SLTLevelPack> levelPacks;
    protected List<SLTExperiment> experiments;

    protected SLTDataHandler saltrHttpDataHandler;

    private int requestIdleTimeout;
    private boolean devMode;
    private boolean started;
    private boolean useNoLevels;
    private boolean useNoFeatures;

    private SLTSaltr(String clientKey, String deviceId, boolean useCache, ContextWrapper contextWrapper) {
        this.clientKey = clientKey;
        this.deviceId = deviceId;
        isLoading = false;
        connected = false;
        saltrUserId = null;
        useNoLevels = false;
        useNoFeatures = false;

        devMode = false;
        started = false;
        requestIdleTimeout = 0;

        activeFeatures = new HashMap<>();
        developerFeatures = new HashMap<>();
        experiments = new ArrayList<>();
        levelPacks = new ArrayList<>();

        repository = useCache ? new SLTMobileRepository(contextWrapper) : new SLTDummyRepository(contextWrapper);
        gson = new Gson();
    }

    public static SLTSaltr getInstance(String clientKey, String deviceId, boolean useCache, ContextWrapper contextWrapper) {
        if (saltr == null) {
            saltr = new SLTSaltr(clientKey, deviceId, useCache, contextWrapper);
        }
        return saltr;
    }

    public void setRepository(ISLTRepository repository) {
        this.repository = repository;
    }

    public void setUseNoLevels(Boolean useNoLevels) {
        this.useNoLevels = useNoLevels;
    }

    public void setUseNoFeatures(Boolean useNoFeatures) {
        this.useNoFeatures = useNoFeatures;
    }

    public void setDevMode(Boolean devMode) {
        this.devMode = devMode;
    }

    public void setRequestIdleTimeout(int requestIdleTimeout) {
        this.requestIdleTimeout = requestIdleTimeout;
    }

    public void setLevelPacks(List<SLTLevelPack> levelPacks) {
        this.levelPacks = levelPacks;
    }

    public List<SLTLevel> getAllLevels() {
        List<SLTLevel> allLevels = new ArrayList<>();
        for (int i = 0, len = levelPacks.size(); i < len; ++i) {
            List<SLTLevel> levels = levelPacks.get(i).getLevels();
            for (int j = 0, len2 = levels.size(); j < len2; ++j) {
                allLevels.add(levels.get(j));
            }
        }

        return allLevels;
    }

    public int getAllLevelsCount() {
        int count = 0;
        for (int i = 0, len = levelPacks.size(); i < len; ++i) {
            count += levelPacks.get(i).getLevels().size();
        }

        return count;
    }

    public SLTLevel getLevelByGlobalIndex(int index) {
        int levelsSum = 0;
        for (int i = 0, len = levelPacks.size(); i < len; ++i) {
            int packLength = levelPacks.get(i).getLevels().size();
            if (index >= levelsSum + packLength) {
                levelsSum += packLength;
            }
            else {
                int localIndex = index - levelsSum;
                return levelPacks.get(i).getLevels().get(localIndex);
            }
        }
        return null;
    }

    public SLTLevelPack getPackByLevelGlobalIndex(int index) {
        int levelsSum = 0;
        for (int i = 0, len = levelPacks.size(); i < len; ++i) {
            int packLength = levelPacks.get(i).getLevels().size();
            if (index >= levelsSum + packLength) {
                levelsSum += packLength;
            }
            else {
                return levelPacks.get(i);
            }
        }
        return null;
    }

    public List<SLTExperiment> getExperiments() {
        return experiments;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }

    public List<String> getActiveFeatureTokens() {
        List<String> tokens = new ArrayList<>();
        for (Map.Entry<String, SLTFeature> entry : activeFeatures.entrySet()) {
            tokens.add(entry.getValue().getToken());
        }

        return tokens;
    }

    public Object getFeatureProperties(String token) {
        SLTFeature activeFeature = activeFeatures.get(token);
        if (activeFeature != null) {
            return activeFeature.getProperties();
        }
        else {
            SLTFeature devFeature = developerFeatures.get(token);
            if (devFeature != null && devFeature.getRequired()) {
                return devFeature.getProperties();
            }
        }

        return null;
    }

    public void importLevels(String path) throws Exception {
        if (started) {
            path = SLTConfig.LOCAL_LEVELPACK_PACKAGE_URL;
            Object applicationData = repository.getObjectFromApplication(path);
            levelPacks = SLTDeserializer.decodeLevels((SLTResponseAppData) applicationData);
        }
        else {
            throw new Exception("Method 'importLevels()' should be called before 'start()' only.");
        }
    }

    /**
     * If you want to have a feature synced with SALTR you should call define before getAppData call.
     */
    public void defineFeature(String token, Map<String, String> properties, boolean required) throws Exception {
        if (!started) {
            developerFeatures.put(token, new SLTFeature(token, properties, required));
        }
        else {
            throw new Exception("Method 'defineFeature()' should be called before 'start()' only.");
        }
    }

    public void start() throws Exception {
        if (deviceId == null) {
            throw new Exception("deviceId field is required and can't be null.");
        }

        if (developerFeatures.isEmpty() && !useNoFeatures) {
            throw new Exception("Features should be defined.");
        }

        if (levelPacks.isEmpty() && !useNoLevels) {
            throw new Exception("Levels should be imported.");
        }

        Object cachedData = repository.getObjectFromCache(SLTConfig.APP_DATA_URL_CACHE);
        if (cachedData == null) {
            for (Map.Entry<String, SLTFeature> entry : developerFeatures.entrySet()) {
                activeFeatures.put(entry.getKey(), entry.getValue());
            }
        }
        else {
            activeFeatures = SLTDeserializer.decodeFeatures((SLTResponseAppData) cachedData);
            experiments = SLTDeserializer.decodeExperiments((SLTResponseAppData) cachedData);
            saltrUserId = ((SLTResponseAppData) cachedData).getSaltrUserId().toString();
        }

        started = true;
    }

    public void connect(SLTDataHandler saltrHttpDataHandler, Object basicProperties, Object customProperties) throws Exception {
        this.saltrHttpDataHandler = saltrHttpDataHandler;
        if (isLoading || !started) {
            return;
        }

        isLoading = true;

        SLTHttpConnection connection = createAppDataConnection(basicProperties, customProperties);
        SLTCallBackProperties details = new SLTCallBackProperties(SLTDataType.APP);

        try {
            connection.call(this, details);
        } catch (Exception e) {
            appDataLoadFailCallback();
        }
    }

    public void loadLevelContent(SLTLevel sltLevel, boolean useCache, SLTDataHandler saltrHttpDataHandler) throws Exception {
        this.saltrHttpDataHandler = saltrHttpDataHandler;

        Object content;
        if (!connected) {
            if (useCache) {
                content = loadLevelContentInternally(sltLevel);
            }
            else {
                content = loadLevelContentFromDisk(sltLevel);
            }
            levelContentLoadSuccessHandler(sltLevel, content);
        }
        else {
            if (!useCache || sltLevel.getVersion() != getCachedLevelVersion(sltLevel)) {
                loadLevelContentFromSaltr(sltLevel);
            }
            else {
                content = loadLevelContentFromCache(sltLevel);
                levelContentLoadSuccessHandler(sltLevel, content);
            }
        }
    }

    public void addProperties(Object basicProperties, Object customProperties) throws Exception {

        SLTCallBackProperties details = new SLTCallBackProperties(SLTDataType.PLAYER_PROPERTY);
        SLTHttpConnection connection = createAddPropConnection(basicProperties, customProperties);

        try {
            if (connection != null) {
                connection.call(this, details);
            }
            else {
                System.err.println("error");
            }
        } catch (Exception e) {
            System.err.println("error");
        }
    }

    private SLTHttpConnection createAddPropConnection(Object basicProperties, Object customProperties) throws Exception {
        if (basicProperties != null && customProperties != null || saltrUserId != null) {
            return null;
        }

        Map<String, Object> args = new HashMap<>();

        args.put("clientKey", clientKey);

        if (deviceId != null) {
            args.put("deviceId", deviceId);
        } else {
            throw new Exception("Field 'deviceId' is a required.");
        }

        if (socialId != null) {
            args.put("socialId", socialId);

        }

        if (saltrUserId != null) {
            args.put("saltrUserId", saltrUserId);
        }

        if (basicProperties != null) {
            args.put("basicProperties", basicProperties);
        }

        if (customProperties != null) {
            args.put("customProperties", customProperties);
        }

        RequestParams params = new RequestParams();
        params.put("args", gson.toJson(args));
        params.put("cmd", SLTConfig.CMD_ADD_PROPERTIES);

        return new SLTHttpConnection(SLTConfig.SALTR_API_URL, params);
    }

    private SLTHttpConnection createAppDataConnection(Object basicProperties, Object customProperties) throws Exception {
        Map<String, Object> args = new HashMap<>();

        args.put("clientKey", clientKey);

        if (deviceId != null) {
            args.put("deviceId", deviceId);
        } else {
            throw new Error("Field 'deviceId' is a required.");
        }

        if (socialId != null) {
            args.put("socialId", socialId);
        }

        if (saltrUserId != null) {
            args.put("saltrUserId", saltrUserId);
        }

        if (basicProperties != null) {
            args.put("basicProperties", basicProperties);
        }

        if (customProperties != null) {
            args.put("customProperties", customProperties);
        }


        RequestParams params = new RequestParams();
        params.put("args", gson.toJson(args));
        params.put("cmd", SLTConfig.CMD_APP_DATA);

        return new SLTHttpConnection(SLTConfig.SALTR_API_URL, params);
    }

    protected void appDataLoadSuccessCallback(String json) throws Exception {
        SLTResponse<SLTResponseAppData> data = gson.fromJson(json, new TypeToken<SLTResponse<SLTResponseAppData>>() {
        }.getType());

        if (data == null) {
            saltrHttpDataHandler.onFailure(new SLTStatusAppDataLoadFail());
            return;
        }

        SLTResponseAppData response = data.getResponseData();
        isLoading = false;

        if (devMode) {
            syncDeveloperFeatures();
        }

        if (data.getStatus().equals(SLTConfig.RESULT_SUCCEED)) {
            Map<String, SLTFeature> saltrFeatures;
            try {
                saltrFeatures = SLTDeserializer.decodeFeatures(response);
            } catch (Exception e) {
                saltrFeatures = null;
                saltrHttpDataHandler.onFailure(new SLTStatusFeaturesParseError());
            }

            try {
                experiments = SLTDeserializer.decodeExperiments(response);
            } catch (Exception e) {
                saltrHttpDataHandler.onFailure(new SLTStatusExperimentsParseError());
            }

            try {
                levelPacks = SLTDeserializer.decodeLevels(response);
            } catch (Exception e) {
                saltrHttpDataHandler.onFailure(new SLTStatusLevelsParseError());
            }

            saltrUserId = response.getSaltrUserId().toString();
            connected = true;
            repository.cacheObject(SLTConfig.APP_DATA_URL_CACHE, "0", response);

            activeFeatures = saltrFeatures;
            saltrHttpDataHandler.onSuccess(this);

            System.out.println("[SALTR] AppData load success. LevelPacks loaded: " + levelPacks.size());
        }
        else {
            saltrHttpDataHandler.onFailure(new SLTStatus(data.getErrorCode(), data.getResponseMessage()));
        }
    }

    protected void appDataLoadFailCallback() {
        isLoading = false;
        saltrHttpDataHandler.onFailure(new SLTStatusAppDataLoadFail());
    }

    public void syncDeveloperFeatures() throws Exception {
        SLTHttpConnection connection = createSyncFeaturesConnection();
        SLTCallBackProperties details = new SLTCallBackProperties(SLTDataType.FEATURE);
        try {
            connection.call(this, details);
        } catch (Exception e) {
        }
    }

    private SLTHttpConnection createSyncFeaturesConnection() throws Exception {
        List<Map<String, String>> featureList = new ArrayList<>();
        for (Map.Entry<String, SLTFeature> entry : developerFeatures.entrySet()) {
            Map<String, String> tempMap = new HashMap<>();
            tempMap.put("token", entry.getValue().getToken());
            tempMap.put("value", gson.toJson(entry.getValue().getProperties()));
            featureList.add(tempMap);
        }

        Map<String, Object> args = new HashMap<>();
        args.put("clientKey", clientKey);
        args.put("developerFeatures", featureList);

        if (deviceId != null) {
            args.put("deviceId", deviceId);
        } else {
            throw new Error("Field 'deviceId' is a required.");
        }

        if (socialId != null) {
            args.put("socialId", socialId);
        }

        if (saltrUserId != null) {
            args.put("saltrUserId", saltrUserId);
        }

        RequestParams params = new RequestParams();
        params.put("args", gson.toJson(args));
        params.put("cmd", SLTConfig.CMD_DEV_SYNC_FEATURES);

        return new SLTHttpConnection(SLTConfig.SALTR_URL, params);
    }

    private String getCachedLevelVersion(SLTLevel sltLevel) {
        String cachedFileName = MessageFormat.format(SLTConfig.LOCAL_LEVEL_CONTENT_CACHE_URL_TEMPLATE, sltLevel.getPackIndex(), sltLevel.getLocalIndex());
        return repository.getObjectVersion(cachedFileName);
    }

    private void cacheLevelContent(SLTLevel sltLevel, Object contentData) {
        String cachedFileName = MessageFormat.format(SLTConfig.LOCAL_LEVEL_CONTENT_CACHE_URL_TEMPLATE, sltLevel.getPackIndex(), sltLevel.getLocalIndex());
        repository.cacheObject(cachedFileName, sltLevel.getVersion(), contentData);
    }

    private Object loadLevelContentInternally(SLTLevel sltLevel) {
        Object content = loadLevelContentFromCache(sltLevel);
        if (content == null) {
            content = loadLevelContentFromDisk(sltLevel);
        }
        return content;
    }

    private Object loadLevelContentFromCache(SLTLevel sltLevel) {
        String url = MessageFormat.format(SLTConfig.LOCAL_LEVEL_CONTENT_CACHE_URL_TEMPLATE, sltLevel.getPackIndex(), sltLevel.getLocalIndex());
        return repository.getObjectFromCache(url);
    }

    private Object loadLevelContentFromDisk(SLTLevel sltLevel) {
        String url = MessageFormat.format(SLTConfig.LOCAL_LEVEL_CONTENT_PACKAGE_URL_TEMPLATE, sltLevel.getPackIndex(), sltLevel.getLocalIndex());
        return repository.getObjectFromApplication(url);
    }


    protected void loadLevelContentFromSaltr(SLTLevel level) throws Exception {
        String dataUrl = level.getContentUrl() + "?_time_=" + new Date().getTime();

        SLTCallBackProperties details = new SLTCallBackProperties(SLTDataType.LEVEL);
        details.setLevel(level);

        try {
            SLTHttpConnection connection = new SLTHttpConnection(dataUrl);
            connection.call(this, details);
        } catch (Exception e) {
            loadFromSaltrFailCallback(details);
        }
    }

    protected void levelContentLoadSuccessHandler(SLTLevel sltLevel, Object content) throws Exception {
        sltLevel.updateContent((SLTResponseLevelData) content);
        saltrHttpDataHandler.onSuccess(this);
    }

    protected void contentDataLoadSuccessCallback(SLTLevel level, SLTResponseLevelData data) throws Exception {
        level.updateContent(data);
        saltrHttpDataHandler.onSuccess(this);
    }

    protected void levelContentLoadFailHandler() {
        saltrHttpDataHandler.onFailure(new SLTStatusLevelContentLoadFail());
    }

    protected void loadFromSaltrSuccessCallback(Object data, SLTCallBackProperties properties) throws Exception {
        if (data != null) {
            cacheLevelContent(properties.getLevel(), data);
        }
        else {
            data = loadLevelContentInternally(properties.getLevel());
        }

        if (data != null) {
            levelContentLoadSuccessHandler(properties.getLevel(), gson.fromJson(data.toString(), SLTResponseLevelData.class));
        }
        else {
            levelContentLoadFailHandler();
        }
    }

    protected void loadFromSaltrFailCallback(SLTCallBackProperties properties) throws Exception {
        Object contentData = loadLevelContentInternally(properties.getLevel());
        contentDataLoadSuccessCallback(properties.getLevel(), gson.fromJson(contentData.toString(), SLTResponseLevelData.class));
    }
}