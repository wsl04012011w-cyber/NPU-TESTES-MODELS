package com.wsl.nputests

import kotlin.math.sqrt

data class BenchmarkResult(
    val label: String,
    val elapsedMs: Long,
    val operations: Long,
    val score: Double
)

object BenchmarkEngine {
    /**
     * A repeatable CPU-only baseline. This deliberately does not claim to test
     * GPU or NPU acceleration.
     */
    fun runCpuPrimeBenchmark(limit: Int = 120_000): BenchmarkResult {
        val start = System.nanoTime()
        var primeCount = 0
        for (candidate in 2..limit) {
            var isPrime = true
            val root = sqrt(candidate.toDouble()).toInt()
            for (divisor in 2..root) {
                if (candidate % divisor == 0) {
                    isPrime = false
                    break
                }
            }
            if (isPrime) primeCount++
        }
        val elapsedMs = ((System.nanoTime() - start) / 1_000_000L).coerceAtLeast(1L)
        return BenchmarkResult(
            label = "CPU • contagem de primos",
            elapsedMs = elapsedMs,
            operations = limit.toLong(),
            score = primeCount.toDouble() / (elapsedMs / 1000.0)
        )
    }
}
