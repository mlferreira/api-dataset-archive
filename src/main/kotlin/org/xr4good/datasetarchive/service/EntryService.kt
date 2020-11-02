package org.xr4good.datasetarchive.service

import org.xr4good.datasetarchive.exception.NotFoundException
import org.xr4good.datasetarchive.entity.Entry
import org.xr4good.datasetarchive.entity.StatusEnum
import org.xr4good.datasetarchive.repository.EntryRepository
import org.xr4good.datasetarchive.vo.CreateEntryVO
import org.xr4good.datasetarchive.vo.UpdateEntryVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.xr4good.datasetarchive.exception.BadRequestException
import org.xr4good.datasetarchive.vo.DisplayableEntryVO
import java.time.LocalDateTime


@Service
class EntryService (
        @Autowired private val entryRepository: EntryRepository,
        @Autowired private val datasetService: DatasetService,
        @Autowired private val userService: UserService,
        @Autowired private val fileService: FileService
) : BaseService() {


    /*
     *  CRUD
     */

    @PreAuthorize("hasPermission(#id, 'org.xr4good.datasetarchive.entity.Entry', 'READ')")
    fun getById(id: Long) : Entry
        = entryRepository.findById(id).orElseThrow{throw NotFoundException("Entry", id)}
            .also { it.downloadLink = updateLink(it) }

    @PostFilter("hasPermission(filterObject, 'READ')")
    fun getAll() = entryRepository.findAll().map { toVo(it) }

    @PreAuthorize("hasPermission(#datasetId, 'org.xr4good.datasetarchive.entity.Dataset', 'WRITE')")
    fun getAllByDataset(datasetId: Long) = entryRepository.findByDatasetId(datasetId).map { toVo(it) }

//    @PostFilter("hasPermission(filterObject, 'READ')")
    fun getPending() = entryRepository.findAllByStatus(StatusEnum.PENDING).map { toVo(it) }.filter { it.dataset.author == getLoggedUser().id }

    @PostFilter("hasPermission(filterObject, 'READ')")
    fun getAllByContributor(userId: Long = getLoggedUser().id)
            = entryRepository.findByContributor(userId).map { toVo(it) }

    fun getPendingByContributor(userId: Long = getLoggedUser().id)
            = entryRepository.findByContributorAndStatus(userId, StatusEnum.PENDING).map { toVo(it) }

    fun getAcceptedByContributor(userId: Long = getLoggedUser().id)
            = entryRepository.findByContributorAndStatus(userId, StatusEnum.ACCEPTED).map { toVo(it) }

    fun getRejectedByContributor(userId: Long = getLoggedUser().id)
            = entryRepository.findByContributorAndStatus(userId, StatusEnum.REJECTED).map { toVo(it) }

    @PostFilter("hasPermission(filterObject, 'WRITE')")
    fun getDraftsByContributor(userId: Long = getLoggedUser().id)
            = entryRepository.findByContributorAndStatus(userId, StatusEnum.DRAFT).map { toVo(it) }

    @PreAuthorize("hasPermission(#createEntryVO.datasetId, 'org.xr4good.datasetarchive.entity.Dataset', 'CREATE')")
    @Transactional
    fun create(createEntryVO: CreateEntryVO) : Entry {

        var entry = Entry(
            contributor = getLoggedUser().id,
            datasetId = createEntryVO.datasetId,
            name = createEntryVO.name ?: "",
            description = createEntryVO.description ?: "",
            creationDate = LocalDateTime.now(),
            updateDate = LocalDateTime.now(),
            contributionAnswer = createEntryVO.contributionAnswer ?: "",
            status = createEntryVO.status ?: StatusEnum.PENDING
        )

        entry = entryRepository.save(entry)

        if(createEntryVO.file != null) {
            try {
                entry.fileName = getFileName(entry) + "." + createEntryVO.file?.getOriginalFilename()?.split('.')?.last()
                entry.downloadLink = fileService.uploadFile(createEntryVO.file!!, entry).url
            } catch (e: Exception) {
                throw BadRequestException("Erro no upload do arquivo", e)
            }
        }

        entry = entryRepository.save(entry)

        aclService.createAcl(entry)

        return entry
    }

    @PreAuthorize("hasPermission(#updateEntryVO.id, 'org.xr4good.datasetarchive.entity.Entry', 'WRITE')")
    @Transactional
    fun update(updateEntryVO: UpdateEntryVO) : Entry {
        val entry : Entry = getById(updateEntryVO.id)

        entry.name = updateEntryVO.name ?: ""
        entry.description = updateEntryVO.description ?: ""
        entry.updateDate = LocalDateTime.now()
        entry.contributionAnswer = updateEntryVO.contributionAnswer ?: ""
        entry.status = updateEntryVO.status ?: StatusEnum.PENDING

        aclService.updateAcl(entry)

        val newEntry = entryRepository.save(entry)

        return entry
    }

    @PreAuthorize("hasPermission(#id, 'org.xr4good.datasetarchive.entity.Entry', 'DELETE')")
    fun delete(id: Long) {
        val entry : Entry = getById(id)

        entry.status = StatusEnum.DELETED
        entry.updateDate = LocalDateTime.now()

        entryRepository.save(entry)
    }


    /*
     *  SUBMISSIONS
     */

    @PreAuthorize("hasPermission(#entry.datasetId, 'org.xr4good.datasetarchive.entity.Dataset', 'CREATE')")
    fun submitEntry(entry: Entry) {
        if(entry.status != StatusEnum.DRAFT) throw BadRequestException("Invalid status")

        entry.status = StatusEnum.PENDING
        entry.updateDate = LocalDateTime.now()

        aclService.attachEntryToDataset(entry)

        // change file folder

        entryRepository.save(entry)
    }

    @PreAuthorize("hasPermission(#entry.datasetId, 'org.xr4good.datasetarchive.entity.Dataset', 'PUBLISH')")
    fun acceptEntry(entry: Entry) {
        if(entry.status != StatusEnum.PENDING) throw BadRequestException("Invalid status")

        entry.status = StatusEnum.ACCEPTED
        entry.updateDate = LocalDateTime.now()

        // change file folder
        val ds = datasetService.getById(entry.datasetId)
        ds.downloadLink = fileService.addFile(ds, entry).url
        ds.updateDate = LocalDateTime.now()

        datasetService.save(ds)
        entryRepository.save(entry)
    }

    @PreAuthorize("hasPermission(#entry.datasetId, 'org.xr4good.datasetarchive.entity.Dataset', 'PUBLISH')")
    fun rejectEntry(entry: Entry) {
        if(entry.status != StatusEnum.REJECTED) throw BadRequestException("Invalid status")

        entry.status = StatusEnum.ACCEPTED
        entry.updateDate = LocalDateTime.now()

        // change file folder? delete?

        entryRepository.save(entry)
    }

//    @PreAuthorize("hasPermission(#entry.id, 'org.xr4good.datasetarchive.entity.Entry', 'DELETE')")
    fun draftEntry(entry: Entry) {

        if(entry.status == StatusEnum.ACCEPTED) throw BadRequestException("Invalid status")

        entry.status = StatusEnum.DRAFT
        entry.updateDate = LocalDateTime.now()

        // change file path

        aclService.unattachEntryToDataset(entry)

        entryRepository.save(entry)
    }


    /*
     *
     */

    fun toVo(entry: Entry): DisplayableEntryVO {
        return DisplayableEntryVO(
                id = entry.id!!,
                name = entry.name,
                description = entry.description,
                contributionAnswer = entry.contributionAnswer,
                status = entry.status,
                dataset = datasetService.getById(entry.datasetId),
                contributor = userService.getById(entry.contributor),
                creationDate = entry.creationDate,
                updateDate = entry.updateDate
        )
    }

    fun updateLink(id: Long): String {
        val entry = getById(id)
        entry.downloadLink = fileService.getUrlAuthenticated(entry)
        entryRepository.save(entry)
        return entry.downloadLink!!
    }

    fun updateLink(entry: Entry): String {
        entry.downloadLink = fileService.getUrlAuthenticated(entry)
        entryRepository.save(entry)
        return entry.downloadLink!!
    }


    /*
     * PRIVATE FUNCTIONS
     */

    private fun getFileName(entry: Entry): String = "dataset-${entry.datasetId ?: 0}/${entry.id}"

    companion object {

        val allSent = listOf(StatusEnum.ACCEPTED, StatusEnum.REJECTED, StatusEnum.PENDING)

    }

}