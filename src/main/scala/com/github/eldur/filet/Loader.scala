package com.github.eldur.filet

import java.io.File

object Loader extends App {

  val dlu = "http://www.ubuntu.com/start-download?distro=desktop&bits=32&release=lts"
  val w = "https://secure.gravatar.com/avatar/7077f3684778ab3d02fa150cfbea0055?s=512"
  val target = new File("target")
  val filesToDownload = Seq((dlu, new File(target, "x")), (dlu, new File(target, "y")))
  trait X extends DownloadStatus {

  }
  val status = UrlDownload.downloadMulti(filesToDownload)
  println("done")
}