/*
 * Copyright (c) 2014 Plexonic Ltd
 */
package saltr.game.matching;

import saltr.game.*;
import saltr.response.level.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The SLTMatchingLevelParser class represents the matching level parser.
 */
public class SLTMatchingLevelParser extends SLTLevelParser {

    private static SLTMatchingLevelParser instance;

    private SLTMatchingLevelParser() {
    }

    /**
     * @return The  instance of SLTMatchingLevelParser class.
     */
    public static SLTMatchingLevelParser getInstance() {
        if (instance == null) {
            instance = new SLTMatchingLevelParser();
        }
        return instance;
    }

    private static void initializeCells(SLTCell[][] cells, SLTResponseBoard boardNode) {
        List<List<Integer>> blockedCells = boardNode.getBlockedCells();
        if (blockedCells == null) {
            blockedCells = new ArrayList<List<Integer>>();
        }

        List<SLTResponseBoardPropertyCell> cellProperties = boardNode.getCellProperties();
        if (cellProperties == null) {
            cellProperties = new ArrayList<SLTResponseBoardPropertyCell>();
        }

        for (int i = 0; i < boardNode.getCols(); i++) {
            for (int j = 0; j < boardNode.getRows(); j++) {
                cells[i][j] = new SLTCell(i, j);
            }
        }

        //assigning cell properties
        for (SLTResponseBoardPropertyCell property : cellProperties) {
            SLTCell cell2 = cells[property.getCoords().get(0)][property.getCoords().get(1)];
            if (cell2 != null) {
                cell2.setProperties(property.getValue());
            }
        }

        //blocking cells
        for (List<Integer> blockedCell : blockedCells) {
            SLTCell cell3 = cells[blockedCell.get(0)][blockedCell.get(1)];
            if (cell3 != null) {
                cell3.setIsBlocked(true);
            }
        }
    }

    private static SLTMatchingBoard parseLevelBoard(SLTResponseBoard boardNode, Map<String, SLTAsset> assetMap) {
        Map<String, Object> boardProperties = boardNode.getProperties();
        if (boardProperties == null) {
            boardProperties = new HashMap<String, Object>();
        }

        SLTCell[][] cells = new SLTCell[boardNode.getCols()][boardNode.getRows()];
        initializeCells(cells, boardNode);

        List<SLTBoardLayer> layers = new ArrayList<SLTBoardLayer>();
        int i = 0;
        for (SLTResponseBoardLayer layerNode : boardNode.getLayers()) {
            SLTMatchingBoardLayer layer = parseLayer(layerNode, i, cells, assetMap);
            layers.add(layer);
            i++;
        }

        return new SLTMatchingBoard(cells, layers, boardProperties);
    }

    private static void parseLayerChunks(SLTMatchingBoardLayer layer, List<SLTResponseBoardChunk> chunkNodes, SLTCell[][] cellMatrix, Map<String, SLTAsset> assetMap) {
        for (SLTResponseBoardChunk chunkNode : chunkNodes) {
            List<List<Integer>> cellNodes = chunkNode.getCells();
            List<SLTCell> chunkCells = new ArrayList<SLTCell>();
            for (List<Integer> cellNode : cellNodes) {
                chunkCells.add(cellMatrix[cellNode.get(0)][cellNode.get(1)]);
            }

            List<SLTResponseBoardChunkAsset> assetNodes = chunkNode.getAssets();
            List<SLTChunkAssetRule> chunkAssetRules = new ArrayList<SLTChunkAssetRule>();
            for (SLTResponseBoardChunkAsset assetNode : assetNodes) {
                chunkAssetRules.add(new SLTChunkAssetRule(assetNode.getAssetId(), assetNode.getDistributionType(),
                        assetNode.getDistributionValue(), assetNode.getStates()));
            }
            layer.addChunk(new SLTChunk(layer.getToken(), layer.getIndex(), chunkCells, chunkAssetRules, assetMap));
        }
    }

    private static void parseFixedAssets(SLTMatchingBoardLayer layer, List<SLTResponseBoardFixedAsset> assetNodes, SLTCell[][] cells, Map<String, SLTAsset> assetMap) {
        for (SLTResponseBoardFixedAsset assetInstanceNode : assetNodes) {
            SLTAsset asset = assetMap.get(assetInstanceNode.getAssetId().toString());
            List<String> stateIds = assetInstanceNode.getStates();
            int[][] cellPositions = assetInstanceNode.getCells();

            for (int[] position : cellPositions) {
                SLTCell cell = cells[position[0]][position[1]];
                cell.setAssetInstance(layer.getToken(), layer.getIndex(), new SLTAssetInstance(asset.getToken(), asset.getInstanceStates(stateIds), asset.getProperties()));
            }
        }
    }

    private static SLTMatchingBoardLayer parseLayer(SLTResponseBoardLayer layerNode, int layerIndex, SLTCell[][] cells, Map<String, SLTAsset> assetMap) {
        SLTMatchingBoardLayer layer = new SLTMatchingBoardLayer(layerNode.getToken(), layerIndex);
        parseFixedAssets(layer, layerNode.getFixedAssets(), cells, assetMap);
        parseLayerChunks(layer, layerNode.getChunks(), cells, assetMap);
        return layer;
    }

    /**
     * Parses the level content.
     *
     * @param boardNodes The board nodes.
     * @param assetMap   The asset map.
     * @return The parsed boards.
     */
    @Override
    public Map<String, SLTBoard> parseLevelContent(Map<String, SLTResponseBoard> boardNodes, Map<String, SLTAsset> assetMap) {
        Map<String, SLTBoard> boards = new HashMap<String, SLTBoard>();
        for (Map.Entry<String, SLTResponseBoard> entry : boardNodes.entrySet()) {
            boards.put(entry.getKey(), parseLevelBoard(entry.getValue(), assetMap));
        }
        return boards;
    }
}
