package com.bethesda.institute.model

data class Program(
    val level: String,
    val title: String,
    val description: String,
    val url: String,
    val category: String
)

object Programs {
    // Mirrors the site's static program pages (these aren't stored in the
    // database — each level is its own page on bethesdabiblicalinstitute.com).
    private const val BASE = "https://bethesdabiblicalinstitute.com/"

    val all = listOf(
        Program(
            level = "LEVEL 1 • CERTIFICATE",
            title = "Certificate in Theology",
            description = "A foundational program introducing core biblical and theological studies.",
            url = BASE + "certificate.php",
            category = "Certificate"
        ),
        Program(
            level = "LEVEL 2 • DIPLOMA",
            title = "Diploma in Theology",
            description = "Deeper study of Scripture, doctrine, and practical ministry skills.",
            url = BASE + "diploma-in-theology.php",
            category = "Diploma"
        ),
        Program(
            level = "LEVEL 3 • BACHELOR'S",
            title = "Bachelor of Theology",
            description = "A comprehensive undergraduate pathway for future pastors and ministers.",
            url = BASE + "bachelor-theology.php",
            category = "Bachelor"
        ),
        Program(
            level = "LEVEL 4 • MASTER'S",
            title = "Master of Divinity",
            description = "Advanced theological training for experienced ministry leaders.",
            url = BASE + "Master-of-Divinity-Subject.php",
            category = "Master"
        ),
        Program(
            level = "LEVEL 5 • DOCTORAL",
            title = "Doctor of Divinity",
            description = "The institute's highest academic distinction in Christian ministry.",
            url = BASE + "doctor-of-divinity.php",
            category = "Doctoral"
        )
    )
}
