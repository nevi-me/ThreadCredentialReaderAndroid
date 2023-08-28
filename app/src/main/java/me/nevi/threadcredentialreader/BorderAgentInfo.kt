package me.nevi.threadcredentialreader

data class BorderAgentInfo(
    // Network Name, max 16 len
    val networkName: String = "",
    val extPanId: ByteArray = ByteArray(16),
    val borderAgentId: ByteArray = ByteArray(16)
)
