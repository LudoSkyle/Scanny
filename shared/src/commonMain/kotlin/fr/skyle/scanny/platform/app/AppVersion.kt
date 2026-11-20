package fr.skyle.scanny.platform.app

/** The version of the app, as the store shows it, and its build number. */
interface AppVersion {

    val name: String

    val build: String
}