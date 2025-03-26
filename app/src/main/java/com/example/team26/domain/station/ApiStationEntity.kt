package com.example.team26.domain.station

data class ApiStationEntity(
    val id: String,
    val hs: String?,
    val hverfi: String?,
    val tsip: String?,
    val ds: String?,
    val heimilisfang: String?,
    val lat: Double?,
    val lon: Double?,
    val telno: String?,
    val comment: String?,
    val astroman: String?,
    val uxstate: String?,
    val inputstate: String?,
    val outputstate: String?,
    val localremote: String?,
    val astrostate: String?,
    val nextastrotime: String?,
    val nextastroOP: String?,
    val lastastrotime: String?,
    val lasastroOP: String?,
    val inputpin: String?,
    val outputpin: String?,
    val created_at: String?,
    val updated_at: String?
)