package com.jetsynthesys.rightlife.ui.NewSleepSounds.newsleepmodel

import java.io.Serializable

data class Service(
    val _id: String,
    val catagoryId: String,
    var catagoryName: String,
    val title: String,
    val url: String,
    val desc: String,
    val image: String,
    val tags: ArrayList<Tag>,
    val meta: Meta,
    var isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
): Serializable

