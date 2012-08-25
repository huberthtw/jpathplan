/**
 * RACT-PAL (RACT Path-Planning Algorithms Library) - A Library of Path Planning
 * Algorithms
 * 
 * Copyright (C) 2010 Abhijeet Anand, RACT - RMIT Agent Contest Team, School of
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
package au.rmit.ract.planning.pathplanning.entity;

/**
 * A Node interface is used to represent the states within the SearchDomain in a uniform
 * way. Though those states could ultimately be represented, explicitly or implicitly, in
 * any form within the domain application, the application should provide those states
 * via the Node interface. As all of the search algorithms at some point need to compare
 * Nodes, the requirement for a Node is that it should be comparable, which an application
 * can provide by implementing the equals(·) method.
 * 
 * @author Abhijeet Anand (<a href="mailto:abhijeet.anand@rmit.edu.au">abhijeet
 *         [dot] anand [at] rmit [dot] edu [dot] au</a>)
 */
public abstract class State {
    
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
    /**
     * This is a transient method which may be removed in future releases since
     * the semantics of a blocked node can be generalised using the cost of the
     * edge itself.
     * 
     * @return true if the current Node is blocked and hence cannot be accessed
     *         from any adjacent Node.
     */
    @Deprecated
    public abstract boolean isBlocked();
    
    /**
     * This should return the parent of this Node in a given path. This should
     * not return the Nodes from which this Node is accessible within the domain
     * but within the path found.
     * 
     * @return The parent node.
     * 
     * @deprecated
     */
    public abstract State parent();
    
    /**
     * Compares two node.
     * 
     * @param o
     * @return true if this node equals the provided node.
     */
    @Override
    public  abstract boolean equals(Object o);
    
    /**
     * Returns a hashcode for this object such that it can be used in a hash table.
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public abstract int hashCode();
    
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
     * =======================================================================*
     * --------------------------- UTILITY METHODS ---------------------------*
     * =======================================================================*
     */

}
