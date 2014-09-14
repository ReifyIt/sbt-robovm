# SBT RoboVM

SBT plugin for building Scala iOS apps with [RoboVM](http://www.robovm.org/).

## Setup

Download and extract [robovm-1.0.0-alpha-01.tar.gz](http://download.robovm.org/robovm-1.0.0-alpha-01.tar.gz) to one of the following directories:

 * $ROBOVM_HOME
 * ~/.robovm/home/
 * ~/Applications/robovm/
 * /usr/local/lib/robovm/
 * /usr/lib/robovm/
 * /opt/robovm/

Add the plugin to your `project/plugins.sbt` file:

```scala
resolvers += Resolver.sonatypeRepo("snapshots")

addSbtPlugin("it.reify" % "sbt-robovm" % "1.0.0-alpha-01-SNAPSHOT")
```

And append `iOSSettings` to your project's settings.

## Tasks

 * `iOS:iPhoneSim` – Runs the App in the iPhone simulator.
 * `iOS:iPadSim` – Runs the App in the iPad simulator.
 * `iOS:device` – Runs the App on a connected device.
 * `iOS:ipa` – Creates an IPA package for distribution on the AppStore.

## Resources

`Info.plist`, `Entitlements.plist`, and `ResourceRules.plist` go in
`src/main/resources`. Put all other iOS app resources–such as images and
interface builder files–in `src/ios/resources`.
