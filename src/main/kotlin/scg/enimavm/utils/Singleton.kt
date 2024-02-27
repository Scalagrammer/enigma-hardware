package scg.enimavm.utils

interface Singleton {
    fun preDestroy()    = Unit
    fun postConstruct() = Unit
}
