package dev.fastcampus.webfluxreactor.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class HelloController {

    @GetMapping("/hello")
    fun hello(@RequestParam name: String?) : Mono<String> = Mono.just("hello $name!!")

    @GetMapping("/")
    fun main() = "main"

}