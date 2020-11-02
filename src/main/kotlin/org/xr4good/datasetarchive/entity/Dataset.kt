package org.xr4good.datasetarchive.entity

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime
import javax.persistence.*


@ApiModel(value = "Dataset")
@Entity
@Table(name = "dataset")
data class Dataset(
        @ApiModelProperty(value = "Dataset's ID", example = "42", position = 0)
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @ApiModelProperty(value = "Dataset's alias", example = "my_dataset_alias", position = 1)
        var alias: String? = null,

        @ApiModelProperty(value = "Dataset's name", example = "My Dataset", position = 2)
        var name: String,

        @ApiModelProperty(value = "Dataset's owner ID", position = 3)
        @Column(name = "author_id")
        var author: Long,

        @ApiModelProperty(value = "Dataset's description", example = "contains stuff", position = 4)
        var description: String? = null,

        @ApiModelProperty(value = "Date and time the dataset was created", example = "2020-03-17T00:00:00.001Z", position = 5)
        @Column(name = "creation_date")
        val creationDate: LocalDateTime = LocalDateTime.now(),

        @ApiModelProperty(value = "Date and time the dataset was last updated", example = "2020-03-17T23:59:59.999Z", position = 6)
        @Column(name = "update_date")
        var updateDate: LocalDateTime = LocalDateTime.now(),

        @ApiModelProperty(value = "Dataset's tags", example = "[\"tag1\", \"tag2\", \"tag3\"]", position = 7)
        @ElementCollection @CollectionTable(name = "tags", joinColumns = [JoinColumn(name="dataset_id")]) @Column(name = "tag")
        var tags: MutableList<String> = mutableListOf(),

        @ApiModelProperty(value = "Dataset's current status", example = "OPEN", position = 9)
        @Enumerated(EnumType.STRING) @Column
        var status: StatusEnum = StatusEnum.DRAFT,

        @ApiModelProperty(value = "Question users have to answer to contribute to the dataset", /*example = "",*/ position = 13)
        var contributionQuestion: String? = null,

        @ApiModelProperty(value = "Entry file name", /*example = "",*/ position = 14)
        @Column(name = "file_name")
        var fileName: String? = null,

        @ApiModelProperty(value = "Link to download the dataset", /*example = "",*/ position = 14)
        @Column(name = "download_link")
        var downloadLink: String? = null

)