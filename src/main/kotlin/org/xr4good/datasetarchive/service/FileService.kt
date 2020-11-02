package org.xr4good.datasetarchive.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.xr4good.datasetarchive.entity.Dataset
import org.xr4good.datasetarchive.entity.Entry
import org.xr4good.datasetarchive.exception.ServerErrorException
import org.xr4good.datasetarchive.util.S3StorageHelper
import org.xr4good.datasetarchive.vo.EntryFileVO
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


@Service
class FileService(
        @Autowired private val s3StorageHelper: S3StorageHelper
) {

    fun uploadFile(multipartFile: MultipartFile, dataset: Dataset): EntryFileVO {
        val file = convert(multipartFile)
        val zip = zipFile(file)

        val fileVO: EntryFileVO = s3StorageHelper.putObject(dataset.fileName!!, zip)

        if (fileVO.url.isNullOrEmpty()) {
            throw ServerErrorException("Error uploading file")
        }

        return fileVO
    }

    fun uploadFile(multipartFile: MultipartFile, entry: Entry): EntryFileVO {
        val file = convert(multipartFile)

        val fileVO: EntryFileVO = s3StorageHelper.putObject(entry.fileName!!, file)

        if (fileVO.url.isNullOrEmpty()) {
            throw ServerErrorException("Error uploading file")
        }

        return fileVO
    }

    fun moveFile(oldFileName: String, newFileName: String): EntryFileVO {
        val fileVO: EntryFileVO = try {
            s3StorageHelper.moveObject(oldFileName, newFileName)
        } catch (e: Exception) {
            throw ServerErrorException("Error moving file", e)
        }
        if (fileVO.url.isNullOrEmpty()) {
            throw ServerErrorException("Error moving file")
        }
        return fileVO
    }

    fun deleteFile(entry: Entry) {
        try {
            s3StorageHelper.deleteObject(entry.fileName!!)
        } catch (e: Exception) {
            throw ServerErrorException("Error deleting file", e)
        }
    }

    fun deleteFile(dataset: Dataset) {
        try {
            s3StorageHelper.deleteObject(dataset.fileName!!)
        } catch (e: Exception) {
            throw ServerErrorException("Error deleting file", e)
        }
    }

    fun deleteFolder(dataset: Dataset) {
        try {
            s3StorageHelper.deleteObject(getFolderName(dataset))
        } catch (e: Exception) {
            throw ServerErrorException("Error deleting folder", e)
        }
    }

    fun getUrlAuthenticated(fileName: String): String {
        val url: URL = s3StorageHelper.createUrlAuthenticated(fileName)
        return url.toString()
    }

    fun getUrlAuthenticated(dataset: Dataset) = getUrlAuthenticated("${dataset.fileName}")

    fun getUrlAuthenticated(entry: Entry) = getUrlAuthenticated("${entry.fileName}")

    fun addFile(dataset: Dataset, entry: Entry): EntryFileVO {
        return s3StorageHelper.mergeEntry("${dataset.fileName}", "${entry.fileName}")
    }


    /*
     *  PRIVATE FUNCTIONS
     */

    private fun getFolderName(dataset: Dataset): String = "dataset-${dataset.id}"

    private fun convert(file: MultipartFile): File {
        val convFile: File
        try {
            convFile = File("tmp/" + (0..10000).random().toString() + file.originalFilename)
            if (convFile.createNewFile()) {
                FileOutputStream(convFile).use { fos -> fos.write(file.bytes) }
            } else {
                throw ServerErrorException("Error converting file")
            }
        } catch (e: Exception) {
            throw ServerErrorException("Error converting file", e)
        }
        return convFile
    }

    private fun zipFile(toZip: File): File {
        val zipOut = ZipOutputStream(FileOutputStream("${toZip.name}.zip"))
        val fis = FileInputStream(toZip)
        val zipEntry = ZipEntry(toZip.name)
        zipOut.putNextEntry(zipEntry)
        val bytes = ByteArray(1024)
        var length: Int
        while (fis.read(bytes).also { length = it } >= 0) {
            zipOut.write(bytes, 0, length)
        }
        zipOut.close()
        fis.close()
        return File("${toZip.name}.zip")
    }

}


