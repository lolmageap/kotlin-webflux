package dev.fastcampus.webfluxreactor.controller

import dev.fastcampus.webfluxreactor.model.CreateArticle
import dev.fastcampus.webfluxreactor.model.UpdateArticle
import dev.fastcampus.webfluxreactor.service.ArticleService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/articles")
class ArticleController(
    private val articleService: ArticleService,
) {

    @GetMapping("{id}")
    fun get(@PathVariable id: Long) = articleService.get(id)

    @GetMapping
    fun getAll(@RequestParam title: String?) = articleService.getAll(title)

    @PostMapping
    fun create(@RequestBody createArticle: CreateArticle) = articleService.create(createArticle)

    @PutMapping("{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody updateArticle: UpdateArticle,
    ) = articleService.update(id, updateArticle)

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = articleService.delete(id)

}