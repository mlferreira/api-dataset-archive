package org.xr4good.datasetarchive.repository

import org.xr4good.datasetarchive.entity.Dataset
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*
import org.xr4good.datasetarchive.entity.StatusEnum


@Repository
interface DatasetRepository : JpaRepository<Dataset, Long> {

//    @PostFilter("hasPermission(filterObject, 'READ')")
    override fun findAll(): List<Dataset>

//    @PostFilter("hasPermission(filterObject, 'READ')")
    fun findByStatusNot(statusEnum: StatusEnum = StatusEnum.DRAFT): List<Dataset>

    override fun findById(id: Long): Optional<Dataset>

    fun findByAlias(alias: String): Dataset?
    fun existsByAlias(alias: String): Boolean

    fun findAllByAuthor(authorId: Long): List<Dataset>

    fun save(dataset: Dataset): Dataset = saveAndFlush(dataset)

}
