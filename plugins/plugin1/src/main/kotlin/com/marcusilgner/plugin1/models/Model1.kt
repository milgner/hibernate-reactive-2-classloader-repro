package com.marcusilgner.plugin1.models

import jakarta.persistence.*

@Entity
class Model1(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "model1_id_seq")
    @SequenceGenerator(allocationSize = 1, name = "model1_id_seq", sequenceName = "model1_id_seq")
    @Column(updatable = false)
    val id: Int? = null,

    @Column(nullable = false)
    val name: String,
)
