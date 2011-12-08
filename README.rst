======================================================================
Organizations
======================================================================

.. contents::


Description
======================================================================

Organization is a project to reify and maintain organizations
structure in a dynamic graph. An organization is a connected set of
nodes that share a common *meta index*.

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
- ``edge removed``, can lead to a *mitose* operation;
- ``node removed``, changes the organization content;
- ``node meta index changed``, can lead to both *merge* or *mitose*
  operations.


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
