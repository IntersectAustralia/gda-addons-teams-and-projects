# Teams and Projects addons for GDA
This is a set of additions to the GDA project to enable team and project functionality.

## Breakdown
The delivery is broken up into two components.
(1) One is the OAI-feed related code. It augments Fedora's oaiprovider with GDA-customisations. When combined with Fedora's OAI provider, it extracts and transforms appropriately formatted Fedora objects and presents them in an OAI-PMH harvestable way. It runs as-is, deployed in a tomcat servlet container. It does not require GDA to be installed.
(2) The second is the code relating to teams and projects in GDA. That is, it is the component of GDA which
- allows researchers to share results with other researchers within GDA,
- allows researchers to build collections (in GDA-speak 'projects'),
- allows researchers to manage teams,
- allows researchers to mark collections (and un-mark collections) for export the the ARDC.
- the back end exports 'gda objects' and turns them into export-ready 'fedora objects' appropriate for component (1).

## Components
gda-oai is an Eclipse project containing code to provide an OAI-PMH harvestable frontend to an appropriately configured Fedora Commons instance. For more details, see [README.txt](https://github.com/vincentt143/gda-addons-teams-and-projects/blob/master/gda-oai/README.txt)

gda-web is an Eclipse project containing the relevant sections of code to be added to an existing GDA project to enable team and project functionality. For more details, see [README.txt](https://github.com/vincentt143/gda-addons-teams-and-projects/blob/master/gda-web/README.txt)

## About GDA
The Genomic Data Analysis Project addresses the data management needs for the next-generation sequencing community. Genomic projects underpin almost all aspects of modern biology. This includes modern molecular biology, biodiversity studies, and medical research including but not limited to research into cancer, vaccines, antibiotics and drug development.

## Contact
Contact [Intersect](http://intersect.org.au/) with any enquiries.
