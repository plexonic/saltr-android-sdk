/*
 * Copyright (c) 2014 Plexonic Ltd
 */
package saltr;

/**
 * Internal configuration of SDK.
 */
public class SLTConfig {
    public static final String ACTION_GET_APP_DATA = "getAppData";
    public static final String ACTION_ADD_PROPERTIES = "addProperties";
    public static final String ACTION_DEV_SYNC_CLIENT_DATA = "sync";
    public static final String ACTION_DEV_REGISTER_DEVICE = "registerDevice";
    public static final String SALTR_API_URL = "https://api.saltr.com/call";
    public static final String SALTR_DEVAPI_URL = "https://devapi.saltr.com/call";

    public static final String APP_DATA_URL_CACHE = "app_data_cache.json";
    /**
     * Default path to the local level files.
     */
    public static final String LOCAL_LEVELPACK_PACKAGE_URL = "saltr/level_packs.json";
    public static final String LOCAL_LEVEL_CONTENT_PACKAGE_URL_TEMPLATE = "saltr/pack_{0}/level_{1}.json";
    public static final String LOCAL_LEVEL_CONTENT_CACHE_URL_TEMPLATE = "pack_{0}_level_{1}.json";

    public static final String DEVICE_TYPE_ANDROID = "android";
    public static final String DEVICE_PLATFORM_ANDROID = "android";
}
