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
package cloud.elit.sdk.structure;

import static java.util.stream.Collectors.toList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;
import cloud.elit.sdk.structure.node.NLPNode;
import cloud.elit.sdk.structure.util.ELITUtils;
import cloud.elit.sdk.structure.util.Fields;

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class Sentence implements Serializable, Comparable<Sentence>, Iterable<NLPNode> {
    private static final long serialVersionUID = -731632653624195711L;
    private int sen_id;
    private NLPNode root;
    private List<NLPNode> nodes;
    private List<Chunk> named_entities;

    //  =================================== Constructors ===================================

    @SuppressWarnings("unchecked")
    public <N> Sentence(int sen_id, List<N> nodes) {
        setID(sen_id);
        setRoot(ELITUtils.createRoot());
        setNamedEntities(new ArrayList<>());

        if (nodes == null || nodes.isEmpty()) setNodes(new ArrayList<>());
        else {
            N node = nodes.get(0);

            if (node instanceof String) {
                List<String> ns = (List<String>) nodes;
                setNodes(IntStream.range(0, nodes.size()).mapToObj(i -> new NLPNode(i, ns.get(i))).collect(toList()));
            } else if (node instanceof NLPNode) setNodes((List<NLPNode>) nodes);
            else throw new IllegalArgumentException("The node type must be either String or NLPNode");
        }
    }

    public <N> Sentence(List<N> nodes) {
        this(-1, nodes);
    }

    public Sentence() {
        this(new ArrayList<>());
    }

    //  =================================== Getters, Setters ===================================

    public int getID() {
        return sen_id;
    }

    public void setID(int id) {
        this.sen_id = id;
    }

    public NLPNode getRoot() {
        return root;
    }

    public void setRoot(NLPNode node) {
        this.root = node;
    }

    public List<NLPNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<NLPNode> nodes) {
        this.nodes = nodes;
    }

    public int size() {
        return nodes.size();
    }

    public NLPNode get(int index) {
        return index < 0 ? root : nodes.get(index);
    }

    public boolean add(NLPNode node) {
        return nodes.add(node);
    }

    public void add(int index, NLPNode node) {
        nodes.add(index, node);
    }

    public NLPNode set(int index, NLPNode node) {
        return nodes.set(index, node);
    }

    public NLPNode remove(int index) {
        return nodes.remove(index);
    }

    public boolean remove(NLPNode node) {
        return nodes.remove(node);
    }

    public List<Chunk> getNamedEntities() {
        return named_entities;
    }

    public void setNamedEntities(List<Chunk> entities) {
        this.named_entities = entities;
    }

    public int numNamedEntities() {
        return named_entities.size();
    }

    public Chunk getNamedEntity(int index) {
        return named_entities.get(index);
    }

    public void addNamedEntity(Chunk entity) {
        named_entities.add(entity);
    }

    public List<String> getTokens() {
        return nodes.stream().map(NLPNode::getToken).collect(toList());
    }

    public List<String> getLemmas() {
        return nodes.stream().map(NLPNode::getLemma).collect(toList());
    }

    public List<String> getPartOfSpeechTags() {
        return nodes.stream().map(NLPNode::getPartOfSpeechTag).collect(toList());
    }

    //  =================================== Helpers ===================================

    @NotNull
    @Override
    public Iterator<NLPNode> iterator() {
        return nodes.iterator();
    }

    @Override
    public int compareTo(@NotNull Sentence o) {
        return sen_id - o.sen_id;
    }

    public String toString() {
        if (nodes.isEmpty()) return "{}";
        NLPNode node = nodes.get(0);
        StringJoiner joiner = new StringJoiner(",");

        joiner.add("\"" + Fields.SID + "\":" + sen_id);
        joiner.add("\"" + Fields.TOK + "\":" + toStringList(NLPNode::getToken));
        if (node.getEndOffset() > 0) joiner.add("\"" + Fields.OFF + "\":" + toStringOffsets());
        if (node.getLemma() != null) joiner.add("\"" + Fields.LEM + "\":" + toStringList(NLPNode::getLemma));

        if (node.getPartOfSpeechTag() != null) joiner.add("\"" + Fields.POS + "\":" + toStringList(
                NLPNode::getPartOfSpeechTag));

        if (named_entities != null && !named_entities.isEmpty()) joiner.add("\"" + Fields.NER + "\":" + named_entities
                .toString());

        if (node.getDependencyLabel() != null) joiner.add("\"" + Fields.DEP + "\":" + toStringPrimaryDependencies());

        String s = toStringSecondaryDependencies();
        if (s != null) joiner.add("\"" + Fields.DEP2 + "\":" + s);

        s = toStringSemanticTags();
        if (s != null) joiner.add("\"" + Fields.SEM + "\":" + s);

        return "{" + joiner.toString() + "}";
    }

    private String toStringList(Function<NLPNode, String> f) {
        return "[" + nodes.stream().map(n -> "\"" + f.apply(n).replace("\"", "\\\"") + "\"").collect(Collectors.joining(
                ",")) + "]";
    }

    private String toStringOffsets() {
        return "[" + nodes.stream().map(n -> "[" + n.getBeginOffset() + "," + n.getEndOffset() + "]").collect(Collectors
                .joining(",")) + "]";
    }

    private String toStringPrimaryDependencies() {
        return "[" + nodes.stream().map(n -> "[" + n.getParent().getTokenID() + ",\"" + n.getDependencyLabel() + "\"]")
                .collect(Collectors.joining(",")) + "]";
    }

    private String toStringSecondaryDependencies() {
        StringJoiner join = new StringJoiner(",");

        for (NLPNode node : nodes) {
            String s = node.getSecondaryParents().stream().map(a -> "[" + node.getTokenID() + "," + a.getNode()
                    .getTokenID() + ",\"" + a.getLabel() + "\"]").collect(Collectors.joining(","));
            if (s.length() > 0) join.add(s);
        }

        return join.length() > 0 ? "[" + join.toString() + "]" : null;
    }

    private String toStringSemanticTags() {
        StringJoiner join = new StringJoiner(",");

        for (NLPNode node : nodes) {
            String s = node.getFeat(Fields.SEM);
            if (s != null) join.add("[" + node.getTokenID() + ",\"" + s + "\"]");
        }

        return join.length() > 0 ? "[" + join.toString() + "]" : null;
    }

    public String toTSV() {
        List<List<String>> conll = nodes.stream().map(NLPNode::toTSV).collect(toList());
        if (named_entities != null) toTSVNamedEntities(conll);
        return conll.stream().map(l -> l.stream().collect(Collectors.joining("\t"))).collect(Collectors.joining("\n"));
    }

    private void toTSVNamedEntities(List<List<String>> conll) {
        for (Chunk c : named_entities) {
            int bidx = c.get(0).getTokenID();
            int eidx = c.get(c.size() - 1).getTokenID();

            if (bidx == eidx) conll.get(bidx).add("U-" + c.getLabel());
            else {
                conll.get(bidx).add("B-" + c.getLabel());
                conll.get(eidx).add("L-" + c.getLabel());
                for (int i = bidx + 1; i < eidx; i++)
                    conll.get(i).add("I-" + c.getLabel());
            }
        }

        for (List<String> c : conll) {
            if (c.size() < 9) c.add("O");
        }
    }
}
