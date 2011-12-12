======================================================================
Organizations
======================================================================

Description
======================================================================

organic is a project to reify and maintain organizations
structure in a dynamic graph. An organization is a connected set of
nodes that share a common *meta index*.


.. contents:: Contents


Required project
======================================================================

- `graphstream/gs-core <https://github.com/graphstream/gs-core>`_
- `gsavin/gs-subgraph <https://github.com/gsavin/gs-subgraph>`_


User guide
======================================================================

How it works ?
----------------------------------------------------------------------

The algorithm uses a special attribute of node called *meta index*
that allows to classify nodes into a restricted set of category. Then,
the organizations manager try to extract connected structures from
this classification and to maintain these structures through time
without computing all the solution from scratch.

Two special operations have to be defined :

**merge**
   this operation is used to merge two organizations. The smallest one
   is dissolved in the largest one. Organizations should have the same
   meta index.
**mitose**
   this operation is called while some links have been removed in an
   organization making it disconnected. The biggest set of connected
   nodes is kept to maintain the organization structure and one or
   more children are created with the remaining nodes.

Following is the list of events that can lead to change in
organizations :

- ``edge added``, can lead to a *merge* operation;
- ``edge removed``, can lead to a *mitose* operation; edge is removed
  from its organization and ``checkOrganization`` is called 
- ``node removed``, changes the organization content; node is removed
  from its organization and ``checkOrganization`` is called 
- ``node meta index changed``, can lead to both *merge* or *mitose*
  operations.

Algorithms
----------------------------------------------------------------------

checkRootNode
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This operation is used to set the root node of an organization as the
node the most connected with other nodes of this organization.

**Input**
  org : the organization to check the root.

Begin::

 root = none
 count = -1
 
 for node in org.nodes:
   c = connections count of node
   
   if c > count:
     root = node
     count = c
 
 if root is not org.root:
   org.root = root
   trigger rootNodeUpdated


checkOrganization
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This operation checks if the organization structure is connected.

**Input**
  *org* : the organization we want to check.

Begin::

 reached = []
 toVisit = []

 toVisit.append(org.root)
 
 while len(toVisit) > 0:
   node = toVisit.pop()
   reached.append(node)
   
   for edge in node.connectedEdges:
     if edge is in org:
       o = edge.oppositeOf(node)
       
       if o.metaOrganizationIndex == node.metaOrganizationIndex:
         toVisit.append(o)
 
 if reached != org.nodes:
   notReached = org.nodes - reached
   mitose(org, notReached)
   
   checkRootNode(org)
 

merge
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This operation merges two organizations. It has to be called when a
connection is detected between these organizations.

**Input**
  *org1*, *org2* : two organizations to merge, assuming that *org1* is
  biggest that *org2*.
**Output**
  result of the merge : *org1* increases with *org2*. *org2* is
  removed in this operation.

Begin::

 nodes = org2.nodes
 
 for node in nodes:
   org2.remove(node)
   org1.include(node)
 
 trigger organizationMerged
 trigger organizationRemoved

 remove org2

 checkRootNode(org1)
 
 invoke validation

 return org1


mitose
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This operation is called when the organization structure is
disconnected.

**Input**
  base : the organization to split.
  orphans : list of nodes not connected anymore to the organization
  structure.
**Output**
  produce at least one more organization.

Begin::

 for node in orphans:
   base.remove(node)
 
 assert base.nodes.size() > 0
 
 suborg = create new organization including nodes in orphans
 
 trigger organizationCreated
 trigger organizationSplited
 
 checkOrganization(suborg)
 
 invoke validation


Organization listener
----------------------------------------------------------------------

What event is triggered ?

* a new organization is created :

  1. organizationCreated

* an organization is removed :

  1. organizationRemoved

* an organization is splited :

  1. organizationCreated
  2. organizationSplited

* two organizations are merged :

  1. organizationMerged
  2. organizationRemoved


Validation
----------------------------------------------------------------------

- ``none``
- ``skeptical``
- ``paranoid``

Hard test
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Organizations have a dedicated method to check the validity of their
structure. The structure of an organization is valid if exactly all
nodes of this organization can be reached from the root node by
exploring connected nodes with the same organization index.

Following is the pseudo-algorithm used to check the integrity of the
structure ::

  reached = []
  toVisit = []

  toVisit.append(org.root)

  while len(toVisit) > 0:
    node = toVisit.pop()
    reached.append(node)

    for edge in node.connectedEdges:
      o = edge.oppositeOf(node)
      if o.metaOrganizationIndex == node.metaOrganizationIndex:
        toVisit.append(o)
  
  if len(reached - org.nodes) > 0:
    produce an error
  
  if len(org.nodes - reached) > 0:
    produce an error


Copyright
======================================================================

This program is free software distributed under the terms of two
licenses, the CeCILL-C license that fits European law, and the GNU
Lesser General Public License. You can  use, modify and/ or
redistribute the software under the terms of the CeCILL-C license as
circulated by CEA, CNRS and INRIA at the following URL
http://www.cecill.info or under the terms of the GNU LGPL as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this program.  If not, see
http://www.gnu.org/licenses/.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C and LGPL licenses and that you accept their
terms.
