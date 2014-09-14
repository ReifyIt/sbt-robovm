package it.reify.robovm

import org.robovm.compiler.AppCompiler
import org.robovm.compiler.config._
import org.robovm.compiler.target.ios._
import org.robovm.compiler.target.ios.IOSSimulatorLaunchParameters.{ Family => IOSFamily }
import sbt._
import sbt.Keys._

object RoboVM extends Plugin {
  val iOS = config("iOS")

  val robovmVersion = SettingKey[String]("robovmVersion")
  val executableName = SettingKey[String]("executableName")
  val propertiesFile = SettingKey[File]("propertiesFile")
  val configFile = SettingKey[File]("configFile")
  val forceLinkClasses = SettingKey[Seq[String]]("forceLinkClasses")
  val frameworks = SettingKey[Seq[String]]("frameworks")
  val nativePath = SettingKey[Seq[File]]("nativePath")
  val distHome = SettingKey[Option[File]]("distHome")
  val skipPngCrush = SettingKey[Boolean]("skipPngCrush")
  val flattenResources = SettingKey[Boolean]("flattenResources")

  val sdkVersion = SettingKey[Option[String]]("sdkVersion")
  val signingIdentity = SettingKey[Option[String]]("signingIdentity")
  val provisioningProfile = SettingKey[Option[String]]("provisioningProfile")
  val infoPList = SettingKey[File]("infoPList")
  val entitlementsPList = SettingKey[File]("entitlementsPList")
  val resourceRulesPList = SettingKey[File]("resourceRulesPList")

  private[this] val robovmConfig = TaskKey[Config.Builder]("robovmConfig")

  val iPhoneSim = TaskKey[Unit]("iPhoneSim", "Runs the App in the iPhone simulator.")
  val iPadSim = TaskKey[Unit]("iPadSim", "Runs the App in the iPad simulator.")
  val device = TaskKey[Unit]("device", "Runs the App on a connected device.")
  val ipa = TaskKey[Unit]("ipa", "Creates an IPA package for distribution on the AppStore.")

  def iOSSettings: Seq[Setting[_]] = Seq(
    name in iOS <<= name,
    target in iOS <<= target,
    mainClass in iOS <<= mainClass in (Compile, run),
    fullClasspath in iOS <<= fullClasspath in Compile,
    resourceDirectory in iOS := sourceDirectory.value / "ios" / "resources",
    robovmVersion in iOS := "1.0.0-alpha-01",
    executableName in iOS := name.value,
    propertiesFile in iOS := (resourceDirectory in Compile).value / "robovm.properties",
    configFile in iOS := (resourceDirectory in Compile).value / "robovm.xml",
    forceLinkClasses in iOS := Seq.empty,
    frameworks in iOS := Seq.empty,
    nativePath in iOS := Seq.empty,
    distHome in iOS := None,
    skipPngCrush in iOS := false,
    flattenResources in iOS := false,
    sdkVersion in iOS := None,
    signingIdentity in iOS := None,
    provisioningProfile in iOS := None,
    infoPList in iOS := (resourceDirectory in Compile).value / "Info.plist",
    entitlementsPList in iOS := (resourceDirectory in Compile).value / "Entitlements.plist",
    resourceRulesPList in iOS := (resourceDirectory in Compile).value / "ResourceRules.plist",
    robovmConfig in iOS <<= robovmConfigTask,
    iPhoneSim in iOS <<= iOSSimTask(IOSFamily.iPhoneRetina4Inch) dependsOn (compile in Compile),
    iPadSim in iOS <<= iOSSimTask(IOSFamily.iPadRetina) dependsOn (compile in Compile),
    device in iOS <<= deviceTask dependsOn (compile in Compile),
    ipa in iOS <<= ipaTask dependsOn (compile in Compile),
    libraryDependencies ++= Seq(
      "org.robovm" % "robovm-compiler"     % (robovmVersion in iOS).value,
      "org.robovm" % "robovm-rt"           % (robovmVersion in iOS).value,
      "org.robovm" % "robovm-objc"         % (robovmVersion in iOS).value,
      "org.robovm" % "robovm-cocoatouch"   % (robovmVersion in iOS).value,
      "org.robovm" % "robovm-cacerts-full" % (robovmVersion in iOS).value))

  private[this] def robovmConfigTask = Def.task {
    val streams = Keys.streams.value
    val target = (Keys.target in iOS).value

    val propertiesFile = (RoboVM.propertiesFile in iOS).value
    val configFile = (RoboVM.configFile in iOS).value
    val infoPList = (RoboVM.infoPList in iOS).value
    val entitlementsPList = (RoboVM.entitlementsPList in iOS).value
    val resourceRulesPList = (RoboVM.resourceRulesPList in iOS).value

    val skipPngCrush = (RoboVM.skipPngCrush in iOS).value
    val flattenResources = (RoboVM.flattenResources in iOS).value

    object logger extends org.robovm.compiler.log.Logger {
      override def debug(s: String, args: AnyRef*): Unit = streams.log.debug(s.format(args: _*))
      override def info(s: String, args: AnyRef*): Unit = streams.log.info(s.format(args: _*))
      override def warn(s: String, args: AnyRef*): Unit = streams.log.warn(s.format(args: _*))
      override def error(s: String, args: AnyRef*): Unit = streams.log.error(s.format(args: _*))
    }

    val config = new Config.Builder()
      .mainClass((mainClass in iOS).value.getOrElse("Main"))
      .executableName((executableName in iOS).value)
      .logger(logger)

    if (propertiesFile.exists) {
      streams.log.debug("Properties: " + propertiesFile.getAbsolutePath)
      config.addProperties(propertiesFile)
    }

    if (configFile.exists) {
      streams.log.debug("Config: " + configFile.getAbsolutePath)
      config.read(configFile)
    }

    for (forceLinkClass <- (forceLinkClasses in iOS).value) {
      streams.log.debug("Force linked class: "+ forceLinkClass)
      config.addForceLinkClass(forceLinkClass)
    }

    for (framework <- (frameworks in iOS).value) {
      streams.log.debug("Framework: " + framework)
      config.addFramework(framework)
    }

    for (nativePath <- (nativePath in iOS).value if nativePath.isDirectory; nativeLib <- nativePath.listFiles) {
      streams.log.debug("Lib: " + nativeLib.getName)
      config.addLib(new Config.Lib(nativeLib.getName, true))
    }

    for (entry <- (fullClasspath in iOS).value) {
      streams.log.debug("Classpath: " + entry.data)
      config.addClasspathEntry(entry.data)
    }

    for (resource <- (resourceDirectory in iOS).value.listFiles if !resource.isHidden) {
      streams.log.debug("Resource: " + resource.getAbsolutePath)
      config.addResource(new Resource(resource).skipPngCrush(skipPngCrush).flatten(flattenResources))
    }

    for (sdkVersion <- (sdkVersion in iOS).value) {
      streams.log.debug("SDK version: " + sdkVersion)
      config.iosSdkVersion(sdkVersion)
    }

    for (signingIdentity <- (signingIdentity in iOS).value) {
      streams.log.debug("Signing identity: " + signingIdentity)
      config.iosSignIdentity(SigningIdentity.find(SigningIdentity.list(), signingIdentity))
    }

    for (provisioningProfile <- (provisioningProfile in iOS).value) {
      streams.log.debug("Provisioning profile: " + provisioningProfile)
      config.iosProvisioningProfile(ProvisioningProfile.find(ProvisioningProfile.list(), provisioningProfile))
    }

    if (infoPList.exists) {
      streams.log.debug("Info.plist: " + infoPList.getAbsolutePath)
      config.iosInfoPList(infoPList)
    }

    if (entitlementsPList.exists) {
      streams.log.debug("Entitlements.plist: " + entitlementsPList.getAbsolutePath)
      config.iosEntitlementsPList(entitlementsPList)
    }

    if (resourceRulesPList.exists) {
      streams.log.debug("ResourceRules.plist: " + resourceRulesPList.getAbsolutePath)
      config.iosResourceRulesPList(resourceRulesPList)
    }

    for (distHome <- (distHome in iOS).value) config.home(new Config.Home(distHome))

    config.installDir(target)
    config.tmpDir(target / "native")

    config
  }

  private[this] def iOSSimTask(family: IOSFamily) = Def.task {
    val config = (robovmConfig in iOS).value
      .arch(Arch.x86)
      .os(OS.ios)
      .targetType(Config.TargetType.ios)
      .skipInstall(true)
      .build()

    streams.value.log.info("Compiling App...")
    new AppCompiler(config).compile()

    streams.value.log.info("Launching App in iOS simulator...")
    val target = config.getTarget.asInstanceOf[IOSTarget]
    val launchParameters = target.createLaunchParameters().asInstanceOf[IOSSimulatorLaunchParameters]
    launchParameters.setFamily(family)
    target.launch(launchParameters).waitFor()
    ()
  }

  private[this] def deviceTask = Def.task {
    val config = (robovmConfig in iOS).value
      .arch(Arch.thumbv7)
      .os(OS.ios)
      .targetType(Config.TargetType.ios)
      .skipInstall(true)
      .build()

    streams.value.log.info("Compiling App...")
    new AppCompiler(config).compile()

    streams.value.log.info("Launching App on device...")
    val target = config.getTarget.asInstanceOf[IOSTarget]
    val launchParameters = target.createLaunchParameters()
    target.launch(launchParameters).waitFor()
    ()
  }

  private[this] def ipaTask = Def.task {
    val config = (robovmConfig in iOS).value
      .arch(Arch.thumbv7)
      .os(OS.ios)
      .targetType(Config.TargetType.ios)
      .build()

    streams.value.log.info("Compiling App...")
    new AppCompiler(config).compile()

    streams.value.log.info("Packaging App...")
    val target = config.getTarget.asInstanceOf[IOSTarget]
    target.createIpa()
    ()
  }
}
