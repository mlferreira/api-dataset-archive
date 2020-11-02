package org.xr4good.datasetarchive.controller

import org.xr4good.datasetarchive.service.EntryService
import io.swagger.annotations.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.xr4good.datasetarchive.entity.Entry
import org.xr4good.datasetarchive.entity.StatusEnum
import org.xr4good.datasetarchive.vo.*


@CrossOrigin(origins = ["*"])
@Api("Entry CRUD")
@RestController
@RequestMapping("/api/entry")
class EntryController (@Autowired private val entryService: EntryService) : ControllerExceptionHandler() {


    /*
     *  CRUD
     */

    @CrossOrigin
    @ApiOperation("Create new entry")
    @ApiResponse(code=200, message="Entry created successfully.")
    @PostMapping
    fun create(@RequestParam name: String? = null,
               @RequestParam description: String? = null,
               @RequestParam datasetId: String? = null,
               @RequestParam contributionAnswer: String? = null,
               @RequestParam status: StatusEnum? = StatusEnum.PENDING,
               @RequestParam file: MultipartFile? = null
    ): ResponseEntity<Entry> {
        val createEntryVO = CreateEntryVO(name, description, datasetId!!.toLong(), contributionAnswer, status, file)
        return ResponseEntity.ok(entryService.create(createEntryVO))
    }

    @CrossOrigin
    @ApiOperation("Retrieve existing entry by ID")
    @ApiImplicitParam(name="id", value="Entry's id", required=true, dataType="String", paramType="path", example="2")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) : ResponseEntity<Entry> {
        entryService.updateLink(id)
        return ResponseEntity.ok(entryService.getById(id))
    }

    @CrossOrigin
    @ApiOperation("List all entries by dataset")
    @ApiImplicitParam(name="datasetId", value="Dataset's id", required=true, dataType="String", paramType="path", example="2")
    @GetMapping("/by-dataset/{datasetId}")
    fun getAllByDataset(@PathVariable datasetId: Long) : ResponseEntity<List<DisplayableEntryVO>> = ResponseEntity.ok(entryService.getAllByDataset(datasetId))

    @CrossOrigin
    @ApiOperation("List all pending contributions")
    @GetMapping("/pending-contributions")
    fun getPendingByDataset() : ResponseEntity<List<DisplayableEntryVO>> = ResponseEntity.ok(entryService.getPending())

    @CrossOrigin
    @ApiOperation("List all entries by contributor")
    @ApiImplicitParam(name="contributorId", value="User's id", required=true, dataType="String", paramType="path", example="2")
    @GetMapping("/by-contributor/{contributorId}")
    fun getAllByContributor(@PathVariable contributorId: Long) : ResponseEntity<List<DisplayableEntryVO>> = ResponseEntity.ok(entryService.getAllByContributor(contributorId))

    @CrossOrigin
    @ApiOperation("List all users pending contributions")
    @GetMapping("/my-pending-entries")
    fun getPendingByUser(): ResponseEntity<List<DisplayableEntryVO>> = ResponseEntity.ok(entryService.getPendingByContributor())

    @CrossOrigin
    @ApiOperation("List all users accepted contributions")
    @GetMapping("/my-accepted-entries")
    fun getAcceptedByUser(): ResponseEntity<List<DisplayableEntryVO>> = ResponseEntity.ok(entryService.getAcceptedByContributor())

    @CrossOrigin
    @ApiOperation("List all users rejected contributions")
    @GetMapping("/my-rejected-entries")
    fun getRejectedByUser(): ResponseEntity<List<DisplayableEntryVO>> = ResponseEntity.ok(entryService.getRejectedByContributor())

    @CrossOrigin
    @ApiOperation("List all users draft contributions")
    @GetMapping("/my-draft-entries")
    fun getDraftByUser(): ResponseEntity<List<DisplayableEntryVO>> = ResponseEntity.ok(entryService.getDraftsByContributor())

    @CrossOrigin
    @ApiOperation("Edit existing entry")
    @ApiResponses(value = [
        ApiResponse(code=200, message="Returns the updated entry."),
        ApiResponse(code=403, message="Not enough permissions to edit this entry."),
        ApiResponse(code=404, message="Entry not found.")])
    @PutMapping
    fun update(@RequestParam id: Long,
               @RequestParam name: String,
               @RequestParam description: String? = null,
               @RequestParam datasetId: Long,
               @RequestParam contributionAnswer: String,
               @RequestParam status: StatusEnum = StatusEnum.PENDING,
               @RequestParam files: MultipartFile? = null
    ): ResponseEntity<Entry> {
        val updateEntryVO = UpdateEntryVO(id, name, description, datasetId, contributionAnswer, status, files)
        return ResponseEntity.ok(entryService.update(updateEntryVO))
    }

    @CrossOrigin
    @ApiOperation("Delete entry")
    @ApiImplicitParam(name="id", value="Entry's id", required=true, dataType="String", paramType="path", example="2")
    @ApiResponses(value = [
        ApiResponse(code=200, message="Entry deleted successfully."),
        ApiResponse(code=403, message="Not enough permissions to delete this entry."),
        ApiResponse(code=404, message="Entry not found.")])
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) : ResponseEntity<Void> {
        entryService.delete(id)
        return ResponseEntity.ok().build()
    }


    /*
     *  SUBMISSIONS
     */

    @CrossOrigin
    @ApiOperation("Submit existing entry for approval")
    @ApiResponses(value = [
        ApiResponse(code=200, message="Entry submitted successfully."),
        ApiResponse(code=403, message="Not enough permissions to submit entry to this dataset."),
        ApiResponse(code=404, message="Entry not found.")])
    @PutMapping("/submit/{id}")
    fun submitEntry(@PathVariable id: Long) : ResponseEntity<Void> {
        entryService.submitEntry(entryService.getById(id))
        return ResponseEntity.ok().build()
    }

    @CrossOrigin
    @ApiOperation("Approve pending entry")
    @ApiResponses(value = [
        ApiResponse(code=200, message="Entry submitted successfully."),
        ApiResponse(code=400, message="Invalid entry."),
        ApiResponse(code=403, message="Not enough permissions to approve entries on this dataset."),
        ApiResponse(code=404, message="Entry not found.")])
    @PutMapping("/approve/{id}")
    fun approveEntry(@PathVariable id: Long) : ResponseEntity<Void> {
        entryService.acceptEntry(entryService.getById(id))
        return ResponseEntity.ok().build()
    }

    @CrossOrigin
    @ApiOperation("Reject pending entry")
    @ApiResponses(value = [
        ApiResponse(code=200, message="Entry rejected successfully."),
        ApiResponse(code=400, message="Invalid entry."),
        ApiResponse(code=403, message="Not enough permissions to reject entries on this dataset."),
        ApiResponse(code=404, message="Entry not found.")])
    @PutMapping("/reject/{id}")
    fun rejectEntry(@PathVariable id: Long) : ResponseEntity<Void> {
        entryService.rejectEntry(entryService.getById(id))
        return ResponseEntity.ok().build()
    }

}
