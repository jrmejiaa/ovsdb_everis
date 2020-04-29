# EVERIS Apps

This repository has a updated version of some apps in the [onos-app-samples](https://github.com/opennetworkinglab/onos-app-samples). The repository of ONOS in their examples are not upadted for the last version of the [ONOS Project](https://wiki.onosproject.org/), therefore it is necessary to update the apps to work with them. This repository was developed by EVERIS to help with the updating process of **IPFix** and **OVSDB-REST API**.

## IPFix

## OVSDB-REST API

This app is used to manage the bridges and its characteristic using OpenVSwitch DataBase. In this case it is necessary to have a `manager device` to work with. If you install this app in ONOS (The test were made using the ONOS 2.2.2 version) you can do the next actions:

- Add a bridge.
- Delete a bridge.
- Add a port to an existing bridge.
- Delete a port to an existing bridge.
- Create a VXLAN Tunnel to an existing bridge.
- Create a Patch peer port to an existing bridge.

Those actions are made using REST API in every case. There is a chance to use this app also to create other types of tunnels. However, it was not the purpose of the project, therefore we cannot work in this possibility. If you want you can fork this project to create those extra features to the app.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

- Basis of REST APIs and how it works
- Networking fundamentals
- Basic Knowledge about how ONOS works. A very good start point would be the [Basic Tutorial ONOS](https://wiki.onosproject.org/display/ONOS/Basic+ONOS+Tutorial)
- An IDE to work with a JAVA Environment, Maven and Bazel. The ONOS Team recommends to work with, here there is the tutorial for a basic installation of the project [Using an IDE for the Project](https://wiki.onosproject.org/pages/viewpage.action?pageId=28836246)
- JAVA 11 installed in the ONOS machine (Prerquisite to install ONOS)
- Installation of [ONOS in a single machine](https://wiki.onosproject.org/display/ONOS/Installing+and+running+ONOS)

### Installing

## Deployment

## Authors

- **Jairo Mejia** - *Work to upadte the app to ONOS 2.2 LTS* - [jrmejiaa](https://github.com/jrmejiaa)
- **Roque Sosa** - *Work to upadte the app to ONOS 2.2 LTS* - [sadalmelik828](https://github.com/sadalmelik828)
- **ONOS Core Team** - *Initial work* - [onos](https://github.com/opennetworkinglab/onos)

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE.md](LICENSE) file for details
