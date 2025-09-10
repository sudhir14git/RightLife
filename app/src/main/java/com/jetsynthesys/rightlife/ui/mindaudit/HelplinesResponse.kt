package com.jetsynthesys.rightlife.ui.mindaudit

data class HelplinesResponse(
    val countries: List<Country>
)

data class Country(
    val name: String,
    val organizations: List<Organization>? = null,
    val states: List<State>? = null
)

data class State(
    val name: String,
    val organizations: List<Organization>
)

data class Organization(
    val name: String,
    val hotline: String? = null,
    val email: String? = null,
    val website: String? = null,
    val hours: String? = null,
    val text: String? = null
)
