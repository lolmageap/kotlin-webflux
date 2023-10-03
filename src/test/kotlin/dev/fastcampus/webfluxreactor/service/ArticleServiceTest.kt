package dev.fastcampus.webfluxreactor.service

import dev.fastcampus.webfluxreactor.model.Article
import dev.fastcampus.webfluxreactor.model.CreateArticle
import dev.fastcampus.webfluxreactor.model.UpdateArticle
import dev.fastcampus.webfluxreactor.repository.ArticleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ArticleServiceTest(
    @Autowired private val articleService: ArticleService,
    @Autowired private val articleRepository: ArticleRepository,
) {

    @Test
    fun get() {
    }

    @Test
    fun getAll() {
        articleRepository.saveAll(
            listOf(
                Article(title = "title 1", body = "body1", authorId = 1234),
                Article(title = "title 2", body = "body2", authorId = 1234),
                Article(title = "title 3", body = "body3", authorId = 1234),
            )
        ).blockLast()

        val keyword2 = articleService.getAll("2").collectList().block()!!
        val all = articleService.getAll().collectList().block()!!

        assertThat(keyword2).hasSize(1)
        assertThat(all).hasSize(3)
    }

    @Test
    fun create() {
        val prev = articleRepository.count().block() ?: 0

        val article = articleService.create(
            CreateArticle(title = "title", body = "body", authorId = 1234)
        ).block()!!

        val current = articleRepository.count().block() ?: 0

        assertThat(current).isEqualTo(prev + 1)

        val get = articleService.get(article.id).block()!!

        assertThat(get.title).isEqualTo(article.title)
        assertThat(get.body).isEqualTo(article.body)
        assertThat(get.authorId).isEqualTo(article.authorId)
    }

    @Test
    fun update() {
        // given
        val article = articleRepository.save(
            Article(title = "title", body = "body", authorId = 1234)
        ).block()!!

        // when
        articleService.update(article.id,
            UpdateArticle(
                title = "update title", body = "update body", authorId = 9999
            )
        ).block()

        // then
        articleRepository.findById(article.id).block()!!.run {
            assertThat(title).isEqualTo("update title")
            assertThat(body).isEqualTo("update body")
            assertThat(authorId).isEqualTo(9999)
        }
    }

    @Test
    fun delete() {
        val prevent = articleRepository.count().block() ?: 0
        val article = articleRepository.save(Article(title = "title", body = "body", authorId = 1234)).block()!!
        articleService.delete(article.id).block()
        val current = articleRepository.count().block() ?: 0

        assertThat(prevent).isEqualTo(current)
    }
}