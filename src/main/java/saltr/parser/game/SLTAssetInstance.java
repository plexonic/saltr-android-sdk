/**
 * Copyright Teoken LLC. (c) 2013. All rights reserved.
 * Copying or usage of any piece of this source code without written notice from Teoken LLC is a major crime.
 * Այս կոդը Թեոկեն ՍՊԸ ընկերության սեփականությունն է:
 * Առանց գրավոր թույլտվության այս կոդի պատճենահանումը կամ օգտագործումը քրեական հանցագործություն է:
 */
package saltr.parser.game;

import java.util.List;

public class SLTAssetInstance {
    protected List<SLTAssetState> states;
    protected Object properties;
    protected String token;

    public SLTAssetInstance(String token, List<SLTAssetState> states, Object properties) {
        this.states = states;
        this.properties = properties;
        this.token = token;
    }

    public List<SLTAssetState> getStates() {
        return states;
    }

    public Object getProperties() {
        return properties;
    }

    public String getToken() {
        return token;
    }
}
