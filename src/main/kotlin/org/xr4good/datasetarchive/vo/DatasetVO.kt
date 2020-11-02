package org.xr4good.datasetarchive.vo

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.springframework.web.multipart.MultipartFile
import org.xr4good.datasetarchive.entity.StatusEnum


@ApiModel(value = "New Dataset")
data class CreateDatasetVO(
        @ApiModelProperty(value = "New dataset's alias", required = false, example = "my_dataset", position = 1)
        var alias: String? = null,
        @ApiModelProperty(value = "New dataset's name", required = true, example = "My Dataset", position = 0)
        val name: String,
        @ApiModelProperty(value = "New dataset's description", required = false, example = "it returns 1", position = 2)
        val description: String? = null,
        @ApiModelProperty(value = "Dataset's tags", required = false, example = "[\"tag1\", \"tag2\", \"tag3\"]", position = 4)
        val tags: List<String>? = emptyList(),
        @ApiModelProperty(value = "Question users have to answer to contribute to the dataset", /*example = "",*/ position = 13)
        var contributionQuestion: String?,
        @ApiModelProperty(value = "Dataset's current status", example = "OPEN", position = 9)
        var status: StatusEnum = StatusEnum.DRAFT,
        @ApiModelProperty(value = "The dataset itself", position = 10)
        var datasetFile: MultipartFile? = null
) {
    init {
        if(alias == "") alias = null
    }
}

@ApiModel(value = "Updated Dataset")
data class UpdateDatasetVO(
        @ApiModelProperty(value = "ID of the dataset that will be edited", required = true, example = "a1b2c3d4-e5f6-g7h8-i9j0-k11l12m13n14", position = 0)
        val id: Long,
        @ApiModelProperty(value = "Dataset's (updated) alias", required = false, example = "my_dataset", position = 2)
        var alias: String? = id.toString(),
        @ApiModelProperty(value = "Dataset's (updated) name", required = true, example = "My Dataset", position = 1)
        val name: String,
        @ApiModelProperty(value = "Dataset's (updated) description", required = false, example = "it returns 1", position = 3)
        val description: String? = null,
        @ApiModelProperty(value = "Dataset's (updated) tags", required = false, example = "[\"tag1\", \"tag2\", \"tag3\"]", position = 5)
        val tags: List<String>? = emptyList(),
        @ApiModelProperty(value = "Question users have to answer to contribute to the dataset", /*example = "",*/ position = 13)
        var contributionQuestion: String? = null,
        @ApiModelProperty(value = "Dataset's current status", example = "OPEN", position = 9)
        var status: StatusEnum = StatusEnum.DRAFT
) {
    init {
        if(alias == "") alias = id.toString()
    }
}