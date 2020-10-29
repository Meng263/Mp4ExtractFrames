package org.example

import org.jcodec.api.FrameGrab
import org.jcodec.api.JCodecException
import org.jcodec.common.model.ColorSpace
import org.jcodec.common.model.Picture
import org.jcodec.scale.ColorUtil
import org.jcodec.scale.RgbToBgr
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import javax.imageio.ImageIO


class VideoFrameExtracter {
    @Throws(IOException::class, JCodecException::class)
    fun createThumbnailFromVideo(file: File, frameNumber: Int): File {
        val frame = FrameGrab.getFrameFromFile(file, frameNumber)

        val targetFile = File("C:/w/generatePreview/src/main/resources/target.png")
            ImageIO.write(toBufferedImage8Bit(frame), "png", targetFile)
        return targetFile
    }

    // this method is from Jcodec AWTUtils.java.
    private fun toBufferedImage8Bit(src: Picture): BufferedImage {
        var src = src
        if (src.color !== ColorSpace.RGB) {
            val transform = ColorUtil.getTransform(src.color, ColorSpace.RGB)
                ?: throw IllegalArgumentException("Unsupported input colorspace: " + src.color)
            val out = Picture.create(src.width, src.height, ColorSpace.RGB)
            transform.transform(src, out)
            RgbToBgr().transform(out, out)
            src = out
        }
        val dst = BufferedImage(
            src.croppedWidth, src.croppedHeight,
            BufferedImage.TYPE_3BYTE_BGR
        )
        if (src.crop == null) toBufferedImage8Bit2(src, dst) else toBufferedImageCropped8Bit(src, dst)
        return dst
    }

    // this method is from Jcodec AWTUtils.java.
    private fun toBufferedImage8Bit2(src: Picture, dst: BufferedImage) {
        val data = (dst.raster.dataBuffer as DataBufferByte).data
        val srcData: ByteArray = src.getPlaneData(0)
        for (i in data.indices) {
            data[i] = (srcData[i] + 128).toByte()
        }
    }

    companion object {
        // this method is from Jcodec AWTUtils.java.
        private fun toBufferedImageCropped8Bit(src: Picture, dst: BufferedImage) {
            val data = (dst.raster.dataBuffer as DataBufferByte).data
            val srcData: ByteArray = src.getPlaneData(0)
            val dstStride = dst.width * 3
            val srcStride: Int = src.width * 3
            var line = 0
            var srcOff = 0
            var dstOff = 0
            while (line < dst.height) {
                var id = dstOff
                var `is` = srcOff
                while (id < dstOff + dstStride) {
                    data[id] = (srcData[`is`] + 128).toByte()
                    data[id + 1] = (srcData[`is` + 1] + 128).toByte()
                    data[id + 2] = (srcData[`is` + 2] + 128).toByte()
                    id += 3
                    `is` += 3
                }
                srcOff += srcStride
                dstOff += dstStride
                line++
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val videoFrameExtracter = VideoFrameExtracter()
//            val file = Paths.get("C:/w/generatePreview/src/main/resources/small.mp4").toFile()
            val file = Paths.get("C:/w/generatePreview/src/main/resources/media.webm").toFile()
            try {
                val imageFrame = videoFrameExtracter.createThumbnailFromVideo(file, 2)
                println("input file name : " + file.absolutePath)
                println("output video frame file name  : " + imageFrame.absolutePath)
            } catch (e: IOException) {
                println("error occurred while extracting image : " + e.message)
            } catch (e: JCodecException) {
                println("error occurred while extracting image : " + e.message)
            }
        }
    }
}
