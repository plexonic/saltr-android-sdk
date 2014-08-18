/*
 * Copyright (c) 2014 Plexonic Ltd
 */
package saltr.response;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class SLTResponseAppData implements Serializable {
    private List<SLTResponsePack> levelPacks;
    private List<SLTResponseFeature> features;
    private List<SLTResponseExperiment> experiments;
    private UUID saltId;
    private UUID saltrUserId;
    private String levelType;

    public String getLevelType() {
        return levelType;
    }

    public void setLevelType(String levelType) {
        this.levelType = levelType;
    }

    public List<SLTResponseExperiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<SLTResponseExperiment> experiments) {
        this.experiments = experiments;
    }

    public List<SLTResponseFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<SLTResponseFeature> features) {
        this.features = features;
    }

    public List<SLTResponsePack> getLevelPacks() {
        return levelPacks;
    }

    public void setLevelPacks(List<SLTResponsePack> levelPacks) {
        this.levelPacks = levelPacks;
    }

    public UUID getSaltId() {
        return saltId;
    }

    public void setSaltId(UUID saltId) {
        this.saltId = saltId;
    }

    public UUID getSaltrUserId() {
        return saltrUserId;
    }

    public void setSaltrUserId(UUID saltrUserId) {
        this.saltrUserId = saltrUserId;
    }
}
