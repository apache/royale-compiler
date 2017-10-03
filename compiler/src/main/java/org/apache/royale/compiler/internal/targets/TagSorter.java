/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.royale.compiler.internal.targets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.exceptions.CircularDependencyException;
import org.apache.royale.compiler.internal.graph.Graph;
import org.apache.royale.compiler.internal.graph.GraphEdge;
import org.apache.royale.compiler.internal.graph.TopologicalSort;
import org.apache.royale.compiler.internal.graph.TopologicalSort.IVisitor;
import org.apache.royale.swf.io.SWFReader;
import org.apache.royale.swf.tags.ICharacterReferrer;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;

/**
 * This is an utility class that can sort tags in topological order. This is especially useful
 * for {@link ICharacterReferrer} tags because the sorting will guarantee that the tags are in
 * an order where referred tags appear before the referrer.
 * 
 * This ordering mimics the ordering in the 4.5.1 Flex Compiler for Native SWF Tags.
 * 
 * Please note that only {@link ICharacterTag} will have a stable ordering in addition to the 
 * topological ordering (it will be based on their character IDs). Other tags will be sorted by 
 * their toString functions, which may changed on execution.
 */
public class TagSorter
{
    private static class TagGraphVisitor implements IVisitor<ITag, GraphEdge<ITag>>
    {
        private List<ITag> orderedList;
        private Map<ITag, Integer> tagOrdering;

        public TagGraphVisitor (List<ITag> orderedList, Map<ITag, Integer> tagOrdering)
        {
            this.orderedList = orderedList;
            this.tagOrdering = tagOrdering;
        }

        @Override
        public int compare(ITag tag1, ITag tag2)
        {
            return tagOrdering.get(tag1) - tagOrdering.get(tag2);
        }

        @Override
        public void visit(ITag v)
        {
            orderedList.add(v);
        }

        /**
         * All ITag edges are Topological
         */
        @Override
        public boolean isTopologicalEdge(GraphEdge<ITag> e)
        {
            return true;
        }
        
        public List<ITag> getOrderedList()
        {
            return orderedList;
        }
    }
    
    /**
     * Adds tag and all referred {@link ITag} to a Graph
     * @param tagGraph
     * @param tag
     */
    private static void walkTags(Graph<ITag, GraphEdge<ITag>> tagGraph, ITag tag, Map<ITag, Integer> ordering)
    {
        if (tagGraph.addVertex(tag))
        {
            if (tag instanceof ICharacterReferrer)
            {
                for (ITag childTag : ((ICharacterReferrer)tag).getReferences())
                {
                    if (childTag == SWFReader.INVALID_TAG)
                        continue;

                    walkTags(tagGraph, childTag, ordering);
                    tagGraph.setEdge(new GraphEdge<ITag>(tag, childTag));
                }
            }
        }

        if (!ordering.containsKey(tag))
        {
            ordering.put(tag, ordering.keySet().size());
        }
    }

    /**
     * Sorts the rootedTags topologically. It is assumed that rootedTags are 
     * nodes of a DAG (no circular {@link ICharacterReferrer})
     * 
     * This algorithm will break ties based on depth first ordering of the getReferences()
     * function of {@link ICharacterReferrer}
     * 
     * @param rootedTags An initial list of tags considered as "root" tags.
     * @return A list sorted in topological order containing the reachable tags from rootedTags
     */
    public static List<ITag> sortFullGraph(List<ITag> rootedTags)
    {
        Graph<ITag, GraphEdge<ITag>> tagGraph = new Graph<ITag, GraphEdge<ITag>>();
        
        Map<ITag, Integer> tagOrder = new HashMap<ITag, Integer>();
        
        //add extra referred tags if needed
        for (ITag rootedTag : rootedTags)
        {
            walkTags(tagGraph, rootedTag, tagOrder);
        }
        
        List<ITag> orderedList = new ArrayList<ITag>();
        
        TagGraphVisitor visitor = new TagGraphVisitor(orderedList, tagOrder);
        
        try 
        {
            TopologicalSort.sort(tagGraph, rootedTags, visitor);
        }
        catch (CircularDependencyException e)
        {
            //This should never happen as tags should not refer to themselves.
            assert false : "CircularDependencyException";
        }
        
        return visitor.getOrderedList();
    }
}
