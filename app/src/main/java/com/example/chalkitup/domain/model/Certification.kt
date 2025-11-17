package com.example.chalkitup.domain.model

/**
 * Data class to represent the structure of a certification.
 * This class contains the file name and the URL of the certification stored in Firebase Storage.
 */
data class Certification(val fileName: String, val fileUrl: String)