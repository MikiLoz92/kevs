package io.kevs.rabbitmq.springboot.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RabbitmqSpringbootApplication

fun main(args: Array<String>) {
    runApplication<RabbitmqSpringbootApplication>(*args)
}
