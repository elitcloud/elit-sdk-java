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

package cloud.elit.sdk.structure.util;

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public interface Fields {
    /**
     * Sentence ID.
     */
    String SID = "sid";
    /**
     * Linguistic form.
     */
    String TOK = "tok";
    /**
     * Character-level offset of each form w.r.t. the original text.
     */
    String OFF = "off";
    /**
     * Lemma.
     */
    String LEM = "lem";
    /**
     * Part-of-speech tag.
     */
    String POS = "pos";
    /**
     * Named entity tag.
     */
    String NER = "ner";
    /**
     * Primary dependency.
     */
    String DEP = "dep";
    /**
     * Secondary dependency.
     */
    String DEP2 = "dep2";
    /**
     * Semantic tags.
     */
    String SEM = "sem";
    /**
     * Coreference relation.
     */
    String REF = "ref";

    /**
     * All components in the pipeline.
     */
    String ALL = "all";

}
