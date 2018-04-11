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

package cloud.elit.sdk.nlp.structure.node;

import cloud.elit.sdk.util.DSUtils;
import org.magicwerk.brownies.collections.GapList;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public abstract class AbstractNode<N extends AbstractNode<N>> {
    // fields
    protected int     token_id;
    protected String  token;
    protected String  lemma;
    protected String  syn_tag;
    protected String  ner_tag;
    protected FeatMap feat_map;

    // primary dependencies
    protected N parent;
    protected N left_sibling;
    protected N right_sibling;
    protected List<N> children;

//  =================================== Constructors ===================================

    public AbstractNode(int token_id, String token, String lemma, String syn_tag, String ner_tag, FeatMap feat_map) {
        init(token_id, token, lemma, syn_tag, ner_tag, feat_map);
    }

    public AbstractNode() {
        this(-1, null, null, null, null, new FeatMap());
    }

    public void init(int token_id, String token, String lemma, String syn_tag, String ner_tag, FeatMap feat_map) {
        setTokenID(token_id);
        setToken(token);
        setLemma(lemma);
        setSyntacticTag(syn_tag);
        setNamedEntityTag(ner_tag);
        setFeatMap(feat_map);

        parent        = null;
        left_sibling  = null;
        right_sibling = null;
        children      = new GapList<>();
    }

//  =================================== Abstract ===================================

    /** @return this node. */
    public abstract N self();

    /** @return the index of the child. */
    public abstract int getChildIndex(N node);

    /** @return the default index for add. */
    protected abstract int getDefaultIndex(List<N> list, N node);

//  =================================== Fields ===================================

    public int getTokenID() {
        return token_id;
    }

    public void setTokenID(int id) {
        this.token_id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String getSyntacticTag() {
        return syn_tag;
    }

    public void setSyntacticTag(String tag) {
        this.syn_tag = tag;
    }

    public String getNamedEntityTag() {
        return ner_tag;
    }

    public void setNamedEntityTag(String tag) {
        this.ner_tag = tag;
    }

    public Map<String, String> getFeatMap() {
        return feat_map;
    }

    public void setFeatMap(FeatMap map) {
        this.feat_map = map;
    }

    public String getFeat(String key) {
        return feat_map.get(key);
    }

    public String putFeat(String key, String value) {
        return feat_map.put(key, value);
    }

    public String removeFeat(String key) {
        return feat_map.remove(key);
    }

//  =================================== Child ===================================

    /**
     * @param index the index.
     * @return the index'th child of this node if exists; otherwise, {@code null}.
     */
    public N getChild(int index) {
        return DSUtils.isRange(children, index) ? children.get(index) : null;
    }

    /**
     * @return the first child of this node if exists; otherwise, {@code null}.
     */
    public N getFirstChild() {
        return getFirstChild(0);
    }
    
    /**
     * @param order displacement (0: 1st, 1: 2nd, etc.).
     * @return the order'th child of this node if exists; otherwise, {@code null}.
     */
    public N getFirstChild(int order) {
        return getChild(order);
    }
    
    /**
     * @param matcher the condition.
     * @return the first child matching the specific condition.
     */
    public N getFirstChild(Predicate<N> matcher) {
        return DSUtils.getFirst(children, matcher);
    }
    
    /** @return the last child of this node if exists; otherwise, {@code null}. */
    public N getLastChild() {
        return getLastChild(0);
    }
    
    /**
     * @param order displacement (0: last, 1: 2nd to the last, etc.).
     * @return the order'th last child of this node if exists; otherwise, {@code null}.
     */
    public N getLastChild(int order) {
        return getChild(children.size() - order - 1);
    }
    
    /**
     * * @param matcher the condition.
     * @return the last child matching the specific condition.
     */
    public N getLastChild(Predicate<N> matcher) {
        return DSUtils.getLast(children, matcher);
    }
    
    /**
     * Adds a child to the default location of this node.
     * @param node the node.
     * @return {@code true} if the child is added; otherwise, {@code false}.
     */
    public boolean addChild(N node) {
        return addChild(getDefaultIndex(children, node), node);
    }

    /**
     * Adds a node as the index'th child of this node if it is not already a child of this node.
     * @param index the index.
     * @param node the node.
     * @return {@code true} if the specific node is added; otherwise, {@code false}.
     */
    public boolean addChild(int index, N node) {
        if (!isParentOf(node)) {
            if (node.hasParent())
                node.parent.removeChild(node);
            
            node.parent = self();
            children.add(index, node);
            setSiblings(getChild(index-1), node);
            setSiblings(node, getChild(index+1));
            return true;
        }
        
        return false;
    }
    
    /**
     * Sets a node as the index'th child of this node if it is not already a child of this node.
     * @param index the index.
     * @param node the node.
     * @return the previously index'th node if added; otherwise, {@code null}.
     */
    public N setChild(int index, N node) {
        if (!isParentOf(node)) {
            if (node.hasParent())
                node.parent.removeChild(node);
            
            node.parent = self();
            N old = children.set(index, node);
            setSiblings(getChild(index-1), node);
            setSiblings(node, getChild(index+1));
            old.isolate();
            return old;    
        }
        
        return null;
    }
    
    /**
     * Removes a child from this node.
     * @param node the node.
     * @return the removed child if exists; otherwise, {@code null}.
     */
    public N removeChild(N node) {
        return removeChild(getChildIndex(node));
    }
    
    /**
     * Removes the index'th child of this node.
     * @param index the index.
     * @return the removed child if exists; otherwise, {@code null}.
     */
    public N removeChild(int index) {
        if (DSUtils.isRange(children, index)) {
            setSiblings(getChild(index-1), getChild(index+1));
            N node = children.remove(index);
            node.isolate();
            return node;
        }
        
        return null;
    }
    
    /**
     * Replaces the old child with the new child.
     * @param old_child the old child.
     * @param new_child the new child.
     */
    public boolean replaceChild(N old_child, N new_child) {
        int index = getChildIndex(old_child);
        
        if (index >= 0) {
            if (new_child.hasParent())
                new_child.parent.removeChild(new_child);
            
            setChild(index, new_child);
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes this node from its parent.
     * If this is the only child, remove its parent from its grandparent, and applies this logic recursively to the ancestors.
     */
    public void removeSelf() {
        N node = self(), parent;
    
        while (node.hasParent()) {
            parent = node.parent;
            parent.removeChild(node);
            if (parent.hasChild()) break;
            node = parent;
        }
    }
    
    /**
     * @return {@code true} if this node has any child; otherwise, {@code false}.
     */
    public boolean hasChild() {
        return !children.isEmpty();
    }

    /**
     * @return {@code true} if this node is a child of the specific node; otherwise, {@code false}.
     */
    public boolean isChildOf(N node) {
        return node != null && parent == node;
    }

    /**
     * @return {@code true} if this node contains a child matching the specific condition; otherwise, {@code false}.
     */
    public boolean containsChild(Predicate<N> matcher) {
        return DSUtils.contains(children, matcher);
    }

//  =================================== Descendants ===================================

    /**
     * @return the number of children.
     */
    public int getChildrenSize() {
        return children.size();
    }

    /**
     * @return the list of children.
     */
    public List<N> getChildren() {
        return children;
    }

    /**
     * The sublist begins at the specific position and extends to the end.
     * @param fst_id the ID of the first child (inclusive).
     * @return an immutable list of sub-children.
     */
    public List<N> getChildren(int fst_id) {
        return children.subList(fst_id, getChildrenSize());
    }

    /**
     * The sublist begins and ends at the specific positions.
     * @param fst_id the ID of the first child (inclusive).
     * @param lst_id the ID of the last child (exclusive)
     * @return an immutable list of sub-children.
     */
    public List<N> getChildren(int fst_id, int lst_id) {
        return children.subList(fst_id, lst_id);
    }

    /**
     * @param matcher the condition.
     * @return the list of children matching the specific condition.
     */
    public List<N> getChildren(Predicate<N> matcher) {
        return DSUtils.getMatchedList(children, matcher);
    }

    /**
     * @return the list of grand-children.
     */
    public List<N> getGrandChildren() {
        return getSecondOrder(N::getChildren);
    }

    /**
     * @param matcher the condition.
     * @return the first descendant matching the specific condition.
     */
    public N getFirstDescendant(Predicate<N> matcher) {
        return getFirstDescendantAux(children, matcher);
    }
    
    private N getFirstDescendantAux(Collection<N> nodes, Predicate<N> matcher) {
        for (N node : nodes) {
            if (matcher.test(node))
                return node;
            
            if ((node = getFirstDescendantAux(node.children, matcher)) != null)
                return node;
        }
        
        return null;
    }
    
    /**
     * @param matcher the condition.
     * @return the first lowest descendant whose intermediate ancestors to this node all match the specific condition.
     */
    public N getFirstLowestChainedDescendant(Predicate<N> matcher) {
        N node = getFirstChild(matcher), descendant = null;
        
        while (node != null) {
            descendant = node;
            node = node.getFirstChild(matcher);
        }

        return descendant;
    }
    
    /**
     * @param node the node.
     * @return {@code true} if the node is a descendant of the specific node.
     */
    public boolean isDescendantOf(N node) {
        return getNearestNode(n -> n == node, N::getParent) != null;
    }

//  =================================== Ancestors ===================================
    
    /**
     * @return the parent of this node if exists; otherwise, {@code null}.
     */
    public N getParent() {
        return parent;
    }
    
    /**
     * @return the grandparent of this node if exists; otherwise, {@code null}.
     */
    public N getGrandParent() {
        return getAncestor(2);
    }
    
    /**
     * @param height height of the ancestor from this node (1: parent, 2: grandparent, etc.).
     * @return the height'th nearest ancestor of this node if exists; otherwise, nul{@code null}.
     */
    public N getAncestor(int height) {
        return getNode(height, n -> n.parent);
    }
    
    /**
     * @param matcher the condition.
     * @return the lowest ancestor matching the specific condition.
     */
    public N getLowestAncestor(Predicate<N> matcher) {
        return getNearestNode(matcher, N::getParent);
    }

    /**
     * @param matcher
     * @return the highest ancestor where all the intermediate ancestors match the specific condition.
     */
    public N getHighestChainedAncestor(Predicate<N> matcher) {
        N node = parent, ancestor = null;
        
        while (node != null) {
            if (matcher.test(node)) ancestor = node;
            else break;

            node = node.parent;
        }
        
        return ancestor;
    }
    
    /**
     * @return the set of all ancestors of this node.
     */
    public Set<N> getAncestorSet() {
        Set<N> set = new HashSet<>();
        N node = getParent();
        
        while (node != null) {
            set.add(node);
            node = node.getParent();
        }
        
        return set;
    }
    
    /**
     * @param node the node.
     * @return the lowest common ancestor of this node and the specified node.
     */
    public N getLowestCommonAncestor(N node) {
        Set<N> set = getAncestorSet();
        set.add(self());
        
        while (node != null) {
            if (set.contains(node)) return node;
            node = node.getParent();
        }
        
        return null;
    }
    
    /**
     * @param node the parent to be set.
     * Sets the parent of this node and its siblings.
     */
    public void setParent(N node) {
        if (node == null) {
            if (hasParent())
                this.parent.removeChild(self());
        }
        else
            node.addChild(self());
    }
    
    /**
     * @param node the node.
     * @return {@code true} if this node is the parent of the specific node; otherwise, {@code false}.
     */
    public boolean isParentOf(N node) {
        return node.isChildOf(self());
    }
    
    /**
     * @param node the node.
     * @return {@code true} if the node is a descendant of the specific node; otherwise, {@code false}.
     */
    public boolean isAncestorOf(N node) {
        return node.isDescendantOf(self());
    }
    
    /**
     * @return {@code true} if this node has a parent; otherwise, {@code false}.
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * @param matcher the condition
     * @return {@code true} if this node has a parent matching the specific condition; otherwise, {@code false}.
     */
    public boolean hasParent(Predicate<N> matcher) {
        return hasParent() && matcher.test(parent);
    }

    /**
     * @return {@code true} if this node has a grand parent; otherwise, {@code false}.
     */
    public boolean hasGrandParent() {
        return getGrandParent() != null;
    }

//  =================================== Siblings ===================================

    /**
     * @return the list of all siblings.
     */
    public List<N> getSiblings() {
        return hasParent() ? parent.children.stream().filter(n -> n != self()).collect(Collectors.toList()) : new ArrayList<>();
    }

    /**
     * @return the left-nearest sibling of this node if exists; otherwise, {@code null}.
     */
    public N getLeftNearestSibling() {
        return left_sibling;
    }
    
    /**
     * @param order displacement (0: left-nearest, 1: 2nd left-nearest, etc.).
     * @return the order'th left-nearest sibling of this node if exists; otherwise, {@code null}.
     */
    public N getLeftNearestSibling(int order) {
        return order >= 0 ? getNode(order+1, N::getLeftNearestSibling) : null; 
    }

    /**
     * @param matcher the condition.
     * @return the left nearest sibling that matches the specific condition.
     */
    public N getLeftNearestSibling(Predicate<N> matcher) {
        return getNearestNode(matcher, N::getLeftNearestSibling);
    }
    
    /**
     * @return the right-nearest sibling of this node if exists; otherwise, {@code null}.
     */
    public N getRightNearestSibling() {
        return right_sibling;
    }
    
    /**
     * @param order displacement (1: right-nearest, 2: 2nd right-nearest, etc.).
     * @return the order'th right-nearest sibling of this node if exists; otherwise, {@code null}.
     */
    public N getRightNearestSibling(int order) {
        return order >= 0 ? getNode(order+1, N::getRightNearestSibling) : null;
    }

    /**
     * @param matcher the condition.
     * @return the right nearest sibling that matches the specific condition.
     */
    public N getRightNearestSibling(Predicate<N> matcher) {
        return getNearestNode(matcher, N::getRightNearestSibling);
    }
    
    /**
     * @return {@code true} if this node has a left sibling; otherwise, {@code false}.
     */
    public boolean hasLeftSibling() {
        return left_sibling != null;
    }

    /**
     * @param matcher the condition.
     * @return {@code true} if this node has a left sibling that matches the condition; otherwise, {@code false}.
     */
    public boolean hasLeftSibling(Predicate<N> matcher) {
        return getLeftNearestSibling(matcher) != null;
    }
    
    /**
     * @return true if this node has a right sibling.
     */
    public boolean hasRightSibling() {
        return right_sibling != null;
    }

    /**
     * @param matcher the condition.
     * @return {@code true} if this node has a right sibling that matches the condition; otherwise, {@code false}.
     */
    public boolean hasRightSibling(Predicate<N> matcher) {
        return getRightNearestSibling(matcher) != null;
    }
    
    /**
     * @param node the node.
     * @return true if this node is a sibling of the specific node.
     */
    public boolean isSiblingOf(N node) {
        return node.isChildOf(parent);
    }
    
    /**
     * @param node the node.
     * @return true if this node is a left sibling of the specific node.
     */
    public boolean isLeftSiblingOf(N node) {
        return node != null && parent == node.parent && getNearestNode(n -> n == node, N::getRightNearestSibling) != null;
    }
    
    /**
     * @param node the node.
     * @return true if this node is a right sibling of the specific node.
     */
    public boolean isRightSiblingOf(N node) {
        return node.isLeftSiblingOf(self());
    }

//  =================================== Helpers ===================================
    
    public Stream<N> flatten() {
        return Stream.concat(Stream.of(self()), children.stream().flatMap(N::flatten));
    }
    
    /**
     * @param order 0: self, 1: nearest, 2: second nearest, etc.
     * @param getter takes a node and returns a node.
     * @return the order'th node with respect to the getter.
     */
    public N getNode(int order, Function<N, N> getter) {
        N node = self();
        
        for (int i=0; i<order; i++) {
            if (node == null) return node;
            node = getter.apply(node);
        }
        
        return node;
    }
    
    /**
     * @param matcher takes a node and the supplement, and returns true if its field matches to the specific predicate.
     * @param getter takes a node and returns a node.
     * @return the first node matching the specific condition.
     */
    public N getNearestNode(Predicate<N> matcher, Function<N, N> getter) {
        N node = getter.apply(self());
        
        while (node != null) {
            if (matcher.test(node)) return node;
            node = getter.apply(node);
        }
        
        return null;
    }
    
    public int distanceToTop()
    {
        N node = parent;
        int dist;
        
        for (dist=0; node != null; dist++)
            node = node.parent;
        
        return dist;
    }
    
    /** Isolates this node from its parent, children, and siblings. */
    protected void isolate()
    {
        parent        = null;
        left_sibling  = null;
        right_sibling = null;
    }
    
    /** Sets two nodes siblings of each other. */
    protected void setSiblings(N left, N right)
    {
        if (left  != null)    left.right_sibling = right;
        if (right != null)    right.left_sibling = left;
    }
    
    /**
     * @param getter takes a node and returns a list of nodes.
     * @return the list of second order elements according to the getter. 
     */
    protected List<N> getSecondOrder(Function<N,List<N>> getter)
    {
        return getter.apply(self()).stream().flatMap(n -> getter.apply(n).stream()).filter(n -> n != self()).collect(Collectors.toList());
    }
}
