package com.example

import org.robovm.apple.coregraphics._
import org.robovm.apple.foundation._
import org.robovm.apple.uikit._

class MyApp extends UIApplicationDelegateAdapter {
  var window: UIWindow = _

  override def didFinishLaunching(application: UIApplication): Unit = {
    val button = UIButton.create(UIButtonType.RoundedRect)
    button.setFrame(new CGRect(100.0f, 121.0f, 121.0f, 37.0f))
    button.setTitle("Don't Tap Me", UIControlState.Normal)

    button.addOnTouchUpInsideListener((control: UIControl, event: UIEvent) => {
      button.setTitle("You Tapped Me", UIControlState.Normal)
    })

    window = new UIWindow(UIScreen.getMainScreen.getBounds)
    window.setBackgroundColor(UIColor.colorWhite)
    window.addSubview(button)
    window.makeKeyAndVisible()
  }
}

object MyApp {
  def main(args: Array[String]): Unit = {
    val pool = new NSAutoreleasePool()
    UIApplication.main(args, null, classOf[MyApp])
    pool.drain()
  }
}
