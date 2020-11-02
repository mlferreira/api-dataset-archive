package org.xr4good.datasetarchive.entity

import javax.persistence.*


@Entity
@Table(name = "status")
class Status (
        @Id @Enumerated(EnumType.STRING) @Column(length = 20)
        val name: StatusEnum
)

enum class StatusEnum {
        DRAFT,
        DELETED,
        OPEN,
        CLOSED,
        PENDING,
        ACCEPTED,
        REJECTED
}

