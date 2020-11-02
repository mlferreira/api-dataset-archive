package org.xr4good.datasetarchive.vo

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.springframework.web.multipart.MultipartFile
import org.xr4good.datasetarchive.entity.Dataset
import org.xr4good.datasetarchive.entity.StatusEnum
import org.xr4good.datasetarchive.entity.User
import java.time.LocalDateTime

@ApiModel(value = "New Entry")
data class CreateEntryVO(
        @ApiModelProperty(value = "New entry's name", required = true, example = "My Entry", position = 0)
        val name: String? = null,
        @ApiModelProperty(value = "New entry's description", required = false, example = "my personal stuff", position = 2)
        val description: String? = null,
        @ApiModelProperty(value = "Id of the parent dataset", required = false, example = "3", position = 3)
        var datasetId: Long,
        @ApiModelProperty(value = "Question users have to answer to contribute to the dataset", /*example = "",*/ position = 13)
        var contributionAnswer: String? = null,
        @ApiModelProperty(value = "Entry's status", example = "PENDING", position = 8)
        var status: StatusEnum? = StatusEnum.PENDING,
        @ApiModelProperty(value = "Entry's file", position = 7)
        var file: MultipartFile? = null
)

@ApiModel(value = "Updated Entry")
data class UpdateEntryVO(
        @ApiModelProperty(value = "ID of the entry that will be updated", required = true, example = "14", position = 0)
        val id: Long,
        @ApiModelProperty(value = "Entry's (updated) name", required = true, example = "My Entry", position = 1)
        val name: String? = null,
        @ApiModelProperty(value = "Entry's (updated) description", required = false, example = "my personal stuff", position = 3)
        val description: String? = null,
        @ApiModelProperty(value = "Id of the parent dataset", required = false, example = "2", position = 3)
        var datasetId: Long? = null,
        @ApiModelProperty(value = "Question users have to answer to contribute to the dataset", /*example = "",*/ position = 13)
        var contributionAnswer: String,
        @ApiModelProperty(value = "Entry's status", example = "PENDING", position = 8)
        var status: StatusEnum = StatusEnum.DRAFT,
        @ApiModelProperty(value = "Entry's file", position = 7)
        var files: MultipartFile? = null
)


data class EntryFileVO (
        val fileName: String? = null,
        val url: String? = null
)

@ApiModel(value = "Entry")
data class DisplayableEntryVO(
        @ApiModelProperty(value = "Entry's id", required = true, example = "0", position = 0)
        val id: Long,
        @ApiModelProperty(value = "New entry's name", required = true, example = "My Entry", position = 1)
        val name: String,
        @ApiModelProperty(value = "New entry's description", required = false, example = "my entry description", position = 3)
        val description: String? = null,
        @ApiModelProperty(value = "Id of the parent dataset", required = false, position = 4)
        var dataset: Dataset,
        @ApiModelProperty(value = "Answer to contribute to the dataset", /*example = "",*/ position = 6)
        var contributionAnswer: String,
        @ApiModelProperty(value = "Entry's status", example = "PENDING", position = 7)
        var status: StatusEnum = StatusEnum.DRAFT,
        @ApiModelProperty(value = "Entry's creation date", position = 7)
        var creationDate: LocalDateTime,
        @ApiModelProperty(value = "Entry's last update date", position = 7)
        var updateDate: LocalDateTime,
        @ApiModelProperty(value = "Contributor", position = 7)
        val contributor: User? = null
)