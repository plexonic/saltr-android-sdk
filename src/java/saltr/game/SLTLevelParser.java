/*
 * Copyright (c) 2014 Plexonic Ltd
 */
package saltr.game;

import saltr.response.level.SLTResponseAsset;
import saltr.response.level.SLTResponseBoard;
import saltr.response.level.SLTResponseBoardChunkAssetState;
import saltr.response.level.SLTResponseLevelContentData;

import java.util.HashMap;
import java.util.Map;

public abstract class SLTLevelParser {

    public abstract Map<String, SLTBoard> parseLevelContent(Map<String, SLTResponseBoard> boardNodes, Map<String, SLTAsset> assetMap);

    public Map<String, SLTAsset> parseLevelAssets(SLTResponseLevelContentData rootNode) {
        Map<String, SLTResponseAsset> assetNodes = rootNode.getAssets();
        Map<String, SLTAsset> assetMap = new HashMap<String, SLTAsset>();
        for (Map.Entry<String, SLTResponseAsset> entry : assetNodes.entrySet()) {
            assetMap.put(entry.getKey(), parseAsset(entry.getValue()));
        }
        return assetMap;
    }

    private SLTAsset parseAsset(SLTResponseAsset assetNode) {
        String token = null;
        Object properties = null;
        Map statesMap = null;

        if (assetNode.getToken() != null) {
            token = assetNode.getToken();
        }

        if (assetNode.getStates() != null) {
            statesMap = parseAssetStates(assetNode.getStates());
        }

        if (assetNode.getProperties() != null) {
            properties = assetNode.getProperties();
        }

        return new SLTAsset(token, statesMap, properties);
    }

    private Map<String, SLTAssetState> parseAssetStates(Map<String, SLTResponseBoardChunkAssetState> stateNodes) {
        Map<String, SLTAssetState> statesMap = new HashMap<String, SLTAssetState>();
        for (Map.Entry<String, SLTResponseBoardChunkAssetState> entry : stateNodes.entrySet()) {
            statesMap.put(entry.getKey(), parseAssetState(entry.getValue()));
        }

        return statesMap;
    }

    protected SLTAssetState parseAssetState(SLTResponseBoardChunkAssetState stateNode) {
        String token = null;
        Object properties = null;

        if (stateNode.getToken() != null) {
            token = stateNode.getToken();
        }

        if (stateNode.getProperties() != null) {
            properties = stateNode.getProperties();
        }

        return new SLTAssetState(token, properties);
    }
}
