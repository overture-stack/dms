# DMS - Overture Data Management System

[<img hspace="5" src="https://img.shields.io/badge/chat-on--slack-blue?style=for-the-badge">](http://slack.overture.bio)
[<img hspace="5" src="https://img.shields.io/badge/License-gpl--v3.0-blue?style=for-the-badge">](https://github.com/overture-stack/dms/blob/develop/LICENSE)
[<img hspace="5" src="https://img.shields.io/badge/Code%20of%20Conduct-2.1-blue?style=for-the-badge">](code_of_conduct.md)

<div>
  <img align="right" alt="Overture overview" src="https://www.overture.bio/static/124ca0fede460933c64fe4e50465b235/a6d66/system-diagram.png" width="45%" hspace="5">
</div>

The Overture Data Management System (DMS) is a collection of microservices designed to support the entire lifecycle of genomics data. The DMS provides a comprehensive data portal allowing users to query data, build cohorts and export queried data for further analyze and interpretation.

</br>

<div>
<img align="left" src="ov-logo.png" height="80" hspace="0"/>
</div>

*[Overture](https://www.overture.bio/) is an ecosystem of research software tools, each with narrow responsibilities, designed to address the changing needs of genome informatics.</br></br>*

The DMS is packaged with the following components:

- [Ego](https://www.overture.bio/products/ego/) and [Ego UI](https://www.overture.bio/products/ego-ui/) for application and user management

- [Score](https://www.overture.bio/products/score/) to handle file transfer and object storage 

- [Song](https://www.overture.bio/products/song/) to track and validate metadata 

- [Maestro](https://www.overture.bio/products/maestro/) to organize distributed song metadata into a single centralized Elasticsearch index

- [Arranger](https://www.overture.bio/products/arranger/), which is the search API that interfaces between the Elasticsearch index and built-in UI components.

## Documentation

- See our Developer [wiki](https://github.com/overture-stack/dms/wiki)
- For our user installation guide see our website [here](https://www.overture.bio/documentation/dms/installation/)
- For administrative guidance see our website [here](https://www.overture.bio/documentation/dms/admin-guide/tasks/)

## Support & Contributions

- Filing an [issue](https://github.com/overture-stack/ego/issues)
- Making a [contribution](CONTRIBUTING.md)
- Connect with us on [Slack](http://slack.overture.bio)
- Add or Upvote a [feature request](https://github.com/overture-stack/ego/issues?q=is%3Aopen+is%3Aissue+label%3Anew-feature+sort%3Areactions-%2B1-desc)

## Acknowledgements

DMS development was supported by [Canarie](https://canarie.ca)
