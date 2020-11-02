package org.xr4good.datasetarchive.util

import com.amazonaws.HttpMethod
import com.amazonaws.auth.*
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.xr4good.datasetarchive.vo.EntryFileVO
import java.io.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


@Service
class S3StorageHelper (
    @Value("\${aws.access-point}") private val accessPoint: String,
    @Value("\${aws.bucket-name}") private val bucket: String
) {


    private lateinit var s3Client: AmazonS3
    private lateinit var awsCredentials: AWSCredentialsProvider

    companion object {
        val S3_URL = "https://s3.amazonaws.com"
    }

    init {
        awsCredentials = DefaultAWSCredentialsProviderChain()
        s3Client = AmazonS3Client.builder()
                .withCredentials(awsCredentials)
                .withRegion(Regions.US_EAST_2)
                .build()
    }

    fun putObject(fileName: String, file: File): EntryFileVO {
        val metadata = ObjectMetadata()
        val request = PutObjectRequest(accessPoint, fileName, file)
        request.metadata = metadata
        s3Client.putObject(request)
        val urlAuthenticated = createUrlAuthenticated(fileName)
        return EntryFileVO(fileName = fileName, url = urlAuthenticated.toString())
    }

    fun moveObject(oldFileName: String, newFileName: String): EntryFileVO {
        val request = CopyObjectRequest(accessPoint, oldFileName, accessPoint, newFileName)
        s3Client.copyObject(request)
        val urlAuthenticated = createUrlAuthenticated(newFileName)
        return EntryFileVO(fileName = newFileName, url = urlAuthenticated.toString())
    }

    fun deleteObject(fileName: String) {
        s3Client.deleteObject(bucket, fileName)
    }

    fun putObject(fileName: String, file: File, contentType: String) {
        val metadata = ObjectMetadata()
        metadata.contentType = contentType
        val request = PutObjectRequest(accessPoint, fileName, file)
        request.metadata = metadata
        s3Client.putObject(request)
    }

    fun getObjectContent(keyName: String): String {
        return s3Client.getObjectAsString(bucket, keyName)
    }

    fun createUrlAuthenticated(s3FileName: String?): URL {
        val generatePresignedUrlRequest: GeneratePresignedUrlRequest = GeneratePresignedUrlRequest(bucket, s3FileName)
                .withMethod(HttpMethod.GET)
                .withBucketName(bucket)
                .withRequestCredentialsProvider(awsCredentials)
        return s3Client.generatePresignedUrl(generatePresignedUrlRequest)
    }

    fun mergeEntry(datasetFileName: String, entryFileName: String): EntryFileVO {

        val datasetUrl = PresignedUrlDownloadRequest(createUrlAuthenticated(datasetFileName))
        val datasetStream = s3Client.download(datasetUrl).s3Object.objectContent
        val datasetFile = File.createTempFile(datasetFileName, "")
        Files.copy(datasetStream, datasetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

        val entryUrl = PresignedUrlDownloadRequest(createUrlAuthenticated(entryFileName))
        val entryStream = s3Client.download(entryUrl).s3Object.objectContent
        val entryFile = File.createTempFile(entryFileName, "")
        Files.copy(entryStream, entryFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

        val new = addFilesToExistingZip(datasetFile, entryFile)

        deleteObject(datasetFileName)
        return putObject(datasetFileName, new)
    }


    private fun addFilesToExistingZip(zipFile: File, fileToAdd: File): File {
        val tempFile = File.createTempFile(zipFile.name, null)
        tempFile.delete()
        val renameOk = zipFile.renameTo(tempFile)
        if (!renameOk) {
            throw RuntimeException("could not rename the file " + zipFile.absolutePath + " to " + tempFile.absolutePath)
        }
        val buf = ByteArray(5 * 1024)
        val zin = ZipInputStream(FileInputStream(tempFile))
        val out = ZipOutputStream(FileOutputStream(zipFile))
        var entry: ZipEntry? = zin.nextEntry
        while (entry != null) {
            out.putNextEntry(ZipEntry(entry.name))
            var len: Int
            while (zin.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            entry = zin.nextEntry
        }
        zin.close()
        val zIn: InputStream = FileInputStream(fileToAdd)
        out.putNextEntry(ZipEntry(fileToAdd.name))
        var len: Int
        while (zIn.read(buf).also { len = it } > 0) {
            out.write(buf, 0, len)
        }
        out.closeEntry()
        zIn.close()
        out.close()
        tempFile.delete()
        return zipFile
    }

}