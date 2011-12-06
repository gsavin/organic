==================================================
Organizations
==================================================

Description
==================================================

Required project
==================================================

- gs-core
- gs-subgraph


User guide
==================================================

Organization listener
--------------------------------------------------

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
