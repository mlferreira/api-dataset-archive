package org.xr4good.datasetarchive.repository

import org.xr4good.datasetarchive.entity.Entry
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.xr4good.datasetarchive.entity.StatusEnum


@Repository
interface EntryRepository : JpaRepository<Entry, Long> {

    override fun findAll(): List<Entry>
    override fun findAll(sort: Sort): List<Entry>

    fun findAllByStatus(status: StatusEnum): List<Entry>

    fun findByIdAndStatus(id: Long, status: StatusEnum): Entry?
    fun findByIdAndStatusNot(id: Long, status: StatusEnum): Entry?

    fun findByDatasetId(datasetId: Long): List<Entry>

    @Query(value = " " +
            " SELECT e.* " +
            " FROM entry e " +
            "    LEFT JOIN dataset d ON e.dataset_id = d.id " +
            " WHERE d.author_id = :author " +
            "    AND e.status = :status ",
        nativeQuery = true)
    fun findByDatasetAuthorAndStatus(@Param("author") datasetAuthor: Long, @Param("status") status: StatusEnum): List<Entry>

    fun findByContributor(contributor: Long): List<Entry>
    fun findByContributorAndStatus(contributor: Long, status: StatusEnum): List<Entry>

    fun save(entry: Entry): Entry = saveAndFlush(entry)
}