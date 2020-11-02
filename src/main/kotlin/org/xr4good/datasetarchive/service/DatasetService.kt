package org.xr4good.datasetarchive.service

import org.xr4good.datasetarchive.exception.NotFoundException
import org.xr4good.datasetarchive.entity.*
import org.xr4good.datasetarchive.repository.DatasetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.xr4good.datasetarchive.exception.BadRequestException
import org.xr4good.datasetarchive.vo.*
import java.time.LocalDateTime


@Service
class DatasetService (
        @Autowired private val datasetRepository: DatasetRepository,
        @Autowired private val fileService: FileService
) : BaseService() {


    /*
     *  CRUD
     */

    @PreAuthorize("hasPermission(#id, 'org.xr4good.datasetarchive.entity.Dataset', 'READ')")
    fun getById(id: Long): Dataset
        = datasetRepository.findById(id).orElseThrow{ throw NotFoundException("Dataset", id)}
            .also { it.downloadLink = updateLink(it) }

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    fun getByAlias(alias: String): Dataset
        = datasetRepository.findByAlias(alias)?.also { it.downloadLink = updateLink(it) } ?: throw NotFoundException("Dataset")

//    @PostFilter("hasPermission(filterObject, 'READ')")
    @PostFilter("hasPermission(filterObject.id, 'org.xr4good.datasetarchive.entity.Dataset', 'READ')")
    fun getAll() : List<Dataset> = datasetRepository.findByStatusNot()

    @PostFilter("hasPermission(filterObject.id, 'org.xr4good.datasetarchive.entity.Dataset', 'WRITE')")
    fun getAllFromUser(): List<Dataset> = datasetRepository.findAll()

    @PostFilter("hasPermission(filterObject.id, 'org.xr4good.datasetarchive.entity.Dataset', 'READ')")
    fun getAllByAuthorId(authorId: Long = getLoggedUser().id) : List<Dataset> = datasetRepository.findAllByAuthor(authorId)

    @PostAuthorize("hasRole('ROLE_USER') or hasRole('USER')")
    @Transactional
    fun create(createDatasetVO: CreateDatasetVO): Dataset {
        checkAlias(createDatasetVO.alias)

        var dataset = Dataset(
                author = getLoggedUser().id,
                name = createDatasetVO.name,
                alias = createDatasetVO.alias,
                description = createDatasetVO.description,
                creationDate = LocalDateTime.now(),
                updateDate = LocalDateTime.now(),
                tags = createDatasetVO.tags as? MutableList<String> ?: mutableListOf(),
                contributionQuestion = createDatasetVO.contributionQuestion,
                status = createDatasetVO.status
        )

        if(dataset.alias == null) dataset.alias = dataset.id.toString()

        dataset = datasetRepository.save(dataset)

        if(createDatasetVO.datasetFile != null) {
            try {
                dataset.fileName = getFileName(dataset) + ".zip"
                dataset.downloadLink = fileService.uploadFile(createDatasetVO.datasetFile!!, dataset).url
            } catch (e: Exception) {
                throw BadRequestException("Erro no upload do arquivo [${e.message}]" , e)
            }
        }

        dataset = datasetRepository.save(dataset)

        aclService.createAcl(dataset)

        return dataset

    }

    @PreAuthorize("hasPermission(#updateDatasetVO.id, 'org.xr4good.datasetarchive.entity.Dataset', 'WRITE')")
    @Transactional
    fun update(updateDatasetVO: UpdateDatasetVO) : Dataset {
        val dataset : Dataset = getById(updateDatasetVO.id)

        if(dataset.alias != updateDatasetVO.alias) {
            checkAlias(updateDatasetVO.alias)
            dataset.alias = updateDatasetVO.alias
        }

        dataset.name = updateDatasetVO.name
        dataset.description = updateDatasetVO.description
        dataset.updateDate = LocalDateTime.now()
        dataset.tags = updateDatasetVO.tags as? MutableList<String> ?: mutableListOf()
        dataset.contributionQuestion = updateDatasetVO.contributionQuestion
        dataset.status = updateDatasetVO.status

        aclService.updateAcl(dataset)

        return datasetRepository.save(dataset)
    }

    @PreAuthorize("hasPermission(#dataset.id, 'org.xr4good.datasetarchive.entity.Dataset', 'WRITE')")
    @Transactional
    fun save(dataset: Dataset): Dataset = datasetRepository.save(dataset)


    @PreAuthorize("hasPermission(#id, 'org.xr4good.datasetarchive.entity.Dataset', 'DELETE')")
    fun delete(id: Long) {
        val dataset: Dataset = getById(id)

        fileService.deleteFolder(dataset)

        datasetRepository.delete(dataset)
    }

    fun updateLink(id: Long): String {
        val ds = getById(id)
        ds.downloadLink = fileService.getUrlAuthenticated(ds)
        datasetRepository.save(ds)
        return ds.downloadLink!!
    }

    fun updateLink(ds: Dataset): String {
        ds.downloadLink = fileService.getUrlAuthenticated(ds)
        datasetRepository.save(ds)
        return ds.downloadLink!!
    }

    /*
     *  PRIVATE FUNCTIONS
     */


    private fun getFileName(dataset: Dataset): String = "dataset-${dataset.id}/${dataset.alias}"

    private fun checkAlias(alias: String?) {
        if(alias != null && datasetRepository.existsByAlias(alias)) throw BadRequestException("Dataset alias already exists!")
    }


}