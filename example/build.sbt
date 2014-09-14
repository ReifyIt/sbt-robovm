organization := "com.example"

name := "MyApp"

version := "0.0-SNAPSHOT"

description := "Example Scala/RoboVM iOS App"

scalaVersion := "2.11.2"

scalacOptions += "-Xexperimental" // Enable SAM closures

iOSSettings // Include iOS build settings

mainClass in iOS := Some("com.example.MyApp")
