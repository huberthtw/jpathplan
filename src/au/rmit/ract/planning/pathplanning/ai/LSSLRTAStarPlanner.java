/**
 * RACT-PAL (RACT Path-Planning Algorithms Library) - A Library of Path Planning
 * Algorithms
 * 
 * Copyright (C) 2011 Abhijeet Anand, RACT - RMIT Agent Contest Team, School of
 * Computer Science and Information Technology,
 * RMIT University, Melbourne VIC 3000.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package au.rmit.ract.planning.pathplanning.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import au.rmit.ract.planning.pathplanning.ai.heuristics.DistanceHeuristics;
import au.rmit.ract.planning.pathplanning.ai.heuristics.Heuristics;
import au.rmit.ract.planning.pathplanning.entity.ComputedPlan;
import au.rmit.ract.planning.pathplanning.entity.State;
import au.rmit.ract.planning.pathplanning.entity.Plan;
import au.rmit.ract.planning.pathplanning.entity.SearchDomain;
import au.rmit.ract.planning.pathplanning.entity.SearchNode;

/**
 * This class provides an implementation of the LSS-LRTA* path planning algorithm
 * designed by Koenig et. al. in 2009. This is a real-time search algorithm which finds a part of a
 * shortest path as the agent moves and the environment/world changes, in real time.
 * 
 * @author Abhijeet Anand (<a href="mailto:abhijeet.anand@rmit.edu.au">abhijeet [dot] anand [at]
 *         rmit [dot] edu [dot] au</a>)
 * 
 */
public class LSSLRTAStarPlanner implements RTPathPlanner {
    
    /**
     * This is the super list containing all the SearchNodes generated.
     */
    private HashMap<State, SearchNode> m_allSNodesListHashMap = null;
    private static final int          BUCKET_SIZE            = 10;
    
    /**
     * Priority Queue containing all the open nodes sorted on their keys
     */
    private PriorityQueue<SearchNode> m_openList          = null;
    private ArrayList<SearchNode>     m_openList_copy     = null;
    
    /**
     * List of closed/expanded nodes
     */
    private HashMap<State, SearchNode> m_closedListHashMap    = null;
    
    /**
     * This list contains the expanded nodes during successive iterations.
     */
    private HashSet<State>             m_expandedNodesList = null;
    
    /**
     * The current lookahead value
     */
    private int                       m_lookahead         = 1;
    
    /**
     * Number of expansions in the current run of AStar
     */
    private int                       expansions          = 0;
    
    /**
     * The heuristics in use by the algorithm
     */
    private DistanceHeuristics        m_heuristics        = null;
    
    /**
     * Private reference to the search domain such that it can be shared among
     * methods.
     */
    private SearchDomain              m_map               = null;
    
    /**
     * The current start state/node.
     */
    private SearchNode                sNode_currentStart  = null;
    
    /**
     * The current goal state/node.
     */
    private SearchNode                sNode_currentGoal   = null;
    
    /**
     * This constant denotes whether something is traversable; can be
     * Float/Boolean
     */
    private static final float        BLOCKED             = Float.POSITIVE_INFINITY;
    
    // Constants defining annotation indices and their default values.
    private static final int          G                   = /* "g" */0;
    private static final int          H                   = /* "h" */1;
    private static final int          F                   = /* "f" */2;
    // private static final int RHS = /* "rhs" */3;
    // private static final int KEY1 = /* "key1" */4;
    // private static final int KEY2 = /* "key1" */5;
    
    private static final float        DEF_G               = BLOCKED;
    private static final float        DEF_H               = 0;
    
    // private static final float DEF_RHS = BLOCKED;
    
    /*
     * =======================================================================*
     * ----------------------------- INNER CLASS -----------------------------*
     * =======================================================================*
     */
    
    /*
     * =======================================================================*
     * ----------------------------- CONSTRUCTORS ----------------------------*
     * =======================================================================*
     */
    
    /**
     * Creates a Local Search Space - Learning Real-Time A-Star Path Planner using the provided
     * heuristic and the number of lookahead steps, having made no assumptions about the
     * connectedness of the domain.
     */
    public LSSLRTAStarPlanner(DistanceHeuristics h, int lookahead) {
        m_closedListHashMap = new HashMap<State, SearchNode>(BUCKET_SIZE);
        m_allSNodesListHashMap = new HashMap<State, SearchNode>(BUCKET_SIZE);
        m_openList = new PriorityQueue<SearchNode>(11, new Comparator<SearchNode>() {
            
            @Override
            public int compare(SearchNode sNode1, SearchNode sNode2) {
                return compareKeys(sNode1, sNode2);
            }
        });
        m_openList_copy = new ArrayList<SearchNode>();
        m_expandedNodesList = new HashSet<State>();
        m_lookahead = lookahead;
        m_heuristics = h;
        
    }
    
    /*
     * =======================================================================*
     * ---------------------------- STATIC METHODS ---------------------------*
     * =======================================================================*
     */
    
    /*
     * =======================================================================*
     * ---------------------------- PUBLIC METHODS ---------------------------*
     * =======================================================================*
     */
    
    /*
     * =======================================================================*
     * --------------------------- ACCESSOR METHODS --------------------------*
     * =======================================================================*
     */
    
    /*
     * =======================================================================*
     * --------------------------- MUTATOR METHODS ---------------------------*
     * =======================================================================*
     */
    
    /*
     * =======================================================================*
     * --------------------- OVERRIDDEN INTERFACE METHODS --------------------*
     * =======================================================================*
     */
    
    /*
     * (non-Javadoc)
     * @see au.rmit.ract.planning.pathplanning.ai.PathPlanner#expandedNodes()
     */
    @Override
    public ArrayList<State> expandedNodes() {
        return new ArrayList<State>(m_expandedNodesList);
    }
    
    /*
     * (non-Javadoc)
     * @see au.rmit.ract.planning.pathplanning.ai.PathPlanner#unexpandedNodes()
     */
    @Override
    public ArrayList<State> unexpandedNodes() {
        ArrayList<State> unexpanded = new ArrayList<State>();
        for (SearchNode node : m_openList_copy) {
            unexpanded.add(node.getNode());
        }
        return unexpanded;
    }
    
    /*
     * (non-Javadoc)
     * @see
     * au.rmit.ract.planning.pathplanning.ai.PathPlanner#findPath(au.edu.rmit.cs.ract.planning
     * .pathplanning.entity.SearchDomain, au.rmit.ract.planning.pathplanning.entity.Node,
     * au.rmit.ract.planning.pathplanning.entity.Node)
     */
    @Override
    public synchronized Plan findPath(SearchDomain map, State sNode, State tNode) {
        // If the destination is not traversable, there can be no path. Same
        // applies to the start node. OR s_start == s_goal
        if (sNode.equals(tNode) || map.isBlocked(sNode) || map.isBlocked(tNode)) {
            return null;
        }
        
        initialise(map, sNode, tNode);
        aStar();
        // System.out.println(m_closedList); // REMOVE
        // System.out.println(m_openList); // REMOVE
        
        // If OPEN list is empty => Goal could not be reached => NO PATH
        if (m_openList.isEmpty()) {
            return null;
        }
        // Identify the transient goal (This is essentially the OPEN.top
        SearchNode sNode_currentGoalPrime = m_openList.peek();
        
        m_openList_copy.addAll(m_openList);
        
        // Call Dijkstra to adjust the heuristic value
        dijkstra();
        
        // System.out.println(m_closedList); // REMOVE
        // System.out.println(m_openList); // REMOVE
        // System.out.println(m_openList_copy); // REMOVE
        
        // Identify and return a partial path between transient goal and current start.
        ComputedPlan path = createPath(sNode_currentGoalPrime);
        
        // System.gc();
        return path;
    }
    
    /*
     * (non-Javadoc)
     * @see
     * au.rmit.ract.planning.pathplanning.ai.RTPathPlanner#findPath(au.rmit.ract.planning.pathplanning
     * .entity.SearchDomain, au.rmit.ract.planning.pathplanning.entity.Node,
     * au.rmit.ract.planning.pathplanning.entity.Node, int)
     */
    @Override
    public synchronized Plan findPath(SearchDomain map, State sNode, State tNode, int lookahead) {
        m_lookahead = lookahead;
        return findPath(map, sNode, tNode);
    }
    
    /*
     * (non-Javadoc)
     * @see
     * au.rmit.ract.planning.pathplanning.ai.PathPlanner#setHeuristics(au.edu.rmit.cs.ract
     * .planning.pathplanning.ai.heuristics.Heuristics)
     */
    @Override
    public boolean setHeuristics(Heuristics heuristics) {
        m_heuristics = DistanceHeuristics.class.cast(heuristics);
        return true;
    }
    
    /*
     * (non-Javadoc)
     * @see au.rmit.ract.planning.pathplanning.ai.PathPlanner#annotations()
     */
    @Override
    public HashMap<State, String> annotations() {
        HashMap<State, String> annotations = new HashMap<State, String>();
        StringBuilder annotStringBuilder = new StringBuilder();
        
        try {
            for (SearchNode sNode : m_allSNodesListHashMap.values()) {
                annotStringBuilder.append("F:").append(sNode.get(F));
                annotStringBuilder.append(", G:").append(sNode.get(G));
                annotStringBuilder.append(", H:").append(sNode.get(H));
                
                annotations.put(sNode.getNode(), annotStringBuilder.toString());
                annotStringBuilder.delete(0, annotStringBuilder.length());
            }
        } catch (Exception excptn) {
            excptn.printStackTrace();
        }
        return annotations;
    }
    
    /*
     * =======================================================================*
     * --------------------------- UTILITY METHODS ---------------------------*
     * =======================================================================*
     */
    
    private void aStar() {
        while (sNode_currentGoal.get(G) > minFvalInOpen() && expansions < m_lookahead) {
            ++expansions;
            
            SearchNode currentSNode = m_openList.peek();
            State currentNode = currentSNode.getNode();
            
            // Terminate the search if the target node was found, else continue searching
            if (sNode_currentGoal.equals(currentSNode)) {
                break;
            }
            m_openList.remove(currentSNode);
            m_closedListHashMap.put(currentNode, currentSNode);
            m_expandedNodesList.add(currentSNode.getNode());
            
            // Now begin searching through all the successors of the current
            // node and evaluate them as candidates for next step, putting them
            // all in the Open List if they have not been visited already.
            ArrayList<State> neighbours = m_map.getSuccessors(currentSNode.getNode());
            for (State node : neighbours) {
                SearchNode neighbourNode = newSearchNode(node);
                
                float nextStepG = currentSNode.get(G) + m_map.cost(currentSNode.getNode(), node);
                if (neighbourNode.get(G) > nextStepG) {
                    neighbourNode.set(G, nextStepG);
                    neighbourNode.setParent(currentSNode);
                    insertInOpenList(neighbourNode);
                }
            }
            
        }
        
    }
    
    private void dijkstra() {
        // Create a temporary PQ from OPEN list, though sorted on h-value instead of f-value.
        PriorityQueue<SearchNode> openHvalueList = new PriorityQueue<SearchNode>(m_openList.size(),
                new Comparator<SearchNode>() {
                    
                    @Override
                    public int compare(SearchNode aNode, SearchNode bNode) {
                        if (aNode.get(H) < bNode.get(H)) {
                            return -1;
                        } else if (aNode.get(H) > bNode.get(H)) {
                            return 1;
                        }
                        return 0;
                    }
                });
        // openHvalueList.addAll(m_openList);
        for (SearchNode searchNode : m_openList) {
            openHvalueList.add(searchNode);
        }
        
        for (SearchNode searchNode : m_closedListHashMap.values()) {
            searchNode.set(H, BLOCKED);
        }
        while (!m_closedListHashMap.isEmpty()) {
            SearchNode s = openHvalueList.poll();
            if (s == null)
                break;
            m_closedListHashMap.remove(s.getNode());
            
            ArrayList<State> preds = m_map.getPredecessors(s.getNode());
            for (State s_primeNode : preds) {
                SearchNode s_prime = newSearchNode(s_primeNode);
                float updatedH = s.get(H) + m_map.cost(s_primeNode, s.getNode());
                if (m_closedListHashMap.containsKey(s_primeNode) && s_prime.get(H) > updatedH) {
                    s_prime.set(H, updatedH);
                    m_heuristics.updateH(m_map, s_primeNode, sNode_currentGoal.getNode(), updatedH);
                    if (!openHvalueList.contains(s_prime)) {
                        // insertInOpenList(s_prime);
                        s_prime.set(F, s_prime.get(G) + s_prime.get(H));
                        openHvalueList.add(s_prime);
                    }
                }
            }
        }
        
        m_openList.clear();
        
        for (SearchNode searchNode : openHvalueList) {
            m_openList.add(searchNode);
        }
        
    }
    
    private int compareKeys(SearchNode aNode, SearchNode bNode) throws NullPointerException {
        if (aNode != null && bNode != null) {
            if (aNode.get(F) < bNode.get(F)) {
                return -1;
            } else if (aNode.get(F) == bNode.get(F)) { // Break ties
                if (aNode.get(G) < bNode.get(G)) {
                    return -1;
                } else if (aNode.get(G) > bNode.get(G)) {
                    return 1;
                } else
                    return 0;
            } else {
                return 1;
            }
        } else {
            throw new NullPointerException(
                    "Cannot compare null SearchNodes: " + aNode == null ? "aNode" : "bNode");
        }
    }
    
    /**
     * This method initialises the system only once. Repeated calls to this
     * method will simply return without performing any further initialisation.
     * 
     * @param map
     *            The SearchDomain to be used
     * @param sNode
     *            The start Node
     * @param tNode
     *            The target Node
     */
    private void initialise(SearchDomain map, State sNode, State tNode) {
        m_allSNodesListHashMap.clear();
        m_openList.clear();
        m_openList_copy.clear();
        m_closedListHashMap.clear();
        m_expandedNodesList.clear();
        
        expansions = 0;
        
        m_map = map;
        sNode_currentStart = newSearchNode(sNode);
        sNode_currentGoal = newSearchNode(tNode);
        
        // Annotate the start node and put it in the open list.
        sNode_currentStart.set(G, 0);
        insertInOpenList(sNode_currentStart);
        
        return;
    }
    
    /**
     * Creates and initialises a new SearchNode for the Node provided as
     * parameter. If the node already exists in the super list, returns it
     * instead.
     * 
     * @param node
     * @return A new SearchNode or the one in the super list if created earlier.
     */
    private SearchNode newSearchNode(State node) {
        if (m_allSNodesListHashMap.containsKey(node)) {
            return m_allSNodesListHashMap.get(node);
        } else {
            SearchNode sNode = new SearchNode(node);
            sNode.set(G, DEF_G);
            sNode.set(H, DEF_H);
            sNode.set(F, DEF_G + DEF_H);
            m_allSNodesListHashMap.put(node, sNode);
            return sNode;
        }
    }
    
    private float minFvalInOpen() {
        return m_openList.peek() != null ? m_openList.peek().get(F) : BLOCKED;
    }
    
    private void insertInOpenList(SearchNode node) {
        node.set(H, m_heuristics.h(m_map, node.getNode(), sNode_currentGoal.getNode()));
        node.set(F, node.get(G) + node.get(H));
        m_openList.add(node);
    }
    
    private ComputedPlan createPath(SearchNode goal) {
        ComputedPlan path = new ComputedPlan();
        SearchNode target = goal;
        while (!sNode_currentStart.equals(target)) {
            path.prependStep(target.getNode());
            target = target.getParent();
        }
        path.prependStep(sNode_currentStart.getNode());
        path.setCost(goal.get(G));
        
        return path;
    }
    
}
