package org.xr4good.datasetarchive.entity

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime
import javax.persistence.*


@ApiModel(value = "Entry")
@Entity
@Table(name = "entry")
data class Entry(
        @ApiModelProperty(value = "Entry's ID", example = "a1b2c3d4-e5f6-g7h8-i9j0-k11l12m13n14", position = 0)
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @ApiModelProperty(value = "ID of the parent dataset", example = "2", position = 2)
        @Column(name = "dataset_id")
        var datasetId: Long = 1,

        @ApiModelProperty(value = "ID of the user that created this contribution", example = "o15p16q17-r18s-19t2-0u21-v22w23x24y2", position = 2)
        @Column(name = "contributor_id")
        var contributor: Long,

        @ApiModelProperty(value = "Entry's name", example = "TODO", position = 3)
        var name: String = "",

        @ApiModelProperty(value = "Entry's description", example = "it returns 1", position = 4)
        var description: String = "",

        @ApiModelProperty(value = "Question users have to answer to contribute to the dataset", /*example = "",*/ position = 13)
        @Column(name = "contribution_answer")
        var contributionAnswer: String = "",

        @ApiModelProperty(value = "Date and time the entry was created", example = "2020-03-17T00:00:00.001Z", position = 5)
        @Column(name = "creation_date")
        val creationDate: LocalDateTime = LocalDateTime.now(),

        @ApiModelProperty(value = "Date and time the entry was last updated", example = "2020-03-17T23:59:59.999Z", position = 6)
        @Column(name = "update_date")
        var updateDate: LocalDateTime = LocalDateTime.now(),

        @ApiModelProperty(value = "Entry's status", example = "DELETED", position = 8)
        @Enumerated(EnumType.STRING) @Column
        var status: StatusEnum = StatusEnum.PENDING,

        @ApiModelProperty(value = "Entry file name", /*example = "",*/ position = 14)
        @Column(name = "file_name")
        var fileName: String? = null,

        @ApiModelProperty(value = "Link to download the entry", /*example = "",*/ position = 14)
        @Column(name = "download_link")
        var downloadLink: String? = null

)

