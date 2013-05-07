package com.github.eldur.filet

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class DownloadStatus(_jobCount: Unit) {
  // TODO
}

object UrlDownload {
  val bufSize = Math.pow(2, 20).intValue

  private class FileDownload(_srcUrl: String, _destFile: File) {
    val srcUrl = _srcUrl
    val destFile = _destFile
  }

  private class FileSizeDownload(_srcUrl: String, _destFile: File, _size: Int)
    extends FileDownload(_srcUrl, _destFile) {
    val size = _size
    var lastPercentage = -1.00
    def update(partsOfSize: Int) {

      val percentage = partsOfSize / size.toDouble
      val percentageRint = Math.rint(percentage * 100) / 100
      if (lastPercentage != percentageRint) {
        println(percentageRint + " " + partsOfSize + "/" + size + " " + _destFile)
        lastPercentage = percentageRint
      }
    }
  }

  private def toRunable(downloadable: FileSizeDownload): Runnable = {
    return new Runnable() {
      override def run() {
        download(downloadable)
      }
    }
  }

  private def toDownload(el: (String, File)): FileDownload = {
    return new FileDownload(el._1, el._2)
  }

  private def sizeOfDownload(srcUrl: String): Int = {
    val url = new URL(srcUrl);
    return url.openConnection().getContentLength()
  }

  private def toSizeDownload(fd: FileDownload): FileSizeDownload = {
    val size = sizeOfDownload(fd.srcUrl)
    return new FileSizeDownload(fd.srcUrl, fd.destFile, size)
  }

  def downloadMulti(urlToDestination: Seq[(String, File)]): DownloadStatus = {
    val pool = Executors.newFixedThreadPool(3)
    val files = urlToDestination.map(toDownload).map(toSizeDownload)
    val downloadJobs = files.map(toRunable).foreach(pool.submit)

    //    files.foreach(download)
    return new DownloadStatus(files)
  }

  private def download(downlod: FileSizeDownload) {
    val url = new URL(downlod.srcUrl);
    val fullLength = downlod.size

    val stream = url.openStream()
    val fileStream = new FileOutputStream(downlod.destFile)

    println(bufSize)
    val buffer = new Array[Byte](bufSize)

    def doStream(in: InputStream, out: OutputStream, max: Int = 0) {
      downlod.update(max)
      val b = in.read(buffer)
      if (b != -1) {
        out.write(buffer, 0, b)
        doStream(in, out, max + b)
      }
    }
    try {
      doStream(stream, fileStream)
    } finally {
      stream.close()
      fileStream.close()
    }

  }
}