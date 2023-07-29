package com.jongnan.sampleservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SampleServiceApplication

fun main(args: Array<String>) {
    runApplication<SampleServiceApplication>(*args)
}
