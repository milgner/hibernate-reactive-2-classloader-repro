package com.marcusilgner.plugin2.models

import com.marcusilgner.plugin1.models.Model1
import io.vertx.core.json.JsonObject
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*


@Entity
class Model2(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "model2_id_seq")
    @SequenceGenerator(
        allocationSize = 1,
        name = "model2_id_seq",
        sequenceName = "model2_id_seq",
    )
    @Column(updatable = false)
    val id: Int? = null,

    @Column(nullable = false)
    val text: String,

    @CreationTimestamp
    val createdAt: Date,

    @UpdateTimestamp
    val updatedAt: Date,
)
