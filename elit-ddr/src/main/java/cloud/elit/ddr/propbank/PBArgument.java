/*
 * Copyright 2018 Emory University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.elit.ddr.propbank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.jetbrains.annotations.NotNull;
import cloud.elit.ddr.util.DSUtils;
import cloud.elit.ddr.util.StringConst;

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class PBArgument implements Serializable, Comparable<PBArgument> {
    private static final long serialVersionUID = -5706844524751492352L;
    /**
     * The delimiter between location and label ("-").
     */
    static public final String DELIM = StringConst.HYPHEN;

    private List<PBLocation> locations;
    private String label;

    public PBArgument() {
        locations = new ArrayList<>();
    }

    public PBArgument(String str) {
        locations = new ArrayList<>();
        int idx = str.indexOf(DELIM);
        String type;

        if (idx == -1)
            throw new IllegalArgumentException(str);

        StringTokenizer tok = new StringTokenizer(str.substring(0, idx), "*&,;", true);
        label = str.substring(idx + 1);

        if (!tok.hasMoreTokens())
            throw new IllegalArgumentException(str);

        addLocation(new PBLocation(tok.nextToken(), StringConst.EMPTY));

        while (tok.hasMoreTokens()) {
            type = tok.nextToken();

            if (!tok.hasMoreTokens())
                throw new IllegalArgumentException(str);

            addLocation(new PBLocation(tok.nextToken(), type));
        }
    }

    public String getLabel() {
        return label;
    }

    /**
     * @return the index'th location of this argument if exists; otherwise, {@code null}.
     */
    public PBLocation getLocation(int index) {
        return DSUtils.isRange(locations, index) ? locations.get(index) : null;
    }

    /**
     * @return the first location matching the specific terminal ID and height in this argument.
     */
    public PBLocation getLocation(int terminalID, int height) {
        return locations.stream().filter(loc -> loc.matches(terminalID, height)).findFirst().orElse(null);
    }

    /**
     * @return a list of locations of this argument.
     */
    public List<PBLocation> getLocations() {
        return locations;
    }

    /**
     * @return the number of locations in this argument.
     */
    public int getLocationSize() {
        return locations.size();
    }

    /**
     * Adds the specific location to this argument.
     */
    public void addLocation(PBLocation location) {
        locations.add(location);
    }

    /**
     * Adds the specific collection of locations to this argument.
     */
    public void addLocations(Collection<PBLocation> locations) {
        this.locations.addAll(locations);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLocations(List<PBLocation> locations) {
        this.locations = locations;
    }

    /**
     * Removes all locations matching the specific terminal ID and height from this argument.
     */
    public void removeLocations(int terminalId, int height) {

        locations.removeIf(loc -> loc.matches(terminalId, height));
        if (!this.locations.isEmpty()) this.locations.get(0).setType(StringConst.EMPTY);
    }

    /**
     * Removes the specific collection of locations from this argument.
     */
    public void removeLocations(Collection<PBLocation> locations) {
        this.locations.removeAll(locations);
        if (!this.locations.isEmpty()) this.locations.get(0).setType(StringConst.EMPTY);
    }

    /**
     * Sorts the locations of this argument by their terminal IDs and heights.
     * @see PBLocation#compareTo(PBLocation)
     */
    public void sortLocations() {
        if (locations.isEmpty()) return;

        Collections.sort(locations);
        PBLocation fst = locations.get(0), loc;

        if (!fst.isType(StringConst.EMPTY)) {
            for (int i = 1; i < locations.size(); i++) {
                loc = locations.get(i);

                if (loc.isType(StringConst.EMPTY)) {
                    loc.setType(fst.getType());
                    break;
                }
            }

            fst.setType(StringConst.EMPTY);
        }
    }

    public boolean containsOperator(String operator) {
        return locations.stream().anyMatch(loc -> loc.isType(operator));
    }

    public boolean isLabel(String label) {
        return this.label.equals(label);
    }

    @Override
    public String toString() {
        StringBuilder build = new StringBuilder();

        for (PBLocation loc : locations)
            build.append(loc.toString());

        build.append(DELIM);
        build.append(label);

        return build.toString();
    }

    @Override
    public int compareTo(@NotNull PBArgument arg) {
        return getLocation(0).compareTo(arg.getLocation(0));
    }
}