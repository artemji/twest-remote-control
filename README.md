Twest-remote-control
====================

Description
---------------------

Projects represents a remote control of Nest via Twitter.

Video presentation that shows a remote control of Nest via Twitter.

### Pattern

[LOCATION] [UP|DOWN] [VALUE]

### Video presentation

[![Presentation](http://img.youtube.com/vi/lPd_eejYRQc/default.jpg)](https://youtu.be/lPd_eejYRQc)

Requirements
====================

* sbt
* Docker

Run
=====

Update application.cong file with your credentials.

Build a new image by running:
> sbt docker

Then run the produced image with:
> docker run -i klika-tech/twest-remote-control