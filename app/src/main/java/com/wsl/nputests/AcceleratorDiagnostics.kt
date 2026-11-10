package com.wsl.nputests

object AcceleratorDiagnostics {
    init {
        System.loadLibrary("ai_lab_native")
    }

    external fun nativeGpuProbe(): String
    external fun nativeQnnProbe(): String

    fun runGpuProbe(): String = runCatching { nativeGpuProbe() }
        .getOrElse { "Vulkan probe failed: ${it.message ?: it::class.java.simpleName}" }

    fun runQnnProbe(): String = runCatching { nativeQnnProbe() }
        .getOrElse { "QNN probe failed: ${it.message ?: it::class.java.simpleName}" }
}
