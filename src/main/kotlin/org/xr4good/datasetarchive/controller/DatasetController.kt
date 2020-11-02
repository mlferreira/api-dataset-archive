package org.xr4good.datasetarchive.controller

import org.xr4good.datasetarchive.entity.Dataset
import org.xr4good.datasetarchive.service.DatasetService
import org.xr4good.datasetarchive.vo.*
import io.swagger.annotations.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.xr4good.datasetarchive.entity.StatusEnum


@CrossOrigin(origins = ["*"])
@Api("Dataset CRUD")
@RestController
@RequestMapping("/api/dataset")
class DatasetController(@Autowired private val datasetService: DatasetService) : ControllerExceptionHandler() {


    /*
     *  CRUD
     */

    @CrossOrigin
    @ApiOperation("Create new dataset")
    @ApiResponse(code=200, message="Dataset created successfully.")
    @PostMapping
    fun create(@RequestParam name: String,
               @RequestParam description: String? = null,
               @RequestParam alias: String? = null,
               @RequestParam tags: List<String>? = emptyList(),
               @RequestParam contributionQuestion: String? = null,
               @RequestParam status: StatusEnum? = StatusEnum.DRAFT,
               @RequestParam datasetFile: MultipartFile? = null
            ): ResponseEntity<Dataset> {
        val createDatasetVO = CreateDatasetVO(alias, name, description, tags, contributionQuestion, status ?: StatusEnum.DRAFT, datasetFile)
        return ResponseEntity.ok(datasetService.create(createDatasetVO))
    }

    @CrossOrigin
    @ApiOperation("Retrieve existing dataset by ID")
    @ApiImplicitParam(name="id", value="Dataset's id", required=true, dataType="String", paramType="path", example="qu3ry-G-1d")
    @ApiResponses(value = [
        ApiResponse(code=200, message="Returns the requested dataset."),
        ApiResponse(code=403, message="Not enough permissions to view this dataset."),
        ApiResponse(code=404, message="Dataset not found.")])
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Dataset> {
        datasetService.updateLink(id)
        return ResponseEntity.ok(datasetService.getById(id))
    }

    @CrossOrigin
    @ApiOperation("Retrieve existing dataset by alias")
    @ApiImplicitParam(name="alias", value="Dataset's alias", required=true, dataType="String", paramType="path", example="dataset_g")
    @ApiResponses(value = [
        ApiResponse(code=200, message="Returns the requested dataset."),
        ApiResponse(code=403, message="Not enough permissions to view this dataset."),
        ApiResponse(code=404, message="Dataset not found.")])
    @GetMapping("/alias/{alias}")
    fun getByAlias(@PathVariable(required=true) alias: String): ResponseEntity<Dataset> {
        return ResponseEntity.ok(datasetService.getByAlias(alias))
    }

    @CrossOrigin
    @ApiOperation("List all datasets (visible to user)")
    @ApiResponse(code=200, message="Returns a list of all datasets the user has access to.", responseContainer="List")
    @GetMapping
    fun getAll(): ResponseEntity<List<Dataset>> = ResponseEntity.ok(datasetService.getAll())

    @CrossOrigin
    @ApiOperation("List all datasets (belonging to user)")
    @ApiResponse(code=200, message="Returns a list of all datasets the user created.", responseContainer="List")
    @GetMapping("/my-datasets")
    fun getAllFromUser(): ResponseEntity<List<Dataset>> = ResponseEntity.ok(datasetService.getAllFromUser())

    @CrossOrigin
    @ApiOperation("Edit existing dataset")
    @ApiResponses(value = [
        ApiResponse(code=200, message="Returns the updated dataset."),
        ApiResponse(code=403, message="Not enough permissions to edit this dataset."),
        ApiResponse(code=404, message="Dataset not found.")])
    @PutMapping
    fun update(@RequestParam id: Long,
               @RequestParam name: String,
               @RequestParam description: String? = null,
               @RequestParam alias: String? = null,
               @RequestParam tags: List<String> = emptyList(),
               @RequestParam contributionQuestion: String? = null,
               @RequestParam status: StatusEnum = StatusEnum.DRAFT
    ): ResponseEntity<Dataset> {
        val updateDatasetVO = UpdateDatasetVO(id, alias, name, description, tags, contributionQuestion, status)
        return ResponseEntity.ok(datasetService.update(updateDatasetVO))
    }

    @CrossOrigin
    @ApiOperation("Edit existing dataset")
    @ApiResponses(value = [
        ApiResponse(code=200, message="Returns the updated dataset."),
        ApiResponse(code=403, message="Not enough permissions to edit this dataset."),
        ApiResponse(code=404, message="Dataset not found.")])
    @PutMapping("/{id}")
    fun update2(@PathVariable id: Long,
                @RequestBody updateDatasetVO: UpdateDatasetVO
    ): ResponseEntity<Dataset> {
        return ResponseEntity.ok(datasetService.update(updateDatasetVO))
    }

    @CrossOrigin
    @ApiOperation("Delete dataset")
    @ApiImplicitParam(name="id", value="Dataset's id", required=true, dataType="String", paramType="path", example="qu3ry-G-1d")
    @ApiResponses(value = [
        ApiResponse(code=200, message="Dataset deleted successfully."),
        ApiResponse(code=403, message="Not enough permissions to delete this dataset."),
        ApiResponse(code=404, message="Dataset not found.")])
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) : ResponseEntity<Void> {
        datasetService.delete(id)
        return ResponseEntity.ok().build()
    }


}
