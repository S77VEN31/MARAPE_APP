package tec.ac.cr.marape.app.model

data class User(
        var nombre: String,
        var email: String,
        var profile: String,
        var isAdmin: Boolean,
        var activeSession: Boolean
)
