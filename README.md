Sismics Reader [![Build Status](https://secure.travis-ci.org/sismics/reader.png)](http://travis-ci.org/sismics/reader)
==============

**Demo application:** <https://reader-demo.sismics.com> (Username: **demo** / password: **demo**)

![](http://www.sismics.com/reader/img/screenshots/big/github.png)
[More screenshots](http://www.sismics.com/reader/#!/screenshots)

What is Reader?
---------------

Reader is an open source, Web-based aggregator of content served by Web Feeds (RSS, Atom).

Reader is written in Java, and may be run on any operating system with Java support.

Features
--------

- Supports RSS and Atom standards
- Organize your feeds into categories and keep track of your favorite articles
- Supports Web based and mobile user interfaces
- Keyboard shortcuts
- RESTful Web API
- Full text search
- OPML import / export
- Skinnable
- Android application

See <http://www.sismics.com/reader/> for a list of features and benefits.

Downloads
---------

Compiled installers are available here for each new versions: <https://sourceforge.net/projects/sismicsreader/files/release/>

License
-------

Reader is released under the terms of the GPL license. See `COPYING` for more
information or see <http://opensource.org/licenses/GPL-2.0>.

Translations
------------

- English
- French
- Chinese by Ryan H. Wang
- Danish by [exaviore](https://github.com/exaviore)
- Italian by Marco Narco
- Japanese
- Korean
- German

How to build Reader from the sources
------------------------------------

Prerequisites: JDK 7, Maven 3

Reader is organized in several Maven modules:

  - reader-parent
  - reader-core
  - reader-web
  - reader-web-common
  - reader-android

First off, clone the repository: `git clone git://github.com/sismics/reader.git`
or download the sources from GitHub.

#### Launch the build

From the `reader-parent` directory:

    mvn clean -DskipTests install

#### Run a stand-alone version

From the `reader-web` directory:

    mvn jetty:run

#### Build a .war to deploy to your servlet container

From the `reader-web` directory:

    mvn -Pprod -DskipTests clean install

You will get your deployable WAR in the `target` directory.

#### Build the Android app

Prerequisites :
  - Gradle
  - Android SDK
  - Environment variables pointing to the keystore (see `build.gradle`)

Then, from the `reader-android` directory:

    gradlew build
    
The generated APK will be in `app/build/apk/app-release.apk`
